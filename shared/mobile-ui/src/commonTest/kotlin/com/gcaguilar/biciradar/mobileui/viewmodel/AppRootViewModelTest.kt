package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.EngagementRepository
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.ReviewEligibility
import com.gcaguilar.biciradar.core.ReviewEligibilityReason
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.mobileui.usecases.ChangelogUseCase
import com.gcaguilar.biciradar.mobileui.usecases.FeedbackUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppRootViewModelTest {
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
  fun `bootstrap exposes pending changelog for the latest cataloged version`() = runTest(dispatcher) {
    val settingsRepository = FakeAppRootSettingsRepository(
      onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true, completedAtEpoch = 1L),
      lastSeenChangelogAppVersion = "0.19.0",
    )
    val favoritesRepository = FakeAppRootFavoritesRepository()
    val stationsRepository = FakeAppRootStationsRepository()
    val engagementRepository = FakeEngagementRepository()
    
    val startupUseCase = StartupUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )
    val feedbackUseCase = FeedbackUseCase(
      appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
      reviewPrompter = AppRootFakeReviewPrompter(),
      engagementRepository = engagementRepository,
    )
    val changelogUseCase = ChangelogUseCase(
      settingsRepository = settingsRepository,
      appVersion = "0.21.0",
    )
    
    val viewModel = AppRootViewModel(
      startupUseCase = startupUseCase,
      feedbackUseCase = feedbackUseCase,
      changelogUseCase = changelogUseCase,
      savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
      surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
      surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
      appVersion = "0.21.0",
    )

    viewModel.onRefreshSignal()
    advanceUntilIdle()

    assertTrue(viewModel.uiState.value.settingsBootstrapped)
    assertTrue(viewModel.uiState.value.favoritesBootstrapped)
    assertTrue(viewModel.uiState.value.startupLaunchReady)
    assertNotNull(viewModel.uiState.value.changelogPresentation)
    assertEquals("0.21.0", viewModel.uiState.value.changelogPresentation?.highlightedVersion)
  }

  @Test
  fun `bootstrap suppresses changelog while onboarding is still pending`() = runTest(dispatcher) {
    val settingsRepository = FakeAppRootSettingsRepository(
      onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true),
      lastSeenChangelogAppVersion = "0.19.0",
    )
    val favoritesRepository = FakeAppRootFavoritesRepository()
    val stationsRepository = FakeAppRootStationsRepository()
    val engagementRepository = FakeEngagementRepository()
    
    val startupUseCase = StartupUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )
    val feedbackUseCase = FeedbackUseCase(
      appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
      reviewPrompter = AppRootFakeReviewPrompter(),
      engagementRepository = engagementRepository,
    )
    val changelogUseCase = ChangelogUseCase(
      settingsRepository = settingsRepository,
      appVersion = "0.21.0",
    )
    
    val viewModel = AppRootViewModel(
      startupUseCase = startupUseCase,
      feedbackUseCase = feedbackUseCase,
      changelogUseCase = changelogUseCase,
      savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
      surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
      surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
      appVersion = "0.21.0",
    )

    advanceUntilIdle()

    assertEquals(null, viewModel.uiState.value.changelogPresentation)
    assertEquals("0.21.0", settingsRepository.lastSeenChangelogAppVersion.value)
  }

  @Test
  fun `auto completes onboarding milestones from favorites and saved places`() = runTest(dispatcher) {
    val settingsRepository = FakeAppRootSettingsRepository(
      onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true),
    )
    val favoritesRepository = FakeAppRootFavoritesRepository().apply {
      favoriteIds.value = setOf("station-1")
      homeStationId.value = "station-1"
      workStationId.value = "station-2"
    }
    val stationsRepository = FakeAppRootStationsRepository()
    val engagementRepository = FakeEngagementRepository()
    
    val startupUseCase = StartupUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )
    val feedbackUseCase = FeedbackUseCase(
      appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
      reviewPrompter = AppRootFakeReviewPrompter(),
      engagementRepository = engagementRepository,
    )
    val changelogUseCase = ChangelogUseCase(
      settingsRepository = settingsRepository,
      appVersion = "0.19.1",
    )
    
    val viewModel = AppRootViewModel(
      startupUseCase = startupUseCase,
      feedbackUseCase = feedbackUseCase,
      changelogUseCase = changelogUseCase,
      savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
      surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
      surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
      appVersion = "0.19.1",
    )

    advanceUntilIdle()

    assertTrue(settingsRepository.onboardingChecklist.value.firstStationSaved)
    assertTrue(settingsRepository.onboardingChecklist.value.savedPlacesConfigured)
    assertEquals(null, viewModel.uiState.value.changelogPresentation)
    assertEquals("0.19.1", settingsRepository.lastSeenChangelogAppVersion.value)
  }

  @Test
  fun `manual changelog dismissal persists last seen version`() = runTest(dispatcher) {
    val settingsRepository = FakeAppRootSettingsRepository(
      onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true, completedAtEpoch = 1L),
      lastSeenChangelogAppVersion = "0.19.1",
    )
    val favoritesRepository = FakeAppRootFavoritesRepository()
    val stationsRepository = FakeAppRootStationsRepository()
    val engagementRepository = FakeEngagementRepository()
    
    val startupUseCase = StartupUseCase(
      settingsRepository = settingsRepository,
      favoritesRepository = favoritesRepository,
      stationsRepository = stationsRepository,
    )
    val feedbackUseCase = FeedbackUseCase(
      appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
      reviewPrompter = AppRootFakeReviewPrompter(),
      engagementRepository = engagementRepository,
    )
    val changelogUseCase = ChangelogUseCase(
      settingsRepository = settingsRepository,
      appVersion = "0.19.1",
    )
    
    val viewModel = AppRootViewModel(
      startupUseCase = startupUseCase,
      feedbackUseCase = feedbackUseCase,
      changelogUseCase = changelogUseCase,
      savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
      surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
      surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
      appVersion = "0.19.1",
    )

    advanceUntilIdle()
    viewModel.showChangelogHistory()
    advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.changelogPresentation)

    viewModel.dismissChangelog()
    advanceUntilIdle()

    assertEquals("0.19.1", settingsRepository.lastSeenChangelogAppVersion.value)
    assertEquals(null, viewModel.uiState.value.changelogPresentation)
  }
}

