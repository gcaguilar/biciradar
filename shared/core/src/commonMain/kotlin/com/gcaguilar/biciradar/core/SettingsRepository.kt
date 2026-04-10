package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.normalizeLegacyOnboardingForMigration
import com.gcaguilar.biciradar.core.local.settingsSnapshotFromDbRow
import com.gcaguilar.biciradar.core.local.upsertSettingsFromSnapshot
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
  suspend fun persistedMapFilterNames(): Set<String> = emptySet()
  suspend fun setPersistedMapFilterNames(names: Set<String>) {}
  suspend fun preferredMonitoringDurationSeconds(): Int? = null
  suspend fun setPreferredMonitoringDurationSeconds(durationSeconds: Int?) {}
  /**
   * Returns whether city selection is already confirmed.
   *
   * Default implementation falls back to in-memory state for tests or non-persistent
   * implementations. Persistent implementations should override and read from storage.
   */
  suspend fun isCityConfirmedPersisted(): Boolean = onboardingChecklist.value.cityConfirmed
  /**
   * First launch with string-based changelog: set [lastSeenChangelogAppVersion] to [appVersion] if still null
   * (new installs + legacy int migration) so no popup is shown until a future update with a catalog entry.
   */
  suspend fun ensureChangelogStringBaseline(appVersion: String)
}

