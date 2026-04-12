package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AutocompleteResult
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
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
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.biciradar.mobileui.usecases.GeoLocationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceMonitoringUseCase
import com.gcaguilar.biciradar.mobileui.usecases.TripManagementUseCase
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
}

private fun buildTripViewModel(
  settingsRepository: TripTestSettingsRepository,
  tripRepository: TripTestTripRepository,
  geoApi: TripTestGeoApi,
): TripViewModel {
  val tripManagementUseCase =
    TripManagementUseCase(
      tripRepository = tripRepository,
      settingsRepository = settingsRepository,
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
    stationsRepository = TripTestStationsRepository(),
    localNotifier = TripTestLocalNotifier(),
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

  override suspend fun startMonitoring(durationSeconds: Int) = Unit

  override fun stopMonitoring() = Unit

  override fun clearTrip() = Unit

  override fun dismissAlert() = Unit

  override suspend fun doFinalBackgroundCheck() = Unit

  fun seedDestination(destination: TripDestination) {
    state.value = state.value.copy(destination = destination)
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
  override val state = MutableStateFlow<SurfaceMonitoringSession?>(null)

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

private class TripTestStationsRepository : StationsRepository {
  override val state = MutableStateFlow(StationsState())
  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() = Unit
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = null
}

private class TripTestLocalNotifier : LocalNotifier {
  override suspend fun requestPermission(): Boolean = true
  override suspend fun notify(title: String, body: String) = Unit
}

private class TripTestRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit
  override fun launchWalkToLocation(destination: GeoPoint) = Unit
}
