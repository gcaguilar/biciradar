package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.mobileui.initialization.AppInitializer
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.ResolveOnboardingPresentationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SettingsAggregationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceManagementUseCase
import com.gcaguilar.biciradar.testutils.FakeEngagementRepository
import com.gcaguilar.biciradar.testutils.FakeFavoritesRepository
import com.gcaguilar.biciradar.testutils.FakeSavedPlaceAlertsRepository
import com.gcaguilar.biciradar.testutils.FakeSettingsRepository
import com.gcaguilar.biciradar.testutils.FakeStationsRepository
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
  fun `bootstrap exposes pending changelog for the latest cataloged version`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true, completedAtEpoch = 1L),
          lastSeenChangelogAppVersion = "0.19.0",
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )

      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )

      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.21.0",
        )

      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )

      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )

      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.21.0",
        )

      assertEquals(false, viewModel.uiState.value.startupLaunchReady)
      assertEquals(null, viewModel.uiState.value.cityConfigured)

      viewModel.onRefreshSignal()
      advanceUntilIdle()

      assertTrue(viewModel.uiState.value.settingsBootstrapped)
      assertTrue(viewModel.uiState.value.favoritesBootstrapped)
      assertTrue(viewModel.uiState.value.startupLaunchReady)
      assertEquals(true, viewModel.uiState.value.cityConfigured)
      assertNotNull(viewModel.uiState.value.changelogPresentation)
      assertEquals(
        "0.21.0",
        viewModel.uiState.value.changelogPresentation
          ?.highlightedVersion,
      )
    }

  @Test
  fun `bootstrap suppresses changelog while onboarding is still pending`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true),
          lastSeenChangelogAppVersion = "0.19.0",
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )

      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )

      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.21.0",
        )

      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )

      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )

      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.21.0",
        )

      advanceUntilIdle()

      assertEquals(null, viewModel.uiState.value.changelogPresentation)
      assertEquals(true, viewModel.uiState.value.cityConfigured)
      assertEquals("0.21.0", settingsRepository.lastSeenChangelogAppVersion.value)
    }

  @Test
  fun `auto completes onboarding milestones from favorites and saved places`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true),
        )
      val favoritesRepository =
        FakeAppRootFavoritesRepository().apply {
          favoriteIds.value = setOf("station-1")
          homeStationId.value = "station-1"
          workStationId.value = "station-2"
        }
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )

      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )

      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.19.1",
        )

      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )

      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )

      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.19.1",
        )

      advanceUntilIdle()

      assertTrue(settingsRepository.onboardingChecklist.value.firstStationSaved)
      assertTrue(settingsRepository.onboardingChecklist.value.savedPlacesConfigured)
      assertEquals(null, viewModel.uiState.value.changelogPresentation)
      assertEquals("0.19.1", settingsRepository.lastSeenChangelogAppVersion.value)
    }

  @Test
  fun `manual changelog dismissal persists last seen version`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true, completedAtEpoch = 1L),
          lastSeenChangelogAppVersion = "0.19.1",
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )

      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )

      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.19.1",
        )

      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )

      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )

      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
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

  @Test
  fun `city configured in ui state reacts to checklist changes after bootstrap`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = false),
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )
      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )
      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.21.0",
        )
      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )
      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )
      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.21.0",
        )

      advanceUntilIdle()
      assertEquals(true, viewModel.uiState.value.settingsBootstrapped)
      assertEquals(false, viewModel.uiState.value.cityConfigured)

      settingsRepository.setOnboardingChecklist(OnboardingChecklistSnapshot(cityConfirmed = true))
      advanceUntilIdle()
      assertEquals(true, viewModel.uiState.value.cityConfigured)

      settingsRepository.setOnboardingChecklist(OnboardingChecklistSnapshot(cityConfirmed = false))
      advanceUntilIdle()
      assertEquals(false, viewModel.uiState.value.cityConfigured)
    }

  @Test
  fun `refresh signal triggers station refresh and marks initial load finished`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true),
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )
      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )
      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.21.0",
        )
      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )
      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )
      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.21.0",
        )

      advanceUntilIdle()
      // Bootstrap ends with maybeRefreshStations(), and refreshStations() syncs favorites then force-refreshes.
      assertEquals(2, favoritesRepository.syncCount)
      assertEquals(1, stationsRepository.forceRefreshCount)
      assertEquals(true, viewModel.uiState.value.initialLoadAttemptFinished)

      viewModel.onRefreshSignal()
      advanceUntilIdle()

      assertEquals(3, favoritesRepository.syncCount)
      assertEquals(2, stationsRepository.forceRefreshCount)
      assertEquals(true, viewModel.uiState.value.initialLoadAttemptFinished)
      assertEquals(true, viewModel.uiState.value.startupLaunchReady)
    }

  @Test
  fun `manual onboarding from settings ignores temporary suppression`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist =
            OnboardingChecklistSnapshot(
              cityConfirmed = true,
              featureHighlightsSeen = true,
              locationDecisionMade = true,
              notificationsDecisionMade = true,
            ),
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )
      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )
      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.21.0",
        )
      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )
      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )
      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.21.0",
        )

      advanceUntilIdle()
      assertEquals(true, viewModel.uiState.value.shouldShowGuidedOnboarding)

      viewModel.onOnboardingOpenFavoritesRequested()
      advanceUntilIdle()
      assertEquals(false, viewModel.uiState.value.shouldShowGuidedOnboarding)

      viewModel.onOnboardingOpenedFromSettings()
      advanceUntilIdle()
      assertEquals(true, viewModel.uiState.value.shouldShowGuidedOnboarding)
    }

  @Test
  fun `skip onboarding marks every milestone as completed`() =
    runTest(dispatcher) {
      val settingsRepository =
        FakeAppRootSettingsRepository(
          onboardingChecklist = OnboardingChecklistSnapshot(cityConfirmed = true),
        )
      val favoritesRepository = FakeAppRootFavoritesRepository()
      val stationsRepository = FakeAppRootStationsRepository()
      val engagementRepository = FakeEngagementRepository()

      val startupUseCase =
        StartupUseCase(
          settingsRepository = settingsRepository,
          favoritesRepository = favoritesRepository,
          favoritesPeerSync = favoritesRepository,
          stationsRepository = stationsRepository,
        )
      val settingsAggregationUseCase =
        SettingsAggregationUseCase(
          settingsRepository = settingsRepository,
        )
      val appLifecycleUseCase =
        AppLifecycleUseCase(
          engagementRepository = engagementRepository,
          appUpdatePrompter = AppRootFakeAppUpdatePrompter(),
          reviewPrompter = AppRootFakeReviewPrompter(),
          settingsAggregationUseCase = settingsAggregationUseCase,
          appVersion = "0.21.0",
        )
      val surfaceManagementUseCase =
        SurfaceManagementUseCase(
          surfaceSnapshotRepository = AppRootFakeSurfaceSnapshotRepository(),
          surfaceMonitoringRepository = AppRootFakeSurfaceMonitoringRepository(),
          savedPlaceAlertsRepository = AppRootFakeSavedPlaceAlertsRepository(),
        )
      val appInitializer =
        AppInitializer(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          surfaceManagementUseCase = surfaceManagementUseCase,
        )
      val viewModel =
        AppRootViewModel(
          startupUseCase = startupUseCase,
          appLifecycleUseCase = appLifecycleUseCase,
          appInitializer = appInitializer,
          onboardingCoordinator =
            OnboardingCoordinator(
              startupUseCase = startupUseCase,
              resolveOnboardingPresentationUseCase = ResolveOnboardingPresentationUseCase(),
            ),
          engagementCoordinator = EngagementCoordinator(appLifecycleUseCase),
          refreshOrchestrator = RefreshOrchestrator(startupUseCase, appInitializer),
          appVersion = "0.21.0",
        )

      advanceUntilIdle()
      assertTrue(viewModel.uiState.value.shouldShowGuidedOnboarding)

      viewModel.onSkipOnboarding()
      advanceUntilIdle()

      assertTrue(settingsRepository.onboardingChecklist.value.isCompleted())
      assertTrue(settingsRepository.onboardingChecklist.value.firstStationSaved)
      assertTrue(settingsRepository.onboardingChecklist.value.savedPlacesConfigured)
      assertTrue(settingsRepository.onboardingChecklist.value.surfacesDiscovered)
      assertEquals(false, viewModel.uiState.value.shouldShowGuidedOnboarding)
    }
}

private typealias FakeAppRootSettingsRepository = FakeSettingsRepository
private typealias FakeAppRootFavoritesRepository = FakeFavoritesRepository
private typealias FakeAppRootStationsRepository = FakeStationsRepository
private typealias AppRootFakeSavedPlaceAlertsRepository = FakeSavedPlaceAlertsRepository

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

  override suspend fun startMonitoringFavoriteStation(
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
  ): Boolean = false

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
