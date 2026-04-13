package com.gcaguilar.biciradar.core

import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

internal class SettingsLegacyMigrator(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val persistToDatabase: (SettingsSnapshot) -> Boolean,
) {
  fun readLegacySnapshot(): SettingsSnapshot? {
    val path = settingsPath()
    if (!fileSystem.exists(path)) return null
    return runCatching {
      json.decodeFromString<SettingsSnapshot>(fileSystem.read(path) { readUtf8() })
    }.getOrNull()
  }

  fun migrateIfNeeded(dbSnapshot: SettingsSnapshot?): SettingsSnapshot? {
    if (dbSnapshot != null) {
      deleteLegacyFile()
      return dbSnapshot
    }

    val legacySnapshot = readLegacySnapshot() ?: return null
    val migrated = persistToDatabase(legacySnapshot)
    if (migrated) {
      deleteLegacyFile()
    }
    return legacySnapshot
  }

  fun deleteLegacyFile() {
    val path = settingsPath()
    if (fileSystem.exists(path)) {
      runCatching { fileSystem.delete(path) }
    }
  }

  private fun settingsPath() = "${storageDirectoryProvider.rootPath}/settings.json".toPath()
}