/**
 * Settings are read from storage (database or file) on every mutation and after external DB changes via
 * [BiciRadarDatabase] queries. In-memory state mirrors the latest decoded snapshot only.
 *
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 * El parámetro [database] es nullable - Metro inyecta null si no hay binding disponible.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class SettingsRepositoryImpl(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
  private val scope: CoroutineScope,
  private val database: BiciRadarDatabase?,
) : SettingsRepository {
  private val persistMutex = Mutex()
  private val readModel = MutableStateFlow(normalizeLegacyOnboardingForMigration(SettingsSnapshot()))
  private var bootstrapped = false

  init {
    val db = database
    if (db != null) {
      scope.launch {
        combine(
          db.biciradarQueries.getAppSettings().asFlow().mapToOneOrNull(Dispatchers.Default),
          db.biciradarQueries.getAllSettingsMapFilterNames().asFlow().mapToList(Dispatchers.Default),
        ) { row, filterNames ->
          if (row == null) return@combine null
          settingsSnapshotFromDbRow(row, filterNames.toSet())
        }.collect { snapshot ->
          if (snapshot != null) applyPersistedSnapshot(snapshot)
        }
      }
    }
  }

  override val searchRadiusMeters: StateFlow<Int> = readModel
    .map { normalizeSearchRadiusMeters(it.searchRadiusMeters) }
    .stateIn(
      scope,
      SharingStarted.Eagerly,
      normalizeSearchRadiusMeters(DEFAULT_SEARCH_RADIUS_METERS),
    )

  override val preferredMapApp: StateFlow<PreferredMapApp> = readModel
    .map { it.preferredMapApp }
    .stateIn(scope, SharingStarted.Eagerly, PreferredMapApp.AppleMaps)

  override val lastSeenChangelogVersion: StateFlow<Int> = readModel
    .map { it.lastSeenChangelogVersion }
    .stateIn(scope, SharingStarted.Eagerly, 0)

  override val lastSeenChangelogAppVersion: StateFlow<String?> = readModel
    .map { it.lastSeenChangelogAppVersion }
    .stateIn(scope, SharingStarted.Eagerly, null)

  override val themePreference: StateFlow<ThemePreference> = readModel
    .map { it.themePreference }
    .stateIn(scope, SharingStarted.Eagerly, ThemePreference.System)

  override val selectedCity: StateFlow<City> = readModel
    .map { snapshot -> snapshot.selectedCityId.let { City.fromId(it) } ?: City.defaultCity() }
    .stateIn(scope, SharingStarted.Eagerly, City.defaultCity())

  override val hasCompletedOnboarding: StateFlow<Boolean> = readModel
    .map { it.hasCompletedOnboarding }
    .stateIn(scope, SharingStarted.Eagerly, false)

  override val onboardingChecklist: StateFlow<OnboardingChecklistSnapshot> = readModel
    .map { it.onboardingChecklist }
    .stateIn(scope, SharingStarted.Eagerly, OnboardingChecklistSnapshot())

  override val engagementSnapshot: StateFlow<EngagementSnapshot> = readModel
    .map { it.engagementSnapshot }
    .stateIn(scope, SharingStarted.Eagerly, EngagementSnapshot())

  override suspend fun bootstrap() {
    if (bootstrapped) return
    applyPersistedSnapshot(readPersistedSnapshot())
    bootstrapped = true
  }

  private fun applyPersistedSnapshot(snapshot: SettingsSnapshot?) {
    readModel.value = normalizeLegacyOnboardingForMigration(snapshot ?: SettingsSnapshot())
  }

  override fun currentSearchRadiusMeters(): Int =
    normalizeSearchRadiusMeters(readModel.value.searchRadiusMeters)

  override fun currentPreferredMapApp(): PreferredMapApp = readModel.value.preferredMapApp

  override fun currentSelectedCity(): City =
    readModel.value.selectedCityId.let { City.fromId(it) } ?: City.defaultCity()

  override fun currentLastSeenChangelogAppVersion(): String? = readModel.value.lastSeenChangelogAppVersion

  override suspend fun ensureChangelogStringBaseline(appVersion: String) {
    if (!bootstrapped) bootstrap()
    persistMutex.withLock {
      val current = readForMutation()
      if (current.lastSeenChangelogAppVersion != null) return@withLock
      val next = normalizeLegacyOnboardingForMigration(current.copy(lastSeenChangelogAppVersion = appVersion))
      writeSnapshot(next)
      applyPersistedSnapshot(next)
    }
  }

  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) {
    mutatePersist { snap ->
      snap.copy(searchRadiusMeters = normalizeSearchRadiusMeters(searchRadiusMeters))
    }
  }

  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) {
    mutatePersist { it.copy(preferredMapApp = preferredMapApp) }
  }

  override suspend fun setLastSeenChangelogVersion(version: Int) {
    mutatePersist { it.copy(lastSeenChangelogVersion = version) }
  }

  override suspend fun setLastSeenChangelogAppVersion(version: String?) {
    mutatePersist { it.copy(lastSeenChangelogAppVersion = version) }
  }

  override suspend fun setThemePreference(preference: ThemePreference) {
    mutatePersist { it.copy(themePreference = preference) }
  }

  override suspend fun setSelectedCity(city: City) {
    mutatePersist { it.copy(selectedCityId = city.id) }
  }

  override suspend fun setHasCompletedOnboarding(completed: Boolean) {
    mutatePersist { snap ->
      val checklist = if (!completed) snap.onboardingChecklist.clearCompleted() else snap.onboardingChecklist
      snap.copy(
        hasCompletedOnboarding = completed,
        onboardingChecklist = checklist,
      )
    }
  }

  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) {
    mutatePersist {
      it.copy(
        onboardingChecklist = snapshot,
        hasCompletedOnboarding = snapshot.isCompleted(),
      )
    }
  }

  override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    mutatePersist { snap ->
      val updated = transform(snap.onboardingChecklist)
      snap.copy(
        onboardingChecklist = updated,
        hasCompletedOnboarding = updated.isCompleted(),
      )
    }
  }

  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) {
    mutatePersist { it.copy(engagementSnapshot = snapshot) }
  }

  override suspend fun persistedMapFilterNames(): Set<String> {
    if (!bootstrapped) bootstrap()
    return readModel.value.mapFilterNames
  }

  override suspend fun setPersistedMapFilterNames(names: Set<String>) {
    mutatePersist { it.copy(mapFilterNames = names) }
  }

  override suspend fun preferredMonitoringDurationSeconds(): Int? {
    if (!bootstrapped) bootstrap()
    return readModel.value.preferredMonitoringDurationSeconds
  }

  override suspend fun setPreferredMonitoringDurationSeconds(durationSeconds: Int?) {
    mutatePersist { it.copy(preferredMonitoringDurationSeconds = durationSeconds) }
  }

  override suspend fun isCityConfirmedPersisted(): Boolean {
    return readPersistedSnapshot()?.onboardingChecklist?.cityConfirmed ?: false
  }

  private fun settingsPath() = "${storageDirectoryProvider.rootPath}/settings.json".toPath()

  private suspend fun mutatePersist(transform: (SettingsSnapshot) -> SettingsSnapshot) {
    if (!bootstrapped) bootstrap()
    persistMutex.withLock {
      val current = readForMutation()
      val next = normalizeLegacyOnboardingForMigration(transform(current))
      writeSnapshot(next)
      applyPersistedSnapshot(next)
    }
  }

  private fun readForMutation(): SettingsSnapshot {
    readFromDatabase()?.let { return normalizeLegacyOnboardingForMigration(it) }
    readFromFile()?.let { legacy ->
      if (database != null) {
        val migrated = persistToDatabase(legacy)
        if (migrated) deleteLegacyFile()
      }
      return normalizeLegacyOnboardingForMigration(legacy)
    }
    return readModel.value
  }

  private fun writeSnapshot(snapshot: SettingsSnapshot) {
    if (database != null) {
      val persisted = persistToDatabase(snapshot)
      if (persisted) deleteLegacyFile()
    } else {
      persistToFile(snapshot)
    }
  }

  private fun readPersistedSnapshot(): SettingsSnapshot? {
    if (database == null) return readFromFile()
    val dbSnapshot = readFromDatabase()
    if (dbSnapshot != null) {
      deleteLegacyFile()
      return dbSnapshot
    }

    val legacySnapshot = readFromFile() ?: return null
    val migrated = persistToDatabase(legacySnapshot)
    if (migrated) deleteLegacyFile()
    return legacySnapshot
  }

  private fun readFromDatabase(): SettingsSnapshot? {
    val db = database ?: return null
    return runCatching {
      val row = db.biciradarQueries.getAppSettings().executeAsOneOrNull() ?: return@runCatching null
      val filters = db.biciradarQueries.getAllSettingsMapFilterNames().executeAsList().toSet()
      settingsSnapshotFromDbRow(row, filters)
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
      upsertSettingsFromSnapshot(db, snapshot)
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
  val mapFilterNames: Set<String> = emptySet(),
  val preferredMonitoringDurationSeconds: Int? = null,
)
