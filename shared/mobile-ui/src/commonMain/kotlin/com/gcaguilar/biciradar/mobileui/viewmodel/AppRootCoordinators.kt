package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.initialization.AppInitializer
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.OnboardingLaunchSource
import com.gcaguilar.biciradar.mobileui.usecases.OnboardingPresentationInput
import com.gcaguilar.biciradar.mobileui.usecases.ResolveOnboardingPresentationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal data class OnboardingPresentationState(
  val onboardingChecklist: OnboardingChecklistSnapshot,
  val isCitySelectionRequired: Boolean,
  val shouldShowGuidedOnboarding: Boolean,
  val shouldResetNavigationSuppression: Boolean,
  val launchSource: OnboardingLaunchSource,
)

@Inject
internal class OnboardingCoordinator(
  private val startupUseCase: StartupUseCase,
  private val resolveOnboardingPresentationUseCase: ResolveOnboardingPresentationUseCase,
) {
  suspend fun updateChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    startupUseCase.updateOnboardingChecklist(transform)
  }

  suspend fun skipOnboarding() {
    startupUseCase.updateOnboardingChecklist {
      it
        .copy(
          featureHighlightsSeen = true,
          locationDecisionMade = true,
          notificationsDecisionMade = true,
          firstStationSaved = true,
          savedPlacesConfigured = true,
          surfacesDiscovered = true,
        ).markCompleted()
    }
  }

  suspend fun completeSurfaces() {
    startupUseCase.updateOnboardingChecklist {
      it.copy(surfacesDiscovered = true).markCompleted()
    }
  }

  suspend fun maybeAutoComplete(
    settingsBootstrapped: Boolean,
    runtime: AppRootRuntimeState,
  ) {
    if (!settingsBootstrapped) return
    if (runtime.latestOnboardingChecklist.cityConfirmed &&
      !runtime.latestOnboardingChecklist.firstStationSaved &&
      runtime.latestFavoriteIds.isNotEmpty()
    ) {
      startupUseCase.updateOnboardingChecklist { snapshot ->
        if (snapshot.firstStationSaved) snapshot else snapshot.copy(firstStationSaved = true)
      }
    }
    if (!runtime.latestOnboardingChecklist.savedPlacesConfigured &&
      runtime.latestHomeStationId != null &&
      runtime.latestWorkStationId != null
    ) {
      startupUseCase.updateOnboardingChecklist { snapshot ->
        if (snapshot.savedPlacesConfigured) snapshot else snapshot.copy(savedPlacesConfigured = true)
      }
    }
  }

  fun resolvePresentation(
    checklist: OnboardingChecklistSnapshot,
    cityConfigured: Boolean,
    runtime: AppRootRuntimeState,
  ): OnboardingPresentationState {
    val resolved =
      resolveOnboardingPresentationUseCase.execute(
        OnboardingPresentationInput(
          checklist = checklist,
          cityConfigured = cityConfigured,
          suppressGuidedOnboardingForNavigation = runtime.suppressGuidedOnboardingForNavigation,
          launchSource = runtime.onboardingLaunchSource,
        ),
      )

    return OnboardingPresentationState(
      onboardingChecklist = resolved.onboardingChecklist,
      isCitySelectionRequired = resolved.isCitySelectionRequired,
      shouldShowGuidedOnboarding = resolved.shouldShowGuidedOnboarding,
      shouldResetNavigationSuppression = resolved.shouldResetNavigationSuppression,
      launchSource =
        if (runtime.onboardingLaunchSource == OnboardingLaunchSource.Settings && !resolved.shouldShowGuidedOnboarding) {
          OnboardingLaunchSource.Automatic
        } else {
          runtime.onboardingLaunchSource
        },
    )
  }
}

