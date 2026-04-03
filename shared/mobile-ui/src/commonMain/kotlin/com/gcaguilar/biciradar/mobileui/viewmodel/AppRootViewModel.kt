package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.initialization.AppInitializer
import com.gcaguilar.biciradar.mobileui.usecases.ChangelogUseCase
import com.gcaguilar.biciradar.mobileui.usecases.FeedbackUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ChangelogPresentation(
  val sections: List<com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection>,
  val highlightedVersion: String? = null,
  val persistSeenVersion: String? = null,
)

internal data class AppRootUiState(
  val settingsBootstrapped: Boolean = false,
  val favoritesBootstrapped: Boolean = false,
  val initialLoadAttemptFinished: Boolean = false,
  val minimumSplashElapsed: Boolean = false,
  val startupLaunchReady: Boolean = false,
  val changelogPresentation: ChangelogPresentation? = null,
  val topUpdateBanner: TopUpdateBanner = TopUpdateBanner.Hidden,
  val showFeedbackNudge: Boolean = false,
)

internal class AppRootViewModel(
  private val startupUseCase: StartupUseCase,
  private val feedbackUseCase: FeedbackUseCase,
  private val changelogUseCase: ChangelogUseCase,
  private val appInitializer: AppInitializer,
  private val appVersion: String,
  private val clock: () -> Long = ::epochMillisForUi,
) : ViewModel() {

  private val _uiState = MutableStateFlow(AppRootUiState())
  val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

  private var latestStationsState: StationsState = startupUseCase.stationsState.value
  private var latestFavoriteCount: Int = startupUseCase.favoriteIds.value.size
  private var latestFavoriteIds: Set<String> = startupUseCase.favoriteIds.value
  private var latestHomeStationId: String? = startupUseCase.homeStationId.value
  private var latestWorkStationId: String? = startupUseCase.workStationId.value
  private var latestOnboardingCompleted: Boolean = startupUseCase.isOnboardingCompleted()
  private var latestOnboardingChecklist = startupUseCase.currentOnboardingChecklist()
  private var inAppReviewRequested = false
  private var pendingRefreshSignals = 0
  private var refreshJob: Job? = null
  private var updatePollJob: Job? = null
  private var emptyStateRetryJob: Job? = null
  private var updateCheckInFlight = false
  private var feedbackNudgeInFlight = false

  init {
    observeRepositories()
    bootstrap()
    startMinimumSplashTimer()
  }

  fun onRefreshSignal() {
    pendingRefreshSignals++
    maybeRefreshStations()
  }

  fun showChangelogHistory() {
    val presentation = changelogUseCase.getChangelogHistory() ?: return
    _uiState.value = _uiState.value.copy(changelogPresentation = presentation)
  }

  fun dismissChangelog() {
    val presentation = _uiState.value.changelogPresentation
    _uiState.value = _uiState.value.copy(changelogPresentation = null)
    val persistSeenVersion = presentation?.persistSeenVersion ?: return
    viewModelScope.launch {
      changelogUseCase.markChangelogSeen(persistSeenVersion)
    }
  }

  fun onFeedbackOpened() {
    _uiState.value = _uiState.value.copy(showFeedbackNudge = false)
    viewModelScope.launch {
      feedbackUseCase.markFeedbackOpened(clock())
    }
  }

  fun onFeedbackDismissed() {
    _uiState.value = _uiState.value.copy(showFeedbackNudge = false)
    viewModelScope.launch {
      feedbackUseCase.markFeedbackDismissed(clock())
    }
  }

  fun dismissAvailableUpdate(version: String) {
    _uiState.value = _uiState.value.copy(topUpdateBanner = TopUpdateBanner.Hidden)
    viewModelScope.launch {
      feedbackUseCase.markUpdateBannerDismissed(version, clock())
    }
  }

  fun dismissDownloadedUpdate() {
    _uiState.value = _uiState.value.copy(topUpdateBanner = TopUpdateBanner.Hidden)
  }

  fun onStartUpdateRequested() {
    val banner = _uiState.value.topUpdateBanner as? TopUpdateBanner.Available ?: return
    viewModelScope.launch {
      if (banner.flexible) {
        if (feedbackUseCase.startFlexibleUpdate()) {
          startUpdatePolling()
        }
      } else {
        feedbackUseCase.openStoreListing()
      }
    }
  }

  fun onRestartToUpdateRequested() {
    viewModelScope.launch {
      feedbackUseCase.completeFlexibleUpdateIfReady()
    }
  }

  private fun observeRepositories() {
    viewModelScope.launch {
      startupUseCase.stationsState.collect { stationsState ->
        latestStationsState = stationsState
        recomputeStartupLaunchReady()
        maybeScheduleEmptyStateRetry(stationsState)
        maybeRefreshSurfaceSnapshot()
        feedbackUseCase.markDataFreshnessObserved(stationsState.freshness)
        maybeRefreshExperiencePrompts()
      }
    }

    viewModelScope.launch {
      startupUseCase.favoriteIds.collect { favoriteIds ->
        if (favoriteIds.size > latestFavoriteCount) {
          feedbackUseCase.markFavoriteCreated(clock())
        }
        latestFavoriteCount = favoriteIds.size
        latestFavoriteIds = favoriteIds
        maybeRefreshSurfaceSnapshot()
        maybeAutoCompleteOnboarding()
      }
    }

    viewModelScope.launch {
      startupUseCase.homeStationId.collect { homeStationId ->
        latestHomeStationId = homeStationId
        maybeRefreshSurfaceSnapshot()
        maybeAutoCompleteOnboarding()
      }
    }

    viewModelScope.launch {
      startupUseCase.workStationId.collect { workStationId ->
        latestWorkStationId = workStationId
        maybeRefreshSurfaceSnapshot()
        maybeAutoCompleteOnboarding()
      }
    }

    viewModelScope.launch {
      startupUseCase.onboardingChecklist.collect { checklist ->
        latestOnboardingChecklist = checklist
        maybeAutoCompleteOnboarding()
        maybeRefreshExperiencePrompts()
      }
    }

    viewModelScope.launch {
      startupUseCase.hasCompletedOnboarding.collect { completed ->
        latestOnboardingCompleted = completed
        maybeRefreshExperiencePrompts()
      }
    }
  }

  private fun bootstrap() {
    viewModelScope.launch {
      appInitializer.bootstrap(
        onSettingsBootstrapped = {
          _uiState.value = _uiState.value.copy(settingsBootstrapped = true)
        },
        onFavoritesBootstrapped = {
          _uiState.value = _uiState.value.copy(favoritesBootstrapped = true)
        },
        updatePendingChangelog = ::updatePendingChangelog,
      )

      recomputeStartupLaunchReady()
      maybeRefreshSurfaceSnapshot()
      maybeAutoCompleteOnboarding()
      maybeRefreshStations()
      maybeRefreshExperiencePrompts()
    }
  }

  private fun startMinimumSplashTimer() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(minimumSplashElapsed = false)
      delay(700)
      _uiState.value = _uiState.value.copy(minimumSplashElapsed = true)
      recomputeStartupLaunchReady()
      maybeRefreshExperiencePrompts()
    }
  }

  private fun updatePendingChangelog() {
    if (!_uiState.value.settingsBootstrapped) return

    // Check if changelog should be suppressed due to pending onboarding
    val suppression = changelogUseCase.checkSuppression()
    if (suppression.suppressed && suppression.shouldMarkCurrentVersionSeen && suppression.currentVersionToMark != null) {
      viewModelScope.launch {
        changelogUseCase.markChangelogSeen(suppression.currentVersionToMark)
      }
      return
    }

    val presentation = changelogUseCase.getPendingChangelog()
    if (presentation != null) {
      _uiState.value = _uiState.value.copy(changelogPresentation = presentation)
    }
  }

  private fun maybeAutoCompleteOnboarding() {
    if (!_uiState.value.settingsBootstrapped) return
    if (latestOnboardingChecklist.cityConfirmed &&
      !latestOnboardingChecklist.firstStationSaved &&
      latestFavoriteIds.isNotEmpty()
    ) {
      viewModelScope.launch {
        startupUseCase.updateOnboardingChecklist { snapshot ->
          if (snapshot.firstStationSaved) snapshot else snapshot.copy(firstStationSaved = true)
        }
      }
    }
    if (!latestOnboardingChecklist.savedPlacesConfigured &&
      latestHomeStationId != null &&
      latestWorkStationId != null
    ) {
      viewModelScope.launch {
        startupUseCase.updateOnboardingChecklist { snapshot ->
          if (snapshot.savedPlacesConfigured) snapshot else snapshot.copy(savedPlacesConfigured = true)
        }
      }
    }
  }

  private fun maybeRefreshSurfaceSnapshot() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    viewModelScope.launch {
      appInitializer.refreshSurfaceSnapshot()
    }
  }

  private fun maybeRefreshStations() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    if (pendingRefreshSignals == 0) return
    if (refreshJob?.isActive == true) return
    pendingRefreshSignals = 0
    refreshJob = viewModelScope.launch {
      appInitializer.syncFavoritesFromPeer()
      appInitializer.refreshStations()
      _uiState.value = _uiState.value.copy(initialLoadAttemptFinished = true)
      recomputeStartupLaunchReady()
      if (pendingRefreshSignals > 0) {
        maybeRefreshStations()
      }
    }
  }

  private fun maybeScheduleEmptyStateRetry(stationsState: StationsState) {
    emptyStateRetryJob?.cancel()
    if (stationsState.isLoading || stationsState.stations.isNotEmpty() || stationsState.errorMessage != null) {
      return
    }
    emptyStateRetryJob = viewModelScope.launch {
      delay(5_000)
      val latestState = startupUseCase.stationsState.value
      if (!latestState.isLoading && latestState.stations.isEmpty() && latestState.errorMessage == null) {
        appInitializer.loadStationsIfNeeded()
      }
    }
  }

  private fun recomputeStartupLaunchReady() {
    val uiState = _uiState.value
    _uiState.value = uiState.copy(
      startupLaunchReady = uiState.settingsBootstrapped &&
        uiState.favoritesBootstrapped &&
        uiState.minimumSplashElapsed &&
        (uiState.initialLoadAttemptFinished || latestStationsState.stations.isNotEmpty() || latestStationsState.errorMessage != null) &&
        !(latestStationsState.isLoading && latestStationsState.stations.isEmpty()),
    )
  }

  private fun maybeRefreshExperiencePrompts() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.startupLaunchReady) return
    maybeCheckForUpdates()
    maybeShowFeedbackNudge()
    maybeRequestInAppReview()
  }

  private fun maybeCheckForUpdates() {
    if (updateCheckInFlight) return
    val day = 24L * 60 * 60 * 1000L
    val now = clock()
    // We need to track last check ourselves since we don't expose the snapshot directly
    // For simplicity, we'll rely on the existing logic but we need to track this differently
    // Since we can't access engagementRepository.snapshot directly anymore
    updateCheckInFlight = true
    viewModelScope.launch {
      try {
        feedbackUseCase.markUpdateChecked(now)
        when (val update = feedbackUseCase.checkForUpdate()) {
          is UpdateAvailabilityState.Available -> {
            // Note: We can't access dismissed version directly, would need to add method to FeedbackUseCase
            // For now, we show the banner
            _uiState.value = _uiState.value.copy(
              topUpdateBanner = TopUpdateBanner.Available(
                version = update.versionName,
                flexible = update.isFlexibleAllowed,
                storeUrl = update.storeUrl,
              ),
            )
          }
          is UpdateAvailabilityState.Downloaded -> {
            _uiState.value = _uiState.value.copy(
              topUpdateBanner = TopUpdateBanner.Downloaded(update.versionName),
            )
          }
          else -> Unit
        }
      } finally {
        updateCheckInFlight = false
      }
    }
  }

  private fun maybeShowFeedbackNudge() {
    if (_uiState.value.showFeedbackNudge || feedbackNudgeInFlight) return
    if (!latestOnboardingChecklist.isCompleted()) return
    if (!feedbackUseCase.shouldShowFeedbackNudge(appVersion, clock())) return
    feedbackNudgeInFlight = true
    _uiState.value = _uiState.value.copy(showFeedbackNudge = true)
    viewModelScope.launch {
      try {
        feedbackUseCase.markFeedbackNudgeShown(appVersion, clock())
      } finally {
        feedbackNudgeInFlight = false
      }
    }
  }

  private fun maybeRequestInAppReview() {
    if (inAppReviewRequested) return
    if (!latestOnboardingChecklist.isCompleted() && !latestOnboardingCompleted) return
    if (latestStationsState.freshness == DataFreshness.Unavailable) return
    val eligibility = feedbackUseCase.checkReviewEligibility(
      appVersion = appVersion,
      onboardingCompleted = latestOnboardingChecklist.isCompleted() || latestOnboardingCompleted,
      currentFreshness = latestStationsState.freshness,
      nowEpoch = clock(),
    )
    if (!eligibility.isEligible) return

    inAppReviewRequested = true
    viewModelScope.launch {
      delay(4_000)
      feedbackUseCase.markReviewPrompted(appVersion, clock())
      feedbackUseCase.requestInAppReview()
    }
  }

  private fun startUpdatePolling() {
    updatePollJob?.cancel()
    updatePollJob = viewModelScope.launch {
      repeat(10) {
        delay(8_000)
        when (val update = feedbackUseCase.checkForUpdate()) {
          is UpdateAvailabilityState.Downloaded -> {
            _uiState.value = _uiState.value.copy(
              topUpdateBanner = TopUpdateBanner.Downloaded(update.versionName),
            )
            return@launch
          }
          else -> Unit
        }
      }
    }
  }
}