private class FakeAppRootSettingsRepository(
  onboardingChecklist: OnboardingChecklistSnapshot,
  lastSeenChangelogAppVersion: String? = null,
) : SettingsRepository {
  override val searchRadiusMeters = MutableStateFlow(500)
  override val preferredMapApp = MutableStateFlow(PreferredMapApp.AppleMaps)
  override val lastSeenChangelogVersion = MutableStateFlow(0)
  override val lastSeenChangelogAppVersion = MutableStateFlow(lastSeenChangelogAppVersion)
  override val themePreference = MutableStateFlow(ThemePreference.System)
  override val selectedCity = MutableStateFlow(City.ZARAGOZA)
  override val hasCompletedOnboarding = MutableStateFlow(onboardingChecklist.isCompleted())
  override val onboardingChecklist = MutableStateFlow(onboardingChecklist)
  override val engagementSnapshot = MutableStateFlow(EngagementSnapshot(installedAtEpoch = 1L))

  override suspend fun bootstrap() = Unit
  override fun currentSearchRadiusMeters(): Int = searchRadiusMeters.value
  override fun currentPreferredMapApp(): PreferredMapApp = preferredMapApp.value
  override fun currentSelectedCity(): City = selectedCity.value
  override fun currentLastSeenChangelogAppVersion(): String? = lastSeenChangelogAppVersion.value
  override suspend fun setSearchRadiusMeters(searchRadiusMeters: Int) {
    this.searchRadiusMeters.value = searchRadiusMeters
  }
  override suspend fun setPreferredMapApp(preferredMapApp: PreferredMapApp) = Unit
  override suspend fun setLastSeenChangelogVersion(version: Int) = Unit
  override suspend fun setLastSeenChangelogAppVersion(version: String?) {
    lastSeenChangelogAppVersion.value = version
  }
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
  override suspend fun setEngagementSnapshot(snapshot: EngagementSnapshot) {
    engagementSnapshot.value = snapshot
  }
  override suspend fun ensureChangelogStringBaseline(appVersion: String) = Unit
}

private class FakeAppRootFavoritesRepository : FavoritesRepository {
  override val favoriteIds = MutableStateFlow(emptySet<String>())
  override val homeStationId = MutableStateFlow<String?>(null)
  override val workStationId = MutableStateFlow<String?>(null)
  var syncCount = 0

  override suspend fun bootstrap() = Unit
  override suspend fun syncFromPeer() {
    syncCount++
  }
  override suspend fun toggle(stationId: String) = Unit
  override suspend fun setHomeStationId(stationId: String?) {
    homeStationId.value = stationId
  }
  override suspend fun setWorkStationId(stationId: String?) {
    workStationId.value = stationId
  }
  override suspend fun clearAll() = Unit
  override fun isFavorite(stationId: String): Boolean = stationId in favoriteIds.value
  override fun currentHomeStationId(): String? = homeStationId.value
  override fun currentWorkStationId(): String? = workStationId.value
}

private class FakeAppRootStationsRepository : StationsRepository {
  override val state = MutableStateFlow(
    StationsState(
      stations = listOf(
        Station(
          id = "station-1",
          name = "Plaza Espana",
          address = "Centro",
          location = GeoPoint(41.65, -0.88),
          bikesAvailable = 5,
          slotsFree = 7,
          distanceMeters = 120,
        ),
      ),
    ),
  )
  var forceRefreshCount = 0
  var loadIfNeededCount = 0

  override suspend fun loadIfNeeded() {
    loadIfNeededCount++
  }

  override suspend fun forceRefresh() {
    forceRefreshCount++
  }

  override suspend fun refreshAvailability(stationIds: List<String>) = Unit

  override fun stationById(stationId: String): Station? = state.value.stations.firstOrNull { it.id == stationId }
}