@Inject
internal class EngagementCoordinator(
  private val appLifecycleUseCase: AppLifecycleUseCase,
) {
  suspend fun maybeCheckForUpdates(
    runtimeState: MutableStateFlow<AppRootRuntimeState>,
    updateUiState: (TopUpdateBanner) -> Unit,
    clock: () -> Long,
  ) {
    if (runtimeState.value.updateCheckInFlight) return
    runtimeState.update { it.copy(updateCheckInFlight = true) }
    try {
      appLifecycleUseCase.markUpdateChecked(clock())
      when (val update = appLifecycleUseCase.checkForUpdate()) {
        is UpdateAvailabilityState.Available ->
          updateUiState(
            TopUpdateBanner.Available(
              version = update.versionName,
              flexible = update.isFlexibleAllowed,
              storeUrl = update.storeUrl,
            ),
          )
        is UpdateAvailabilityState.Downloaded ->
          updateUiState(TopUpdateBanner.Downloaded(update.versionName))
        else -> Unit
      }
    } finally {
      runtimeState.update { it.copy(updateCheckInFlight = false) }
    }
  }

  fun maybeShowFeedbackNudge(
    isOnboardingCompleted: Boolean,
    appVersion: String,
    clock: () -> Long,
  ): Boolean {
    if (!isOnboardingCompleted) return false
    return appLifecycleUseCase.shouldShowFeedbackNudge(appVersion, clock())
  }

  suspend fun maybeRequestInAppReview(
    isOnboardingCompleted: Boolean,
    dataFreshness: DataFreshness,
    appVersion: String,
    clock: () -> Long,
  ) {
    if (!isOnboardingCompleted) return
    val eligibility =
      appLifecycleUseCase.checkReviewEligibility(
        appVersion = appVersion,
        onboardingCompleted = isOnboardingCompleted,
        currentFreshness = dataFreshness,
        nowEpoch = clock(),
      )
    if (eligibility.isEligible) {
      appLifecycleUseCase.requestInAppReview()
      appLifecycleUseCase.markReviewPrompted(appVersion, clock())
    }
  }

  /**
   * Inicia el polling de actualizaciones descargadas.
   *
   * Devuelve el [Job] para que el llamador (AppRootViewModel) lo gestione
   * en su propio ciclo de vida en lugar de almacenarlo en el estado.
   */
  fun startUpdatePolling(
    scope: CoroutineScope,
    updateUiState: (TopUpdateBanner) -> Unit,
  ): Job {
    return scope.launch {
      while (true) {
        delay(3_000)
        val status = appLifecycleUseCase.checkForUpdate()
        if (status is UpdateAvailabilityState.Downloaded) {
          updateUiState(TopUpdateBanner.Downloaded(status.versionName))
          break
        }
      }
    }
  }
}

@Inject
internal class RefreshOrchestrator(
  private val startupUseCase: StartupUseCase,
  private val appInitializer: AppInitializer,
) {
  fun maybeRefreshSurfaceSnapshot(
    scope: CoroutineScope,
    uiState: AppRootUiState,
  ) {
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    scope.launch { appInitializer.refreshSurfaceSnapshot() }
  }

  fun maybeRefreshStations(
    scope: CoroutineScope,
    uiState: AppRootUiState,
    runtimeState: MutableStateFlow<AppRootRuntimeState>,
    refreshJob: MutableStateFlow<Job?>,
    onInitialLoadFinished: () -> Unit,
    recomputeStartupLaunchReady: () -> Unit,
  ) {
    val runtime = runtimeState.value
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    if (runtime.pendingRefreshSignals == 0) return
    if (refreshJob.value?.isActive == true) return

    runtimeState.update { it.copy(pendingRefreshSignals = 0) }
    refreshJob.value =
      scope.launch {
        appInitializer.refreshStations()
        onInitialLoadFinished()
        recomputeStartupLaunchReady()
        if (runtimeState.value.pendingRefreshSignals > 0) {
          maybeRefreshStations(
            scope = scope,
            uiState = uiState,
            runtimeState = runtimeState,
            refreshJob = refreshJob,
            onInitialLoadFinished = onInitialLoadFinished,
            recomputeStartupLaunchReady = recomputeStartupLaunchReady,
          )
        }
      }
  }

  fun maybeScheduleEmptyStateRetry(
    scope: CoroutineScope,
    emptyStateRetryJob: MutableStateFlow<Job?>,
    stationsState: StationsState,
  ) {
    emptyStateRetryJob.value?.cancel()
    if (stationsState.isLoading || stationsState.stations.isNotEmpty() || stationsState.errorMessage != null) {
      return
    }
    emptyStateRetryJob.value =
      scope.launch {
        delay(5_000)
        val latestState = startupUseCase.stationsState.value
        if (!latestState.isLoading && latestState.stations.isEmpty() && latestState.errorMessage == null) {
          appInitializer.loadStationsIfNeeded()
        }
      }
  }
}
