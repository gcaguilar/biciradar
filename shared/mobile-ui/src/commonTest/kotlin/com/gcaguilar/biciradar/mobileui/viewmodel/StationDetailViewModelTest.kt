package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DatosBiziApi
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
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.usecases.StationDetailUseCase
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
class StationDetailViewModelTest {
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
  fun `ui state derives from repository flows and pattern refresh`() = runTest(dispatcher) {
    val favoritesRepository = FakeStationDetailFavoritesRepository()
    val settingsRepository = FakeStationDetailSettingsRepository()
    val savedPlaceAlertsRepository = FakeStationDetailAlertsRepository()
    val datosBiziApi = FakeStationDetailDatosBiziApi(
      patterns = listOf(
        StationHourlyPattern(
          stationId = "station-1",
          dayType = "weekday",
          hour = 9,
          bikesAvg = 3.0,
          anchorsAvg = 5.0,
          occupancyAvg = 0.42,
          sampleCount = 12,
        ),
      ),
    )
    val stationDetailUseCase = StationDetailUseCase(
      favoritesRepository = favoritesRepository,
      settingsRepository = settingsRepository,
      savedPlaceAlertsRepository = savedPlaceAlertsRepository,
      datosBiziApi = datosBiziApi,
      routeLauncher = NoOpStationDetailRouteLauncher,
    )

    val viewModel = StationDetailViewModel(
      stationId = "station-1",
      stationDetailUseCase = stationDetailUseCase,
    )

    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.isFavorite)
    assertTrue(viewModel.uiState.value.isHomeStation)
    assertFalse(viewModel.uiState.value.isWorkStation)
    assertTrue(viewModel.uiState.value.supportsUsagePatterns)
    assertEquals(City.ZARAGOZA.id, viewModel.uiState.value.savedPlaceAlertsCityId)
    assertEquals(1, viewModel.uiState.value.patterns.size)
    assertFalse(viewModel.uiState.value.patternsLoading)
    assertFalse(viewModel.uiState.value.patternsError)

    favoritesRepository.favoriteIds.value = emptySet()
    favoritesRepository.homeStationId.value = null
    favoritesRepository.workStationId.value = "station-1"
    settingsRepository.selectedCity.value = City.MADRID
    savedPlaceAlertsRepository.rules.value = listOf(
      SavedPlaceAlertRule(
        id = "rule-1",
        target = SavedPlaceAlertTarget.Work(
          stationId = "station-1",
          cityId = City.MADRID.id,
        ),
        condition = SavedPlaceAlertCondition.BikesAtLeast(2),
        isEnabled = true,
      ),
    )
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isFavorite)
    assertFalse(viewModel.uiState.value.isHomeStation)
    assertTrue(viewModel.uiState.value.isWorkStation)
    assertFalse(viewModel.uiState.value.supportsUsagePatterns)
    assertEquals(City.MADRID.id, viewModel.uiState.value.savedPlaceAlertsCityId)
    assertEquals(1, viewModel.uiState.value.savedPlaceAlertRules.size)
  }
}

private class FakeStationDetailFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(setOf("station-1"))
  override val homeStationId = MutableStateFlow<String?>("station-1")
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

private class FakeStationDetailSettingsRepository : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(true)
  override val onboardingChecklist = MutableStateFlow(OnboardingChecklistSnapshot(completedAtEpoch = 1L))
  override val engagementSnapshot = MutableStateFlow(com.gcaguilar.biciradar.core.EngagementSnapshot())
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
  override suspend fun setEngagementSnapshot(snapshot: com.gcaguilar.biciradar.core.EngagementSnapshot) = Unit
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class FakeStationDetailAlertsRepository : SavedPlaceAlertsRepository {
  override val rules = MutableStateFlow<List<SavedPlaceAlertRule>>(emptyList())
  override suspend fun bootstrap() = Unit
  override fun currentRules(): List<SavedPlaceAlertRule> = rules.value
  override fun ruleForTarget(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? = null
  override suspend fun upsertRule(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition, enabled: Boolean) = Unit
  override suspend fun removeRule(ruleId: String) = Unit
  override suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget) = Unit
  override suspend fun setRuleEnabled(ruleId: String, enabled: Boolean) = Unit
  override suspend fun replaceAll(rules: List<SavedPlaceAlertRule>) = Unit
}

private class FakeStationDetailDatosBiziApi(
  private val patterns: List<StationHourlyPattern>,
) : DatosBiziApi {
  override suspend fun fetchPatterns(stationId: String): List<StationHourlyPattern> = patterns
}

private object NoOpStationDetailRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit
  override fun launchWalkToLocation(destination: GeoPoint) = Unit
}
