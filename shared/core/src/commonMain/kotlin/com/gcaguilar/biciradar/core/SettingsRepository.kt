package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
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
  /** Legacy integer changelog marker; kept for migration only. */
  val lastSeenChangelogVersion: StateFlow<Int>
  val lastSeenChangelogAppVersion: StateFlow<String?>
  val themePreference: StateFlow<ThemePreference>
  val selectedCity: StateFlow<City>
  val hasCompletedOnboarding: StateFlow<Boolean>
  val onboardingChecklist: StateFlow<OnboardingChecklistSnapshot>
  val engagementSnapshot: StateFlow<EngagementSnapshot>
  suspend fun bootstrap()
  fun currentSearchRadiusMeters(): Int
  fun currentPreferredMapApp(): PreferredMapApp
  fun currentSelectedCity(): City
  fun currentLastSeenChangelogAppVersion(): String?
  suspend fun setSearchRadiusMeters(searchRadiusMeters: Int)
  suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp)
  suspend fun setLastSeenChangelogVersion(version: Int)
  suspend fun setLastSeenChangelogAppVersion(version: String?)
  suspend fun setThemePreference(preference: ThemePreference)
  suspend fun setSelectedCity(city: City)
  suspend fun setHasCompletedOnboarding(completed: Boolean)
  suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot)
  suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot)
  suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot)
  /**
   * First launch with string-based changelog: set [lastSeenChangelogAppVersion] to [appVersion] if still null
   * (new installs + legacy int migration) so no popup is shown until a future update with a catalog entry.
   */
  suspend fun ensureChangelogStringBaseline(appVersion: String)
}

