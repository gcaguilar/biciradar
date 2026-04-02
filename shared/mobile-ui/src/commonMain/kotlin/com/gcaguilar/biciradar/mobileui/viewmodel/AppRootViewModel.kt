package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.EngagementRepository
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.compareAppVersionStrings
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.core.normalizeAppVersionForCatalog
import com.gcaguilar.biciradar.core.pendingChangelogVersion
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.experience.ChangelogCatalog
import com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal data class ChangelogPresentation(
  val sections: List<ChangelogVersionSection>,
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
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val engagementRepository: EngagementRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val appUpdatePrompter: AppUpdatePrompter,
  private val reviewPrompter: ReviewPrompter,
  private val appVersion: String,
  private val clock: () -> Long = ::epochMillisForUi,
) : ViewModel() {

  private val _uiState = MutableStateFlow(AppRootUiState())
  val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

  private var latestStationsState: StationsState = stationsRepository.state.value
  private var latestFavoriteCount: Int = favoritesRepository.favoriteIds.value.size
  private var latestFavoriteIds: Set<String> = favoritesRepository.favoriteIds.value
  private var latestHomeStationId: String? = favoritesRepository.homeStationId.value
  private var latestWorkStationId: String? = favoritesRepository.workStationId.value
  private var latestOnboardingCompleted: Boolean = settingsRepository.hasCompletedOnboarding.value
  private var latestOnboardingChecklist = settingsRepository.onboardingChecklist.value
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
    val sections = ChangelogCatalog.history()
    if (sections.isEmpty()) return
    _uiState.value = _uiState.value.copy(
      changelogPresentation = ChangelogPresentation(
        sections = sections,
        highlightedVersion = ChangelogCatalog.latestVersionAtOrBefore(appVersion),
        persistSeenVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion,
      ),
    )
  }

  fun dismissChangelog() {
    val presentation = _uiState.value.changelogPresentation
    _uiState.value = _uiState.value.copy(changelogPresentation = null)
    val persistSeenVersion = presentation?.persistSeenVersion ?: return
    viewModelScope.launch {
      settingsRepository.setLastSeenChangelogAppVersion(persistSeenVersion)
    }
  }

  fun onFeedbackOpened() {
    _uiState.value = _uiState.value.copy(showFeedbackNudge = false)
    viewModelScope.launch {
      engagementRepository.markFeedbackOpened(clock())
    }
  }

  fun onFeedbackDismissed() {
    _uiState.value = _uiState.value.copy(showFeedbackNudge = false)
    viewModelScope.launch {
      engagementRepository.markFeedbackDismissed(clock())
    }
  }

  fun dismissAvailableUpdate(version: String) {
    _uiState.value = _uiState.value.copy(topUpdateBanner = TopUpdateBanner.Hidden)
    viewModelScope.launch {
      engagementRepository.markUpdateBannerDismissed(version, clock())
    }
  }

  fun dismissDownloadedUpdate() {
    _uiState.value = _uiState.value.copy(topUpdateBanner = TopUpdateBanner.Hidden)
  }

  fun onStartUpdateRequested() {
    val banner = _uiState.value.topUpdateBanner as? TopUpdateBanner.Available ?: return
    viewModelScope.launch {
      if (banner.flexible) {
        if (appUpdatePrompter.startFlexibleUpdate()) {
          startUpdatePolling()
        }
      } else {
        appUpdatePrompter.openStoreListing()
      }
    }
  }

  fun onRestartToUpdateRequested() {
    viewModelScope.launch {
      appUpdatePrompter.completeFlexibleUpdateIfReady()
    }
  }

  private fun observeRepositories() {
    viewModelScope.launch {
      stationsRepository.state.collect { stationsState ->
        latestStationsState = stationsState
        recomputeStartupLaunchReady()
        maybeScheduleEmptyStateRetry(stationsState)
        maybeRefreshSurfaceSnapshot()
        engagementRepository.markDataFreshnessObserved(stationsState.freshness)
        maybeRefreshExperiencePrompts()
      }
    }

    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { favoriteIds ->
        if (favoriteIds.size > latestFavoriteCount) {
          engagementRepository.markFavoriteCreated(clock())
        }
        latestFavoriteCount = favoriteIds.size
        latestFavoriteIds = favoriteIds
        maybeRefreshSurfaceSnapshot()
        maybeAutoCompleteOnboarding()
      }
    }

    viewModelScope.launch {
      favoritesRepository.homeStationId.collect { homeStationId ->
        latestHomeStationId = homeStationId
        maybeRefreshSurfaceSnapshot()
        maybeAutoCompleteOnboarding()
      }
    }

    viewModelScope.launch {
      favoritesRepository.workStationId.collect { workStationId ->
        latestWorkStationId = workStationId
        maybeRefreshSurfaceSnapshot()
        maybeAutoCompleteOnboarding()
      }
    }

    viewModelScope.launch {
      settingsRepository.onboardingChecklist.collect { checklist ->
        latestOnboardingChecklist = checklist
        maybeAutoCompleteOnboarding()
        maybeRefreshExperiencePrompts()
      }
    }

    viewModelScope.launch {
      settingsRepository.hasCompletedOnboarding.collect { completed ->
        latestOnboardingCompleted = completed
        maybeRefreshExperiencePrompts()
      }
    }

    viewModelScope.launch {
      engagementRepository.snapshot.collect {
        maybeRefreshExperiencePrompts()
      }
    }
  }

  private fun bootstrap() {
    viewModelScope.launch {
      surfaceSnapshotRepository.bootstrap()
      surfaceMonitoringRepository.bootstrap()

      _uiState.value = _uiState.value.copy(settingsBootstrapped = false)
      runCatching { settingsRepository.bootstrap() }
      _uiState.value = _uiState.value.copy(settingsBootstrapped = true)

      updatePendingChangelog()

      runCatching { savedPlaceAlertsRepository.bootstrap() }
      engagementRepository.bootstrap()
      engagementRepository.markSessionStarted(clock())
      engagementRepository.markUsefulSession(clock())

      _uiState.value = _uiState.value.copy(favoritesBootstrapped = false)
      runCatching { favoritesRepository.syncFromPeer() }
      _uiState.value = _uiState.value.copy(favoritesBootstrapped = true)

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
    if (suppressChangelogWhileOnboardingPending()) return
    val lastSeen = settingsRepository.currentLastSeenChangelogAppVersion() ?: "0.0.0"
    val pending = pendingChangelogVersion(
      appVersion,
      lastSeen,
      ChangelogCatalog.catalogVersionSet(),
    )
    val entries = pending?.let { ChangelogCatalog.entriesFor(it) }.orEmpty()
    if (pending != null && entries.isNotEmpty()) {
      _uiState.value = _uiState.value.copy(
        changelogPresentation = ChangelogPresentation(
          sections = ChangelogCatalog.history(),
          highlightedVersion = pending,
          persistSeenVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion,
        ),
      )
    }
  }

  private fun suppressChangelogWhileOnboardingPending(): Boolean {
    val onboardingCompleted = settingsRepository.onboardingChecklist.value.isCompleted() ||
      settingsRepository.hasCompletedOnboarding.value
    if (onboardingCompleted) return false

    val normalizedCurrentVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion
    val normalizedLastSeen = normalizeAppVersionForCatalog(settingsRepository.currentLastSeenChangelogAppVersion())
    if (normalizedLastSeen == null || compareAppVersionStrings(normalizedLastSeen, normalizedCurrentVersion) < 0) {
      viewModelScope.launch {
        settingsRepository.setLastSeenChangelogAppVersion(normalizedCurrentVersion)
      }
    }
    _uiState.value = _uiState.value.copy(changelogPresentation = null)
    return true
  }

  private fun maybeAutoCompleteOnboarding() {
    if (!_uiState.value.settingsBootstrapped) return
    if (latestOnboardingChecklist.cityConfirmed &&
      !latestOnboardingChecklist.firstStationSaved &&
      latestFavoriteIds.isNotEmpty()
    ) {
      viewModelScope.launch {
        settingsRepository.updateOnboardingChecklist { snapshot ->
          if (snapshot.firstStationSaved) snapshot else snapshot.copy(firstStationSaved = true)
        }
      }
    }
    if (!latestOnboardingChecklist.savedPlacesConfigured &&
      latestHomeStationId != null &&
      latestWorkStationId != null
    ) {
      viewModelScope.launch {
        settingsRepository.updateOnboardingChecklist { snapshot ->
          if (snapshot.savedPlacesConfigured) snapshot else snapshot.copy(savedPlacesConfigured = true)
        }
      }
    }
  }

  private fun maybeRefreshSurfaceSnapshot() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    viewModelScope.launch {
      surfaceSnapshotRepository.refreshSnapshot()
    }
  }

  private fun maybeRefreshStations() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    if (pendingRefreshSignals == 0) return
    if (refreshJob?.isActive == true) return
    pendingRefreshSignals = 0
    refreshJob = viewModelScope.launch {
      runCatching { favoritesRepository.syncFromPeer() }
      stationsRepository.forceRefresh()
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
      val latestState = stationsRepository.state.value
      if (!latestState.isLoading && latestState.stations.isEmpty() && latestState.errorMessage == null) {
        stationsRepository.loadIfNeeded()
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
    val snapshot = engagementRepository.snapshot.value
    val lastCheck = snapshot.lastUpdateCheckAtEpoch
    if (lastCheck != null && now - lastCheck < day) return
    updateCheckInFlight = true
    viewModelScope.launch {
      try {
        engagementRepository.markUpdateChecked(nowEpoch = now)
        when (val update = appUpdatePrompter.checkForUpdate()) {
          is UpdateAvailabilityState.Available -> {
            if (update.versionName == engagementRepository.snapshot.value.dismissedUpdateVersion) return@launch
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
    if (!engagementRepository.shouldShowFeedbackNudge(appVersion, clock())) return
    feedbackNudgeInFlight = true
    _uiState.value = _uiState.value.copy(showFeedbackNudge = true)
    viewModelScope.launch {
      try {
        engagementRepository.markFeedbackNudgeShown(appVersion, clock())
      } finally {
        feedbackNudgeInFlight = false
      }
    }
  }

  private fun maybeRequestInAppReview() {
    if (inAppReviewRequested) return
    if (!latestOnboardingChecklist.isCompleted() && !latestOnboardingCompleted) return
    if (latestStationsState.freshness == DataFreshness.Unavailable) return
    val eligibility = engagementRepository.reviewEligibility(
      appVersion = appVersion,
      onboardingCompleted = latestOnboardingChecklist.isCompleted() || latestOnboardingCompleted,
      currentFreshness = latestStationsState.freshness,
      nowEpoch = clock(),
    )
    if (!eligibility.isEligible) return

    inAppReviewRequested = true
    viewModelScope.launch {
      delay(4_000)
      engagementRepository.markReviewPrompted(appVersion, clock())
      reviewPrompter.requestInAppReview()
    }
  }

  private fun startUpdatePolling() {
    updatePollJob?.cancel()
    updatePollJob = viewModelScope.launch {
      repeat(10) {
        delay(8_000)
        when (val update = appUpdatePrompter.checkForUpdate()) {
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
