package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.toDomain
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCATION_LOOKUP_TIMEOUT_MILLIS = 5_000L

interface StationsRepository {
  val state: StateFlow<StationsState>

  suspend fun loadIfNeeded()

  suspend fun forceRefresh()

  suspend fun refreshAvailability(stationIds: List<String>)

  fun stationById(stationId: String): Station?
}

private data class StationsSession(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val userLocation: GeoPoint? = null,
  val lastRefreshAttemptEpoch: Long? = null,
  val lastRefreshFailureEpoch: Long? = null,
  val dataSource: StationDataSource = StationDataSource.Network,
  val servingCacheAfterFailure: Boolean = false,
  val hardFailure: Boolean = false,
)

/**
 * Implementación de StationsRepository que delega responsabilidades específicas
 * a abstracciones dedicadas (SRP):
 * - [remoteDataSource]: Obtiene datos de la red
 * - [cacheManager]: Gestiona el caché local
 * - [locationProvider]: Proporciona la ubicación actual
 * - [settingsRepository]: Accede a la configuración (ciudad seleccionada)
 *
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class StationsRepositoryImpl(
  private val remoteDataSource: StationsRemoteDataSource,
  private val cacheManager: StationsCacheManager,
  private val locationProvider: LocationProvider,
  private val settingsRepository: SettingsRepository,
  private val scope: CoroutineScope,
) : StationsRepository {
  private val mutableState = MutableStateFlow(StationsState(isLoading = false))
  private val sessionState = MutableStateFlow(StationsSession())

  @kotlin.concurrent.Volatile private var loaded = false

  @kotlin.concurrent.Volatile private var lastLoadedCityId: String? = null
  private val loadMutex = Mutex()

  private var lastGoodLocation: GeoPoint? = null

  private var fetchGeneration = 0

  private val useReactiveCache: Boolean = cacheManager.stationsFlow != null

  override val state: StateFlow<StationsState> =
    if (useReactiveCache) {
      combine(
        cacheManager.stationsFlow!!,
        cacheManager.metadataFlow!!,
        settingsRepository.selectedCity,
        sessionState,
      ) { stations, metadata, city, session ->
        mergeDbStationsState(stations, metadata, city, session)
      }.stateIn(scope, SharingStarted.Eagerly, StationsState(isLoading = false))
    } else {
      mutableState.asStateFlow()
    }

  private fun fallbackLocation(): GeoPoint {
    lastGoodLocation?.let { return it }
    val city = settingsRepository.currentSelectedCity()
    return GeoPoint(city.defaultLatitude, city.defaultLongitude)
  }

  override suspend fun forceRefresh() {
    val currentLocation = fetchCurrentLocation()
    val origin = currentLocation ?: fallbackLocation()
    val city = settingsRepository.currentSelectedCity()
    val attemptAt = currentTimeMs()

    val generation = loadMutex.withLock {
      fetchGeneration++
      updateLoadingState(
        isLoading = true,
        errorMessage = null,
        lastRefreshAttemptEpoch = attemptAt,
        userLocation = currentLocation,
      )
      fetchGeneration
    }

    refreshStations(origin = origin, currentLocation = currentLocation, city = city, generation = generation)
  }

  override suspend fun loadIfNeeded() {
    if (loaded) return

    val currentLocation = fetchCurrentLocation()
    val origin = currentLocation ?: fallbackLocation()
    val city = settingsRepository.currentSelectedCity()

    val generation = loadMutex.withLock {
      if (loaded) return@withLock 0

      val attemptAt = currentTimeMs()
      updateLoadingState(
        isLoading = true,
        errorMessage = null,
        lastRefreshAttemptEpoch = attemptAt,
        userLocation = currentLocation,
      )

      if (lastLoadedCityId != null && lastLoadedCityId != city.id) {
        cacheManager.clear()
        lastGoodLocation = null
        loaded = false
        lastLoadedCityId = null
      }

      fetchGeneration++
      val gen = fetchGeneration

      if (cacheManager.isFresh(city.id)) {
        handleCacheHit(currentLocation, city, gen)
        return@withLock 0
      }

      gen
    }

    if (generation == 0) return
    refreshStations(origin = origin, currentLocation = currentLocation, city = city, generation = generation)
  }

  override suspend fun refreshAvailability(stationIds: List<String>) {
    if (stationIds.isEmpty()) return

    val availability =
      runCatching {
        remoteDataSource.fetchAvailability(stationIds)
      }.getOrNull() ?: return

    if (availability.isEmpty()) return

    val refreshedAt = currentTimeMs()
    cacheManager.updateAvailability(
      availability.mapValues { (_, v) -> v.bikesAvailable to v.slotsFree },
      refreshedAt,
    )

    updateSessionAfterAvailabilityRefresh(refreshedAt)
  }

  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }

  // Helper methods

  private suspend fun fetchCurrentLocation(): GeoPoint? =
    withTimeoutOrNull(LOCATION_LOOKUP_TIMEOUT_MILLIS) {
      runCatching { locationProvider.currentLocation() }.getOrNull()
    }?.also { lastGoodLocation = it }

  private fun updateLoadingState(
    isLoading: Boolean,
    errorMessage: String?,
    lastRefreshAttemptEpoch: Long,
    userLocation: GeoPoint?,
  ) {
    if (useReactiveCache) {
      sessionState.update {
        it.copy(
          isLoading = isLoading,
          errorMessage = errorMessage,
          lastRefreshAttemptEpoch = lastRefreshAttemptEpoch,
          userLocation = userLocation ?: it.userLocation,
        )
      }
    } else {
      mutableState.update {
        it.copy(
          isLoading = isLoading,
          errorMessage = errorMessage,
          lastRefreshAttemptEpoch = lastRefreshAttemptEpoch,
        )
      }
    }
  }

  private suspend fun refreshStations(
    origin: GeoPoint,
    currentLocation: GeoPoint?,
    city: City,
    generation: Int,
  ) {
    runCatching { remoteDataSource.fetchStations(origin) }
      .onSuccess { stations ->
        handleFetchSuccess(stations, currentLocation, city, generation)
      }.onFailure { error ->
        handleFetchFailure(error, currentLocation, city, origin, generation)
      }
  }

  private suspend fun handleCacheHit(
    currentLocation: GeoPoint?,
    city: City,
    generation: Int,
  ) {
    val lastUpdatedEpoch = cacheManager.lastUpdated(city.id)
    val now = currentTimeMs()

    if (useReactiveCache) {
      sessionState.value =
        StationsSession(
          isLoading = false,
          errorMessage = null,
          userLocation = currentLocation,
          lastRefreshAttemptEpoch = sessionState.value.lastRefreshAttemptEpoch,
          lastRefreshFailureEpoch = null,
          dataSource = StationDataSource.Cache,
          servingCacheAfterFailure = false,
          hardFailure = false,
        )
    } else {
      mutableState.value =
        StationsState(
          stations = emptyList(),
          isLoading = false,
          userLocation = currentLocation,
          lastUpdatedEpoch = lastUpdatedEpoch,
          dataSource = StationDataSource.Cache,
          freshness =
            computeStationsFreshness(
              lastUpdatedEpoch = lastUpdatedEpoch,
              nowEpoch = now,
              servingCacheAfterFailure = false,
              stationsEmpty = false,
              hardFailure = false,
            ),
        )
    }

    loaded = true
    lastLoadedCityId = city.id
  }

  private suspend fun handleFetchSuccess(
    stations: List<Station>,
    currentLocation: GeoPoint?,
    city: City,
    generation: Int,
  ) {
    if (isStaleGeneration(generation)) return
    val cityId = city.id

    if (useReactiveCache) {
      sessionState.value =
        StationsSession(
          isLoading = false,
          errorMessage = null,
          userLocation = currentLocation,
          lastRefreshAttemptEpoch = currentTimeMs(),
          lastRefreshFailureEpoch = null,
          dataSource = StationDataSource.Network,
          servingCacheAfterFailure = false,
          hardFailure = false,
        )
    } else {
      mutableState.value =
        StationsState(
          stations = stations,
          isLoading = false,
          userLocation = currentLocation,
          lastUpdatedEpoch = currentTimeMs(),
          dataSource = StationDataSource.Network,
          freshness = DataFreshness.Fresh,
          lastRefreshAttemptEpoch = currentTimeMs(),
          lastRefreshFailureEpoch = null,
        )
    }

    cacheManager.save(cityId, stations)

    loadMutex.withLock {
      if (generation != fetchGeneration) return
      loaded = true
      lastLoadedCityId = cityId
    }
  }

  private suspend fun handleFetchFailure(
    error: Throwable,
    currentLocation: GeoPoint?,
    city: City,
    origin: GeoPoint,
    generation: Int,
  ) {
    if (isStaleGeneration(generation)) return

    val lastUpdatedEpoch = cacheManager.lastUpdated(city.id)
    val now = currentTimeMs()

    if (lastUpdatedEpoch != null) {
      if (useReactiveCache) {
        sessionState.value =
          StationsSession(
            isLoading = false,
            errorMessage = null,
            userLocation = currentLocation,
            lastRefreshAttemptEpoch = now,
            lastRefreshFailureEpoch = now,
            dataSource = StationDataSource.Cache,
            servingCacheAfterFailure = true,
            hardFailure = false,
          )
      } else {
        mutableState.value =
          StationsState(
            stations = emptyList(),
            isLoading = false,
            errorMessage = null,
            userLocation = currentLocation,
            lastUpdatedEpoch = lastUpdatedEpoch,
            dataSource = StationDataSource.Cache,
            freshness =
              computeStationsFreshness(
                lastUpdatedEpoch = lastUpdatedEpoch,
                nowEpoch = now,
                servingCacheAfterFailure = true,
                stationsEmpty = false,
                hardFailure = false,
              ),
            lastRefreshAttemptEpoch = now,
            lastRefreshFailureEpoch = now,
          )
      }

      loadMutex.withLock {
        if (generation != fetchGeneration) return
        loaded = true
        lastLoadedCityId = city.id
      }
      return
    }

    if (useReactiveCache) {
      sessionState.value =
        StationsSession(
          isLoading = false,
          errorMessage = error.message ?: "No se pudo cargar BiciRadar.",
          userLocation = currentLocation,
          lastRefreshAttemptEpoch = now,
          lastRefreshFailureEpoch = now,
          dataSource = StationDataSource.Unavailable,
          servingCacheAfterFailure = false,
          hardFailure = true,
        )
    } else {
      mutableState.update {
        it.copy(
          isLoading = false,
          errorMessage = error.message ?: "No se pudo cargar BiciRadar.",
          userLocation = currentLocation,
          stations = emptyList(),
          lastUpdatedEpoch = null,
          dataSource = StationDataSource.Unavailable,
          freshness = DataFreshness.Unavailable,
          lastRefreshAttemptEpoch = now,
          lastRefreshFailureEpoch = now,
        )
      }
    }
  }

  private fun updateSessionAfterAvailabilityRefresh(refreshedAt: Long) {
    if (useReactiveCache) {
      sessionState.update {
        it.copy(
          lastRefreshAttemptEpoch = refreshedAt,
          lastRefreshFailureEpoch = null,
          dataSource = StationDataSource.Network,
          servingCacheAfterFailure = false,
          hardFailure = false,
          errorMessage = null,
        )
      }
    } else {
      mutableState.update { current ->
        current.copy(
          lastUpdatedEpoch = refreshedAt,
          dataSource = StationDataSource.Network,
          freshness = DataFreshness.Fresh,
          lastRefreshAttemptEpoch = refreshedAt,
          lastRefreshFailureEpoch = null,
          errorMessage = null,
        )
      }
    }
  }

  private fun isStaleGeneration(generation: Int): Boolean = generation != fetchGeneration
}

private fun mergeDbStationsState(
  stations: List<com.gcaguilar.biciradar.core.local.StationEntity>,
  metadata: CacheMetadata?,
  city: City,
  session: StationsSession,
): StationsState {
  val now = currentTimeMs()
  val origin = session.userLocation ?: GeoPoint(city.defaultLatitude, city.defaultLongitude)
  val cityMatches = metadata?.cityId == city.id
  val stationsFromCache =
    if (cityMatches && stations.isNotEmpty()) {
      stations.map { it.toDomain(origin) }
    } else {
      emptyList()
    }
  val lastUpdatedEpoch = metadata?.takeIf { it.cityId == city.id }?.lastUpdated

  if (session.hardFailure) {
    return StationsState(
      stations = emptyList(),
      isLoading = session.isLoading,
      errorMessage = session.errorMessage,
      userLocation = session.userLocation,
      lastUpdatedEpoch = lastUpdatedEpoch,
      dataSource = StationDataSource.Unavailable,
      freshness = DataFreshness.Unavailable,
      lastRefreshAttemptEpoch = session.lastRefreshAttemptEpoch,
      lastRefreshFailureEpoch = session.lastRefreshFailureEpoch,
    )
  }

  val stationsEmpty = stationsFromCache.isEmpty()
  val freshness =
    computeStationsFreshness(
      lastUpdatedEpoch = lastUpdatedEpoch,
      nowEpoch = now,
      servingCacheAfterFailure = session.servingCacheAfterFailure,
      stationsEmpty = stationsEmpty,
      hardFailure = false,
    )

  return StationsState(
    stations = stationsFromCache,
    isLoading = session.isLoading,
    errorMessage = session.errorMessage,
    userLocation = session.userLocation,
    lastUpdatedEpoch = lastUpdatedEpoch,
    dataSource = session.dataSource,
    freshness = freshness,
    lastRefreshAttemptEpoch = session.lastRefreshAttemptEpoch,
    lastRefreshFailureEpoch = session.lastRefreshFailureEpoch,
  )
}
