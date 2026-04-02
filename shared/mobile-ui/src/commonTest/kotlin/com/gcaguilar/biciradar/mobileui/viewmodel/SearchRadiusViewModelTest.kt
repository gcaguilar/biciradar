package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AutocompleteResult
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.DEFAULT_SEARCH_RADIUS_METERS
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PlaceDetails
import com.gcaguilar.biciradar.core.PlacePrediction
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.TripDestination
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.TripState
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SearchRadiusViewModelTest {
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
  fun `nearby view model reflects search radius updates from settings`() = runTest(dispatcher) {
    val settingsRepository = FakeSettingsRepository(searchRadius = 300)
    val viewModel = NearbyViewModel(
      stationsRepository = FakeStationsRepository(
        StationsState(stations = listOf(station(distanceMeters = 650))),
      ),
      favoritesRepository = FakeFavoritesRepository(),
      settingsRepository = settingsRepository,
      routeLauncher = NoOpRouteLauncher,
    )

    advanceUntilIdle()
    assertEquals(300, viewModel.uiState.value.searchRadiusMeters)
    assertEquals(300, viewModel.uiState.value.nearestSelection.radiusMeters)

    settingsRepository.searchRadiusMeters.value = 1500

    advanceUntilIdle()
    assertEquals(1500, viewModel.uiState.value.searchRadiusMeters)
    assertEquals(1500, viewModel.uiState.value.nearestSelection.radiusMeters)
  }

  @Test
  fun `trip view model uses latest settings radius when selecting a suggestion`() = runTest(dispatcher) {
    val settingsRepository = FakeSettingsRepository(searchRadius = 300)
    val tripRepository = FakeTripRepository()
    val viewModel = TripViewModel(
      tripRepository = tripRepository,
      surfaceMonitoringRepository = FakeSurfaceMonitoringRepository(),
      geoSearchUseCase = GeoSearchUseCase(
        geoApi = FakeGeoApi(),
        googlePlacesApi = FakeGooglePlacesApi(),
        googleMapsApiKey = null,
      ),
      reverseGeocodeUseCase = ReverseGeocodeUseCase(
        geoApi = FakeGeoApi(),
        googlePlacesApi = FakeGooglePlacesApi(),
        googleMapsApiKey = null,
      ),
      settingsRepository = settingsRepository,
    )

    advanceUntilIdle()
    settingsRepository.searchRadiusMeters.value = 2000
    advanceUntilIdle()

    viewModel.onSuggestionSelected(
      GeoResult(
        id = "dest-1",
        name = "Destino",
        address = "Centro, Zaragoza",
        latitude = 41.65,
        longitude = -0.88,
      ),
    )

    advanceUntilIdle()
    assertEquals(2000, tripRepository.lastSearchRadiusMeters)
    assertEquals("Destino, Centro, Zaragoza", tripRepository.lastDestination?.name)
  }

  @Test
  fun `profile view model shows setup card when onboarding is complete but setup requirements are missing`() = runTest(dispatcher) {
    val settingsRepository = FakeSettingsRepository()
    val favoritesRepository = FakeFavoritesRepository().apply {
      favoriteIds.value = setOf("home")
      homeStationId.value = "home"
      workStationId.value = "work"
    }
    val stationsRepository = FakeStationsRepository()
    val viewModel = ProfileViewModel(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = settingsRepository,
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
      ),
      permissionPrompter = FakePermissionPrompter(hasLocationPermission = false),
      localNotifier = FakeLocalNotifier(hasPermission = false),
    )

    advanceUntilIdle()
    assertEquals(true, viewModel.uiState.value.showProfileSetupCard)
  }

  @Test
  fun `profile view model clears favorites and refreshes stations when changing city`() = runTest(dispatcher) {
    val settingsRepository = FakeSettingsRepository(city = City.ZARAGOZA)
    val favoritesRepository = FakeFavoritesRepository().apply {
      favoriteIds.value = setOf("home")
    }
    val stationsRepository = FakeStationsRepository()
    val viewModel = ProfileViewModel(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = settingsRepository,
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
      ),
      permissionPrompter = FakePermissionPrompter(hasLocationPermission = true),
      localNotifier = FakeLocalNotifier(hasPermission = true),
    )

    advanceUntilIdle()
    viewModel.onCitySelected(City.MADRID)
    advanceUntilIdle()

    assertEquals(City.MADRID, settingsRepository.selectedCity.value)
    assertEquals(1, favoritesRepository.clearAllCalls)
    assertEquals(1, stationsRepository.forceRefreshCalls)
  }
}

