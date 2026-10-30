package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AutocompleteResult
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.ObserveFavorites
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
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
import com.gcaguilar.biciradar.core.TripMonitoringState
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.TripState
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.biciradar.mobileui.usecases.GeoLocationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceMonitoringUseCase
import com.gcaguilar.biciradar.mobileui.usecases.TripManagementUseCase
import com.gcaguilar.biciradar.testutils.FakeFavoritesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelTest {
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
  fun `query search updates suggestions`() =
    runTest(dispatcher) {
      val geoApi =
        TripTestGeoApi(
          searchResults =
            mapOf(
              "plaza españa" to
                listOf(
                  GeoResult(
                    id = "geo-1",
                    name = "Plaza de Espana",
                    address = "Centro, Zaragoza",
                    latitude = 41.65,
                    longitude = -0.88,
                  ),
                ),
            ),
        )
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = TripTestTripRepository(),
          geoApi = geoApi,
        )

      viewModel.onQueryChange("plaza españa")
      advanceTimeBy(500)
      advanceUntilIdle()

      assertTrue(geoApi.searchCalls.isNotEmpty())
      assertTrue(
        viewModel.uiState.value.suggestions
          .isNotEmpty(),
      )
      assertEquals(null, viewModel.uiState.value.suggestionsError)
      assertTrue(
        viewModel.uiState.value.isLoadingSuggestions
          .not(),
      )
    }

  @Test
  fun `favorite stations are offered as destination shortcuts ordered by distance`() =
    runTest(dispatcher) {
      val near =
        Station(
          id = "fav-near",
          name = "Plaza España",
          address = "Centro",
          location = GeoPoint(41.6488, -0.8891),
          bikesAvailable = 3,
          slotsFree = 4,
          distanceMeters = 100,
        )
      val far =
        Station(
          id = "fav-far",
          name = "Estación Delicias",
          address = "Delicias",
          location = GeoPoint(41.6585, -0.9077),
          bikesAvailable = 2,
          slotsFree = 6,
          distanceMeters = 900,
        )
      val other =
        Station(
          id = "not-favorite",
          name = "Puerta del Carmen",
          address = "Centro",
          location = GeoPoint(41.6499, -0.8872),
          bikesAvailable = 5,
          slotsFree = 5,
          distanceMeters = 50,
        )
      val favoritesRepository =
        FakeFavoritesRepository().apply {
          favoriteIds.value = setOf(far.id, near.id)
        }
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = TripTestTripRepository(),
          geoApi = TripTestGeoApi(),
          favoritesRepository = favoritesRepository,
          stationsRepository = TripTestStationsRepository(listOf(far, other, near)),
        )

      advanceUntilIdle()

      val favoriteStations = viewModel.uiState.value.favoriteStations
      assertEquals(listOf(near.id, far.id), favoriteStations.map { it.id })

      viewModel.onFavoriteDestinationSelected(near)
      advanceUntilIdle()

      val destination = viewModel.uiState.value.destination
      assertEquals(near.name, destination?.name)
      assertEquals(near.location, destination?.location)
    }

  @Test
  fun `valid persisted duration is restored and selection persists new value`() =
    runTest(dispatcher) {
      val tripRepository = TripTestTripRepository()
      val settingsRepository =
        TripTestSettingsRepository(preferredMonitoringDurationSeconds = MONITORING_DURATION_OPTIONS_SECONDS[2])
      val viewModel =
        buildTripViewModel(
          settingsRepository = settingsRepository,
          tripRepository = tripRepository,
          geoApi = TripTestGeoApi(),
        )

      advanceUntilIdle()
      assertEquals(MONITORING_DURATION_OPTIONS_SECONDS[2], viewModel.uiState.value.selectedDurationSeconds)

      val selected = MONITORING_DURATION_OPTIONS_SECONDS[1]
      viewModel.onDurationSelected(selected)
      advanceUntilIdle()

      assertEquals(selected, viewModel.uiState.value.selectedDurationSeconds)
      assertEquals(selected, settingsRepository.lastPersistedMonitoringDuration)
    }

  @Test
  fun `map picked destination requires explicit confirmation`() =
    runTest(dispatcher) {
      val location = GeoPoint(41.65, -0.88)
      val tripRepository = TripTestTripRepository()
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = tripRepository,
          geoApi =
            TripTestGeoApi(
              reverseGeocodeResults =
                mapOf(
                  location to
                    GeoResult(
                      id = "geo-destination",
                      name = "Plaza de Espana",
                      address = "Centro, Zaragoza",
                      latitude = location.latitude,
                      longitude = location.longitude,
                    ),
                ),
            ),
        )

      viewModel.onEnterMapPicker(TripMapPickerMode.Destination)
      viewModel.onLocationPicked(location)
      advanceUntilIdle()

      assertEquals(0, tripRepository.setDestinationCalls)
      assertTrue(viewModel.uiState.value.canConfirmMapSelection)
      assertEquals("Plaza de Espana", viewModel.uiState.value.selectedMapLocationLabel)

      viewModel.onConfirmMapSelection()
      advanceUntilIdle()

      assertEquals(1, tripRepository.setDestinationCalls)
      assertEquals("Plaza de Espana", tripRepository.lastDestination?.name)
      assertEquals(location, tripRepository.lastDestination?.location)
      assertEquals(null, viewModel.uiState.value.mapPickerMode)
      assertFalse(viewModel.uiState.value.canConfirmMapSelection)
    }

  @Test
  fun `confirmed station selection preserves destination and calls select station`() =
    runTest(dispatcher) {
      val tripRepository =
        TripTestTripRepository().apply {
          seedDestination(
            TripDestination(
              name = "Destino actual",
              location = GeoPoint(41.65, -0.88),
            ),
          )
        }
      val station =
        Station(
          id = "station-1",
          name = "Plaza Espana",
          address = "Centro, Zaragoza",
          location = GeoPoint(41.649, -0.887),
          bikesAvailable = 4,
          slotsFree = 7,
          distanceMeters = 120,
        )
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = tripRepository,
          geoApi = TripTestGeoApi(),
        )

      viewModel.onEnterMapPicker(TripMapPickerMode.Station)
      viewModel.onStationPickedFromMap(station)
      viewModel.onConfirmMapSelection()
      advanceUntilIdle()

      assertEquals(listOf(station), tripRepository.selectedStations)
      assertEquals(
        "Destino actual",
        tripRepository.state.value.destination
          ?.name,
      )
      assertEquals(0, tripRepository.setDestinationCalls)
      assertEquals(station, tripRepository.state.value.nearestStationWithSlots)
    }

  @Test
  fun `start monitoring is blocked and asks to sign in when anonymous`() =
    runTest(dispatcher) {
      val tripRepository = TripTestTripRepository().apply { seedStation(testStation()) }
      val notifier = TripTestLocalNotifier()
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = tripRepository,
          geoApi = TripTestGeoApi(),
          authProvider = TripTestAuthProvider(AuthState.Anonymous, loginResult = false),
          localNotifier = notifier,
        )

      viewModel.onStartMonitoringRequested()
      advanceUntilIdle()

      assertEquals(0, tripRepository.startMonitoringCalls)
      assertEquals(0, notifier.permissionRequests)
      assertTrue(viewModel.uiState.value.authLoginError)
    }

  @Test
  fun `start monitoring requests permission and starts when authenticated`() =
    runTest(dispatcher) {
      val tripRepository = TripTestTripRepository().apply { seedStation(testStation()) }
      val notifier = TripTestLocalNotifier()
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = tripRepository,
          geoApi = TripTestGeoApi(),
          authProvider = TripTestAuthProvider(AuthState.Authenticated),
          localNotifier = notifier,
        )

      viewModel.onStartMonitoringRequested()
      advanceUntilIdle()

      assertEquals(1, notifier.permissionRequests)
      assertEquals(1, tripRepository.startMonitoringCalls)
    }

  @Test
  fun `successful login updates auth state and clears error`() =
    runTest(dispatcher) {
      val viewModel =
        buildTripViewModel(
          settingsRepository = TripTestSettingsRepository(),
          tripRepository = TripTestTripRepository(),
          geoApi = TripTestGeoApi(),
          authProvider = TripTestAuthProvider(AuthState.Anonymous, loginResult = true),
        )

      viewModel.onLogin()
      advanceUntilIdle()

      assertEquals(AuthState.Authenticated, viewModel.uiState.value.authState)
      assertFalse(viewModel.uiState.value.authLoginInProgress)
      assertFalse(viewModel.uiState.value.authLoginError)
    }
}

