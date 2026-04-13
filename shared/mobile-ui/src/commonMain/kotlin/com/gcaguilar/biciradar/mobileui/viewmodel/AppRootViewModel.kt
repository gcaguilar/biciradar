package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AppVersion
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection
import com.gcaguilar.biciradar.mobileui.initialization.AppInitializer
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.OnboardingLaunchSource
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
  val cityConfigured: Boolean? = null,
  val onboardingChecklist: OnboardingChecklistSnapshot = OnboardingChecklistSnapshot(),
  val isCitySelectionRequired: Boolean = false,
  val shouldShowGuidedOnboarding: Boolean = false,
  val changelogPresentation: ChangelogPresentation? = null,
  val topUpdateBanner: TopUpdateBanner = TopUpdateBanner.Hidden,
  val showFeedbackNudge: Boolean = false,
)

internal data class AppRootRuntimeState(
  val latestStationsState: StationsState,
  val latestFavoriteCount: Int,
  val latestFavoriteIds: Set<String>,
  val latestHomeStationId: String?,
  val latestWorkStationId: String?,
  val latestOnboardingCompleted: Boolean,
  val latestOnboardingChecklist: OnboardingChecklistSnapshot,
  val pendingRefreshSignals: Int = 0,
  // Estados runtime de sub-módulos (antes en ViewModels separados)
  val updateCheckInFlight: Boolean = false,
  // NOTA: updatePollJob se gestiona como campo privado en AppRootViewModel para
  // mantener AppRootRuntimeState como dato puro (Job tiene identidad mutable).
  val suppressGuidedOnboardingForNavigation: Boolean = false,
  val onboardingLaunchSource: OnboardingLaunchSource = OnboardingLaunchSource.Automatic,
)

private data class StartupSnapshot(
  val stationsState: StationsState,
  val favoriteIds: Set<String>,
  val homeStationId: String?,
  val workStationId: String?,
  val onboardingChecklist: OnboardingChecklistSnapshot,
  val hasCompletedOnboarding: Boolean,
)

/**
 * ViewModel principal de la aplicación - COORDINADOR.
 *
 * Este ViewModel integra la lógica de todos los sub-módulos:
 * - Engagement (updates, feedback, reviews)
 * - Onboarding (flujo guiado)
 * - Changelog (novedades de versión)
 *
 * Antes instanciaba child ViewModels directamente, lo que evitaba el lifecycle
 * correcto. Ahora integra su lógica directamente, garantizando que onCleared()
 * cancele todas las coroutines y jobs pendientes.
 *
 * SRP: Coordina la inicialización y gestiona el estado UI de toda la app.
 */
