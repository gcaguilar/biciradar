package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.StationEntity
import com.gcaguilar.biciradar.core.local.toDomain
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

private const val LOCATION_LOOKUP_TIMEOUT_MILLIS = 3_000L
private const val WATCH_SYNC_TIMEOUT_MILLIS = 2_000L
const val STATION_CACHE_REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutes

interface StationsRepository {
  val state: StateFlow<StationsState>
  suspend fun loadIfNeeded()
  suspend fun forceRefresh()
  suspend fun refreshAvailability(stationIds: List<String>)
  fun stationById(stationId: String): Station?
}

interface FavoritesRepository {
  val favoriteIds: StateFlow<Set<String>>
  val homeStationId: StateFlow<String?>
  val workStationId: StateFlow<String?>
  suspend fun bootstrap()
  suspend fun syncFromPeer() = Unit
  suspend fun toggle(stationId: String)
  suspend fun setHomeStationId(stationId: String?)
  suspend fun setWorkStationId(stationId: String?)
  suspend fun clearAll()
  fun isFavorite(stationId: String): Boolean
  fun currentHomeStationId(): String?
  fun currentWorkStationId(): String?
}

@Inject
class StationsRepositoryImpl(
  private val biziApi: BiziApi,
  private val appConfiguration: AppConfiguration,
  private val locationProvider: LocationProvider,
  private val settingsRepository: SettingsRepository,
  private val database: BiciRadarDatabase?,
) : StationsRepository {

  companion object {
    fun clearCacheForCityChange(database: BiciRadarDatabase, newCityId: String) {
      try {
        val metadata = database.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
        if (metadata?.city_id != newCityId) {
          database.transaction {
            database.biciradarQueries.deleteAllStations()
            database.biciradarQueries.deleteAllCacheMetadata()
          }
        }
      } catch (e: Exception) {
        // Silently fail
      }
    }
  }
  private val mutableState = MutableStateFlow(StationsState(isLoading = false))
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

    // Clear cache if city changed
    if (database != null && lastLoadedCityId != null && lastLoadedCityId != city.id) {
      clearCache()
    }

    // Check cache first (only if database is available)
    if (database != null) {
      val cachedStations = loadFromCache(city.id)
      val isCacheValid = cachedStations != null && isCacheFresh(city.id)

      if (isCacheValid && cachedStations.isNotEmpty()) {
        // Load from cache (transparent to user)
        val stations = cachedStations.map { it.toDomain(origin) }
        val lastUp = metadataLastUpdated(city.id)
        val now = currentTimeMs()
        mutableState.value = StationsState(
          stations = stations,
          isLoading = false,
          userLocation = currentLocation,
          lastUpdatedEpoch = lastUp,
          dataSource = StationDataSource.Cache,
          freshness = computeStationsFreshness(
            lastUpdatedEpoch = lastUp,
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
    // Fetch from API and cache
    runCatching { biziApi.fetchStations(origin) }
      .onSuccess { stations ->
        // Save to cache
        saveToCache(city.id, stations)
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
        // Try to use stale cache if available.
        if (database != null) {
          val staleStations = loadFromCache(city.id)
          if (staleStations != null && staleStations.isNotEmpty()) {
            val stations = staleStations.map { it.toDomain(origin) }
            val lastUp = metadataLastUpdated(city.id)
            val now = currentTimeMs()
            mutableState.value = StationsState(
              stations = stations,
              isLoading = false,
              errorMessage = null,
              userLocation = currentLocation,
              lastUpdatedEpoch = lastUp,
              dataSource = StationDataSource.Cache,
              freshness = computeStationsFreshness(
                lastUpdatedEpoch = lastUp,
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

  private fun loadFromCache(cityId: String): List<StationEntity>? {
    return try {
      val db = database ?: return null
      val metadata = db.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
      if (metadata?.city_id == cityId) {
        db.biciradarQueries.getAllStations().executeAsList().map { row ->
          StationEntity(
            id = row.id,
            name = row.name,
            address = row.address ?: "",
            latitude = row.latitude,
            longitude = row.longitude,
            bikesAvailable = row.bikes_available.toInt(),
            slotsFree = row.slots_free.toInt(),
            ebikesAvailable = row.ebikes_available.toInt(),
            regularBikesAvailable = row.regular_bikes_available.toInt(),
            updatedAt = row.updated_at,
          )
        }
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }

  private fun isCacheFresh(cityId: String): Boolean {
    return try {
      val db = database ?: return false
      val metadata = db.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
      if (metadata?.city_id == cityId) {
        val elapsed = currentTimeMs() - metadata.last_updated
         elapsed < STATION_CACHE_REFRESH_INTERVAL_MS
      } else {
        false
      }
    } catch (e: Exception) {
      false
    }
  }

  private fun clearCache() {
    try {
      val db = database ?: return
      db.transaction {
        db.biciradarQueries.deleteAllStations()
        db.biciradarQueries.deleteAllCacheMetadata()
      }
    } catch (e: Exception) {
      // Silently fail
    }
  }

  private fun metadataLastUpdated(cityId: String): Long? {
    return try {
      val db = database ?: return null
      db.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
        ?.takeIf { it.city_id == cityId }
        ?.last_updated
    } catch (e: Exception) {
      null
    }
  }

  private fun saveToCache(cityId: String, stations: List<Station>) {
    try {
      val db = database ?: return
      db.transaction {
        // Clear old data
        db.biciradarQueries.deleteAllStations()
        db.biciradarQueries.deleteAllCacheMetadata()
        // Insert new stations
        stations.forEach { station ->
          db.biciradarQueries.insertStation(
            id = station.id,
            name = station.name,
            address = station.address,
            latitude = station.location.latitude,
            longitude = station.location.longitude,
            bikesAvailable = station.bikesAvailable.toLong(),
            slotsFree = station.slotsFree.toLong(),
            ebikesAvailable = station.ebikesAvailable.toLong(),
            regularBikesAvailable = station.regularBikesAvailable.toLong(),
            updatedAt = currentTimeMs(),
          )
        }
        // Save metadata
        db.biciradarQueries.upsertCacheMetadata(
          cityId = cityId,
          lastUpdated = currentTimeMs(),
        )
      }
    } catch (e: Exception) {
      // Silently fail - cache is optional
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

@Inject
class FavoritesRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val watchSyncBridge: WatchSyncBridge,
) : FavoritesRepository {
  private val mutableFavoriteIds = MutableStateFlow(emptySet<String>())
  private val mutableHomeStationId = MutableStateFlow<String?>(null)
  private val mutableWorkStationId = MutableStateFlow<String?>(null)
  private var bootstrapped = false

  override val favoriteIds: StateFlow<Set<String>> = mutableFavoriteIds.asStateFlow()
  override val homeStationId: StateFlow<String?> = mutableHomeStationId.asStateFlow()
  override val workStationId: StateFlow<String?> = mutableWorkStationId.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    syncMergedSnapshot(readLocalSnapshot(), pushIfChanged = true)
    bootstrapped = true
  }

  override suspend fun syncFromPeer() {
    if (!bootstrapped) {
      bootstrap()
      return
    }
    syncMergedSnapshot(currentSnapshot(), pushIfChanged = false)
  }

  override suspend fun toggle(stationId: String) {
    if (!bootstrapped) bootstrap()
    val updatedFavoriteIds = mutableFavoriteIds.value.toMutableSet().apply {
      if (!add(stationId)) remove(stationId)
    }.toSet()
    val updatedSnapshot = currentSnapshot().copy(favoriteIds = updatedFavoriteIds)
    applySnapshot(updatedSnapshot)
    persist(updatedSnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(updatedSnapshot)
    }
  }

  override suspend fun setHomeStationId(stationId: String?) {
    if (!bootstrapped) bootstrap()
    val updatedSnapshot = currentSnapshot()
      .copy(homeStationId = stationId?.takeIf(String::isNotBlank))
      .deduplicated()
    applySnapshot(updatedSnapshot)
    persist(updatedSnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(updatedSnapshot)
    }
  }

  override suspend fun setWorkStationId(stationId: String?) {
    if (!bootstrapped) bootstrap()
    val updatedSnapshot = currentSnapshot()
      .copy(workStationId = stationId?.takeIf(String::isNotBlank))
      .deduplicated()
    applySnapshot(updatedSnapshot)
    persist(updatedSnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(updatedSnapshot)
    }
  }

  override suspend fun clearAll() {
    if (!bootstrapped) bootstrap()
    val emptySnapshot = FavoritesSyncSnapshot()
    applySnapshot(emptySnapshot)
    persist(emptySnapshot)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavorites(emptySnapshot)
    }
  }

  override fun isFavorite(stationId: String): Boolean = mutableFavoriteIds.value.contains(stationId)
  override fun currentHomeStationId(): String? = mutableHomeStationId.value
  override fun currentWorkStationId(): String? = mutableWorkStationId.value

  private fun favoritesPath() = "${storageDirectoryProvider.rootPath}/favorites.json".toPath()

  private fun readLocalSnapshot(): FavoritesSyncSnapshot {
    val snapshotPath = favoritesPath()
    return if (fileSystem.exists(snapshotPath)) {
      json.decodeFromString<FavoritesSyncSnapshot>(fileSystem.read(snapshotPath) { readUtf8() })
    } else {
      FavoritesSyncSnapshot()
    }
  }

  private fun currentSnapshot(): FavoritesSyncSnapshot = FavoritesSyncSnapshot(
    favoriteIds = mutableFavoriteIds.value,
    homeStationId = mutableHomeStationId.value,
    workStationId = mutableWorkStationId.value,
  )

  private suspend fun syncMergedSnapshot(
    localSnapshot: FavoritesSyncSnapshot,
    pushIfChanged: Boolean,
  ) {
    val remoteSnapshot = withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.latestFavorites()
    } ?: FavoritesSyncSnapshot()
    val mergedSnapshot = FavoritesSyncSnapshot(
      favoriteIds = localSnapshot.favoriteIds + remoteSnapshot.favoriteIds,
      homeStationId = localSnapshot.homeStationId ?: remoteSnapshot.homeStationId,
      workStationId = localSnapshot.workStationId ?: remoteSnapshot.workStationId,
    ).deduplicated()
    val existingSnapshot = currentSnapshot()
    val hasChanged = mergedSnapshot != existingSnapshot
    if (hasChanged) {
      applySnapshot(mergedSnapshot)
      persist(mergedSnapshot)
    } else if (!bootstrapped && mergedSnapshot.hasData()) {
      persist(mergedSnapshot)
    }
    if (mergedSnapshot.hasData() && (hasChanged || !bootstrapped || pushIfChanged)) {
      withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
        watchSyncBridge.pushFavorites(mergedSnapshot)
      }
    }
  }

  private fun applySnapshot(snapshot: FavoritesSyncSnapshot) {
    mutableFavoriteIds.value = snapshot.favoriteIds
    mutableHomeStationId.value = snapshot.homeStationId
    mutableWorkStationId.value = snapshot.workStationId
  }

  private suspend fun persist(snapshot: FavoritesSyncSnapshot) {
    val path = favoritesPath()
    val dir = path.parent ?: return
    fileSystem.createDirectories(dir)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(snapshot))
    }
  }
}

private fun FavoritesSyncSnapshot.deduplicated(): FavoritesSyncSnapshot =
  if (homeStationId != null && homeStationId == workStationId) {
    copy(workStationId = null)
  } else {
    this
  }

private fun FavoritesSyncSnapshot.hasData(): Boolean =
  favoriteIds.isNotEmpty() || homeStationId != null || workStationId != null
