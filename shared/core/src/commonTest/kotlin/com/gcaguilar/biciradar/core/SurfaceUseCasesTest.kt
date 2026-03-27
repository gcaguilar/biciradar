package com.gcaguilar.biciradar.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SurfaceUseCasesTest {
  @Test
  fun `get favorite stations prioritizes home station and respects limit`() = runTest {
    val stationsRepository = FakeUseCaseStationsRepository(
      StationsState(
        stations = listOf(
          station(id = "work", distanceMeters = 320),
          station(id = "home", distanceMeters = 150),
          station(id = "other", distanceMeters = 80),
        ),
        lastUpdatedEpoch = 5_000L,
      ),
    )
    val useCase = GetFavoriteStations(
      favoritesRepository = FakeUseCaseFavoritesRepository(
        favoriteIds = setOf("home", "work"),
        homeStationId = "home",
      ),
      settingsRepository = FakeUseCaseSettingsRepository(),
      stationsRepository = stationsRepository,
    )

    val favorites = useCase.execute(limit = 1)

    assertEquals(listOf("home"), favorites.map { it.id })
    assertTrue(favorites.first().isFavorite)
    assertTrue(stationsRepository.loadIfNeededCalls > 0)
  }

  @Test
  fun `get station status uses cached snapshot before loading repository`() = runTest {
    val stationsRepository = FakeUseCaseStationsRepository(
      StationsState(
        stations = listOf(station(id = "station-1")),
        lastUpdatedEpoch = 5_000L,
      ),
    )
    val cachedSnapshot = station(id = "cached", distanceMeters = 40).toSurfaceSnapshot(
      cityId = City.ZARAGOZA.id,
      lastUpdatedEpoch = 10_000L,
      isFavorite = true,
    )
    val useCase = GetStationStatus(
      settingsRepository = FakeUseCaseSettingsRepository(),
      stationsRepository = stationsRepository,
      surfaceSnapshotRepository = FakeSurfaceSnapshotRepository(
        bundle = SurfaceSnapshotBundle(
          generatedAtEpoch = 11_000L,
          favoriteStation = cachedSnapshot,
          state = defaultSurfaceState(),
        ),
      ),
    )

    val snapshot = useCase.execute("cached")

    assertEquals("cached", snapshot?.id)
    assertEquals(0, stationsRepository.loadIfNeededCalls)
  }

  @Test
  fun `get station status uses saved place snapshots before loading repository`() = runTest {
    val stationsRepository = FakeUseCaseStationsRepository(
      StationsState(
        stations = listOf(station(id = "station-1")),
        lastUpdatedEpoch = 5_000L,
      ),
    )
    val cachedSnapshot = station(id = "work", distanceMeters = 55).toSurfaceSnapshot(
      cityId = City.ZARAGOZA.id,
      lastUpdatedEpoch = 10_000L,
    )
    val useCase = GetStationStatus(
      settingsRepository = FakeUseCaseSettingsRepository(),
      stationsRepository = stationsRepository,
      surfaceSnapshotRepository = FakeSurfaceSnapshotRepository(
        bundle = SurfaceSnapshotBundle(
          generatedAtEpoch = 11_000L,
          workStation = cachedSnapshot,
          state = defaultSurfaceState(),
        ),
      ),
    )

    val snapshot = useCase.execute("work")

    assertEquals("work", snapshot?.id)
    assertEquals(0, stationsRepository.loadIfNeededCalls)
  }

  @Test
  fun `suggested alternative prefers higher availability`() = runTest {
    val useCase = GetSuggestedAlternativeStation(
      settingsRepository = FakeUseCaseSettingsRepository(searchRadiusMetersValue = 500),
      stationsRepository = FakeUseCaseStationsRepository(
        StationsState(
          stations = listOf(
            station(id = "origin", latitude = 41.65, longitude = -0.88, bikesAvailable = 0),
            station(id = "near-low", latitude = 41.6502, longitude = -0.8802, bikesAvailable = 1),
            station(id = "far-high", latitude = 41.6509, longitude = -0.8809, bikesAvailable = 4),
          ),
          lastUpdatedEpoch = 8_000L,
        ),
      ),
    )

    val alternative = useCase.execute(
      stationId = "origin",
      kind = SurfaceMonitoringKind.Bikes,
    )

    assertEquals("far-high", alternative?.id)
  }

  @Test
  fun `refresh station data if needed refreshes repositories and returns bundle`() = runTest {
    val stationsRepository = FakeUseCaseStationsRepository(
      StationsState(stations = listOf(station(id = "station-1")), lastUpdatedEpoch = 9_000L),
    )
    val snapshotRepository = FakeSurfaceSnapshotRepository(
      bundle = SurfaceSnapshotBundle(
        generatedAtEpoch = 9_000L,
        favoriteStation = station(id = "station-1").toSurfaceSnapshot(
          cityId = City.ZARAGOZA.id,
          lastUpdatedEpoch = 9_000L,
          isFavorite = true,
        ),
        state = defaultSurfaceState(),
      ),
    )
    val useCase = RefreshStationDataIfNeeded(
      stationsRepository = stationsRepository,
      surfaceSnapshotRepository = snapshotRepository,
    )

    val bundle = useCase.execute(forceRefresh = true)

    assertEquals(1, stationsRepository.forceRefreshCalls)
    assertEquals(1, snapshotRepository.refreshCalls)
    assertEquals("station-1", bundle?.favoriteStation?.id)
  }

  @Test
  fun `start and stop station monitoring delegate to repository`() = runTest {
    val monitoringRepository = FakeSurfaceMonitoringRepository()
    val startUseCase = StartStationMonitoring(monitoringRepository)
    val stopUseCase = StopStationMonitoring(monitoringRepository)

    val started = startUseCase.execute(
      stationId = "station-42",
      durationSeconds = 180,
      kind = SurfaceMonitoringKind.Bikes,
    )
    stopUseCase.execute(clear = false)
    stopUseCase.execute(clear = true)

    assertTrue(started)
    assertEquals("station-42", monitoringRepository.lastStartedStationId)
    assertEquals(SurfaceMonitoringKind.Bikes, monitoringRepository.lastStartedKind)
    assertTrue(monitoringRepository.stopCalled)
    assertTrue(monitoringRepository.clearCalled)
    assertEquals(null, monitoringRepository.state.value)
  }
}

