package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.ThemePreference
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
class ProfileViewModelTest {
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
  fun `runtime checklist controls setup card visibility`() = runTest(dispatcher) {
    val settingsRepository = ProfileTestSettingsRepository()
    val favoritesRepository = ProfileTestFavoritesRepository()
    val stationsRepository = ProfileTestStationsRepository()
    val viewModel = ProfileViewModel(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = settingsRepository,
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
      ),
      canSelectGoogleMapsInIos = true,
    )

    settingsRepository.onboardingChecklist.value = OnboardingChecklistSnapshot(cityConfirmed = true)
    advanceUntilIdle()
    assertEquals(true, viewModel.uiState.value.showProfileSetupCard)

    settingsRepository.onboardingChecklist.value = OnboardingChecklistSnapshot(cityConfirmed = true, completedAtEpoch = 1L)
    advanceUntilIdle()
    assertEquals(false, viewModel.uiState.value.showProfileSetupCard)

    settingsRepository.onboardingChecklist.value = OnboardingChecklistSnapshot(cityConfirmed = false)
    advanceUntilIdle()
    assertEquals(false, viewModel.uiState.value.showProfileSetupCard)
  }

  @Test
  fun `city selection delegates through public method`() = runTest(dispatcher) {
    val settingsRepository = ProfileTestSettingsRepository(city = City.ZARAGOZA)
    val favoritesRepository = ProfileTestFavoritesRepository()
    val stationsRepository = ProfileTestStationsRepository()
    val viewModel = ProfileViewModel(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      changeCityUseCase = ChangeCityUseCase(
        settingsRepository = settingsRepository,
        favoritesRepository = favoritesRepository,
        stationsRepository = stationsRepository,
      ),
      canSelectGoogleMapsInIos = true,
    )

    viewModel.onCitySelected(City.MADRID)
    advanceUntilIdle()

    assertEquals(City.MADRID, settingsRepository.selectedCity.value)
    assertEquals(1, favoritesRepository.clearAllCalls)
    assertEquals(1, stationsRepository.forceRefreshCalls)
    assertEquals(true, settingsRepository.onboardingChecklist.value.cityConfirmed)
  }
}

private class ProfileTestSettingsRepository(
  city: City = City.ZARAGOZA,
) : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(city)
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
  override suspend fun setHasCompletedOnboarding(completed: Boolean) {
    hasCompletedOnboarding.value = completed
  }
  override suspend fun setOnboardingChecklist(snapshot: OnboardingChecklistSnapshot) {
    onboardingChecklist.value = snapshot
    hasCompletedOnboarding.value = snapshot.isCompleted()
  }
  override suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    val updated = transform(onboardingChecklist.value)
    onboardingChecklist.value = updated
    hasCompletedOnboarding.value = updated.isCompleted()
  }
  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) = Unit
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class ProfileTestFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  var clearAllCalls = 0

  override suspend fun bootstrap() = Unit
  override suspend fun syncFromPeer() = Unit
  override suspend fun toggle(stationId: String) = Unit
  override suspend fun setHomeStationId(stationId: String?) = Unit
  override suspend fun setWorkStationId(stationId: String?) = Unit
  override suspend fun clearAll() {
    clearAllCalls++
  }
  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value
  override fun currentHomeStationId(): String? = homeStationId.value
  override fun currentWorkStationId(): String? = workStationId.value
}

private class ProfileTestStationsRepository : StationsRepository {
  override val state = MutableStateFlow(StationsState())
  var forceRefreshCalls = 0
  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() {
    forceRefreshCalls++
  }
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = null
}