private fun testStation(): Station =
  Station(
    id = "station-1",
    name = "Plaza Espana",
    address = "Centro, Zaragoza",
    location = GeoPoint(41.649, -0.887),
    bikesAvailable = 4,
    slotsFree = 7,
    distanceMeters = 120,
  )

private fun buildTripViewModel(
  settingsRepository: TripTestSettingsRepository,
  tripRepository: TripTestTripRepository,
  geoApi: TripTestGeoApi,
  authProvider: AuthProvider = TripTestAuthProvider(AuthState.Authenticated),
  localNotifier: TripTestLocalNotifier = TripTestLocalNotifier(),
  favoritesRepository: FakeFavoritesRepository = FakeFavoritesRepository(),
  stationsRepository: TripTestStationsRepository = TripTestStationsRepository(),
): TripViewModel {
  val tripManagementUseCase =
    TripManagementUseCase(
      tripRepository = tripRepository,
      settingsRepository = settingsRepository,
      authProvider = authProvider,
    )
  val surfaceMonitoringUseCase =
    SurfaceMonitoringUseCase(
      surfaceMonitoringRepository = TripTestSurfaceMonitoringRepository(),
    )
  val geoLocationUseCase =
    GeoLocationUseCase(
      geoSearchUseCase =
        GeoSearchUseCase(
          geoApi = geoApi,
          googlePlacesApi = TripTestGooglePlacesApi(),
          googleMapsApiKey = null,
        ),
      reverseGeocodeUseCase =
        ReverseGeocodeUseCase(
          geoApi = geoApi,
          googlePlacesApi = TripTestGooglePlacesApi(),
          googleMapsApiKey = null,
        ),
    )
  return TripViewModel(
    tripManagementUseCase = tripManagementUseCase,
    surfaceMonitoringUseCase = surfaceMonitoringUseCase,
    geoLocationUseCase = geoLocationUseCase,
    stationsRepository = stationsRepository,
    observeFavorites = ObserveFavorites(favoritesRepository),
    localNotifier = localNotifier,
    routeLauncher = TripTestRouteLauncher(),
  )
}