@Inject
class SettingsRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val database: BiciRadarDatabase? = null,
) : SettingsRepository {
  private val mutableSearchRadiusMeters = MutableStateFlow(DEFAULT_SEARCH_RADIUS_METERS)
  private val mutablePreferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  private val mutableLastSeenChangelogVersion = MutableStateFlow(0)
  private val mutableLastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  private val mutableThemePreference = MutableStateFlow(ThemePreference.System)
  private val mutableSelectedCity = MutableStateFlow(City.defaultCity())
  private val mutableHasCompletedOnboarding = MutableStateFlow(false)
  private val mutableOnboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot())
  private val mutableEngagementSnapshot = MutableStateFlow(EngagementSnapshot())
  private var bootstrapped = false

  override val searchRadiusMeters: StateFlow<Int> = mutableSearchRadiusMeters.asStateFlow()
  override val preferredMapApp: StateFlow<PreferredMapApp> = mutablePreferredMapApp.asStateFlow()
  override val lastSeenChangelogVersion: StateFlow<Int> = mutableLastSeenChangelogVersion.asStateFlow()
  override val lastSeenChangelogAppVersion: StateFlow<String?> = mutableLastSeenChangelogAppVersion.asStateFlow()
  override val themePreference: StateFlow<ThemePreference> = mutableThemePreference.asStateFlow()
  override val selectedCity: StateFlow<City> = mutableSelectedCity.asStateFlow()
  override val hasCompletedOnboarding: StateFlow<Boolean> = mutableHasCompletedOnboarding.asStateFlow()
  override val onboardingChecklist: StateFlow<OnboardingChecklistSnapshot> = mutableOnboardingChecklist.asStateFlow()
  override val engagementSnapshot: StateFlow<EngagementSnapshot> = mutableEngagementSnapshot.asStateFlow()

  override suspend fun bootstrap() {
    if (bootstrapped) return
    val snapshot = readPersistedSnapshot()
    mutableSearchRadiusMeters.value = normalizeSearchRadiusMeters(
      snapshot?.searchRadiusMeters ?: DEFAULT_SEARCH_RADIUS_METERS,
    )
    mutablePreferredMapApp.value = snapshot?.preferredMapApp ?: PreferredMapApp.AppleMaps
    mutableLastSeenChangelogVersion.value = snapshot?.lastSeenChangelogVersion ?: 0
    mutableLastSeenChangelogAppVersion.value = snapshot?.lastSeenChangelogAppVersion
    mutableThemePreference.value = snapshot?.themePreference ?: ThemePreference.System
    mutableSelectedCity.value = snapshot?.selectedCityId?.let { City.fromId(it) } ?: City.defaultCity()
    mutableEngagementSnapshot.value = snapshot?.engagementSnapshot ?: EngagementSnapshot()

    var checklist = snapshot?.onboardingChecklist ?: OnboardingChecklistSnapshot()
    if (snapshot?.hasCompletedOnboarding == true && !checklist.isCompleted()) {
      checklist = OnboardingChecklistSnapshot(
        cityConfirmed = true,
        featureHighlightsSeen = true,
        locationDecisionMade = true,
        notificationsDecisionMade = true,
        firstStationSaved = true,
        savedPlacesConfigured = true,
        surfacesDiscovered = true,
        completedAtEpoch = checklist.completedAtEpoch ?: currentTimeMs(),
      )
    }
    mutableOnboardingChecklist.value = checklist
    mutableHasCompletedOnboarding.value = checklist.isCompleted() || snapshot?.hasCompletedOnboarding == true

    bootstrapped = true
  }

  override fun currentSearchRadiusMeters(): Int = mutableSearchRadiusMeters.value

  override fun currentPreferredMapApp(): PreferredMapApp = mutablePreferredMapApp.value

  override fun currentSelectedCity(): City = mutableSelectedCity.value

  override fun currentLastSeenChangelogAppVersion(): String? = mutableLastSeenChangelogAppVersion.value

  override suspend fun ensureChangelogStringBaseline(appVersion: String) {
    if (!bootstrapped) bootstrap()
    if (mutableLastSeenChangelogAppVersion.value != null) return
    mutableLastSeenChangelogAppVersion.value = appVersion
    persistCurrentSnapshot()
  }

  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) {
    if (!bootstrapped) bootstrap()
    mutableSearchRadiusMeters.value = normalizeSearchRadiusMeters(searchRadiusMeters)
    persistCurrentSnapshot()
  }

  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) {
    if (!bootstrapped) bootstrap()
    mutablePreferredMapApp.value = preferredMapApp
    persistCurrentSnapshot()
  }

  override suspend fun setLastSeenChangelogVersion(version: Int) {
    if (!bootstrapped) bootstrap()
    mutableLastSeenChangelogVersion.value = version
    persistCurrentSnapshot()
  }

  override suspend fun setLastSeenChangelogAppVersion(version: String?) {
    if (!bootstrapped) bootstrap()
    mutableLastSeenChangelogAppVersion.value = version
    persistCurrentSnapshot()
  }

  override suspend fun setThemePreference(preference: ThemePreference) {
    if (!bootstrapped) bootstrap()
    mutableThemePreference.value = preference
    persistCurrentSnapshot()
  }

  override suspend fun setSelectedCity(city: City) {
    if (!bootstrapped) bootstrap()
    mutableSelectedCity.value = city
    persistCurrentSnapshot()
  }

  override suspend fun setHasCompletedOnboarding(completed: Boolean) {
    if (!bootstrapped) bootstrap()
    mutableHasCompletedOnboarding.value = completed
    persistCurrentSnapshot()
  }

  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) {
    if (!bootstrapped) bootstrap()
    mutableOnboardingChecklist.value = snapshot
    if (snapshot.isCompleted()) {
      mutableHasCompletedOnboarding.value = true
    }
    persistCurrentSnapshot()
  }

  override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    if (!bootstrapped) bootstrap()
    val updated = transform(mutableOnboardingChecklist.value)
    mutableOnboardingChecklist.value = updated
    if (updated.isCompleted()) {
      mutableHasCompletedOnboarding.value = true
    }
    persistCurrentSnapshot()
  }

  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) {
    if (!bootstrapped) bootstrap()
    mutableEngagementSnapshot.value = snapshot
    persistCurrentSnapshot()
  }

  private fun settingsPath() = "${storageDirectoryProvider.rootPath}/settings.json".toPath()

  private suspend fun persistCurrentSnapshot() {
    val snapshot = SettingsSnapshot(
      searchRadiusMeters = mutableSearchRadiusMeters.value,
      preferredMapApp = mutablePreferredMapApp.value,
      lastSeenChangelogVersion = mutableLastSeenChangelogVersion.value,
      lastSeenChangelogAppVersion = mutableLastSeenChangelogAppVersion.value,
      themePreference = mutableThemePreference.value,
      selectedCityId = mutableSelectedCity.value.id,
      hasCompletedOnboarding = mutableHasCompletedOnboarding.value,
      onboardingChecklist = mutableOnboardingChecklist.value,
      engagementSnapshot = mutableEngagementSnapshot.value,
    )
    if (database != null) {
      val persisted = persistToDatabase(snapshot)
      if (persisted) {
        deleteLegacyFile()
      } else {
        persistToFile(snapshot)
      }
      return
    }
    persistToFile(snapshot)
  }

  private fun readPersistedSnapshot(): SettingsSnapshot? {
    if (database == null) return readFromFile()
    val dbSnapshot = readFromDatabase()
    val legacySnapshot = readFromFile()
    if (legacySnapshot == null) return dbSnapshot
    if (dbSnapshot != null) {
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

  private fun readFromDatabase(): SettingsSnapshot? {
    val db = database ?: return null
    return runCatching {
      val row = db.biciradarQueries.getSettingsSnapshot().executeAsOneOrNull() ?: return@runCatching null
      json.decodeFromString<SettingsSnapshot>(row)
    }.getOrNull()
  }

  private fun readFromFile(): SettingsSnapshot? {
    val path = settingsPath()
    if (!fileSystem.exists(path)) return null
    return runCatching {
      json.decodeFromString<SettingsSnapshot>(fileSystem.read(path) { readUtf8() })
    }.getOrNull()
  }

  private fun persistToDatabase(snapshot: SettingsSnapshot): Boolean {
    val db = database ?: return false
    return runCatching {
      db.biciradarQueries.upsertSettingsSnapshot(
        snapshotJson = json.encodeToString(snapshot),
      )
    }.isSuccess
  }

  private fun persistToFile(snapshot: SettingsSnapshot) {
    val path = settingsPath()
    val dir = path.parent ?: return
    fileSystem.createDirectories(dir)
    fileSystem.write(path) {
      writeUtf8(json.encodeToString(snapshot))
    }
  }

  private fun deleteLegacyFile() {
    val path = settingsPath()
    if (fileSystem.exists(path)) {
      runCatching { fileSystem.delete(path) }
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
  val lastSeenChangelogAppVersion: String? = null,
  val themePreference: ThemePreference = ThemePreference.System,
  val selectedCityId: String = City.defaultCity().id,
  val hasCompletedOnboarding: Boolean = false,
  val onboardingChecklist: OnboardingChecklistSnapshot = OnboardingChecklistSnapshot(),
  val engagementSnapshot: EngagementSnapshot = EngagementSnapshot(),
)
