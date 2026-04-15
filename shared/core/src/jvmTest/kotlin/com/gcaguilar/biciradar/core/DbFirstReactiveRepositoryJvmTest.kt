package com.gcaguilar.biciradar.core

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.createJdbcDriver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.FileSystem
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DbFirstReactiveRepositoryJvmTest {
  @Test
  fun `saved place alerts bootstrap exposes existing DB rules immediately`() =
    runTest {
      val database = createTestDatabase()
      database.biciradarQueries.upsertSavedPlaceAlertRule(
        id = "home:station-db-home:zaragoza",
        targetKind = SavedPlaceKind.Home.name,
        targetStationId = "station-db-home",
        targetCityId = "zaragoza",
        targetStationName = "Casa",
        targetCategoryId = null,
        targetCategoryLabel = null,
        conditionKind = "BikesAtLeast",
        conditionThreshold = 1L,
        isEnabled = 1L,
        lastTriggeredEpoch = null,
        lastConditionMatched = 0L,
        lastObservedValue = null,
      )

      val repository =
        SavedPlaceAlertsRepositoryImpl(
          fileSystem = FileSystem.SYSTEM,
          json = Json,
          storageDirectoryProvider =
            object : StorageDirectoryProvider {
              override val rootPath: String = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString()
            },
          scope = backgroundScope,
          database = database,
        )

      repository.bootstrap()

      assertEquals(
        listOf(
          SavedPlaceAlertRule(
            id = "home:station-db-home:zaragoza",
            target =
              SavedPlaceAlertTarget.Home(
                stationId = "station-db-home",
                cityId = "zaragoza",
                stationName = "Casa",
              ),
            condition = SavedPlaceAlertCondition.BikesAtLeast(1),
            isEnabled = true,
          ),
        ),
        repository.currentRules(),
      )
    }

  @Test
  fun `favorites DB writes are observed through state flow`() =
    runTest {
      val database = createTestDatabase()
      val repository =
        FavoritesRepositoryImpl(
          fileSystem = FileSystem.SYSTEM,
          json = Json,
          storageDirectoryProvider =
            object : StorageDirectoryProvider {
              override val rootPath: String = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString()
            },
          watchSyncBridge =
            object : WatchSyncBridge {
              override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit

              override suspend fun latestFavorites(): FavoritesSyncSnapshot? = null
            },
          scope = backgroundScope,
          database = database,
        )

      repository.favoriteIds.test {
        assertEquals(emptySet(), awaitItem())
        repository.bootstrap()
        repository.toggle("station-db-1")
        assertEquals(setOf("station-db-1"), awaitMatch { it == setOf("station-db-1") })
        cancelAndIgnoreRemainingEvents()
      }

      repository.homeStationId.test {
        assertEquals(null, awaitItem())
        repository.setHomeStationId("station-db-home")
        assertEquals("station-db-home", awaitMatch { it == "station-db-home" })
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `stations availability updates write-through DB and emit via state flow`() =
    runTest {
      val database = createTestDatabase()
      val settingsRepository =
        object : SettingsRepository {
          override val searchRadiusMeters = MutableStateFlow(DEFAULT_SEARCH_RADIUS_METERS)
          override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
          override val lastSeenChangelogVersion = MutableStateFlow(0)
          override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
          override val themePreference = MutableStateFlow(ThemePreference.System)
          override val selectedCity = MutableStateFlow(City.ZARAGOZA)
          override val hasCompletedOnboarding = MutableStateFlow(true)
          override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
          override val engagementSnapshot = MutableStateFlow(EngagementSnapshot())

          override suspend fun bootstrap() = Unit

          override fun currentSearchRadiusMeters(): Int = searchRadiusMeters.value

          override fun currentPreferredMapApp(): PreferredMapApp = preferredMapApp.value

          override fun currentSelectedCity(): City = selectedCity.value

          override fun currentLastSeenChangelogAppVersion(): String? = null

          override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) = Unit

          override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit

          override suspend fun setLastSeenChangelogVersion(version: Int) = Unit

          override suspend fun setLastSeenChangelogAppVersion(version: String?) = Unit

          override suspend fun setThemePreference(preference: ThemePreference) = Unit

          override suspend fun setSelectedCity(city: City) = Unit

          override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit

          override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit

          override suspend fun updateOnboardingChecklist(
            transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot,
          ) = Unit

          override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit

          override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
        }

      val remoteDataSource =
        object : StationsRemoteDataSource {
          override suspend fun fetchStations(origin: GeoPoint): List<Station> =
            listOf(
              Station(
                id = "st-1",
                name = "Plaza Espana",
                address = "Centro",
                location = GeoPoint(41.65, -0.88),
                bikesAvailable = 3,
                slotsFree = 7,
                distanceMeters = 120,
              ),
            )

          override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> =
            mapOf(
              "st-1" to StationAvailability(bikesAvailable = 9, slotsFree = 1),
            )
        }

      val cacheManager =
        StationsCacheManagerImpl(
          database = database,
          cacheStore = StationCacheStore(database),
        )

      val stationsRepository =
        StationsRepositoryImpl(
          remoteDataSource = remoteDataSource,
          cacheManager = cacheManager,
          locationProvider =
            object : LocationProvider {
              override suspend fun currentLocation(): GeoPoint? = null
            },
          settingsRepository = settingsRepository,
          scope = backgroundScope,
        )

      stationsRepository.state.test {
        awaitItem() // initial
        stationsRepository.forceRefresh()
        val refreshed = awaitMatch { state -> state.stations.firstOrNull()?.bikesAvailable == 3 }
        assertEquals(3, refreshed.stations.first().bikesAvailable)

        stationsRepository.refreshAvailability(listOf("st-1"))
        val availabilityRefreshed = awaitMatch { state -> state.stations.firstOrNull()?.bikesAvailable == 9 }
        assertEquals(9, availabilityRefreshed.stations.first().bikesAvailable)
        assertEquals(1, availabilityRefreshed.stations.first().slotsFree)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `surface snapshot DB writes update current bundle immediately`() =
    runTest {
      val database = createTestDatabase()
      val repository =
        SurfaceSnapshotRepositoryImpl(
          fileSystem = FileSystem.SYSTEM,
          json = Json,
          localNotifier =
            object : LocalNotifier {
              override suspend fun hasPermission(): Boolean = true

              override suspend fun requestPermission(): Boolean = true

              override suspend fun notify(
                title: String,
                body: String,
              ) = Unit
            },
          storageDirectoryProvider =
            object : StorageDirectoryProvider {
              override val rootPath: String = FileSystem.SYSTEM_TEMPORARY_DIRECTORY.toString()
            },
          settingsRepository =
            object : SettingsRepository {
              override val searchRadiusMeters = MutableStateFlow(DEFAULT_SEARCH_RADIUS_METERS)
              override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
              override val lastSeenChangelogVersion = MutableStateFlow(0)
              override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
              override val themePreference = MutableStateFlow(ThemePreference.System)
              override val selectedCity = MutableStateFlow(City.ZARAGOZA)
              override val hasCompletedOnboarding = MutableStateFlow(true)
              override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
              override val engagementSnapshot = MutableStateFlow(EngagementSnapshot())

              override suspend fun bootstrap() = Unit

              override fun currentSearchRadiusMeters(): Int = searchRadiusMeters.value

              override fun currentPreferredMapApp(): PreferredMapApp = preferredMapApp.value

              override fun currentSelectedCity(): City = selectedCity.value

              override fun currentLastSeenChangelogAppVersion(): String? = null

              override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) = Unit

              override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit

              override suspend fun setLastSeenChangelogVersion(version: Int) = Unit

              override suspend fun setLastSeenChangelogAppVersion(version: String?) = Unit

              override suspend fun setThemePreference(preference: ThemePreference) = Unit

              override suspend fun setSelectedCity(city: City) = Unit

              override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit

              override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit

              override suspend fun updateOnboardingChecklist(
                transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot,
              ) = Unit

              override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit

              override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
            },
          favoritesRepository =
            object : FavoritesRepository {
              override val favoriteIds = MutableStateFlow(setOf("station-db-1"))
              override val homeStationId = MutableStateFlow<String?>(null)
              override val workStationId = MutableStateFlow<String?>(null)

              override suspend fun bootstrap() = Unit

              override suspend fun toggle(stationId: String) = Unit

              override suspend fun setHomeStationId(stationId: String?) = Unit

              override suspend fun setWorkStationId(stationId: String?) = Unit

              override suspend fun clearAll() = Unit

              override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value

              override fun currentHomeStationId(): String? = homeStationId.value

              override fun currentWorkStationId(): String? = workStationId.value
            },
          stationsRepository =
            object : StationsRepository {
              override val state =
                MutableStateFlow(
                  StationsState(
                    stations =
                      listOf(
                        Station(
                          id = "station-db-1",
                          name = "Station DB 1",
                          address = "Centro",
                          location = GeoPoint(41.65, -0.88),
                          bikesAvailable = 5,
                          slotsFree = 6,
                          distanceMeters = 100,
                        ),
                      ),
                      userLocation = GeoPoint(41.65, -0.88),
                      lastUpdatedEpoch = 10_000L,
                    ),
                )

              override suspend fun loadIfNeeded() = Unit

              override suspend fun forceRefresh() = Unit

              override suspend fun refreshAvailability(stationIds: List<String>) = Unit

              override fun stationById(stationId: String): Station? =
                state.value.stations.firstOrNull { it.id == stationId }
            },
          scope = backgroundScope,
          database = database,
        )

      repository.bootstrap()

      val collectJob =
        backgroundScope.launch {
          repository.bundle.collect { }
        }
      repository.refreshSnapshot()

      val bundle = repository.currentBundle()
      assertNotNull(bundle)
      assertEquals("station-db-1", bundle.favoriteStation?.id)

      collectJob.cancel()
    }
}

private suspend fun <T> ReceiveTurbine<T>.awaitMatch(predicate: (T) -> Boolean): T {
  repeat(30) {
    val item = awaitItem()
    if (predicate(item)) return item
  }
  val fallback = awaitItem()
  assertTrue(predicate(fallback))
  return fallback
}

private fun createTestDatabase(): BiciRadarDatabase {
  val dbPath = "${File(System.getProperty("java.io.tmpdir")).absolutePath}/biciradar-jvmtest-${Random.nextInt()}.db"
  val driver = createJdbcDriver(dbPath)
  return BiciRadarDatabase(driver)
}
