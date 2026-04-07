package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.AssistantResolution
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.ThemePreference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ShortcutsViewModelTest {
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
  fun `resolves assistant action into latest answer`() = runTest(dispatcher) {
    val viewModel = ShortcutsViewModel(
      assistantIntentResolver = RecordingAssistantIntentResolver(),
      stationsRepository = FakeShortcutsStationsRepository(
        StationsState(
          stations = listOf(
            Station(
              id = "station-1",
              name = "Plaza Espana",
              address = "Centro",
              location = GeoPoint(41.65, -0.88),
              bikesAvailable = 7,
              slotsFree = 5,
              distanceMeters = 120,
            ),
          ),
        ),
      ),
      favoritesRepository = FakeShortcutsFavoritesRepository(setOf("station-1")),
      settingsRepository = FakeShortcutsSettingsRepository(searchRadiusMeters = 900),
    )

    viewModel.resolveInitialAction(AssistantAction.NearestStation)

    advanceUntilIdle()

    assertEquals("resolved:1:1:900", viewModel.uiState.value.latestAnswer)
    assertEquals(900, viewModel.uiState.value.searchRadiusMeters)
  }

  @Test
  fun `latest request result wins when older request finishes later`() = runTest(dispatcher) {
    val resolver = ControllableAssistantIntentResolver()
    val viewModel = ShortcutsViewModel(
      assistantIntentResolver = resolver,
      stationsRepository = FakeShortcutsStationsRepository(
        StationsState(
          stations = listOf(
            Station(
              id = "station-1",
              name = "Plaza Espana",
              address = "Centro",
              location = GeoPoint(41.65, -0.88),
              bikesAvailable = 7,
              slotsFree = 5,
              distanceMeters = 120,
            ),
          ),
        ),
      ),
      favoritesRepository = FakeShortcutsFavoritesRepository(setOf("station-1")),
      settingsRepository = FakeShortcutsSettingsRepository(searchRadiusMeters = 900),
    )

    viewModel.resolveInitialAction(AssistantAction.NearestStation)
    viewModel.resolveInitialAction(AssistantAction.NearestStation)
    advanceUntilIdle()

    resolver.completeRequest(
      index = 1,
      resolution = AssistantResolution(spokenResponse = "newest-response"),
    )
    advanceUntilIdle()
    assertEquals("newest-response", viewModel.uiState.value.latestAnswer)

    resolver.completeRequest(
      index = 0,
      resolution = AssistantResolution(spokenResponse = "stale-response"),
    )
    advanceUntilIdle()
    assertEquals("newest-response", viewModel.uiState.value.latestAnswer)
  }
}

private class RecordingAssistantIntentResolver : AssistantIntentResolver {
  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution = AssistantResolution(
    spokenResponse = "resolved:${stationsState.stations.size}:${favoriteIds.size}:$searchRadiusMeters",
  )
}

private class ControllableAssistantIntentResolver : AssistantIntentResolver {
  private val pendingRequests = mutableListOf<CompletableDeferred<AssistantResolution>>()

  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution {
    val deferred = CompletableDeferred<AssistantResolution>()
    pendingRequests += deferred
    return deferred.await()
  }

  fun completeRequest(index: Int, resolution: AssistantResolution) {
    val request = pendingRequests.getOrNull(index)
    assertNotNull(request, "Expected pending request at index $index")
    request.complete(resolution)
  }
}

private class FakeShortcutsStationsRepository(
  initialState: StationsState,
) : StationsRepository {
  override val state = MutableStateFlow(initialState)
  override suspend fun loadIfNeeded() = Unit
  override suspend fun forceRefresh() = Unit
  override suspend fun refreshAvailability(stationIds: List<String>) = Unit
  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class FakeShortcutsFavoritesRepository(initialFavoriteIds: Set<String>) : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(initialFavoriteIds)
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

private class FakeShortcutsSettingsRepository(
  searchRadiusMeters: Int,
) : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(searchRadiusMeters)
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
