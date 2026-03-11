package com.gcaguilar.bizizaragoza.core

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import okio.Path.Companion.toPath
import okio.FileSystem

class CoreRepositoryTest {
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
        override suspend fun pushFavoriteIds(favoriteIds: Set<String>) = Unit

        override suspend fun latestFavoriteIds(): Set<String>? = null
      },
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
    )

    repository.bootstrap()
    repository.setSearchRadiusMeters(750)

    assertEquals(750, repository.currentSearchRadiusMeters())
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
        override suspend fun pushFavoriteIds(favoriteIds: Set<String>) = Unit

        override suspend fun latestFavoriteIds(): Set<String> = setOf("station-watch")
      },
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
    val watchBridge = RecordingWatchSyncBridge(remoteFavoriteIds = setOf("station-watch"))
    val repository = FavoritesRepositoryImpl(
      fileSystem = fileSystem,
      json = Json,
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      watchSyncBridge = watchBridge,
    )

    repository.bootstrap()

    assertEquals(
      listOf(setOf("station-local", "station-watch")),
      watchBridge.pushedFavoriteIds,
    )
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
    )

    repository.bootstrap()
    repository.toggle("station-1")
    repository.toggle("station-2")
    repository.toggle("station-1")

    assertEquals(
      listOf(setOf("station-1"), setOf("station-1", "station-2"), setOf("station-2")),
      watchBridge.pushedFavoriteIds,
    )
  }

  @Test
  fun `stations repository refresh uses current user location when available`() = runTest {
    var requestedOrigin: GeoPoint? = null
    val repository = StationsRepositoryImpl(
      biziApi = object : BiziApi {
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
      },
      appConfiguration = AppConfiguration(),
      locationProvider = object : LocationProvider {
        override suspend fun currentLocation(): GeoPoint = GeoPoint(41.65, -0.88)
      },
    )

    repository.refresh()

    assertEquals(GeoPoint(41.65, -0.88), requestedOrigin)
    assertEquals(GeoPoint(41.65, -0.88), repository.state.value.userLocation)
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
    assertTrue(nearest.spokenResponse.contains("Plaza Aragón"))
    assertTrue(nearestWithBikes.spokenResponse.contains("bicis disponibles"))
    assertTrue(nearestWithSlots.spokenResponse.contains("huecos libres"))
    assertTrue(favorites.spokenResponse.contains("2 estaciones"))
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
    assertEquals("No he encontrado esa estación.", resolution.spokenResponse)
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
    assertTrue(bikes.spokenResponse.contains("6 bicis"))
    assertTrue(slots.spokenResponse.contains("8 huecos"))
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
    assertTrue(resolution.spokenResponse.contains("dentro de 500 m"))
    assertTrue(resolution.spokenResponse.contains("720 m"))
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
    assertEquals("station-42", findStationMatchingQuery(stations, "42")?.id)
    assertEquals("station-99", findStationMatchingQuery(stations, "universidad")?.id)
  }
}

private class RecordingWatchSyncBridge(
  private val remoteFavoriteIds: Set<String>? = null,
) : WatchSyncBridge {
  val pushedFavoriteIds = mutableListOf<Set<String>>()

  override suspend fun pushFavoriteIds(favoriteIds: Set<String>) {
    pushedFavoriteIds += favoriteIds
  }

  override suspend fun latestFavoriteIds(): Set<String>? = remoteFavoriteIds
}
