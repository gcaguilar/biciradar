package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LaunchCoordinatorTest {
  @Test
  fun `mobile select city reuses shared city change semantics`() = runTest {
    val settingsRepository = LaunchFakeSettingsRepository()
    val favoritesRepository = LaunchFakeFavoritesRepository()
    val stationsRepository = LaunchFakeStationsRepository()
    val coordinator = LaunchCoordinator(
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = settingsRepository,
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
      ),
      favoritesRepository = favoritesRepository,
      localNotifier = LaunchFakeLocalNotifier(),
      routeLauncher = LaunchFakeRouteLauncher(),
      stationsRepository = stationsRepository,
      surfaceMonitoringRepository = LaunchFakeSurfaceMonitoringRepository(),
      surfaceSnapshotRepository = LaunchFakeSurfaceSnapshotRepository(),
    )

    val resolution = coordinator.resolveMobileLaunch(
      request = MobileLaunchRequest.SelectCity(City.BARCELONA.id),
      stations = stationsRepository.state.value.stations,
      searchRadiusMeters = 500,
    )

    assertEquals(City.BARCELONA, settingsRepository.selectedCity.value)
    assertEquals(1, favoritesRepository.clearAllCount)
    assertEquals(1, stationsRepository.forceRefreshCount)
    assertEquals(true, settingsRepository.onboardingChecklist.value.cityConfirmed)
    assertEquals(Screen.Nearby, resolution?.screen)
  }

  @Test
  fun `assistant search station keeps query and routes to detail when matched`() = runTest {
    val favoritesRepository = LaunchFakeFavoritesRepository().apply {
      homeStationId.value = "station-home"
    }
    val coordinator = LaunchCoordinator(
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = LaunchFakeSettingsRepository(),
        favoritesRepository = favoritesRepository,
        stationsRepository = LaunchFakeStationsRepository(),
      ),
      favoritesRepository = favoritesRepository,
      localNotifier = LaunchFakeLocalNotifier(),
      routeLauncher = LaunchFakeRouteLauncher(),
      stationsRepository = LaunchFakeStationsRepository(),
      surfaceMonitoringRepository = LaunchFakeSurfaceMonitoringRepository(),
      surfaceSnapshotRepository = LaunchFakeSurfaceSnapshotRepository(),
    )
    val stations = listOf(
      station(id = "station-home", name = "Plaza Espana"),
      station(id = "station-2", name = "Actur"),
    )

    val resolution = coordinator.resolveAssistantLaunch(
      request = AssistantLaunchRequest.SearchStation("home"),
      stations = stations,
    )

    assertEquals("home", resolution.searchQuery)
    assertEquals(Screen.StationDetail("station-home"), resolution.screen)
    assertNull(resolution.assistantAction)
  }

  @Test
  fun `assistant route falls back to map with query when station is unknown`() = runTest {
    val routeLauncher = LaunchFakeRouteLauncher()
    val coordinator = LaunchCoordinator(
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = LaunchFakeSettingsRepository(),
        favoritesRepository = LaunchFakeFavoritesRepository(),
        stationsRepository = LaunchFakeStationsRepository(),
      ),
      favoritesRepository = LaunchFakeFavoritesRepository(),
      localNotifier = LaunchFakeLocalNotifier(),
      routeLauncher = routeLauncher,
      stationsRepository = LaunchFakeStationsRepository(),
      surfaceMonitoringRepository = LaunchFakeSurfaceMonitoringRepository(),
      surfaceSnapshotRepository = LaunchFakeSurfaceSnapshotRepository(),
    )

    val resolution = coordinator.resolveAssistantLaunch(
      request = AssistantLaunchRequest.RouteToStation(stationQuery = "delicias"),
      stations = emptyList(),
    )

    assertEquals(Screen.Map, resolution.screen)
    assertEquals("delicias", resolution.searchQuery)
    assertNull(routeLauncher.launchedStationId)
  }
}

