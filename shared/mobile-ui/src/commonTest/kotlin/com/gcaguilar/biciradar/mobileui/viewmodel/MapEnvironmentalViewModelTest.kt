package com.gcaguilar.biciradar.mobileui.viewmodel

import app.cash.turbine.test
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.EnvironmentalReading
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import com.gcaguilar.biciradar.mobileui.MapFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
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
class MapEnvironmentalViewModelTest {
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
  fun `loads environmental snapshots from repository when a layer becomes active`() =
    runTest(dispatcher) {
      val settingsRepository = FakeMapEnvironmentalSettingsRepository()
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )

      viewModel.onStationsChanged(
        listOf(
          station("ne", 41.7, -0.8),
          station("nw", 41.7, -0.9),
          station("se", 41.6, -0.8),
          station("sw", 41.6, -0.9),
        ),
      )
      viewModel.onEnvironmentalLayerChanged(MapEnvironmentalLayer.AirQuality)

      viewModel.uiState.test {
        skipItems(1)
        advanceUntilIdle()
        val state = awaitItem()
        assertEquals(4, state.zones.size)
        assertEquals(listOf(10, 20, 30, 40), state.zones.map { it.airQualityScore })
        assertEquals(1, settingsRepository.bootstrapCalls)
        cancelAndIgnoreRemainingEvents()
      }
    }

  @Test
  fun `clears snapshots when environmental layer is removed`() =
    runTest(dispatcher) {
      val settingsRepository = FakeMapEnvironmentalSettingsRepository()
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )

      viewModel.onStationsChanged(listOf(station("only", 41.65, -0.88)))
      viewModel.onEnvironmentalLayerChanged(MapEnvironmentalLayer.Pollen)
      advanceUntilIdle()
      assertEquals(1, viewModel.uiState.value.zones.size)

      viewModel.onEnvironmentalLayerChanged(null)
      advanceUntilIdle()

      assertEquals(emptyList(), viewModel.uiState.value.zones)
    }

  @Test
  fun `reacts when stations arrive after the environmental layer is already active`() =
    runTest(dispatcher) {
      val settingsRepository = FakeMapEnvironmentalSettingsRepository()
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )

      viewModel.onEnvironmentalLayerChanged(MapEnvironmentalLayer.Pollen)
      advanceUntilIdle()
      assertEquals(emptyList(), viewModel.uiState.value.zones)

      viewModel.onStationsChanged(listOf(station("only", 41.65, -0.88)))
      advanceUntilIdle()

      assertEquals(1, viewModel.uiState.value.zones.size)
      assertEquals(
        listOf(5),
        viewModel.uiState.value.zones
          .map { it.pollenScore },
      )
    }

  @Test
  fun `loads persisted filters on bootstrap and persists when updated`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeMapEnvironmentalSettingsRepository(
          persistedFilterNames = linkedSetOf(MapFilter.ONLY_BIKES.name),
        )
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )

      advanceUntilIdle()
      assertEquals(setOf(MapFilter.ONLY_BIKES), viewModel.uiState.value.persistedActiveFilters)

      viewModel.onPersistedMapFiltersChanged(setOf(MapFilter.POLLEN))
      advanceUntilIdle()

      assertEquals(setOf(MapFilter.POLLEN), viewModel.uiState.value.persistedActiveFilters)
      assertEquals(
        listOf(setOf(MapFilter.POLLEN.name)),
        settingsRepository.persistedFilterWrites,
      )
    }

  @Test
  fun `onToggleFilter sanitizes and controls environmental sheet visibility`() =
    runTest(dispatcher) {
      val settingsRepository = FakeMapEnvironmentalSettingsRepository()
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )
      advanceUntilIdle()

      val availableFilters = setOf(MapFilter.ONLY_BIKES, MapFilter.POLLEN)
      viewModel.onToggleFilter(MapFilter.POLLEN, availableFilters)
      advanceUntilIdle()
      assertEquals(setOf(MapFilter.POLLEN), viewModel.uiState.value.persistedActiveFilters)
      assertTrue(viewModel.uiState.value.showEnvironmentalSheet)

      viewModel.onToggleFilter(MapFilter.POLLEN, availableFilters)
      advanceUntilIdle()
      assertEquals(emptySet(), viewModel.uiState.value.persistedActiveFilters)
      assertFalse(viewModel.uiState.value.showEnvironmentalSheet)

      assertEquals(
        listOf(
          setOf(MapFilter.POLLEN.name),
          emptySet<String>(),
        ),
        settingsRepository.persistedFilterWrites,
      )
    }

  @Test
  fun `onAvailableFiltersChanged sanitizes active filters and persists when changed`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeMapEnvironmentalSettingsRepository(
          persistedFilterNames = linkedSetOf(MapFilter.ONLY_BIKES.name, MapFilter.POLLEN.name),
        )
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )
      advanceUntilIdle()
      settingsRepository.persistedFilterWrites.clear()

      viewModel.onAvailableFiltersChanged(setOf(MapFilter.ONLY_BIKES))
      advanceUntilIdle()

      assertEquals(setOf(MapFilter.ONLY_BIKES), viewModel.uiState.value.persistedActiveFilters)
      assertEquals(
        listOf(setOf(MapFilter.ONLY_BIKES.name)),
        settingsRepository.persistedFilterWrites,
      )

      viewModel.onAvailableFiltersChanged(setOf(MapFilter.ONLY_BIKES))
      advanceUntilIdle()
      assertEquals(1, settingsRepository.persistedFilterWrites.size)
    }

  @Test
  fun `selection and sheet events update new ui fields`() =
    runTest(dispatcher) {
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = FakeMapEnvironmentalSettingsRepository(),
        )
      advanceUntilIdle()

      assertEquals(null, viewModel.uiState.value.selectedMapStationId)
      assertFalse(viewModel.uiState.value.hasExplicitMapSelection)
      assertFalse(viewModel.uiState.value.isCardDismissed)
      assertFalse(viewModel.uiState.value.showEnvironmentalSheet)
      assertEquals(0, viewModel.uiState.value.recenterRequestToken)

      viewModel.onStationSelected("s1")
      advanceUntilIdle()
      assertEquals("s1", viewModel.uiState.value.selectedMapStationId)
      assertTrue(viewModel.uiState.value.hasExplicitMapSelection)
      assertFalse(viewModel.uiState.value.isCardDismissed)

      viewModel.onStationCardDismissed()
      advanceUntilIdle()
      assertTrue(viewModel.uiState.value.isCardDismissed)

      viewModel.onRecenterRequested()
      advanceUntilIdle()
      assertEquals(1, viewModel.uiState.value.recenterRequestToken)
      assertFalse(viewModel.uiState.value.isCardDismissed)

      viewModel.onEnvironmentalSheetShown()
      advanceUntilIdle()
      assertTrue(viewModel.uiState.value.showEnvironmentalSheet)
      viewModel.onEnvironmentalSheetDismissed()
      advanceUntilIdle()
      assertFalse(viewModel.uiState.value.showEnvironmentalSheet)
    }

  @Test
  fun `clearing environmental filters removes environmental items hides sheet and persists`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeMapEnvironmentalSettingsRepository(
          persistedFilterNames = linkedSetOf(MapFilter.ONLY_BIKES.name, MapFilter.POLLEN.name),
        )
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = settingsRepository,
        )
      advanceUntilIdle()
      viewModel.onEnvironmentalSheetShown()
      settingsRepository.persistedFilterWrites.clear()

      viewModel.onClearEnvironmentalFilters()
      advanceUntilIdle()

      assertEquals(setOf(MapFilter.ONLY_BIKES), viewModel.uiState.value.persistedActiveFilters)
      assertFalse(viewModel.uiState.value.showEnvironmentalSheet)
      assertEquals(
        listOf(setOf(MapFilter.ONLY_BIKES.name)),
        settingsRepository.persistedFilterWrites,
      )
    }

  @Test
  fun `reconcileSelection falls back to nearest and handles search overrides`() =
    runTest(dispatcher) {
      val viewModel =
        MapEnvironmentalViewModel(
          environmentalRepository = FakeEnvironmentalRepository(),
          settingsRepository = FakeMapEnvironmentalSettingsRepository(),
        )
      val a = station("a", 41.7, -0.8)
      val b = station("b", 41.6, -0.9)
      val c = station("c", 41.65, -0.88)

      viewModel.onStationSelected("c")
      viewModel.onStationCardDismissed()
      advanceUntilIdle()
      assertTrue(viewModel.uiState.value.hasExplicitMapSelection)
      assertTrue(viewModel.uiState.value.isCardDismissed)

      viewModel.reconcileSelection(
        mapStations = listOf(a, b),
        nearestSelection =
          NearbyStationSelection(
            withinRadiusStation = b,
            fallbackStation = a,
            radiusMeters = 500,
          ),
        searchQuery = "",
      )
      advanceUntilIdle()
      assertEquals("b", viewModel.uiState.value.selectedMapStationId)
      assertFalse(viewModel.uiState.value.hasExplicitMapSelection)
      assertFalse(viewModel.uiState.value.isCardDismissed)

      viewModel.reconcileSelection(
        mapStations = listOf(a, b),
        nearestSelection =
          NearbyStationSelection(
            withinRadiusStation = b,
            fallbackStation = a,
            radiusMeters = 500,
          ),
        searchQuery = "centro",
      )
      advanceUntilIdle()
      assertEquals("a", viewModel.uiState.value.selectedMapStationId)
      assertFalse(viewModel.uiState.value.hasExplicitMapSelection)
    }
}