private class AppRootFakeSavedPlaceAlertsRepository : SavedPlaceAlertsRepository {
  override val rules = MutableStateFlow<List<SavedPlaceAlertRule>>(emptyList())
  var bootstrapCount = 0

  override suspend fun bootstrap() {
    bootstrapCount++
  }
  override fun currentRules(): List<SavedPlaceAlertRule> = rules.value
  override fun ruleForTarget(target: SavedPlaceAlertTarget): SavedPlaceAlertRule? = null
  override suspend fun upsertRule(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition, enabled: Boolean) = Unit
  override suspend fun removeRule(ruleId: String) = Unit
  override suspend fun removeRuleForTarget(target: SavedPlaceAlertTarget) = Unit
  override suspend fun setRuleEnabled(ruleId: String, enabled: Boolean) = Unit
  override suspend fun replaceAll(rules: List<SavedPlaceAlertRule>) = Unit
}

private class FakeEngagementRepository : EngagementRepository {
  override val snapshot: MutableStateFlow<EngagementSnapshot> = MutableStateFlow(EngagementSnapshot(installedAtEpoch = 1L))

  override suspend fun bootstrap() = Unit
  override suspend fun markSessionStarted(nowEpoch: Long) {
    snapshot.value = snapshot.value.markSessionStarted(nowEpoch)
  }
  override suspend fun markUsefulSession(nowEpoch: Long) {
    snapshot.value = snapshot.value.markUsefulSession(nowEpoch)
  }
  override suspend fun markFavoriteCreated(nowEpoch: Long) {
    snapshot.value = snapshot.value.markFavoriteSaved(nowEpoch)
  }
  override suspend fun markRouteOpened(nowEpoch: Long) = Unit
  override suspend fun markMonitoringCompleted(nowEpoch: Long) = Unit
  override suspend fun markDataFreshnessObserved(freshness: DataFreshness) = Unit
  override suspend fun markFeedbackNudgeShown(appVersion: String, nowEpoch: Long) {
    snapshot.value = snapshot.value.markFeedbackNudged(appVersion, nowEpoch)
  }
  override suspend fun markFeedbackOpened(nowEpoch: Long) = Unit
  override suspend fun markFeedbackDismissed(nowEpoch: Long) = Unit
  override suspend fun markReviewPrompted(appVersion: String, nowEpoch: Long) = Unit
  override suspend fun markUpdateChecked(nowEpoch: Long) {
    snapshot.value = snapshot.value.markUpdateChecked(nowEpoch)
  }
  override suspend fun markUpdateBannerDismissed(version: String, nowEpoch: Long) {
    snapshot.value = snapshot.value.markUpdateBannerDismissed(version, nowEpoch)
  }
  override fun shouldShowFeedbackNudge(appVersion: String, nowEpoch: Long): Boolean = false
  override fun reviewEligibility(
    appVersion: String,
    onboardingCompleted: Boolean,
    currentFreshness: DataFreshness,
    nowEpoch: Long,
  ): ReviewEligibility = ReviewEligibility(
    isEligible = false,
    reason = ReviewEligibilityReason.NotEnoughPositiveSignals,
    positiveSignals = 0,
  )
}

private class AppRootFakeSurfaceSnapshotRepository : SurfaceSnapshotRepository {
  override val bundle: MutableStateFlow<SurfaceSnapshotBundle?> = MutableStateFlow(null)
  var bootstrapCount = 0
  var refreshCount = 0

  override suspend fun bootstrap() {
    bootstrapCount++
  }
  override suspend fun refreshSnapshot() {
    refreshCount++
  }
  override suspend fun saveMonitoringSession(session: SurfaceMonitoringSession?) = Unit
  override fun currentBundle(): SurfaceSnapshotBundle? = bundle.value
}

private class AppRootFakeSurfaceMonitoringRepository : SurfaceMonitoringRepository {
  override val state: MutableStateFlow<SurfaceMonitoringSession?> = MutableStateFlow(null)
  var bootstrapCount = 0

  override suspend fun bootstrap() {
    bootstrapCount++
  }
  override suspend fun startMonitoring(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean = false
  override suspend fun startMonitoringFavoriteStation(durationSeconds: Int, kind: SurfaceMonitoringKind): Boolean = false
  override fun stopMonitoring() = Unit
  override suspend fun clearMonitoring() = Unit
}

private class AppRootFakeAppUpdatePrompter(
  private val availability: UpdateAvailabilityState = UpdateAvailabilityState.Unknown,
) : AppUpdatePrompter {
  override suspend fun checkForUpdate(): UpdateAvailabilityState = availability
  override suspend fun startFlexibleUpdate(): Boolean = false
  override suspend fun completeFlexibleUpdateIfReady(): Boolean = false
  override fun openStoreListing() = Unit
}

private class AppRootFakeReviewPrompter : ReviewPrompter {
  var requestCount = 0
  override suspend fun requestInAppReview() {
    requestCount++
  }
  override fun openStoreWriteReview() = Unit
}
