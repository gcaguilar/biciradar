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
  suspend fun bootstrap()
  suspend fun toggle(stationId: String)
  fun isFavorite(stationId: String): Boolean
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
  private var bootstrapped = false

  override val favoriteIds: StateFlow<Set<String>> = mutableFavoriteIds.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    val snapshotPath = favoritesPath()
    val localFavoriteIds = if (fileSystem.exists(snapshotPath)) {
      val snapshot = json.decodeFromString<FavoritesSnapshot>(fileSystem.read(snapshotPath) { readUtf8() })
      snapshot.favoriteIds
    } else {
      emptySet()
    }
    val remoteFavoriteIds = withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.latestFavoriteIds()
    }.orEmpty()
    val mergedFavoriteIds = localFavoriteIds + remoteFavoriteIds
    mutableFavoriteIds.value = mergedFavoriteIds
    if (mergedFavoriteIds.isNotEmpty()) {
      persist(mergedFavoriteIds)
      withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
        watchSyncBridge.pushFavoriteIds(mergedFavoriteIds)
      }
    }
    bootstrapped = true
  }

  override suspend fun toggle(stationId: String) {
    if (!bootstrapped) bootstrap()
    val updated = mutableFavoriteIds.value.toMutableSet().apply {
      if (!add(stationId)) remove(stationId)
    }.toSet()
    mutableFavoriteIds.value = updated
    persist(updated)
    withTimeoutOrNull(WATCH_SYNC_TIMEOUT_MILLIS) {
      watchSyncBridge.pushFavoriteIds(updated)
    }
  }

  override fun isFavorite(stationId: String): Boolean = mutableFavoriteIds.value.contains(stationId)

  private fun favoritesPath() = "${storageDirectoryProvider.rootPath}/favorites.json".toPath()

  private suspend fun persist(favoriteIds: Set<String>) {
    val path = favoritesPath()
    fileSystem.createDirectories(path.parent!!)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(FavoritesSnapshot(favoriteIds)))
    }
  }
}

@Serializable
internal data class FavoritesSnapshot(
  val favoriteIds: Set<String>,
)
