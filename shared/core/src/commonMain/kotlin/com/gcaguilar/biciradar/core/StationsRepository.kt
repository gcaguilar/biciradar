package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.toDomain
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

private const val LOCATION_LOOKUP_TIMEOUT_MILLIS = 3_000L
const val STATION_CACHE_REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

interface StationsRepository {
  val state: StateFlow<StationsState>
  suspend fun loadIfNeeded()
  suspend fun forceRefresh()
  suspend fun refreshAvailability(stationIds: List<String>)
  fun stationById(stationId: String): Station?
}

@Inject
class StationsRepositoryImpl(
  private val biziApi: BiziApi,
  private val appConfiguration: AppConfiguration,
  private val locationProvider: LocationProvider,
  private val settingsRepository: SettingsRepository,
  private val database: BiciRadarDatabase?,
) : StationsRepository {
  private val mutableState = MutableStateFlow(StationsState(isLoading = false))
  private val cacheStore = database?.let(::StationCacheStore)
  private var loaded = false
  private var lastLoadedCityId: String? = null
  private val loadMutex = Mutex()

  override val state: StateFlow<StationsState> = mutableState.asStateFlow()

  private fun defaultLocation(): GeoPoint {
    val city = settingsRepository.currentSelectedCity()
    return GeoPoint(city.defaultLatitude, city.defaultLongitude)
  }

  override suspend fun forceRefresh() {
    val currentLocation = withTimeoutOrNull(LOCATION_LOOKUP_TIMEOUT_MILLIS) {
      runCatching { locationProvider.currentLocation() }.getOrNull()
    }
    val origin = currentLocation ?: defaultLocation()
    val city = settingsRepository.currentSelectedCity()
    val attemptAt = currentTimeMs()
    mutableState.update {
      it.copy(
        isLoading = true,
        errorMessage = null,
        lastRefreshAttemptEpoch = attemptAt,
      )
    }
    refreshStations(origin = origin, currentLocation = currentLocation, city = city)
  }

  override suspend fun loadIfNeeded() {
    val currentLocation = withTimeoutOrNull(LOCATION_LOOKUP_TIMEOUT_MILLIS) {
      runCatching { locationProvider.currentLocation() }.getOrNull()
    }
    val origin = currentLocation ?: defaultLocation()
    val city = settingsRepository.currentSelectedCity()

    loadMutex.withLock {
      if (loaded) return
      val attemptAt = currentTimeMs()
      mutableState.update {
        it.copy(
          isLoading = true,
          errorMessage = null,
          lastRefreshAttemptEpoch = attemptAt,
        )
      }
    }

    if (cacheStore != null && lastLoadedCityId != null && lastLoadedCityId != city.id) {
      cacheStore.clear()
    }

    if (cacheStore != null) {
      val cachedStations = cacheStore.loadStations(city.id)
      val isCacheValid = cachedStations != null && cacheStore.isFresh(city.id)

      if (isCacheValid && cachedStations.isNotEmpty()) {
        val stations = cachedStations.map { it.toDomain(origin) }
        val lastUpdatedEpoch = cacheStore.lastUpdated(city.id)
        val now = currentTimeMs()
        mutableState.value = StationsState(
          stations = stations,
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
        loadMutex.withLock {
          loaded = true
          lastLoadedCityId = city.id
        }
        return
      }
    }

    refreshStations(origin = origin, currentLocation = currentLocation, city = city)
  }

  private suspend fun refreshStations(
    origin: GeoPoint,
    currentLocation: GeoPoint?,
    city: City,
  ) {
    runCatching { biziApi.fetchStations(origin) }
      .onSuccess { stations ->
        cacheStore?.save(city.id, stations)
        val lastUpdatedEpoch = currentTimeMs()
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
        loadMutex.withLock {
          loaded = true
          lastLoadedCityId = city.id
        }
      }
      .onFailure { error ->
        if (cacheStore != null) {
          val staleStations = cacheStore.loadStations(city.id)
          if (staleStations != null && staleStations.isNotEmpty()) {
            val stations = staleStations.map { it.toDomain(origin) }
            val lastUpdatedEpoch = cacheStore.lastUpdated(city.id)
            val now = currentTimeMs()
            mutableState.value = StationsState(
              stations = stations,
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
            loadMutex.withLock {
              loaded = true
              lastLoadedCityId = city.id
            }
            return@onFailure
          }
        }
        val now = currentTimeMs()
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

  override suspend fun refreshAvailability(stationIds: List<String>) {
    if (stationIds.isEmpty()) return
    val availability = runCatching { biziApi.fetchAvailability(stationIds) }.getOrNull() ?: return
    if (availability.isEmpty()) return
    val refreshedAt = currentTimeMs()
    mutableState.update { current ->
      current.copy(
        stations = current.stations.map { station ->
          val update = availability[station.id] ?: return@map station
          station.copy(bikesAvailable = update.bikesAvailable, slotsFree = update.slotsFree)
        },
        lastUpdatedEpoch = refreshedAt,
        dataSource = StationDataSource.Network,
        freshness = DataFreshness.Fresh,
        lastRefreshAttemptEpoch = refreshedAt,
        lastRefreshFailureEpoch = null,
      )
    }
  }

  override fun stationById(stationId: String): Station? =
    mutableState.value.stations.firstOrNull { it.id == stationId }
}