@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
internal class AppRootViewModel(
  private val startupUseCase: StartupUseCase,
  private val appLifecycleUseCase: AppLifecycleUseCase,
  private val appInitializer: AppInitializer,
  private val onboardingCoordinator: OnboardingCoordinator,
  private val engagementCoordinator: EngagementCoordinator,
  private val refreshOrchestrator: RefreshOrchestrator,
  @AppVersion private val appVersion: String,
) : ViewModel() {
  private val clock: () -> Long = ::epochMillisForUi
  private val _uiState = MutableStateFlow(AppRootUiState())
  val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

  private val runtimeState =
    MutableStateFlow(
      AppRootRuntimeState(
        latestStationsState = startupUseCase.stationsState.value,
        latestFavoriteCount = startupUseCase.favoriteIds.value.size,
        latestFavoriteIds = startupUseCase.favoriteIds.value,
        latestHomeStationId = startupUseCase.homeStationId.value,
        latestWorkStationId = startupUseCase.workStationId.value,
        latestOnboardingCompleted = startupUseCase.isOnboardingCompleted(),
        latestOnboardingChecklist = startupUseCase.currentOnboardingChecklist(),
      ),
    )

  private val refreshJob = MutableStateFlow<Job?>(null)
  private val emptyStateRetryJob = MutableStateFlow<Job?>(null)

  /** Job de polling de actualizaciones. Se gestiona aquí para no contaminar [AppRootRuntimeState]. */
  private var updatePollJob: Job? = null

  init {
    observeRepositories()
    bootstrap()
    startMinimumSplashTimer()
  }

  /**
   * Cancela todos los jobs pendientes cuando el ViewModel se destruye.
   * Esto garantiza la limpieza adecuada que antes no estaba asegurada
   * con child ViewModels instanciados manualmente.
   */
  override fun onCleared() {
    updatePollJob?.cancel()
    refreshJob.value?.cancel()
    emptyStateRetryJob.value?.cancel()
    super.onCleared()
  }

  fun onRefreshSignal() {
    runtimeState.update { it.copy(pendingRefreshSignals = it.pendingRefreshSignals + 1) }
    maybeRefreshStations()
  }

  // ================== ONBOARDING ==================

  fun onOnboardingOpenFavoritesRequested() {
    runtimeState.update { it.copy(suppressGuidedOnboardingForNavigation = true) }
    recomputeOnboardingPresentation()
  }

  fun onOnboardingOpenedFromSettings() {
    runtimeState.update { it.copy(onboardingLaunchSource = OnboardingLaunchSource.Settings) }
    recomputeOnboardingPresentation()
  }

  fun onOnboardingFeatureHighlightsContinued() {
    viewModelScope.launch {
      onboardingCoordinator.updateChecklist { it.copy(featureHighlightsSeen = true) }
    }
  }

  fun onOnboardingLocationDecisionMade() {
    viewModelScope.launch {
      onboardingCoordinator.updateChecklist { it.copy(locationDecisionMade = true) }
    }
  }

  fun onOnboardingNotificationsDecisionMade() {
    viewModelScope.launch {
      onboardingCoordinator.updateChecklist { it.copy(notificationsDecisionMade = true) }
    }
  }

  fun onOnboardingFirstFavoriteDismissed() {
    viewModelScope.launch {
      onboardingCoordinator.updateChecklist { it.copy(firstStationSaved = true) }
    }
  }

  fun onOnboardingSavedPlacesDismissed() {
    viewModelScope.launch {
      onboardingCoordinator.updateChecklist { it.copy(savedPlacesConfigured = true) }
    }
  }

  fun onOnboardingFavoritesDismissed() {
    viewModelScope.launch {
      onboardingCoordinator.updateChecklist {
        it.copy(firstStationSaved = true, savedPlacesConfigured = true)
      }
    }
  }

  fun onSkipOnboarding() {
    viewModelScope.launch {
      onboardingCoordinator.skipOnboarding()
    }
  }

  fun onOnboardingSurfacesCompleted() {
    viewModelScope.launch {
      onboardingCoordinator.completeSurfaces()
    }
  }

  // ================== CHANGELOG ==================

  fun showChangelogHistory() {
    val presentation = appLifecycleUseCase.getChangelogHistory() ?: return
    _uiState.update { it.copy(changelogPresentation = presentation) }
  }

  fun dismissChangelog() {
    val presentation = _uiState.value.changelogPresentation
    _uiState.update { it.copy(changelogPresentation = null) }
    val persistSeenVersion = presentation?.persistSeenVersion ?: return
    viewModelScope.launch {
      appLifecycleUseCase.markChangelogSeen(persistSeenVersion)
    }
  }

  // ================== ENGAGEMENT ==================

  fun onFeedbackOpened() {
    _uiState.update { it.copy(showFeedbackNudge = false) }
    viewModelScope.launch {
      appLifecycleUseCase.markFeedbackOpened(clock())
    }
  }

  fun onFeedbackDismissed() {
    _uiState.update { it.copy(showFeedbackNudge = false) }
    viewModelScope.launch {
      appLifecycleUseCase.markFeedbackDismissed(clock())
    }
  }

  fun dismissAvailableUpdate(version: String) {
    _uiState.update { it.copy(topUpdateBanner = TopUpdateBanner.Hidden) }
    viewModelScope.launch {
      appLifecycleUseCase.markUpdateBannerDismissed(version, clock())
    }
  }

  fun dismissDownloadedUpdate() {
    _uiState.update { it.copy(topUpdateBanner = TopUpdateBanner.Hidden) }
  }

  fun onStartUpdateRequested() {
    val banner = _uiState.value.topUpdateBanner as? TopUpdateBanner.Available ?: return
    viewModelScope.launch {
      if (banner.flexible) {
        if (appLifecycleUseCase.startFlexibleUpdate()) {
          startUpdatePolling()
        }
      } else {
        appLifecycleUseCase.openStoreListing()
      }
    }
  }

  fun onRestartToUpdateRequested() {
    viewModelScope.launch {
      appLifecycleUseCase.completeFlexibleUpdateIfReady()
    }
  }

  // ================== PRIVATE ==================

  private fun observeRepositories() {
    val stationsAndFavorites =
      combine(
        startupUseCase.stationsState,
        startupUseCase.favoriteIds,
        startupUseCase.homeStationId,
      ) { stationsState, favoriteIds, homeStationId ->
        Triple(stationsState, favoriteIds, homeStationId)
      }
    val onboardingAndWork =
      combine(
        startupUseCase.workStationId,
        startupUseCase.onboardingChecklist,
        startupUseCase.hasCompletedOnboarding,
      ) { workStationId, checklist, completed ->
        Triple(workStationId, checklist, completed)
      }
    combine(stationsAndFavorites, onboardingAndWork) { left, right ->
      val (stationsState, favoriteIds, homeStationId) = left
      val (workStationId, checklist, completed) = right
      StartupSnapshot(
        stationsState = stationsState,
        favoriteIds = favoriteIds,
        homeStationId = homeStationId,
        workStationId = workStationId,
        onboardingChecklist = checklist,
        hasCompletedOnboarding = completed,
      )
    }.onEach { snapshot ->
      val runtime = runtimeState.value
      if (snapshot.favoriteIds.size > runtime.latestFavoriteCount) {
        appLifecycleUseCase.markFavoriteCreated(clock())
      }
      runtimeState.update {
        it.copy(
          latestStationsState = snapshot.stationsState,
          latestFavoriteCount = snapshot.favoriteIds.size,
          latestFavoriteIds = snapshot.favoriteIds,
          latestHomeStationId = snapshot.homeStationId,
          latestWorkStationId = snapshot.workStationId,
          latestOnboardingChecklist = snapshot.onboardingChecklist,
          latestOnboardingCompleted = snapshot.hasCompletedOnboarding,
        )
      }
      _uiState.update { it.copy(cityConfigured = snapshot.onboardingChecklist.cityConfirmed) }
      recomputeStartupLaunchReady()
      refreshOrchestrator.maybeScheduleEmptyStateRetry(viewModelScope, emptyStateRetryJob, snapshot.stationsState)
      refreshOrchestrator.maybeRefreshSurfaceSnapshot(viewModelScope, _uiState.value)
      recomputeOnboardingPresentation(
        checklist = snapshot.onboardingChecklist,
        cityConfigured = snapshot.onboardingChecklist.cityConfirmed,
      )
      maybeAutoCompleteOnboarding()
      appLifecycleUseCase.markDataFreshnessObserved(snapshot.stationsState.freshness)
      maybeRefreshExperiencePrompts()
    }.launchIn(viewModelScope)
  }

  private fun bootstrap() {
    viewModelScope.launch {
      appInitializer.bootstrap(
        onSettingsBootstrapped = {
          _uiState.update { it.copy(settingsBootstrapped = true) }
        },
        onFavoritesBootstrapped = {
          _uiState.update { it.copy(favoritesBootstrapped = true) }
        },
        updatePendingChangelog = ::checkPendingChangelog,
      )

      recomputeStartupLaunchReady()
      refreshOrchestrator.maybeRefreshSurfaceSnapshot(viewModelScope, _uiState.value)
      maybeAutoCompleteOnboarding()
      if (runtimeState.value.pendingRefreshSignals == 0) {
        runtimeState.update { it.copy(pendingRefreshSignals = 1) }
      }
      maybeRefreshStations()
      maybeRefreshExperiencePrompts()
    }
  }

  private fun startMinimumSplashTimer() {
    viewModelScope.launch {
      _uiState.update { it.copy(minimumSplashElapsed = false) }
      delay(700)
      _uiState.update { it.copy(minimumSplashElapsed = true) }
      recomputeStartupLaunchReady()
      maybeRefreshExperiencePrompts()
    }
  }

  private fun checkPendingChangelog() {
    val settingsBootstrapped = _uiState.value.settingsBootstrapped
    val isOnboardingComplete = runtimeState.value.latestOnboardingCompleted

    if (!settingsBootstrapped) return

    val suppression = appLifecycleUseCase.checkChangelogSuppression()
    if (suppression.suppressed &&
      suppression.shouldMarkCurrentVersionSeen &&
      suppression.currentVersionToMark != null
    ) {
      viewModelScope.launch {
        appLifecycleUseCase.markChangelogSeen(suppression.currentVersionToMark)
      }
      return
    }

    val presentation = appLifecycleUseCase.getPendingChangelog()
    if (presentation != null) {
      _uiState.update { it.copy(changelogPresentation = presentation) }
    }
  }

  private fun maybeAutoCompleteOnboarding() {
    viewModelScope.launch {
      onboardingCoordinator.maybeAutoComplete(_uiState.value.settingsBootstrapped, runtimeState.value)
    }
  }

  private fun maybeRefreshStations() {
    refreshOrchestrator.maybeRefreshStations(
      scope = viewModelScope,
      uiState = _uiState.value,
      runtimeState = runtimeState,
      refreshJob = refreshJob,
      onInitialLoadFinished = {
        _uiState.update { state -> state.copy(initialLoadAttemptFinished = true) }
      },
      recomputeStartupLaunchReady = ::recomputeStartupLaunchReady,
    )
  }

  private fun recomputeStartupLaunchReady() {
    val uiState = _uiState.value
    val runtime = runtimeState.value
    _uiState.update {
      uiState.copy(
        startupLaunchReady =
          uiState.settingsBootstrapped &&
            uiState.favoritesBootstrapped &&
            uiState.minimumSplashElapsed &&
            (
              uiState.initialLoadAttemptFinished ||
                runtime.latestStationsState.stations.isNotEmpty() ||
                runtime.latestStationsState.errorMessage != null
            ) &&
            !(runtime.latestStationsState.isLoading && runtime.latestStationsState.stations.isEmpty()),
      )
    }
  }

  private fun maybeRefreshExperiencePrompts() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.startupLaunchReady) return

    val isOnboardingCompleted = runtimeState.value.latestOnboardingCompleted
    val dataFreshness = runtimeState.value.latestStationsState.freshness

    maybeCheckForUpdates()
    maybeShowFeedbackNudge(isOnboardingCompleted)
    maybeRequestInAppReview(isOnboardingCompleted, dataFreshness)
  }

  // ================== ENGAGEMENT LOGIC (antes en EngagementViewModel) ==================

  private fun maybeCheckForUpdates() {
    viewModelScope.launch {
      engagementCoordinator.maybeCheckForUpdates(runtimeState, { banner ->
        _uiState.update { it.copy(topUpdateBanner = banner) }
      }, clock)
    }
  }

  private fun startUpdatePolling() {
    updatePollJob =
      engagementCoordinator.startUpdatePolling(viewModelScope) { banner ->
        _uiState.update { it.copy(topUpdateBanner = banner) }
      }
  }

  private fun maybeShowFeedbackNudge(isOnboardingCompleted: Boolean) {
    val shouldShow = engagementCoordinator.maybeShowFeedbackNudge(isOnboardingCompleted, appVersion, clock)
    if (shouldShow) {
      _uiState.update { it.copy(showFeedbackNudge = true) }
    }
  }

  private fun maybeRequestInAppReview(
    isOnboardingCompleted: Boolean,
    dataFreshness: DataFreshness,
  ) {
    viewModelScope.launch {
      engagementCoordinator.maybeRequestInAppReview(isOnboardingCompleted, dataFreshness, appVersion, clock)
    }
  }

  // ================== ONBOARDING LOGIC (antes en OnboardingViewModel) ==================

  private fun recomputeOnboardingPresentation(
    checklist: OnboardingChecklistSnapshot? = null,
    cityConfigured: Boolean? = null,
  ) {
    val currentChecklist = checklist ?: startupUseCase.currentOnboardingChecklist()
    val currentCityConfigured = cityConfigured ?: currentChecklist.cityConfirmed
    val currentRuntime = runtimeState.value

    val resolved = onboardingCoordinator.resolvePresentation(currentChecklist, currentCityConfigured, currentRuntime)

    _uiState.update {
      it.copy(
        onboardingChecklist = resolved.onboardingChecklist,
        isCitySelectionRequired = resolved.isCitySelectionRequired,
        shouldShowGuidedOnboarding = resolved.shouldShowGuidedOnboarding,
      )
    }

    // Resetear supresión de navegación si es necesario
    if (resolved.shouldResetNavigationSuppression ||
      (currentRuntime.onboardingLaunchSource == OnboardingLaunchSource.Settings && !resolved.shouldShowGuidedOnboarding)
    ) {
      runtimeState.update { currentRuntimeCopy ->
        currentRuntimeCopy.copy(
          suppressGuidedOnboardingForNavigation =
            if (resolved.shouldResetNavigationSuppression) {
              false
            } else {
              currentRuntimeCopy.suppressGuidedOnboardingForNavigation
            },
          onboardingLaunchSource = resolved.launchSource,
        )
      }
    }
  }
}
