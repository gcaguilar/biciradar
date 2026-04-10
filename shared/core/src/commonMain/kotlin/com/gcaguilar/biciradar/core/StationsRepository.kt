package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.toDomain
import dev.zacsweers.metro.Inject
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

private const val LOCATION_LOOKUP_TIMEOUT_MILLIS = 3_000L

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
 */
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
  private var loaded = false
  private var lastLoadedCityId: String? = null
  private val loadMutex = Mutex()

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

  private fun defaultLocation(): GeoPoint {
    val city = settingsRepository.currentSelectedCity()
    return GeoPoint(city.defaultLatitude, city.defaultLongitude)
  }

  override suspend fun forceRefresh() {
    val currentLocation = fetchCurrentLocation()
    val origin = currentLocation ?: defaultLocation()
    val city = settingsRepository.currentSelectedCity()
    val attemptAt = currentTimeMs()

    updateLoadingState(
      isLoading = true,
      errorMessage = null,
      lastRefreshAttemptEpoch = attemptAt,
      userLocation = currentLocation,
    )

    refreshStations(origin = origin, currentLocation = currentLocation, city = city)
  }

  override suspend fun loadIfNeeded() {
    val currentLocation = fetchCurrentLocation()
    val origin = currentLocation ?: defaultLocation()
    val city = settingsRepository.currentSelectedCity()

    loadMutex.withLock {
      if (loaded) return
      val attemptAt = currentTimeMs()
      updateLoadingState(
        isLoading = true,
        errorMessage = null,
        lastRefreshAttemptEpoch = attemptAt,
        userLocation = currentLocation,
      )
    }

    // Limpiar caché si cambió la ciudad
    if (lastLoadedCityId != null && lastLoadedCityId != city.id) {
      cacheManager.clear()
    }

    // Intentar usar caché primero
    if (cacheManager.isFresh(city.id)) {
      handleCacheHit(currentLocation, city)
      return
    }

    refreshStations(origin = origin, currentLocation = currentLocation, city = city)
  }

  override suspend fun refreshAvailability(stationIds: List<String>) {
    if (stationIds.isEmpty()) return

    val availability = runCatching {
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

  override fun stationById(stationId: String): Station? =
    state.value.stations.firstOrNull { it.id == stationId }

  // Helper methods

  private suspend fun fetchCurrentLocation(): GeoPoint? {
    return withTimeoutOrNull(LOCATION_LOOKUP_TIMEOUT_MILLIS) {
      runCatching { locationProvider.currentLocation() }.getOrNull()
    }
  }

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
  ) {
    runCatching { remoteDataSource.fetchStations(origin) }
      .onSuccess { stations ->
        handleFetchSuccess(stations, currentLocation, city)
      }
      .onFailure { error ->
        handleFetchFailure(error, currentLocation, city, origin)
      }
  }

  private suspend fun handleCacheHit(
    currentLocation: GeoPoint?,
    city: City,
  ) {
    val lastUpdatedEpoch = cacheManager.lastUpdated(city.id)
    val now = currentTimeMs()

    if (useReactiveCache) {
      sessionState.value = StationsSession(
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
      // En modo no-reactivo, necesitamos obtener las estaciones del caché
      // Esto es un caso edge, normalmente useReactiveCache = true cuando hay base de datos
      mutableState.value = StationsState(
        stations = emptyList(), // Se poblará desde el flow reactivo
        isLoading = false,
        userLocation = currentLocation,
        lastUpdatedEpoch = lastUpdatedEpoch,
        dataSource = StationDataSource.Cache,
        freshness = computeStationsFreshness(
          lastUpdatedEpoch = lastUpdatedEpoch,
          nowEpoch = now,
          servingCacheAfterFailure = false,
          stationsEmpty = false,
          hardFailure = false,
        ),
      )
    }

    loadMutex.withLock {
      loaded = true
      lastLoadedCityId = city.id
    }
  }

  private suspend fun handleFetchSuccess(
    stations: List<Station>,
    currentLocation: GeoPoint?,
    city: City,
  ) {
    val cityId = city.id
    cacheManager.save(cityId, stations)
    val lastUpdatedEpoch = currentTimeMs()

    if (useReactiveCache) {
      sessionState.value = StationsSession(
        isLoading = false,
        errorMessage = null,
        userLocation = currentLocation,
        lastRefreshAttemptEpoch = lastUpdatedEpoch,
        lastRefreshFailureEpoch = null,
        dataSource = StationDataSource.Network,
        servingCacheAfterFailure = false,
        hardFailure = false,
      )
    } else {
      mutableState.value = StationsState(
        stations = stations,
        isLoading = false,
        userLocation = currentLocation,
        lastUpdatedEpoch = lastUpdatedEpoch,
        dataSource = StationDataSource.Network,
        freshness = DataFreshness.Fresh,
        lastRefreshAttemptEpoch = lastUpdatedEpoch,
        lastRefreshFailureEpoch = null,
      )
    }

    loadMutex.withLock {
      loaded = true
      lastLoadedCityId = cityId
    }
  }

  private suspend fun handleFetchFailure(
    error: Throwable,
    currentLocation: GeoPoint?,
    city: City,
    origin: GeoPoint,
  ) {
    // Intentar usar caché stale como fallback
    val lastUpdatedEpoch = cacheManager.lastUpdated(city.id)
    val now = currentTimeMs()

    if (lastUpdatedEpoch != null) {
      // Hay caché disponible (aunque sea stale)
      if (useReactiveCache) {
        sessionState.value = StationsSession(
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
        mutableState.value = StationsState(
          stations = emptyList(), // Se poblará desde el flow
          isLoading = false,
          errorMessage = null,
          userLocation = currentLocation,
          lastUpdatedEpoch = lastUpdatedEpoch,
          dataSource = StationDataSource.Cache,
          freshness = computeStationsFreshness(
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
        loaded = true
        lastLoadedCityId = city.id
      }
      return
    }

    // No hay caché disponible, reportar error
    if (useReactiveCache) {
      sessionState.value = StationsSession(
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
        )
      }
    }
  }
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
  val stationsFromCache = if (cityMatches && stations.isNotEmpty()) {
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
  val freshness = computeStationsFreshness(
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
