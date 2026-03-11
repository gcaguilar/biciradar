package com.gcaguilar.bizizaragoza.core

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

interface SettingsRepository {
  val searchRadiusMeters: StateFlow<Int>
  suspend fun bootstrap()
  fun currentSearchRadiusMeters(): Int
  suspend fun setSearchRadiusMeters(searchRadiusMeters: Int)
}

@Inject
class SettingsRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
) : SettingsRepository {
  private val mutableSearchRadiusMeters = MutableStateFlow(DEFAULT_SEARCH_RADIUS_METERS)
  private var bootstrapped = false

  override val searchRadiusMeters: StateFlow<Int> = mutableSearchRadiusMeters.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    val path = settingsPath()
    val snapshot = if (fileSystem.exists(path)) {
      runCatching {
        json.decodeFromString<SettingsSnapshot>(fileSystem.read(path) { readUtf8() })
      }.getOrNull()
    } else {
      null
    }
    mutableSearchRadiusMeters.value = normalizeSearchRadiusMeters(
      snapshot?.searchRadiusMeters ?: DEFAULT_SEARCH_RADIUS_METERS,
    )
    bootstrapped = true
  }

  override fun currentSearchRadiusMeters(): Int = mutableSearchRadiusMeters.value

  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) {
    if (!bootstrapped) bootstrap()
    val normalizedRadius = normalizeSearchRadiusMeters(searchRadiusMeters)
    mutableSearchRadiusMeters.value = normalizedRadius
    persist(normalizedRadius)
  }

  private fun settingsPath() = "${storageDirectoryProvider.rootPath}/settings.json".toPath()

  private suspend fun persist(searchRadiusMeters: Int) {
    val path = settingsPath()
    fileSystem.createDirectories(path.parent!!)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(SettingsSnapshot(searchRadiusMeters)))
    }
  }
}

@Serializable
internal data class SettingsSnapshot(
  val searchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
)
