package com.gcaguilar.biciradar.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CityChangeUseCaseTest {
  @Test
  fun `change city clears favorites refreshes stations and can confirm onboarding`() = runTest {
    val settingsRepository = FakeCityChangeSettingsRepository(
      onboardingChecklist = OnboardingChecklistSnapshot(),
    )
    val favoritesRepository = FakeCityChangeFavoritesRepository()
    val stationsRepository = FakeCityChangeStationsRepository()
    val useCase = ChangeCityUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )

    useCase.execute(city = City.BARCELONA)

    assertEquals(City.BARCELONA, settingsRepository.selectedCity.value)
    assertTrue(settingsRepository.onboardingChecklist.value.cityConfirmed)
    assertEquals(
      listOf("updateOnboardingChecklist", "setSelectedCity"),
      settingsRepository.calls.take(2),
    )
    assertEquals(1, favoritesRepository.clearAllCount)
    assertEquals(1, stationsRepository.forceRefreshCount)
  }

  @Test
  fun `change city can preserve favorites and still confirms city`() = runTest {
    val settingsRepository = FakeCityChangeSettingsRepository(
      onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = false),
    )
    val favoritesRepository = FakeCityChangeFavoritesRepository()
    val stationsRepository = FakeCityChangeStationsRepository()
    val useCase = ChangeCityUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )

    useCase.execute(
      city = City.MADRID,
      clearFavorites = false,
    )

    assertEquals(City.MADRID, settingsRepository.selectedCity.value)
    assertEquals(0, favoritesRepository.clearAllCount)
    assertEquals(true, settingsRepository.onboardingChecklist.value.cityConfirmed)
    assertEquals(1, stationsRepository.forceRefreshCount)
  }
}

private class FakeCityChangeSettingsRepository(
  onboardingChecklist: OnboardingChecklistSnapshot,
) : SettingsRepository {
  val calls = mutableListOf<String>()
  override val searchRadiusMeters = MutableStateFlow(DEFAULT_SEARCH_RADIUS_METERS)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.GoogleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow<String?>(null)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(onboardingChecklist.isCompleted())
  override val onboardingChecklist = MutableStateFlow(onboardingChecklist)
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
    calls += "setSelectedCity"
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
    calls += "updateOnboardingChecklist"
    val updated = transform(onboardingChecklist.value)
    onboardingChecklist.value = updated
    hasCompletedOnboarding.value = updated.isCompleted()
  }
  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) {
    engagementSnapshot.value = snapshot
  }
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class FakeCityChangeFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  var clearAllCount = 0

  override suspend fun bootstrap() = Unit
  override suspend fun syncFromPeer() = Unit
  override suspend fun toggle(stationId: String) = Unit
  override suspend fun setHomeStationId(stationId: String?) = Unit
  override suspend fun setWorkStationId(stationId: String?) = Unit
  override suspend fun clearAll() {
    clearAllCount++
  }
  override fun isFavorite(stationId: String): Boolean = false
  override fun currentHomeStationId(): String? = null
  override fun currentWorkStationId(): String? = null
}

private class FakeCityChangeStationsRepository : StationsRepository {
  override val state = MutableStateFlow(StationsState())
  var forceRefreshCount = 0

  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() {
    forceRefreshCount++
  }
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = null
}