private class LaunchFakeSettingsRepository : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.GoogleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(false)
  override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot())
  override val engagementSnapshot = MutableStateFlow(EngagementSnapshot())

  override suspend fun bootstrap() = Unit
  override fun currentSearchRadiusMeters(): Int = searchRadiusMeters.value
  override fun currentPreferredMapApp(): PreferredMapApp = preferredMapApp.value
  override fun currentSelectedCity(): City = selectedCity.value
  override fun currentLastSeenChangelogAppVersion(): String? = lastSeenChangelogAppVersion.value
  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) = Unit
  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit
  override suspend fun setLastSeenChangelogVersion(version: Int) = Unit
  override suspend fun setLastSeenChangelogAppVersion(version: String?) = Unit
  override suspend fun setThemePreference(preference: ThemePreference) = Unit
  override suspend fun setSelectedCity(city: City) {
    selectedCity.value = city
  }
  override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit
  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) {
    onboardingChecklist.value = snapshot
  }
  override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    onboardingChecklist.value = transform(onboardingChecklist.value)
  }
  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class LaunchFakeFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  var clearAllCount = 0

  override suspend fun bootstrap() = Unit
  override suspend fun syncFromPeer() = Unit
  override suspend fun toggle(stationId: String) = Unit
  override suspend fun setHomeStationId(stationId: String?) {
    homeStationId.value = stationId
  }
  override suspend fun setWorkStationId(stationId: String?) {
    workStationId.value = stationId
  }
  override suspend fun clearAll() {
    clearAllCount++
  }
  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value
  override fun currentHomeStationId(): String? = homeStationId.value
  override fun currentWorkStationId(): String? = workStationId.value
}

private class LaunchFakeStationsRepository : StationsRepository {
  override val state = MutableStateFlow(
    StationsState(
      stations = listOf(
        station(id = "station-home", name = "Plaza Espana"),
        station(id = "station-2", name = "Actur"),
      ),
    ),
  )
  var forceRefreshCount = 0

  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() {
    forceRefreshCount++
  }
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class LaunchFakeLocalNotifier : LocalNotifier {
  override suspend fun requestPermission(): Boolean = true
  override suspend fun notify(title: String, body: String) = Unit
}

private class LaunchFakeRouteLauncher : RouteLauncher {
  var launchedStationId: String? = null
  var launchedWalkDestination: GeoPoint? = null

  override fun launch(station: Station) {
    launchedStationId = station.id
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    launchedWalkDestination = destination
  }
}

private class LaunchFakeSurfaceSnapshotRepository : SurfaceSnapshotRepository {
  override val bundle = MutableStateFlow<SurfaceSnapshotBundle?>(null)
  var refreshCount = 0

  override suspend fun bootstrap() = Unit
  override suspend fun refreshSnapshot() {
    refreshCount++
  }
  override suspend fun saveMonitoringSession(session: SurfaceMonitoringSession?) = Unit
  override fun currentBundle(): SurfaceSnapshotBundle? = bundle.value
}

private class LaunchFakeSurfaceMonitoringRepository : SurfaceMonitoringRepository {
  override val state = MutableStateFlow<SurfaceMonitoringSession?>(null)
  var startMonitoringStationId: String? = null

  override suspend fun bootstrap() = Unit
  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean {
    startMonitoringStationId = stationId
    state.value = SurfaceMonitoringSession(
      stationId = stationId,
      stationName = stationId,
      cityId = City.ZARAGOZA.id,
      kind = kind,
      status = SurfaceMonitoringStatus.Monitoring,
      bikesAvailable = 5,
      docksAvailable = 6,
      statusLevel = SurfaceStatusLevel.Good,
      startedAtEpoch = 1L,
      expiresAtEpoch = 2L,
      lastUpdatedEpoch = 1L,
      isActive = true,
    )
    return true
  }
  override suspend fun startMonitoringFavoriteStation(durationSeconds: Int, kind: SurfaceMonitoringKind): Boolean = false
  override fun stopMonitoring() = Unit
  override suspend fun clearMonitoring() = Unit
}

private fun station(
  id: String,
  name: String,
): Station = Station(
  id = id,
  name = name,
  address = "Address for $name",
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = 5,
  slotsFree = 6,
  distanceMeters = 100,
)
