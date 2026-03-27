package com.gcaguilar.biciradar.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class SurfaceRepositoryTest {
  @Test
  fun `surface snapshot repository prefers home station and sorts nearby by distance`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/biciradar-surface-${Random.nextInt()}"
    val settingsRepository = FakeSettingsRepository()
    val favoritesRepository = FakeFavoritesRepository(
      favoriteIds = setOf("home", "mid", "other-favorite"),
      homeStationId = "home",
      workStationId = "mid",
    )
    val stationsRepository = FakeStationsRepository(
      StationsState(
        stations = listOf(
          station(id = "far", distanceMeters = 450, bikesAvailable = 9),
          station(id = "home", distanceMeters = 120, bikesAvailable = 7),
          station(id = "near", distanceMeters = 60, bikesAvailable = 2),
          station(id = "mid", distanceMeters = 180, bikesAvailable = 4),
        ),
        isLoading = false,
        userLocation = GeoPoint(41.65, -0.88),
        lastUpdatedEpoch = 1_000L,
      ),
    )

    val repository = SurfaceSnapshotRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = testJson(),
      localNotifier = FakeLocalNotifier(permissionGranted = true),
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )

    repository.bootstrap()
    repository.refreshSnapshot()

    val bundle = repository.currentBundle()!!
    assertEquals("home", bundle.favoriteStation?.id)
    assertEquals("home", bundle.homeStation?.id)
    assertEquals("mid", bundle.workStation?.id)
    assertEquals(listOf("near", "home", "mid"), bundle.nearbyStations.map { it.id })
    assertTrue(FileSystem.SYSTEM.exists("$temporaryRoot/surface_snapshot.json".toPath()))
  }

  @Test
  fun `surface snapshot hides nearby stations without location permission and preserves notification state`() = runTest {
    val temporaryRoot = "${FileSystem.SYSTEM_TEMPORARY_DIRECTORY}/biciradar-surface-nolocation-${Random.nextInt()}"
    val repository = SurfaceSnapshotRepositoryImpl(
      fileSystem = FileSystem.SYSTEM,
      json = testJson(),
      localNotifier = FakeLocalNotifier(permissionGranted = false),
      storageDirectoryProvider = object : StorageDirectoryProvider {
        override val rootPath: String = temporaryRoot
      },
      settingsRepository = FakeSettingsRepository(),
      favoritesRepository = FakeFavoritesRepository(
        favoriteIds = setOf("home"),
        homeStationId = "home",
      ),
      stationsRepository = FakeStationsRepository(
        StationsState(
          stations = listOf(
            station(id = "home", distanceMeters = 120),
            station(id = "near", distanceMeters = 60),
          ),
          isLoading = false,
          userLocation = null,
          lastUpdatedEpoch = 2_000L,
        ),
      ),
    )

    repository.bootstrap()
    repository.refreshSnapshot()

    val bundle = repository.currentBundle()!!
    assertEquals(emptyList<String>(), bundle.nearbyStations.map { it.id })
    assertFalse(bundle.state.hasLocationPermission)
    assertFalse(bundle.state.hasNotificationPermission)
  }

  @Test
  fun `alternative selection prioritizes higher availability before nearer station`() = runTest {
    val monitoredStation = station(id = "monitor", latitude = 41.65, longitude = -0.88, bikesAvailable = 1, slotsFree = 6)
    val nearLow = station(id = "near-low", latitude = 41.6503, longitude = -0.8803, bikesAvailable = 1, slotsFree = 8)
    val farHigh = station(id = "far-high", latitude = 41.651, longitude = -0.881, bikesAvailable = 5, slotsFree = 2)

    val alternative = selectAlternativeStation(
      monitoredStation = monitoredStation,
      candidates = listOf(monitoredStation, nearLow, farHigh),
      kind = SurfaceMonitoringKind.Bikes,
      maxRadiusMeters = 500,
    )

    assertEquals("far-high", alternative?.id)
  }

  @Test
  fun `surface helpers format relative time and status levels`() {
    val empty = station(id = "empty", bikesAvailable = 0, slotsFree = 9)
    val full = station(id = "full", bikesAvailable = 4, slotsFree = 0)
    val low = station(id = "low", bikesAvailable = 2, slotsFree = 7)
    val good = station(id = "good", bikesAvailable = 7, slotsFree = 8)

    assertEquals(SurfaceStatusLevel.Empty, empty.surfaceStatusLevel())
    assertEquals(SurfaceStatusLevel.Full, full.surfaceStatusLevel())
    assertEquals(SurfaceStatusLevel.Low, low.surfaceStatusLevel())
    assertEquals(SurfaceStatusLevel.Good, good.surfaceStatusLevel())
    assertEquals("Ahora", formatRelativeMinutes(lastUpdatedEpoch = 10_000L, nowEpoch = 10_020L))
    assertEquals("Hace 3 min", formatRelativeMinutes(lastUpdatedEpoch = 10_000L, nowEpoch = 190_000L))
  }
}

