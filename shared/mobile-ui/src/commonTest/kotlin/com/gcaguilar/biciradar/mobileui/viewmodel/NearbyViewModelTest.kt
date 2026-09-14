package com.gcaguilar.biciradar.mobileui.viewmodel

import app.cash.turbine.test
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.NoOpPermissionPrompter
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.TripMode
import com.gcaguilar.biciradar.mobileui.NearbyMaxDistance
import com.gcaguilar.biciradar.mobileui.NearbySort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class NearbyViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `ui state consolidates stations favorites radius and freshness from runtime collectors`() =
    runTest(dispatcher) {
      val stationsRepository = NearbyTestStationsRepository()
      val favoritesRepository = NearbyTestFavoritesRepository()
      val settingsRepository = NearbyTestSettingsRepository()
      val viewModel =
        NearbyViewModel(
          stationsRepository = stationsRepository,
          favoritesRepository = favoritesRepository,
          settingsRepository = settingsRepository,
          routeLauncher = NearbyNoOpRouteLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )

      val station = nearbyStation(id = "s1", distanceMeters = 150)
      stationsRepository.state.value =
        StationsState(
          stations = listOf(station),
          isLoading = false,
          errorMessage = null,
          freshness = DataFreshness.StaleUsable,
          lastUpdatedEpoch = 1234L,
        )
      favoritesRepository.favoriteIds.value = setOf("s1")
      settingsRepository.searchRadiusMeters.value = 300

      viewModel.uiState.test {
        skipItems(1)
        runCurrent()
        val state = awaitItem()
        assertEquals(listOf(station), state.stations)
        assertEquals(setOf("s1"), state.favoriteIds)
        assertEquals(300, state.searchRadiusMeters)
        assertEquals(300, state.nearestSelection.radiusMeters)
        assertEquals("s1", state.nearestSelection.withinRadiusStation?.id)
        assertEquals(DataFreshness.StaleUsable, state.dataFreshness)
        assertEquals(1234L, state.lastUpdatedEpoch)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `public actions delegate to repositories and route launcher`() =
    runTest(dispatcher) {
      val stationsRepository = NearbyTestStationsRepository()
      val favoritesRepository = NearbyTestFavoritesRepository()
      val settingsRepository = NearbyTestSettingsRepository()
      val routeLauncher = NearbyRecordingRouteLauncher()
      val viewModel =
        NearbyViewModel(
          stationsRepository = stationsRepository,
          favoritesRepository = favoritesRepository,
          settingsRepository = settingsRepository,
          routeLauncher = routeLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )
      val station = nearbyStation(id = "station-route", distanceMeters = 250)

      viewModel.onRetry()
      viewModel.onRefresh()
      viewModel.onFavoriteToggle(station)
      viewModel.onQuickRoute(station)
      runCurrent()

      assertEquals(0, stationsRepository.loadIfNeededCalls)
      assertEquals(2, stationsRepository.forceRefreshCalls)
      assertEquals(listOf("station-route"), favoritesRepository.toggledIds)
      assertEquals("station-route", routeLauncher.lastStationId)
    }

  @Test
  fun `quick route walks on foot and cycles when riding`() =
    runTest(dispatcher) {
      val settingsRepository = NearbyTestSettingsRepository()
      val routeLauncher = NearbyRecordingRouteLauncher()
      val viewModel =
        NearbyViewModel(
          stationsRepository = NearbyTestStationsRepository(),
          favoritesRepository = NearbyTestFavoritesRepository(),
          settingsRepository = settingsRepository,
          routeLauncher = routeLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )
      val station = nearbyStation(id = "station-route", distanceMeters = 250)

      viewModel.onQuickRoute(station)
      runCurrent()
      assertEquals("station-route", routeLauncher.lastStationId)
      assertEquals(null, routeLauncher.lastBikeDestination)

      settingsRepository.tripMode.value = TripMode.Cyclist
      viewModel.onQuickRoute(station)
      runCurrent()

      assertEquals(station.location, routeLauncher.lastBikeDestination)
    }

  @Test
  fun `trip mode orders stations and updates the nearest selection`() =
    runTest(dispatcher) {
      val stationsRepository = NearbyTestStationsRepository()
      val settingsRepository = NearbyTestSettingsRepository()
      val viewModel =
        NearbyViewModel(
          stationsRepository = stationsRepository,
          favoritesRepository = NearbyTestFavoritesRepository(),
          settingsRepository = settingsRepository,
          routeLauncher = NearbyNoOpRouteLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )
      stationsRepository.state.value =
        StationsState(
          stations =
            listOf(
              nearbyStation(id = "without-bikes", distanceMeters = 50, bikes = 0, slots = 5),
              nearbyStation(id = "with-bikes", distanceMeters = 300, bikes = 3, slots = 0),
            ),
          isLoading = false,
        )

      viewModel.uiState.test {
        skipItems(1)
        runCurrent()

        val pedestrian = awaitItem()
        assertEquals(listOf("with-bikes"), pedestrian.stations.map { it.id })
        assertEquals("with-bikes", pedestrian.nearestSelection.highlightedStation?.id)

        viewModel.onTripModeChanged(TripMode.Cyclist)
        runCurrent()

        val cyclist = awaitItem()
        assertEquals(TripMode.Cyclist, cyclist.tripMode)
        assertEquals(listOf("without-bikes"), cyclist.stations.map { it.id })
        assertEquals("without-bikes", cyclist.nearestSelection.highlightedStation?.id)
        assertEquals(TripMode.Cyclist, settingsRepository.tripMode.value)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `setActive triggers load when stations list is empty`() =
    runTest(dispatcher) {
      val stationsRepository = NearbyTestStationsRepository()
      val viewModel =
        NearbyViewModel(
          stationsRepository = stationsRepository,
          favoritesRepository = NearbyTestFavoritesRepository(),
          settingsRepository = NearbyTestSettingsRepository(),
          routeLauncher = NearbyNoOpRouteLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )

      viewModel.setActive(true)
      runCurrent()

      assertEquals(1, stationsRepository.loadIfNeededCalls)
      viewModel.setActive(false)
      runCurrent()
    }

  @Test
  fun `setActive does not load when stations already available`() =
    runTest(dispatcher) {
      val stationsRepository = NearbyTestStationsRepository()
      val viewModel =
        NearbyViewModel(
          stationsRepository = stationsRepository,
          favoritesRepository = NearbyTestFavoritesRepository(),
          settingsRepository = NearbyTestSettingsRepository(),
          routeLauncher = NearbyNoOpRouteLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )
      stationsRepository.state.value =
        StationsState(
          stations = listOf(nearbyStation(id = "s1", distanceMeters = 100)),
          isLoading = false,
        )

      viewModel.setActive(true)
      runCurrent()

      assertEquals(0, stationsRepository.loadIfNeededCalls)
      viewModel.setActive(false)
      runCurrent()
    }

  @Test
  fun `filters and sort reshape the visible station list`() =
    runTest(dispatcher) {
      val stationsRepository = NearbyTestStationsRepository()
      val favoritesRepository = NearbyTestFavoritesRepository()
      val viewModel =
        NearbyViewModel(
          stationsRepository = stationsRepository,
          favoritesRepository = favoritesRepository,
          settingsRepository = NearbyTestSettingsRepository(),
          routeLauncher = NearbyNoOpRouteLauncher,
          permissionPrompter = NoOpPermissionPrompter,
          datosBiziApi = NearbyNoOpDatosBiziApi,
        )
      stationsRepository.state.value =
        StationsState(
          stations =
            listOf(
              nearbyStation(id = "near-few", distanceMeters = 100, bikes = 1, slots = 5),
              nearbyStation(id = "far-many", distanceMeters = 1_500, bikes = 8, slots = 2),
            ),
          isLoading = false,
        )
      favoritesRepository.favoriteIds.value = setOf("far-many")

      fun visibleIds(): List<String> {
        val visibleStations = viewModel.uiState.value.stations
        return visibleStations.map { it.id }
      }

      advanceUntilIdle()
      val initial = viewModel.uiState.value
      assertEquals(listOf("near-few", "far-many"), visibleIds())
      assertEquals(true, initial.filters.isDefault)
      assertEquals(true, initial.supportsBikeTypeFilter)

      viewModel.onSortSelected(NearbySort.MOST_BIKES)
      advanceUntilIdle()
      assertEquals(listOf("far-many", "near-few"), visibleIds())

      viewModel.onMaxDistanceSelected(NearbyMaxDistance.KILOMETERS_1)
      advanceUntilIdle()
      assertEquals(listOf("near-few"), visibleIds())

      viewModel.onClearFilters()
      viewModel.onFavoritesOnlyToggled()
      advanceUntilIdle()
      assertEquals(listOf("far-many"), visibleIds())

      viewModel.onClearFilters()
      advanceUntilIdle()
      assertEquals(listOf("near-few", "far-many"), visibleIds())
    }
}

private class NearbyTestStationsRepository : StationsRepository {
  override val state = MutableStateFlow(StationsState())
  var loadIfNeededCalls = 0
  var forceRefreshCalls = 0

  override suspend fun loadIfNeeded() {
    loadIfNeededCalls++
  }

  override suspend fun forceRefresh() {
    forceRefreshCalls++
  }

  override suspend fun refreshAvailability(stationIds: List<String>) = Unit

  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class NearbyTestFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  val toggledIds = mutableListOf<String>()

  override suspend fun bootstrap() = Unit

  override suspend fun toggle(stationId: String) {
    toggledIds += stationId
  }

  override suspend fun setHomeStationId(stationId: String?) = Unit

  override suspend fun setWorkStationId(stationId: String?) = Unit

  override suspend fun clearAll() = Unit

  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value

  override fun currentHomeStationId(): String? = homeStationId.value

  override fun currentWorkStationId(): String? = workStationId.value
}

private class NearbyTestSettingsRepository : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(true)
  override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
  override val engagementSnapshot = MutableStateFlow(EngagementSnapshot())
  override val tripMode = MutableStateFlow(TripMode.Pedestrian)

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

  override suspend fun setSelectedCity(city: City) = Unit

  override suspend fun setTripMode(mode: TripMode) {
    tripMode.value = mode
  }

  override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit

  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit

  override suspend fun updateOnboardingChecklist(
    transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot,
  ) = Unit

  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit

  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class NearbyRecordingRouteLauncher : RouteLauncher {
  var lastStationId: String? = null
  var lastBikeDestination: GeoPoint? = null

  override fun launch(station: Station) {
    lastStationId = station.id
  }

  override fun launchWalkToLocation(destination: GeoPoint) = Unit

  override fun launchBikeToLocation(destination: GeoPoint) {
    lastBikeDestination = destination
  }
}

private object NearbyNoOpRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit

  override fun launchWalkToLocation(destination: GeoPoint) = Unit
}

private object NearbyNoOpDatosBiziApi : DatosBiziApi {
  override suspend fun fetchPatterns(stationId: String): List<StationHourlyPattern> = emptyList()
}

private fun nearbyStation(
  id: String,
  distanceMeters: Int,
  bikes: Int = 4,
  slots: Int = 6,
): Station =
  Station(
    id = id,
    name = "Station $id",
    address = "Centro",
    location = GeoPoint(41.65, -0.88),
    bikesAvailable = bikes,
    slotsFree = slots,
    distanceMeters = distanceMeters,
  )