private class FakeUseCaseStationsRepository(
  initialState: StationsState,
) : StationsRepository {
  override val state = MutableStateFlow(initialState)
  var loadIfNeededCalls = 0
  var forceRefreshCalls = 0

  override suspend fun loadIfNeeded() {
    loadIfNeededCalls += 1
  }

  override suspend fun forceRefresh() {
    forceRefreshCalls += 1
  }

  override suspend fun refreshAvailability(stationIds: List<String>) = Unit

  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class FakeUseCaseFavoritesRepository(
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
    homeStationId.value = stationId
  }

  override suspend fun setWorkStationId(stationId: String?) {
    workStationId.value = stationId
  }

  override suspend fun clearAll() = Unit
  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value
  override fun currentHomeStationId(): String? = homeStationId.value
  override fun currentWorkStationId(): String? = workStationId.value
}

private class FakeUseCaseSettingsRepository(
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

private class FakeSurfaceSnapshotRepository(
  bundle: SurfaceSnapshotBundle? = null,
) : SurfaceSnapshotRepository {
  private val mutableBundle = MutableStateFlow(bundle)
  var refreshCalls = 0
  var bootstrapCalls = 0

  override val bundle: StateFlow<SurfaceSnapshotBundle?> = mutableBundle

  override suspend fun bootstrap() {
    bootstrapCalls += 1
  }

  override suspend fun refreshSnapshot() {
    refreshCalls += 1
  }

  override suspend fun saveMonitoringSession(session: SurfaceMonitoringSession?) {
    mutableBundle.value = mutableBundle.value?.copy(monitoringSession = session)
  }

  override fun currentBundle(): SurfaceSnapshotBundle? = mutableBundle.value
}

private class FakeSurfaceMonitoringRepository : SurfaceMonitoringRepository {
  private val mutableState = MutableStateFlow<SurfaceMonitoringSession?>(null)
  var lastStartedStationId: String? = null
  var lastStartedKind: SurfaceMonitoringKind? = null
  var stopCalled = false
  var clearCalled = false

  override val state: StateFlow<SurfaceMonitoringSession?> = mutableState

  override suspend fun bootstrap() = Unit

  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean {
    lastStartedStationId = stationId
    lastStartedKind = kind
    mutableState.value = SurfaceMonitoringSession(
      stationId = stationId,
      stationName = stationId,
      cityId = City.ZARAGOZA.id,
      kind = kind,
      status = SurfaceMonitoringStatus.Monitoring,
      bikesAvailable = 5,
      docksAvailable = 4,
      statusLevel = SurfaceStatusLevel.Good,
      startedAtEpoch = 1L,
      expiresAtEpoch = durationSeconds.toLong() * 1_000L,
      lastUpdatedEpoch = 1L,
      isActive = true,
    )
    return true
  }

  override suspend fun startMonitoringFavoriteStation(
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean = startMonitoring("favorite", durationSeconds, kind)

  override fun stopMonitoring() {
    stopCalled = true
    mutableState.value = mutableState.value?.copy(isActive = false, status = SurfaceMonitoringStatus.Ended)
  }

  override suspend fun clearMonitoring() {
    clearCalled = true
    mutableState.value = null
  }
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

private fun defaultSurfaceState(): SurfaceState = SurfaceState(
  hasLocationPermission = true,
  hasNotificationPermission = true,
  hasFavoriteStation = true,
  isDataFresh = true,
  lastSyncEpoch = 1L,
  cityId = City.ZARAGOZA.id,
  cityName = City.ZARAGOZA.displayName,
)