private class FakeLocalNotifier(
  private val permissionGranted: Boolean,
) : LocalNotifier {
  override suspend fun hasPermission(): Boolean = permissionGranted
  override suspend fun requestPermission(): Boolean = permissionGranted
  override suspend fun notify(title: String, body: String) = Unit
}

private fun station(
  id: String,
  distanceMeters: Int = 100,
  latitude: Double = 41.65,
  longitude: Double = -0.88,
  bikesAvailable: Int = 6,
  slotsFree: Int = 5,
): Station = Station(
  id = id,
  name = id,
  address = id,
  location = GeoPoint(latitude, longitude),
  bikesAvailable = bikesAvailable,
  slotsFree = slotsFree,
  distanceMeters = distanceMeters,
)

private fun testJson(): Json = Json {
  ignoreUnknownKeys = true
  explicitNulls = false
}

private class FakeStationsRepository(
  initialState: StationsState,
) : StationsRepository {
  override val state = MutableStateFlow(initialState)

  override suspend fun loadIfNeeded() = Unit

  override suspend fun forceRefresh() = Unit

  override suspend fun refreshAvailability(stationIds: List<String>) = Unit

  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class FakeFavoritesRepository(
  favoriteIds: Set<String> = emptySet(),
  homeStationId: String? = null,
  workStationId: String? = null,
) : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(favoriteIds)
  override val homeStationId = MutableStateFlow(homeStationId)
  override val workStationId = MutableStateFlow(workStationId)

  override suspend fun bootstrap() = Unit

  override suspend fun toggle(stationId: String) = Unit

  override suspend fun setHomeStationId(stationId: String?) {
    this.homeStationId.value = stationId
  }

  override suspend fun setWorkStationId(stationId: String?) {
    this.workStationId.value = stationId
  }

  override suspend fun clearAll() {
    favoriteIds.value = emptySet()
    homeStationId.value = null
    workStationId.value = null
  }

  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value

  override fun currentHomeStationId(): String? = homeStationId.value

  override fun currentWorkStationId(): String? = workStationId.value
}

private class FakeSettingsRepository(
  private val searchRadiusMetersValue: Int = DEFAULT_SEARCH_RADIUS_METERS,
  private val city: City = City.ZARAGOZA,
) : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(searchRadiusMetersValue)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(city)
  override val hasCompletedOnboarding = MutableStateFlow(true)

  override suspend fun bootstrap() = Unit

  override fun currentSearchRadiusMeters(): Int = searchRadiusMetersValue

  override fun currentPreferredMapApp(): PreferredMapApp = PreferredMapApp.AppleMaps

  override fun currentSelectedCity(): City = city

  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) = Unit

  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit

  override suspend fun setLastSeenChangelogVersion(version: Int) = Unit

  override suspend fun setThemePreference(preference: ThemePreference) = Unit

  override suspend fun setSelectedCity(city: City) = Unit

  override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit
}
