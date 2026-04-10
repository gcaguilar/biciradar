package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.crypto.SecureKeyStore


import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import okio.Path.Companion.toPath
import okio.FileSystem

class CoreRepositoryTest {
  private fun testCoroutineScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

  @Test
  fun `distanceBetween returns stable Zaragoza distance ordering`() {
    val origin = GeoPoint(41.6488, -0.8891)
    val near = GeoPoint(41.6492, -0.8888)
    val far = GeoPoint(41.6592, -0.9001)

    assertTrue(distanceBetween(origin, near) < distanceBetween(origin, far))
  }

  @Test
  fun `favorites repository persists ids to disk`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-test-${Random.nextInt()}"
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = object : WatchSyncBridge {
        override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit

        override suspend fun latestFavorites(): FavoritesSyncSnapshot? = null
      },
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    repository.toggle("station-1")

    assertEquals(setOf("station-1"), repository.favoriteIds.value)
    assertTrue(FileSystem.SYSTEM.exists("$temporaryRoot/favorites.json".toPath()))
  }

  @Test
  fun `settings repository persists search radius`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-settings-${Random.nextInt()}"
    val repository = SettingsRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    repository.setSearchRadiusMeters(750)

    assertEquals(750, repository.currentSearchRadiusMeters())
    assertTrue(FileSystem.SYSTEM.exists("$temporaryRoot/settings.json".toPath()))
  }

  @Test
  fun `settings repository persists preferred map app`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-mapapp-${Random.nextInt()}"
    val repository = SettingsRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    repository.setPreferredMapApp(PreferredMapApp.GoogleMaps)

    assertEquals(PreferredMapApp.GoogleMaps, repository.currentPreferredMapApp())
    assertTrue(FileSystem.SYSTEM.exists("$temporaryRoot/settings.json".toPath()))
  }

  @Test
  fun `favorites repository merges local and wearable ids on bootstrap`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-sync-${Random.nextInt()}"
    val fileSystem = FileSystem.SYSTEM
    val rootPath = "$temporaryRoot/favorites.json".toPath()
    fileSystem.createDirectories(rootPath.parent!!)
    fileSystem.write(rootPath) {
      writeUtf8("""{"favoriteIds":["station-local"]}""")
    }

    val repository = FavoritesRepositoryImpl(
      fileSystem = fileSystem,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = object : WatchSyncBridge {
        override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit

        override suspend fun latestFavorites(): FavoritesSyncSnapshot = FavoritesSyncSnapshot(
          favoriteIds = setOf("station-watch"),
        )
      },
      scope = testCoroutineScope(),
    )

    repository.bootstrap()

    assertEquals(setOf("station-local", "station-watch"), repository.favoriteIds.value)
  }

  @Test
  fun `favorites repository pushes merged ids back to wearable on bootstrap`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-bootstrap-${Random.nextInt()}"
    val fileSystem = FileSystem.SYSTEM
    val rootPath = "$temporaryRoot/favorites.json".toPath()
    fileSystem.createDirectories(rootPath.parent!!)
    fileSystem.write(rootPath) {
      writeUtf8("""{"favoriteIds":["station-local"]}""")
    }
    val watchBridge = RecordingWatchSyncBridge(
      remoteSnapshot = FavoritesSyncSnapshot(favoriteIds = setOf("station-watch")),
    )
    val repository = FavoritesRepositoryImpl(
      fileSystem = fileSystem,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = watchBridge,
      scope = testCoroutineScope(),
    )

    repository.bootstrap()

    assertEquals(1, watchBridge.pushedSnapshots.size)
    assertEquals(
      setOf("station-local", "station-watch"),
      watchBridge.pushedSnapshots.single().favoriteIds,
    )
  }

  @Test
  fun `favorites repository syncFromPeer pulls newer wearable ids`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-sync-peer-${Random.nextInt()}"
    val watchBridge = object : WatchSyncBridge {
      val pushedSnapshots = mutableListOf<FavoritesSyncSnapshot>()
      var remoteSnapshot: FavoritesSyncSnapshot? = FavoritesSyncSnapshot(
        favoriteIds = setOf("station-watch"),
      )

      override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
        pushedSnapshots += snapshot
      }

      override suspend fun latestFavorites(): FavoritesSyncSnapshot? = remoteSnapshot
    }
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = watchBridge,
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    watchBridge.remoteSnapshot = FavoritesSyncSnapshot(
      favoriteIds = setOf("station-watch", "station-remote"),
      homeStationId = "station-home",
    )

    repository.syncFromPeer()

    assertEquals(setOf("station-watch", "station-remote"), repository.favoriteIds.value)
    assertEquals("station-home", repository.currentHomeStationId())
    assertEquals(2, watchBridge.pushedSnapshots.size)
    assertEquals(setOf("station-watch"), watchBridge.pushedSnapshots[0].favoriteIds)
    assertEquals(setOf("station-watch", "station-remote"), watchBridge.pushedSnapshots[1].favoriteIds)
    assertEquals("station-home", watchBridge.pushedSnapshots[1].homeStationId)
  }

  @Test
  fun `favorites repository pushes updated ids on toggle`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-toggle-${Random.nextInt()}"
    val watchBridge = RecordingWatchSyncBridge()
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = watchBridge,
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    repository.toggle("station-1")
    repository.toggle("station-2")
    repository.toggle("station-1")

    assertEquals(3, watchBridge.pushedSnapshots.size)
    assertEquals(setOf("station-1"), watchBridge.pushedSnapshots[0].favoriteIds)
    assertEquals(setOf("station-1", "station-2"), watchBridge.pushedSnapshots[1].favoriteIds)
    assertEquals(setOf("station-2"), watchBridge.pushedSnapshots[2].favoriteIds)
  }

  @Test
  fun `favorites bootstrap does not block when wearable sync is slow`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-slow-sync-${Random.nextInt()}"
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = object : WatchSyncBridge {
        override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit

        override suspend fun latestFavorites(): FavoritesSyncSnapshot? {
          delay(10_000)
          return FavoritesSyncSnapshot(favoriteIds = setOf("station-watch"))
        }
      },
      scope = testCoroutineScope(),
    )

    repository.bootstrap()

    assertTrue(repository.favoriteIds.value.isEmpty())
  }

  @Test
  fun `favorites repository persists home and work stations`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-pinned-${Random.nextInt()}"
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = RecordingWatchSyncBridge(),
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    repository.setHomeStationId("station-home")
    repository.setWorkStationId("station-work")

    assertEquals("station-home", repository.currentHomeStationId())
    assertEquals("station-work", repository.currentWorkStationId())
    assertTrue(FileSystem.SYSTEM.exists("$temporaryRoot/favorites.json".toPath()))
  }

  @Test
  fun `favorites repository deduplicates home and work stations`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-deduplicate-${Random.nextInt()}"
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = RecordingWatchSyncBridge(),
      scope = testCoroutineScope(),
    )

    repository.bootstrap()
    repository.setHomeStationId("station-1")
    repository.setWorkStationId("station-1")

    assertEquals("station-1", repository.currentHomeStationId())
    assertNull(repository.currentWorkStationId())
  }

  @Test
  fun `favorites repository merges remote home and work aliases`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-home-work-${Random.nextInt()}"
    val repository = FavoritesRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = RecordingWatchSyncBridge(
        remoteSnapshot = FavoritesSyncSnapshot(
          favoriteIds = setOf("station-watch"),
          homeStationId = "station-home",
          workStationId = "station-work",
        ),
      ),
      scope = testCoroutineScope(),
    )

    repository.bootstrap()

    assertEquals(setOf("station-watch"), repository.favoriteIds.value)
    assertEquals("station-home", repository.currentHomeStationId())
    assertEquals("station-work", repository.currentWorkStationId())
  }

  @Test
  fun `stations repository refresh uses current user location when available`() = runTest {
    var requestedOrigin: GeoPoint? = null
    val remoteDataSource = object : StationsRemoteDataSource {
      override suspend fun fetchStations(origin: GeoPoint): List<Station> {
        requestedOrigin = origin
        return listOf(
          Station(
            id = "station-1",
            name = "Plaza Espana",
            address = "Centro",
            location = origin,
            bikesAvailable = 10,
            slotsFree = 5,
            distanceMeters = 0,
          ),
        )
      }
      override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> = emptyMap()
    }
    val settingsRepository = object : SettingsRepository {
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
      override fun currentSearchRadiusMeters() = DEFAULT_SEARCH_RADIUS_METERS
      override fun currentPreferredMapApp() = PreferredMapApp.AppleMaps
      override fun currentSelectedCity() = City.ZARAGOZA
      override fun currentLastSeenChangelogAppVersion(): String? = null
      override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) = Unit
      override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit
      override suspend fun setLastSeenChangelogVersion(version: Int) = Unit
      override suspend fun setLastSeenChangelogAppVersion(version: String?) = Unit
      override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
      override suspend fun setThemePreference(preference: ThemePreference) = Unit
      override suspend fun setSelectedCity(city: City) = Unit
      override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit
      override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit
      override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) = Unit
      override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit
    }
    val repository = StationsRepositoryImpl(
      remoteDataSource = remoteDataSource,
      cacheManager = NoOpStationsCacheManager(),
      locationProvider = object : LocationProvider {
        override suspend fun currentLocation(): GeoPoint = GeoPoint(41.65, -0.88)
      },
      settingsRepository = settingsRepository,
      scope = testCoroutineScope(),
    )

    repository.loadIfNeeded()

    assertEquals(GeoPoint(41.65, -0.88), requestedOrigin)
    assertEquals(GeoPoint(41.65, -0.88), repository.state.value.userLocation)
  }

  @Test
  fun `stations repository falls back to default location when current location times out`() = runTest {
    var requestedOrigin: GeoPoint? = null
    val defaultLocation = GeoPoint(City.ZARAGOZA.defaultLatitude, City.ZARAGOZA.defaultLongitude)
    val remoteDataSource = object : StationsRemoteDataSource {
      override suspend fun fetchStations(origin: GeoPoint): List<Station> {
        requestedOrigin = origin
        return listOf(
          Station(
            id = "station-1",
            name = "Plaza Espana",
            address = "Centro",
            location = origin,
            bikesAvailable = 10,
            slotsFree = 5,
            distanceMeters = 0,
          ),
        )
      }

      override suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability> = emptyMap()
    }
    val settingsRepository = object : SettingsRepository {
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
      override fun currentSearchRadiusMeters() = DEFAULT_SEARCH_RADIUS_METERS
      override fun currentPreferredMapApp() = PreferredMapApp.AppleMaps
      override fun currentSelectedCity() = City.ZARAGOZA
      override fun currentLastSeenChangelogAppVersion(): String? = null
      override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) = Unit
      override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit
      override suspend fun setLastSeenChangelogVersion(version: Int) = Unit
      override suspend fun setLastSeenChangelogAppVersion(version: String?) = Unit
      override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
      override suspend fun setThemePreference(preference: ThemePreference) = Unit
      override suspend fun setSelectedCity(city: City) = Unit
      override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit
      override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit
      override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) = Unit
      override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit
    }
    val repository = StationsRepositoryImpl(
      remoteDataSource = remoteDataSource,
      cacheManager = NoOpStationsCacheManager(),
      locationProvider = object : LocationProvider {
        override suspend fun currentLocation(): GeoPoint? {
          delay(10_000)
          return GeoPoint(41.65, -0.88)
        }
      },
      settingsRepository = settingsRepository,
      scope = testCoroutineScope(),
    )

    repository.loadIfNeeded()

    assertEquals(defaultLocation, requestedOrigin)
    assertNull(repository.state.value.userLocation)
    assertEquals(1, repository.state.value.stations.size)
  }

  @Test
  fun `shared graph memoizes stateful repositories`() {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/bizizaragoza-graph-${Random.nextInt()}"
    val graph = SharedGraph.Companion.create(
      object : PlatformBindings {
        override val appConfiguration: AppConfiguration = AppConfiguration()
        override val appVersion: String = "test-app"
        override val assistantIntentResolver: AssistantIntentResolver = DefaultAssistantIntentResolver()
        override val fileSystem: FileSystem = FileSystem.SYSTEM
        override val googleMapsApiKey: String? = null
        override val httpClientFactory: BiziHttpClientFactory = object : BiziHttpClientFactory {
          override fun create(json: Json): HttpClient = HttpClient()
        }
        override val localNotifier: LocalNotifier = object : LocalNotifier {
          override suspend fun hasPermission(): Boolean = true
          override suspend fun requestPermission(): Boolean = true
          override suspend fun notify(title: String, body: String) = Unit
        }
        override val locationProvider: LocationProvider = object : LocationProvider {
          override suspend fun currentLocation(): GeoPoint? = null
        }
        override val routeLauncher: RouteLauncher = object : RouteLauncher {
          override fun launch(station: Station) = Unit
          override fun launchWalkToLocation(destination: GeoPoint) = Unit
        }
        override val mapSupport: MapSupport = object : MapSupport {
          override fun currentStatus() = MapSupportStatus(
            embeddedProvider = EmbeddedMapProvider.None,
            googleMapsSdkLinked = false,
            googleMapsApiKeyConfigured = false,
          )
        }
        override val osVersion: String = "test-os"
        override val platform: String = "test-platform"
        override val secureKeyStore: SecureKeyStore = SecureKeyStore()
        override val storageDirectoryProvider: StorageDirectoryProvider = object : StorageDirectoryProvider {
          override val rootPath: String = temporaryRoot
        }
        override val watchSyncBridge: WatchSyncBridge = object : WatchSyncBridge {
          override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit
          override suspend fun latestFavorites(): FavoritesSyncSnapshot? = null
        }
        override val databaseFactory: DatabaseFactory? = null
      },
    )

    assertSame(graph.stationsRepository, graph.stationsRepository)
    assertSame(graph.favoritesRepository, graph.favoritesRepository)
    assertSame(graph.settingsRepository, graph.settingsRepository)
  }

  @Test
  fun `assistant resolver highlights nearest station and favorite count`() = runTest {
    val resolver = DefaultAssistantIntentResolver()
    val stations = listOf(
      Station(
        id = "station-2",
        name = "Plaza Aragón",
        address = "Centro",
        location = GeoPoint(41.6495, -0.8881),
        bikesAvailable = 0,
        slotsFree = 9,
        distanceMeters = 80,
      ),
      Station(
        id = "station-1",
        name = "Plaza Espana",
        address = "Centro",
        location = GeoPoint(41.6488, -0.8891),
        bikesAvailable = 7,
        slotsFree = 4,
        distanceMeters = 120,
      ),
    )

    val nearest = resolver.resolve(
      action = AssistantAction.NearestStation,
      stationsState = StationsState(stations = stations, isLoading = false),
      favoriteIds = setOf("station-1", "station-2"),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )
    val nearestWithBikes = resolver.resolve(
      action = AssistantAction.NearestStationWithBikes,
      stationsState = StationsState(stations = stations, isLoading = false),
      favoriteIds = setOf("station-1", "station-2"),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )
    val nearestWithSlots = resolver.resolve(
      action = AssistantAction.NearestStationWithSlots,
      stationsState = StationsState(stations = stations, isLoading = false),
      favoriteIds = setOf("station-1", "station-2"),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )
    val favorites = resolver.resolve(
      action = AssistantAction.FavoriteStations,
      stationsState = StationsState(stations = stations, isLoading = false),
      favoriteIds = setOf("station-1", "station-2"),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )

    assertEquals("station-2", nearest.highlightedStationId)
    assertEquals("station-1", nearestWithBikes.highlightedStationId)
    assertEquals("station-2", nearestWithSlots.highlightedStationId)
    assertEquals("Nearest station: Plaza Aragón. 0 bikes available, 9 slots free", nearest.spokenResponse)
    assertEquals("Nearest station with bikes: Plaza Espana. 7 bikes available, 4 slots free", nearestWithBikes.spokenResponse)
    assertEquals("Nearest station with slots: Plaza Aragón. 0 bikes available, 9 slots free", nearestWithSlots.spokenResponse)
    assertEquals("You have 2 favorite stations", favorites.spokenResponse)
  }

  @Test
  fun `assistant resolver returns missing status when station is unknown`() = runTest {
    val resolution = DefaultAssistantIntentResolver().resolve(
      action = AssistantAction.StationStatus("missing"),
      stationsState = StationsState(stations = emptyList(), isLoading = false),
      favoriteIds = emptySet(),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )

    assertNull(resolution.highlightedStationId)
    assertEquals("Station not found", resolution.spokenResponse)
  }

  @Test
  fun `assistant resolver returns bike and slot counts for known station`() = runTest {
    val station = Station(
      id = "station-42",
      name = "Plaza Espana",
      address = "Centro",
      location = GeoPoint(41.6488, -0.8891),
      bikesAvailable = 6,
      slotsFree = 8,
      distanceMeters = 100,
    )
    val state = StationsState(stations = listOf(station), isLoading = false)
    val resolver = DefaultAssistantIntentResolver()

    val bikes = resolver.resolve(
      action = AssistantAction.StationBikeCount("station-42"),
      stationsState = state,
      favoriteIds = emptySet(),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )
    val slots = resolver.resolve(
      action = AssistantAction.StationSlotCount("station-42"),
      stationsState = state,
      favoriteIds = emptySet(),
      searchRadiusMeters = DEFAULT_SEARCH_RADIUS_METERS,
    )

    assertEquals("station-42", bikes.highlightedStationId)
    assertEquals("station-42", slots.highlightedStationId)
    assertEquals("Plaza Espana has 6 bikes available", bikes.spokenResponse)
    assertEquals("Plaza Espana has 8 free slots", slots.spokenResponse)
  }

  @Test
  fun `assistant resolver falls back to nearest outside configured radius`() = runTest {
    val stations = listOf(
      Station(
        id = "station-7",
        name = "Plaza Aragón",
        address = "Centro",
        location = GeoPoint(41.6495, -0.8881),
        bikesAvailable = 3,
        slotsFree = 5,
        distanceMeters = 720,
      ),
    )

    val resolution = DefaultAssistantIntentResolver().resolve(
      action = AssistantAction.NearestStationWithBikes,
      stationsState = StationsState(stations = stations, isLoading = false),
      favoriteIds = emptySet(),
      searchRadiusMeters = 500,
    )

    assertEquals("station-7", resolution.highlightedStationId)
    assertEquals("No stations with bikes within 500m. Nearest is 720m away: Plaza Aragón. 3 bikes available, 5 slots free", resolution.spokenResponse)
  }

  @Test
  fun `findStationMatchingQuery resolves by name accent and numeric id`() {
    val stations = listOf(
      Station(
        id = "station-42",
        name = "42- Plaza España",
        address = "Centro",
        location = GeoPoint(41.6488, -0.8891),
        bikesAvailable = 6,
        slotsFree = 8,
        distanceMeters = 100,
      ),
      Station(
        id = "station-99",
        name = "Universidad",
        address = "Campus",
        location = GeoPoint(41.65, -0.88),
        bikesAvailable = 3,
        slotsFree = 5,
        distanceMeters = 200,
      ),
    )

    assertEquals("station-42", findStationMatchingQuery(stations, "Plaza Espana")?.id)
    assertEquals("station-42", findStationMatchingQuery(stations, "Pza. de España")?.id)
    assertEquals("station-42", findStationMatchingQuery(stations, "42")?.id)
    assertEquals("station-99", findStationMatchingQuery(stations, "universidad")?.id)
  }

  @Test
  fun `findStationMatchingQuery prioritizes exact station names over partial matches`() {
    val stations = listOf(
      Station(
        id = "station-far",
        name = "Plaza España Delicias",
        address = "Delicias",
        location = GeoPoint(41.65, -0.92),
        bikesAvailable = 4,
        slotsFree = 3,
        distanceMeters = 1200,
      ),
      Station(
        id = "station-exact",
        name = "Plaza España",
        address = "Centro",
        location = GeoPoint(41.6488, -0.8891),
        bikesAvailable = 6,
        slotsFree = 8,
        distanceMeters = 250,
      ),
    )

    assertEquals("station-exact", findStationMatchingQuery(stations, "Plaza España")?.id)
    assertEquals("station-exact", filterStationsByQuery(stations, "Plaza España").firstOrNull()?.id)
  }

  @Test
  fun `findStationMatchingQueryOrPinnedAlias resolves home and work aliases`() {
    val stations = listOf(
      Station(
        id = "station-home",
        name = "Plaza España",
        address = "Centro",
        location = GeoPoint(41.6488, -0.8891),
        bikesAvailable = 6,
        slotsFree = 8,
        distanceMeters = 100,
      ),
      Station(
        id = "station-work",
        name = "Actur",
        address = "Norte",
        location = GeoPoint(41.66, -0.87),
        bikesAvailable = 2,
        slotsFree = 9,
        distanceMeters = 400,
      ),
    )

    assertEquals(
      "station-home",
      findStationMatchingQueryOrPinnedAlias(stations, "casa", "station-home", "station-work")?.id,
    )
    assertEquals(
      "station-work",
      findStationMatchingQueryOrPinnedAlias(stations, "mi trabajo", "station-home", "station-work")?.id,
    )
  }
}

private class RecordingWatchSyncBridge(
  private val remoteSnapshot: FavoritesSyncSnapshot? = null,
) : WatchSyncBridge {
  val pushedSnapshots = mutableListOf<FavoritesSyncSnapshot>()

  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    pushedSnapshots += snapshot
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? = remoteSnapshot
}
