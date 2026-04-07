package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.StationEntity
import com.gcaguilar.biciradar.core.local.toDomain
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
const val STATION_CACHE_REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

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

@Inject
class StationsRepositoryImpl(
  private val biziApi: BiziApi,
  private val appConfiguration: AppConfiguration,
  private val locationProvider: LocationProvider,
  private val settingsRepository: SettingsRepository,
  private val database: BiciRadarDatabase?,
  private val scope: CoroutineScope,
) : StationsRepository {
  private val mutableState = MutableStateFlow(StationsState(isLoading = false))
  private val cacheStore = database?.let(::StationCacheStore)
  private val sessionState = MutableStateFlow(StationsSession())
  private var loaded = false
  private var lastLoadedCityId: String? = null
  private val loadMutex = Mutex()

  private val useReactiveCache: Boolean = database != null

  override val state: StateFlow<StationsState> =
    if (useReactiveCache) {
      combine(
        database!!.biciradarQueries.getAllStations().asFlow().mapToList(Dispatchers.Default),
        database.biciradarQueries.getCacheMetadata().asFlow().mapToOneOrNull(Dispatchers.Default),
        settingsRepository.selectedCity,
        sessionState,
      ) { rows, meta, city, session ->
        mergeDbStationsState(rows, meta, city, session)
      }.stateIn(scope, SharingStarted.Eagerly, StationsState(isLoading = false))
    } else {
      mutableState.asStateFlow()
    }

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
    if (useReactiveCache) {
      sessionState.update {
        it.copy(
          isLoading = true,
          errorMessage = null,
          lastRefreshAttemptEpoch = attemptAt,
          userLocation = currentLocation ?: it.userLocation,
        )
      }
    } else {
      mutableState.update {
        it.copy(
          isLoading = true,
          errorMessage = null,
          lastRefreshAttemptEpoch = attemptAt,
        )
      }
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
      if (useReactiveCache) {
        sessionState.update {
          it.copy(
            isLoading = true,
            errorMessage = null,
            lastRefreshAttemptEpoch = attemptAt,
            userLocation = currentLocation ?: it.userLocation,
          )
        }
      } else {
        mutableState.update {
          it.copy(
            isLoading = true,
            errorMessage = null,
            lastRefreshAttemptEpoch = attemptAt,
          )
        }
      }
    }

    if (cacheStore != null && lastLoadedCityId != null && lastLoadedCityId != city.id) {
      cacheStore.clear()
    }

    if (cacheStore != null) {
      val cachedStations = cacheStore.loadStations(city.id)
      val isCacheValid = cachedStations != null && cacheStore.isFresh(city.id)

      if (isCacheValid && cachedStations.isNotEmpty()) {
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
          val lastUpdatedEpoch = cacheStore.lastUpdated(city.id)
          val now = currentTimeMs()
          mutableState.value = StationsState(
            stations = cachedStations.map { it.toDomain(origin) },
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
          lastLoadedCityId = city.id
        }
      }
      .onFailure { error ->
        if (cacheStore != null) {
          val staleStations = cacheStore.loadStations(city.id)
          if (staleStations != null && staleStations.isNotEmpty()) {
            val lastUpdatedEpoch = cacheStore.lastUpdated(city.id)
            val now = currentTimeMs()
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
                stations = staleStations.map { it.toDomain(origin) },
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
            return@onFailure
          }
        }
        val now = currentTimeMs()
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
  }

  override suspend fun refreshAvailability(stationIds: List<String>) {
    if (stationIds.isEmpty()) return
    val availability = runCatching { biziApi.fetchAvailability(stationIds) }.getOrNull() ?: return
    if (availability.isEmpty()) return
    val refreshedAt = currentTimeMs()
    if (useReactiveCache && cacheStore != null) {
      cacheStore.updateAvailability(
        availability.mapValues { (_, v) -> v.bikesAvailable to v.slotsFree },
        refreshedAt,
      )
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
  }

  override fun stationById(stationId: String): Station? =
    state.value.stations.firstOrNull { it.id == stationId }
}

private fun mergeDbStationsState(
  rows: List<Stations>,
  meta: Cache_metadata?,
  city: City,
  session: StationsSession,
): StationsState {
  val now = currentTimeMs()
  val origin = session.userLocation ?: GeoPoint(city.defaultLatitude, city.defaultLongitude)
  val cityMatches = meta?.city_id == city.id
  val stationsFromCache = if (cityMatches && rows.isNotEmpty()) {
    rows.map { it.toStationEntity().toDomain(origin) }
  } else {
    emptyList()
  }
  val lastUpdatedEpoch = meta?.takeIf { it.city_id == city.id }?.last_updated

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

private fun Stations.toStationEntity(): StationEntity = StationEntity(
  id = id,
  name = name,
  address = address ?: "",
  latitude = latitude,
  longitude = longitude,
  bikesAvailable = bikes_available.toInt(),
  slotsFree = slots_free.toInt(),
  ebikesAvailable = ebikes_available.toInt(),
  regularBikesAvailable = regular_bikes_available.toInt(),
  updatedAt = updated_at,
)