private class FakeEnvironmentalRepository : EnvironmentalRepository {
  override suspend fun readingAt(
    latitude: Double,
    longitude: Double,
  ): EnvironmentalReading? {
    val key = "$latitude,$longitude"
    return when (key) {
      "41.7,-0.8" -> EnvironmentalReading(airQualityIndex = 10, pollenIndex = 1)
      "41.7,-0.9" -> EnvironmentalReading(airQualityIndex = 20, pollenIndex = 2)
      "41.6,-0.8" -> EnvironmentalReading(airQualityIndex = 30, pollenIndex = 3)
      "41.6,-0.9" -> EnvironmentalReading(airQualityIndex = 40, pollenIndex = 4)
      "41.65,-0.88" -> EnvironmentalReading(airQualityIndex = 50, pollenIndex = 5)
      else -> null
    }
  }
}

private fun station(
  id: String,
  latitude: Double,
  longitude: Double,
) = Station(
  id = id,
  name = id,
  address = "Centro",
  location = GeoPoint(latitude, longitude),
  bikesAvailable = 5,
  slotsFree = 5,
  distanceMeters = 100,
)

private class FakeMapEnvironmentalSettingsRepository(
  persistedFilterNames: Set<String> = emptySet(),
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

  var bootstrapCalls: Int = 0
  var storedPersistedFilterNames: Set<String> = persistedFilterNames
  val persistedFilterWrites = mutableListOf<Set<String>>()

  override suspend fun bootstrap() {
    bootstrapCalls++
  }

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

  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit

  override suspend fun persistedMapFilterNames(): Set<String> = storedPersistedFilterNames

  override suspend fun setPersistedMapFilterNames(names: Set<String>) {
    storedPersistedFilterNames = names
    persistedFilterWrites += names
  }
}
