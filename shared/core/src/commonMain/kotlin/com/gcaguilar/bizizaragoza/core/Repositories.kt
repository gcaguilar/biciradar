package com.gcaguilar.bizizaragoza.core

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

private const val LOCATION_LOOKUP_TIMEOUT_MILLIS = 3_000L
private const val WATCH_SYNC_TIMEOUT_MILLIS = 2_000L

interface StationsRepository {
  val state: StateFlow<StationsState>
  suspend fun loadIfNeeded()
  suspend fun refreshAvailability(stationIds: List<String>)
  fun stationById(stationId: String): Station?
}

interface FavoritesRepository {
  val favoriteIds: StateFlow<Set<String>>
  val homeStationId: StateFlow<String?>
  val workStationId: StateFlow<String?>
  suspend fun bootstrap()
  suspend fun toggle(stationId: String)
  suspend fun setHomeStationId(stationId: String?)
  suspend fun setWorkStationId(stationId: String?)
  fun isFavorite(stationId: String): Boolean
  fun currentHomeStationId(): String?
  fun currentWorkStationId(): String?
}

@Inject
class StationsRepositoryImpl(
  private val biziApi: BiziApi,
  private val appConfiguration: AppConfiguration,
  private val locationProvider: LocationProvider,
) : StationsRepository {
  private val mutableState = MutableStateFlow(StationsState(isLoading = false))
  private var loaded = false

  override val state: StateFlow<StationsState> = mutableState.asStateFlow()

  override suspend fun loadIfNeeded() {
    if (loaded) return
    mutableState.update { it.copy(isLoading = true, errorMessage = null) }
    val currentLocation = withTimeoutOrNull(LOCATION_LOOKUP_TIMEOUT_MILLIS) {
      runCatching { locationProvider.currentLocation() }.getOrNull()
    }
    val origin = currentLocation ?: appConfiguration.defaultLocation()
    runCatching { biziApi.fetchStations(origin) }
      .onSuccess { stations ->
        mutableState.value = StationsState(
          stations = stations,
          isLoading = false,
          userLocation = currentLocation,
        )
        loaded = true
      }
      .onFailure { error ->
        // No marcar loaded=true para permitir reintentos
        mutableState.update {
          it.copy(
            isLoading = false,
            errorMessage = error.message ?: "No se pudo cargar Bizi Zaragoza.",
            userLocation = currentLocation,
          )
        }
      }
  }

  override suspend fun refreshAvailability(stationIds: List<String>) {
    if (stationIds.isEmpty()) return
    val availability = runCatching { biziApi.fetchAvailability(stationIds) }.getOrNull() ?: return
    if (availability.isEmpty()) return
    mutableState.update { current ->
      current.copy(
        stations = current.stations.map { station ->
          val update = availability[station.id] ?: return@map station
          station.copy(bikesAvailable = update.bikesAvailable, slotsFree = update.slotsFree)
        },
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
    val snapshotPath = favoritesPath()
    val localSnapshot = if (fileSystem.exists(snapshotPath)) {
      json.decodeFromString<FavoritesSyncSnapshot>(fileSystem.read(snapshotPath) { readUtf8() })
    } else {
      FavoritesSyncSnapshot()
    }
    val remoteSnapshot = withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.latestFavorites()
    } ?: FavoritesSyncSnapshot()
    val mergedSnapshot = FavoritesSyncSnapshot(
      favoriteIds = localSnapshot.favoriteIds + remoteSnapshot.favoriteIds,
      homeStationId = localSnapshot.homeStationId ?: remoteSnapshot.homeStationId,
      workStationId = localSnapshot.workStationId ?: remoteSnapshot.workStationId,
    ).deduplicated()
    applySnapshot(mergedSnapshot)
    if (mergedSnapshot.hasData()) {
      persist(mergedSnapshot)
      withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
        watchSyncBridge.pushFavorites(mergedSnapshot)
      }
    }
    bootstrapped = true
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

  override fun isFavorite(stationId: String): Boolean = mutableFavoriteIds.value.contains(stationId)
  override fun currentHomeStationId(): String? = mutableHomeStationId.value
  override fun currentWorkStationId(): String? = mutableWorkStationId.value

  private fun favoritesPath() = "${storageDirectoryProvider.rootPath}/favorites.json".toPath()

  private fun currentSnapshot(): FavoritesSyncSnapshot = FavoritesSyncSnapshot(
    favoriteIds = mutableFavoriteIds.value,
    homeStationId = mutableHomeStationId.value,
    workStationId = mutableWorkStationId.value,
  )

  private fun applySnapshot(snapshot: FavoritesSyncSnapshot) {
    mutableFavoriteIds.value = snapshot.favoriteIds
    mutableHomeStationId.value = snapshot.homeStationId
    mutableWorkStationId.value = snapshot.workStationId
  }

  private suspend fun persist(snapshot: FavoritesSyncSnapshot) {
    val path = favoritesPath()
    fileSystem.createDirectories(path.parent!!)
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