private class TripTestTripRepository : TripRepository {
  override val state =
    MutableStateFlow(
      TripState(
        monitoring = TripMonitoringState(isActive = false),
      ),
    )
  var setDestinationCalls = 0
  var lastDestination: TripDestination? = null
  val selectedStations = mutableListOf<Station>()
  var startMonitoringCalls = 0

  override suspend fun setDestination(
    destination: TripDestination,
    searchRadiusMeters: Int,
  ) {
    setDestinationCalls++
    lastDestination = destination
    state.value = state.value.copy(destination = destination)
  }

  override suspend fun selectStation(station: Station) {
    selectedStations += station
    state.value = state.value.copy(nearestStationWithSlots = station)
  }

  override suspend fun startMonitoring(durationSeconds: Int) {
    startMonitoringCalls++
  }

  override fun stopMonitoring() = Unit

  override fun clearTrip() = Unit

  override fun dismissAlert() = Unit

  override suspend fun doFinalBackgroundCheck() = Unit

  fun seedDestination(destination: TripDestination) {
    state.value = state.value.copy(destination = destination)
  }

  fun seedStation(station: Station) {
    state.value = state.value.copy(nearestStationWithSlots = station)
  }
}

private class TripTestSettingsRepository(
  preferredMonitoringDurationSeconds: Int? = null,
) : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(true)
  override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
  override val engagementSnapshot = MutableStateFlow(EngagementSnapshot())
  private val preferredDuration = MutableStateFlow(preferredMonitoringDurationSeconds)
  var lastPersistedMonitoringDuration: Int? = null

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

  override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit

  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit

  override suspend fun updateOnboardingChecklist(
    transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot,
  ) = Unit

  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit

  override suspend fun preferredMonitoringDurationSeconds(): Int? = preferredDuration.value

  override suspend fun setPreferredMonitoringDurationSeconds(durationSeconds: Int?) {
    preferredDuration.value = durationSeconds
    lastPersistedMonitoringDuration = durationSeconds
  }

  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class TripTestSurfaceMonitoringRepository : SurfaceMonitoringRepository {
  override val state = MutableStateFlow<Map<String, SurfaceMonitoringSession>>(emptyMap())

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

  override fun stopMonitoring(stationId: String?) = Unit

  override suspend fun clearMonitoring() = Unit

  override fun sessionFor(stationId: String): SurfaceMonitoringSession? = state.value[stationId]

  override fun activeSessions(): List<SurfaceMonitoringSession> = state.value.values.filter { it.isActive }
}