private class FakeSettingsRepository(
  searchRadius: Int = DEFAULT_SEARCH_RADIUS_METERS,
  city: City = City.ZARAGOZA,
) : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(searchRadius)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(city)
  override val hasCompletedOnboarding = MutableStateFlow(true)
  override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
  override val engagementSnapshot = MutableStateFlow(EngagementSnapshot())

  override suspend fun bootstrap() = Unit
  override fun currentSearchRadiusMeters(): Int = searchRadiusMeters.value
  override fun currentPreferredMapApp(): PreferredMapApp = preferredMapApp.value
  override fun currentSelectedCity(): City = selectedCity.value
  override fun currentLastSeenChangelogAppVersion(): String? = lastSeenChangelogAppVersion.value
  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) {
    this.searchRadiusMeters.value = searchRadiusMeters
  }
  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) {
    this.preferredMapApp.value = preferredMapApp
  }
  override suspend fun setLastSeenChangelogVersion(version: Int) = Unit
  override suspend fun setLastSeenChangelogAppVersion(version: String?) {
    lastSeenChangelogAppVersion.value = version
  }
  override suspend fun setThemePreference(preference: ThemePreference) {
    themePreference.value = preference
  }
  override suspend fun setSelectedCity(city: City) {
    selectedCity.value = city
  }
  override suspend fun setHasCompletedOnboarding(completed: Boolean) {
    hasCompletedOnboarding.value = completed
  }
  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) {
    onboardingChecklist.value = snapshot
  }
  override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    onboardingChecklist.value = transform(onboardingChecklist.value)
  }
  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) {
    engagementSnapshot.value = snapshot
  }
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class FakeStationsRepository(
  initialState: StationsState = StationsState(),
) : StationsRepository {
  override val state: MutableStateFlow<StationsState> = MutableStateFlow(initialState)
  var forceRefreshCalls: Int = 0
  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() {
    forceRefreshCalls++
  }
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class FakeFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  var clearAllCalls: Int = 0

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
    clearAllCalls++
  }
  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value
  override fun currentHomeStationId(): String? = homeStationId.value
  override fun currentWorkStationId(): String? = workStationId.value
}

private object NoOpRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit
  override fun launchWalkToLocation(destination: GeoPoint) = Unit
}

private class FakeTripRepository : TripRepository {
  override val state: StateFlow<TripState> = MutableStateFlow(TripState())
  var lastDestination: TripDestination? = null
  var lastSearchRadiusMeters: Int? = null

  override suspend fun setDestination(destination: TripDestination, searchRadiusMeters: Int) {
    lastDestination = destination
    lastSearchRadiusMeters = searchRadiusMeters
  }

  override suspend fun startMonitoring(durationSeconds: Int) = Unit
  override fun stopMonitoring() = Unit
  override fun clearTrip() = Unit
  override fun dismissAlert() = Unit
  override suspend fun doFinalBackgroundCheck() = Unit
}

private class FakeSurfaceMonitoringRepository : SurfaceMonitoringRepository {
  override val state: StateFlow<SurfaceMonitoringSession?> = MutableStateFlow(null)
  override suspend fun bootstrap() = Unit
  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean = true

  override suspend fun startMonitoringFavoriteStation(
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean = true

  override fun stopMonitoring() = Unit
  override suspend fun clearMonitoring() = Unit
}

private class FakeGeoApi : GeoApi {
  override suspend fun search(query: String): List<GeoResult> = emptyList()
  override suspend fun reverseGeocode(location: GeoPoint): GeoResult? = null
}

private class FakeGooglePlacesApi : GooglePlacesApi {
  override suspend fun autocomplete(query: String, biasLocation: GeoPoint?, apiKey: String): List<PlacePrediction> = emptyList()
  override suspend fun placeDetails(placeId: String, apiKey: String): PlaceDetails? = null
  override suspend fun reverseGeocode(location: GeoPoint, apiKey: String): String? = null
  override suspend fun autocompleteWithStatus(query: String, biasLocation: GeoPoint?, apiKey: String): AutocompleteResult =
    AutocompleteResult(predictions = emptyList(), status = "OK")
}

private class FakePermissionPrompter(
  private val hasLocationPermission: Boolean,
) : PermissionPrompter {
  override suspend fun hasLocationPermission(): Boolean = hasLocationPermission
  override suspend fun requestLocationPermission(): Boolean = hasLocationPermission
}

private class FakeLocalNotifier(
  private val hasPermission: Boolean,
) : LocalNotifier {
  override suspend fun hasPermission(): Boolean = hasPermission
  override suspend fun requestPermission(): Boolean = hasPermission
  override suspend fun notify(title: String, body: String) = Unit
}

private fun station(distanceMeters: Int) = Station(
  id = "station-$distanceMeters",
  name = "Station $distanceMeters",
  address = "Centro",
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = 4,
  slotsFree = 6,
  distanceMeters = distanceMeters,
)
