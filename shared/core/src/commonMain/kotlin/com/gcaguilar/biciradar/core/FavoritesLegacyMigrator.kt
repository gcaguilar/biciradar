package com.gcaguilar.biciradar.core

import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

internal class FavoritesLegacyMigrator(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val persistToDatabase: (FavoritesSyncSnapshot) -> Boolean,
) {
  fun readLegacySnapshot(): FavoritesSyncSnapshot {
    val snapshotPath = favoritesPath()
    return if (fileSystem.exists(snapshotPath)) {
      runCatching {
        json.decodeFromString<FavoritesSyncSnapshot>(fileSystem.read(snapshotPath) { readUtf8() })
      }.getOrDefault(FavoritesSyncSnapshot())
    } else {
      FavoritesSyncSnapshot()
    }
  }

  fun migrateIfNeeded(dbSnapshot: FavoritesSyncSnapshot?): FavoritesSyncSnapshot {
    val normalizedDb = dbSnapshot ?: FavoritesSyncSnapshot()
    val legacySnapshot = readLegacySnapshot()
    val dbHasData = normalizedDb.hasLegacyData()
    val legacyHasData = legacySnapshot.hasLegacyData()

    if (!legacyHasData) return normalizedDb
    if (dbHasData) {
      if (normalizedDb == legacySnapshot) {
        deleteLegacyFile()
      }
      return normalizedDb
    }

    val migrated = persistToDatabase(legacySnapshot)
    if (migrated) {
      deleteLegacyFile()
    }
    return legacySnapshot
  }

  fun deleteLegacyFile() {
    val path = favoritesPath()
    if (fileSystem.exists(path)) {
      runCatching { fileSystem.delete(path) }
    }
  }

  private fun favoritesPath() = "${storageDirectoryProvider.rootPath}/favorites.json".toPath()
}
