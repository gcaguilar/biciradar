package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

private const val WATCH_SYNC_TIMEOUT_MILLIS = 2_000L

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
class FavoritesRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val watchSyncBridge: WatchSyncBridge,
  private val database: BiciRadarDatabase? = null,
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
    syncMergedSnapshot(readPersistedSnapshot(), pushIfChanged = true)
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

  private fun readPersistedSnapshot(): FavoritesSyncSnapshot {
    if (database == null) return readLocalSnapshot()
    val dbSnapshot = readDatabaseSnapshot() ?: FavoritesSyncSnapshot()
    val legacySnapshot = readLocalSnapshot()
    val dbHasData = dbSnapshot.hasData()
    val legacyHasData = legacySnapshot.hasData()

    if (!legacyHasData) return dbSnapshot
    if (dbHasData) {
      if (dbSnapshot == legacySnapshot) {
        deleteLegacyFile()
      }
      return dbSnapshot
    }

    val migrated = persistToDatabase(legacySnapshot)
    if (migrated) {
      deleteLegacyFile()
      return legacySnapshot
    }
    return legacySnapshot
  }

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
    if (database != null) {
      val persisted = persistToDatabase(snapshot)
      if (persisted) {
        deleteLegacyFile()
      } else {
        persistToLocalFile(snapshot)
      }
      return
    }
    persistToLocalFile(snapshot)
  }

  private fun readDatabaseSnapshot(): FavoritesSyncSnapshot? {
    val db = database ?: return null
    return runCatching {
      val ids = db.biciradarQueries.getAllFavoriteIds().executeAsList().toSet()
      val roles = db.biciradarQueries.getFavoriteRoles().executeAsOneOrNull()
      FavoritesSyncSnapshot(
        favoriteIds = ids,
        homeStationId = roles?.home_station_id,
        workStationId = roles?.work_station_id,
      )
    }.getOrNull()
  }

  private fun persistToDatabase(snapshot: FavoritesSyncSnapshot): Boolean {
    val db = database ?: return false
    return runCatching {
      db.transaction {
        db.biciradarQueries.deleteAllFavoriteIds()
        snapshot.favoriteIds.forEach { stationId ->
          db.biciradarQueries.insertFavoriteId(stationId)
        }
        if (snapshot.homeStationId == null && snapshot.workStationId == null) {
          db.biciradarQueries.clearFavoriteRoles()
        } else {
          db.biciradarQueries.upsertFavoriteRoles(snapshot.homeStationId, snapshot.workStationId)
        }
      }
    }.isSuccess
  }

  private fun persistToLocalFile(snapshot: FavoritesSyncSnapshot) {
    val path = favoritesPath()
    val dir = path.parent ?: return
    fileSystem.createDirectories(dir)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(snapshot))
    }
  }

  private fun deleteLegacyFile() {
    val path = favoritesPath()
    if (fileSystem.exists(path)) {
      runCatching { fileSystem.delete(path) }
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
