package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.usecases.FavoritesManagementUseCase
import com.gcaguilar.biciradar.mobileui.usecases.RouteLaunchUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SavedPlaceAlertsUseCase
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

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
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
  fun `assignment candidate uses ranked station matching instead of raw name contains`() = runTest(dispatcher) {
    val stationsRepository = FakeFavoriteStationsRepository(
        listOf(
          Station(
            id = "station-1",
            name = "Universidad",
            address = "Plaza San Francisco",
            location = GeoPoint(41.64, -0.89),
            bikesAvailable = 4,
            slotsFree = 8,
            distanceMeters = 200,
          ),
        ),
    )
    val favoritesRepository = FakeFavoritesRepo()
    val settingsRepository = FakeFavoriteSettingsRepository()
    val savedPlaceAlertsRepository = FakeFavoriteAlertsRepository()
    val viewModel = FavoritesViewModel(
      favoritesManagementUseCase = FavoritesManagementUseCase(
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
        settingsRepository = settingsRepository,
      ),
      savedPlaceAlertsUseCase = SavedPlaceAlertsUseCase(
        savedPlaceAlertsRepository = savedPlaceAlertsRepository,
      ),
      routeLaunchUseCase = RouteLaunchUseCase(
        routeLauncher = NoOpFavoriteRouteLauncher,
      ),
    )

    viewModel.onSearchQueryChange("san francisco")
    advanceUntilIdle()

    assertEquals("station-1", viewModel.uiState.value.assignmentCandidate?.id)
  }

  @Test
  fun `ui state follows repository updates without manual publish`() = runTest(dispatcher) {
    val favoritesRepository = FakeFavoritesRepo()
    val stationsRepository = FakeFavoriteStationsRepository(
      listOf(
        Station(
          id = "station-1",
          name = "Universidad",
          address = "Plaza San Francisco",
          location = GeoPoint(41.64, -0.89),
          bikesAvailable = 4,
          slotsFree = 8,
          distanceMeters = 200,
        ),
      ),
    )
    val settingsRepository = FakeFavoriteSettingsRepository()
    val savedPlaceAlertsRepository = FakeFavoriteAlertsRepository()
    val viewModel = FavoritesViewModel(
      favoritesManagementUseCase = FavoritesManagementUseCase(
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
        settingsRepository = settingsRepository,
      ),
      savedPlaceAlertsUseCase = SavedPlaceAlertsUseCase(
        savedPlaceAlertsRepository = savedPlaceAlertsRepository,
      ),
      routeLaunchUseCase = RouteLaunchUseCase(
        routeLauncher = NoOpFavoriteRouteLauncher,
      ),
    )

    advanceUntilIdle()
    assertEquals(City.ZARAGOZA.id, viewModel.uiState.value.savedPlaceAlertsCityId)
    assertEquals(emptyList<Station>(), viewModel.uiState.value.favoriteStations)

    favoritesRepository.favoriteIds.value = setOf("station-1")
    settingsRepository.selectedCity.value = City.MADRID
    savedPlaceAlertsRepository.rules.value = listOf(
      SavedPlaceAlertRule(
        id = "rule-1",
        target = SavedPlaceAlertTarget.Home(
          stationId = "station-1",
          cityId = City.ZARAGOZA.id,
        ),
        condition = SavedPlaceAlertCondition.BikesAtLeast(1),
        isEnabled = true,
      ),
    )
    advanceUntilIdle()

    assertEquals("station-1", viewModel.uiState.value.favoriteStations.single().id)
    assertEquals(null, viewModel.uiState.value.homeStation)
    assertEquals(City.MADRID.id, viewModel.uiState.value.savedPlaceAlertsCityId)
    assertEquals(1, viewModel.uiState.value.savedPlaceAlertRules.size)
  }
}

private class FakeFavoritesRepo : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  override suspend fun bootstrap() = Unit
  override suspend fun syncFromPeer() = Unit
  override suspend fun toggle(stationId: String) = Unit
  override suspend fun setHomeStationId(stationId: String?) = Unit
  override suspend fun setWorkStationId(stationId: String?) = Unit
  override suspend fun clearAll() = Unit
  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value
  override fun currentHomeStationId(): String? = homeStationId.value
  override fun currentWorkStationId(): String? = workStationId.value
}

private class FakeFavoriteStationsRepository(stations: List<Station>) : StationsRepository {
  override val state = MutableStateFlow(StationsState(stations = stations))
  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() = Unit
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class FakeFavoriteSettingsRepository : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(true)
  override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
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
  override suspend fun setSelectedCity(city: City) = Unit
  override suspend fun setHasCompletedOnboarding(completed: Boolean) = Unit
  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) = Unit
  override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) = Unit
  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class FakeFavoriteAlertsRepository : SavedPlaceAlertsRepository {
  override val rules = MutableStateFlow<List<SavedPlaceAlertRule>>(emptyList())
  override suspend fun bootstrap() = Unit
  override fun currentRules(): List<SavedPlaceAlertRule> = emptyList()
  override fun ruleForTarget(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? = null
  override suspend fun upsertRule(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition, enabled: Boolean) = Unit
  override suspend fun removeRule(ruleId: String) = Unit
  override suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget) = Unit
  override suspend fun setRuleEnabled(ruleId: String, enabled: Boolean) = Unit
  override suspend fun replaceAll(rules: List<SavedPlaceAlertRule>) = Unit
}

private object NoOpFavoriteRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit
  override fun launchWalkToLocation(destination: GeoPoint) = Unit
}