private class TripTestGeoApi(
  private val searchResults: Map<String, List<GeoResult>> = emptyMap(),
  private val reverseGeocodeResults: Map<GeoPoint, GeoResult> = emptyMap(),
) : GeoApi {
  val searchCalls = mutableListOf<String>()

  override suspend fun search(query: String): List<GeoResult> {
    searchCalls += query
    return searchResults[query] ?: searchResults.values.firstOrNull().orEmpty()
  }

  override suspend fun reverseGeocode(location: GeoPoint): GeoResult? = reverseGeocodeResults[location]
}

private class TripTestGooglePlacesApi : GooglePlacesApi {
  override suspend fun autocomplete(
    query: String,
    biasLocation: GeoPoint?,
    apiKey: String,
  ): List<PlacePrediction> = emptyList()

  override suspend fun placeDetails(
    placeId: String,
    apiKey: String,
  ): PlaceDetails? = null

  override suspend fun reverseGeocode(
    location: GeoPoint,
    apiKey: String,
  ): String? = null

  override suspend fun autocompleteWithStatus(
    query: String,
    biasLocation: GeoPoint?,
    apiKey: String,
  ): AutocompleteResult = AutocompleteResult(predictions = emptyList(), status = "OK")
}

private class TripTestStationsRepository(
  stations: List<Station> = emptyList(),
) : StationsRepository {
  override val state = MutableStateFlow(StationsState(stations = stations))

  override suspend fun loadIfNeeded() = Unit

  override suspend fun forceRefresh() = Unit

  override suspend fun refreshAvailability(stationIds: List<String>) = Unit

  override fun stationById(stationId: String): Station? = null
}

private class TripTestLocalNotifier : LocalNotifier {
  var permissionRequests = 0

  override suspend fun requestPermission(): Boolean {
    permissionRequests++
    return true
  }

  override suspend fun notify(
    title: String,
    body: String,
  ) = Unit
}

private class TripTestAuthProvider(
  initialState: AuthState,
  private val loginResult: Boolean = initialState == AuthState.Authenticated,
) : AuthProvider {
  override val credentialsState = MutableStateFlow(initialState)

  override suspend fun login(): Boolean {
    if (loginResult) credentialsState.value = AuthState.Authenticated
    return loginResult
  }

  override suspend fun logout() {
    credentialsState.value = AuthState.Anonymous
  }

  override suspend fun accessToken(): String? = null
}

private class TripTestRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit

  override fun launchWalkToLocation(destination: GeoPoint) = Unit
}
