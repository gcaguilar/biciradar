package com.gcaguilar.biciradar.core

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
  val preferredMapApp: StateFlow<PreferredMapApp>
  val lastSeenChangelogVersion: StateFlow<Int>
  val themePreference: StateFlow<ThemePreference>
  val selectedCity: StateFlow<City>
  val hasCompletedOnboarding: StateFlow<Boolean>
  suspend fun bootstrap()
  fun currentSearchRadiusMeters(): Int
  fun currentPreferredMapApp(): PreferredMapApp
  fun currentSelectedCity(): City
  suspend fun setSearchRadiusMeters(searchRadiusMeters: Int)
  suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp)
  suspend fun setLastSeenChangelogVersion(version: Int)
  suspend fun setThemePreference(preference: ThemePreference)
  suspend fun setSelectedCity(city: City)
  suspend fun setHasCompletedOnboarding(completed: Boolean)
}

@Inject
class SettingsRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
) : SettingsRepository {
  private val mutableSearchRadiusMeters = MutableStateFlow(DEFAULT_SEARCH_RADIUS_METERS)
  private val mutablePreferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  private val mutableLastSeenChangelogVersion = MutableStateFlow(0)
  private val mutableThemePreference = MutableStateFlow(ThemePreference.System)
  private val mutableSelectedCity = MutableStateFlow(City.defaultCity())
  private val mutableHasCompletedOnboarding = MutableStateFlow(false)
  private var bootstrapped = false

  override val searchRadiusMeters: StateFlow<Int> = mutableSearchRadiusMeters.asStateFlow()
  override val preferredMapApp: StateFlow<PreferredMapApp> = mutablePreferredMapApp.asStateFlow()
  override val lastSeenChangelogVersion: StateFlow<Int> = mutableLastSeenChangelogVersion.asStateFlow()
  override val themePreference: StateFlow<ThemePreference> = mutableThemePreference.asStateFlow()
  override val selectedCity: StateFlow<City> = mutableSelectedCity.asStateFlow()
  override val hasCompletedOnboarding: StateFlow<Boolean> = mutableHasCompletedOnboarding.asStateFlow()

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
    mutablePreferredMapApp.value = snapshot?.preferredMapApp ?: PreferredMapApp.AppleMaps
    mutableLastSeenChangelogVersion.value = snapshot?.lastSeenChangelogVersion ?: 0
    mutableThemePreference.value = snapshot?.themePreference ?: ThemePreference.System
    mutableSelectedCity.value = snapshot?.selectedCityId?.let { City.fromId(it) } ?: City.defaultCity()
    mutableHasCompletedOnboarding.value = snapshot?.hasCompletedOnboarding ?: false
    bootstrapped = true
  }

  override fun currentSearchRadiusMeters(): Int = mutableSearchRadiusMeters.value

  override fun currentPreferredMapApp(): PreferredMapApp = mutablePreferredMapApp.value

  override fun currentSelectedCity(): City = mutableSelectedCity.value

  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) {
    if (!bootstrapped) bootstrap()
    val normalizedRadius = normalizeSearchRadiusMeters(searchRadiusMeters)
    mutableSearchRadiusMeters.value = normalizedRadius
    persist(
      searchRadiusMeters = normalizedRadius,
      preferredMapApp = mutablePreferredMapApp.value,
      lastSeenChangelogVersion = mutableLastSeenChangelogVersion.value,
      themePreference = mutableThemePreference.value,
      selectedCityId = mutableSelectedCity.value.id,
      hasCompletedOnboarding = mutableHasCompletedOnboarding.value,
    )
  }

  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) {
    if (!bootstrapped) bootstrap()
    mutablePreferredMapApp.value = preferredMapApp
    persist(
      searchRadiusMeters = mutableSearchRadiusMeters.value,
      preferredMapApp = preferredMapApp,
      lastSeenChangelogVersion = mutableLastSeenChangelogVersion.value,
      themePreference = mutableThemePreference.value,
      selectedCityId = mutableSelectedCity.value.id,
      hasCompletedOnboarding = mutableHasCompletedOnboarding.value,
    )
  }

  override suspend fun setLastSeenChangelogVersion(version: Int) {
    if (!bootstrapped) bootstrap()
    mutableLastSeenChangelogVersion.value = version
    persist(
      searchRadiusMeters = mutableSearchRadiusMeters.value,
      preferredMapApp = mutablePreferredMapApp.value,
      lastSeenChangelogVersion = version,
      themePreference = mutableThemePreference.value,
      selectedCityId = mutableSelectedCity.value.id,
      hasCompletedOnboarding = mutableHasCompletedOnboarding.value,
    )
  }

  override suspend fun setThemePreference(preference: ThemePreference) {
    if (!bootstrapped) bootstrap()
    mutableThemePreference.value = preference
    persist(
      searchRadiusMeters = mutableSearchRadiusMeters.value,
      preferredMapApp = mutablePreferredMapApp.value,
      lastSeenChangelogVersion = mutableLastSeenChangelogVersion.value,
      themePreference = preference,
      selectedCityId = mutableSelectedCity.value.id,
      hasCompletedOnboarding = mutableHasCompletedOnboarding.value,
    )
  }

  override suspend fun setSelectedCity(city: City) {
    if (!bootstrapped) bootstrap()
    mutableSelectedCity.value = city
    persist(
      searchRadiusMeters = mutableSearchRadiusMeters.value,
      preferredMapApp = mutablePreferredMapApp.value,
      lastSeenChangelogVersion = mutableLastSeenChangelogVersion.value,
      themePreference = mutableThemePreference.value,
      selectedCityId = city.id,
      hasCompletedOnboarding = mutableHasCompletedOnboarding.value,
    )
  }

  override suspend fun setHasCompletedOnboarding(completed: Boolean) {
    if (!bootstrapped) bootstrap()
    mutableHasCompletedOnboarding.value = completed
    persist(
      searchRadiusMeters = mutableSearchRadiusMeters.value,
      preferredMapApp = mutablePreferredMapApp.value,
      lastSeenChangelogVersion = mutableLastSeenChangelogVersion.value,
      themePreference = mutableThemePreference.value,
      selectedCityId = mutableSelectedCity.value.id,
      hasCompletedOnboarding = completed,
    )
  }

  private fun settingsPath() = "${storageDirectoryProvider.rootPath}/settings.json".toPath()

  private suspend fun persist(
    searchRadiusMeters: Int,
    preferredMapApp: PreferredMapApp,
    lastSeenChangelogVersion: Int,
    themePreference: ThemePreference,
    selectedCityId: String,
    hasCompletedOnboarding: Boolean,
  ) {
    val path = settingsPath()
    val dir = path.parent ?: return
    fileSystem.createDirectories(dir)
    fileSystem.write(path) {
      writeUtf8(
        json.encodeToString(
          SettingsSnapshot(
            searchRadiusMeters = searchRadiusMeters,
            preferredMapApp = preferredMapApp,
            lastSeenChangelogVersion = lastSeenChangelogVersion,
            themePreference = themePreference,
            selectedCityId = selectedCityId,
            hasCompletedOnboarding = hasCompletedOnboarding,
          ),
        ),
      )
    }
  }
}

@Serializable
enum class PreferredMapApp {
  AppleMaps,
  GoogleMaps,
}

@Serializable
enum class ThemePreference {
  System,
  Light,
  Dark,
}

@Serializable
internal data class SettingsSnapshot(
  val searchRadiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
  val preferredMapApp: PreferredMapApp = PreferredMapApp.AppleMaps,
  val lastSeenChangelogVersion: Int = 0,
  val themePreference: ThemePreference = ThemePreference.System,
  val selectedCityId: String = City.defaultCity().id,
  val hasCompletedOnboarding: Boolean = false,
)
