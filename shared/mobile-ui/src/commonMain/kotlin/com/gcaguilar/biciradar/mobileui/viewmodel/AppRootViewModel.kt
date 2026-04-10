package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.initialization.AppInitializer
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.ResolveOnboardingPresentationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
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
  val latestOnboardingChecklist: com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot,
  val pendingRefreshSignals: Int = 0,
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
 * ViewModel principal de la aplicación - COORDINADOR con ViewModels especializados.
 *
 * Este ViewModel delega responsabilidades específicas a ViewModels especializados:
 * - [EngagementViewModel]: Gestiona reviews, feedback y actualizaciones
 * - [OnboardingViewModel]: Gestiona todo el flujo de onboarding
 * - [ChangelogViewModel]: Gestiona el changelog
 *
 * SRP: Coordina la inicialización y delega responsabilidades específicas.
 *
 * @param startupUseCase Caso de uso para acceder a repositorios de startup
 * @param appLifecycleUseCase Caso de uso para ciclo de vida de la app
 * @param resolveOnboardingPresentationUseCase Caso de uso para resolver presentación de onboarding
 * @param appInitializer Inicializador de la aplicación
 * @param appVersion Versión de la aplicación
 * @param clock Función para obtener el tiempo actual (inyectable para testing)
 */
internal class AppRootViewModel(
  private val startupUseCase: StartupUseCase,
  private val appLifecycleUseCase: AppLifecycleUseCase,
  private val resolveOnboardingPresentationUseCase: ResolveOnboardingPresentationUseCase,
  private val appInitializer: AppInitializer,
  private val appVersion: String,
  private val clock: () -> Long = ::epochMillisForUi,
) : ViewModel() {

  private val _uiState = MutableStateFlow(AppRootUiState())
  val uiState: StateFlow<AppRootUiState> = _uiState.asStateFlow()

  private val runtimeState = MutableStateFlow(
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

  // ViewModels especializados
  private val engagementViewModel = EngagementViewModel(
    appLifecycleUseCase = appLifecycleUseCase,
    appVersion = appVersion,
    clock = clock,
  )

  private val onboardingViewModel = OnboardingViewModel(
    startupUseCase = startupUseCase,
    resolveOnboardingPresentationUseCase = resolveOnboardingPresentationUseCase,
  )

  private val changelogViewModel = ChangelogViewModel(
    appLifecycleUseCase = appLifecycleUseCase,
  )

  private val refreshJob = MutableStateFlow<Job?>(null)
  private val emptyStateRetryJob = MutableStateFlow<Job?>(null)

  init {
    observeRepositories()
    observeSpecializedViewModels()
    bootstrap()
    startMinimumSplashTimer()
  }

  /**
   * Observa los cambios en los ViewModels especializados y sincroniza con el estado principal.
   */
  private fun observeSpecializedViewModels() {
    // Engagement
    engagementViewModel.uiState
      .onEach { engagementState ->
        _uiState.update {
          it.copy(
            topUpdateBanner = engagementState.topUpdateBanner,
            showFeedbackNudge = engagementState.showFeedbackNudge,
          )
        }
      }
      .launchIn(viewModelScope)

    // Onboarding
    onboardingViewModel.uiState
      .onEach { onboardingState ->
        _uiState.update {
          it.copy(
            onboardingChecklist = onboardingState.onboardingChecklist,
            isCitySelectionRequired = onboardingState.isCitySelectionRequired,
            shouldShowGuidedOnboarding = onboardingState.shouldShowGuidedOnboarding,
          )
        }
      }
      .launchIn(viewModelScope)

    // Changelog
    changelogViewModel.uiState
      .onEach { changelogState ->
        _uiState.update {
          it.copy(changelogPresentation = changelogState.changelogPresentation)
        }
      }
      .launchIn(viewModelScope)
  }

  fun onRefreshSignal() {
    runtimeState.update { it.copy(pendingRefreshSignals = it.pendingRefreshSignals + 1) }
    maybeRefreshStations()
  }

  // Onboarding delegates
  fun onOnboardingOpenFavoritesRequested() {
    onboardingViewModel.onOpenFavoritesRequested()
  }

  fun onOnboardingOpenedFromSettings() {
    onboardingViewModel.onOpenedFromSettings()
  }

  fun onOnboardingFeatureHighlightsContinued() {
    onboardingViewModel.onFeatureHighlightsContinued()
  }

  fun onOnboardingLocationDecisionMade() {
    onboardingViewModel.onLocationDecisionMade()
  }

  fun onOnboardingNotificationsDecisionMade() {
    onboardingViewModel.onNotificationsDecisionMade()
  }

  fun onOnboardingFirstFavoriteDismissed() {
    onboardingViewModel.onFirstFavoriteDismissed()
  }

  fun onOnboardingSavedPlacesDismissed() {
    onboardingViewModel.onSavedPlacesDismissed()
  }

  fun onOnboardingFavoritesDismissed() {
    onboardingViewModel.onFavoritesDismissed()
  }

  fun onSkipOnboarding() {
    onboardingViewModel.onSkipOnboarding()
  }

  fun onOnboardingSurfacesCompleted() {
    onboardingViewModel.onSurfacesCompleted()
  }

  // Changelog delegates
  fun showChangelogHistory() {
    changelogViewModel.showChangelogHistory()
  }

  fun dismissChangelog() {
    changelogViewModel.dismissChangelog()
  }

  // Engagement delegates
  fun onFeedbackOpened() {
    engagementViewModel.onFeedbackOpened()
  }

  fun onFeedbackDismissed() {
    engagementViewModel.onFeedbackDismissed()
  }

  fun dismissAvailableUpdate(version: String) {
    engagementViewModel.dismissAvailableUpdate(version)
  }

  fun dismissDownloadedUpdate() {
    engagementViewModel.dismissDownloadedUpdate()
  }

  fun onStartUpdateRequested() {
    engagementViewModel.onStartUpdateRequested()
  }

  fun onRestartToUpdateRequested() {
    engagementViewModel.onRestartToUpdateRequested()
  }

  private fun observeRepositories() {
    val stationsAndFavorites = combine(
      startupUseCase.stationsState,
      startupUseCase.favoriteIds,
      startupUseCase.homeStationId,
    ) { stationsState, favoriteIds, homeStationId ->
      Triple(stationsState, favoriteIds, homeStationId)
    }
    val onboardingAndWork = combine(
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
      maybeScheduleEmptyStateRetry(snapshot.stationsState)
      maybeRefreshSurfaceSnapshot()

      // Actualizar ViewModels especializados
      onboardingViewModel.updateOnboardingState(
        latestOnboardingChecklist = snapshot.onboardingChecklist,
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
      maybeRefreshSurfaceSnapshot()
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
    changelogViewModel.checkPendingChangelog(
      isSettingsBootstrapped = _uiState.value.settingsBootstrapped,
      isOnboardingComplete = runtimeState.value.latestOnboardingCompleted,
    )
  }

  private fun maybeAutoCompleteOnboarding() {
    if (!_uiState.value.settingsBootstrapped) return
    val runtime = runtimeState.value
    if (runtime.latestOnboardingChecklist.cityConfirmed &&
      !runtime.latestOnboardingChecklist.firstStationSaved &&
      runtime.latestFavoriteIds.isNotEmpty()
    ) {
      viewModelScope.launch {
        startupUseCase.updateOnboardingChecklist { snapshot ->
          if (snapshot.firstStationSaved) snapshot else snapshot.copy(firstStationSaved = true)
        }
      }
    }
    if (!runtime.latestOnboardingChecklist.savedPlacesConfigured &&
      runtime.latestHomeStationId != null &&
      runtime.latestWorkStationId != null
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
    val runtime = runtimeState.value
    if (!uiState.settingsBootstrapped || !uiState.favoritesBootstrapped) return
    if (runtime.pendingRefreshSignals == 0) return
    if (refreshJob.value?.isActive == true) return
    runtimeState.update { it.copy(pendingRefreshSignals = 0) }
    refreshJob.update {
      viewModelScope.launch {
        appInitializer.refreshStations()
        _uiState.update { state -> state.copy(initialLoadAttemptFinished = true) }
        recomputeStartupLaunchReady()
        if (runtimeState.value.pendingRefreshSignals > 0) {
          maybeRefreshStations()
        }
      }
    }
  }

  private fun maybeScheduleEmptyStateRetry(stationsState: StationsState) {
    emptyStateRetryJob.value?.cancel()
    if (stationsState.isLoading || stationsState.stations.isNotEmpty() || stationsState.errorMessage != null) {
      return
    }
    emptyStateRetryJob.update {
      viewModelScope.launch {
        delay(5_000)
        val latestState = startupUseCase.stationsState.value
        if (!latestState.isLoading && latestState.stations.isEmpty() && latestState.errorMessage == null) {
          appInitializer.loadStationsIfNeeded()
        }
      }
    }
  }

  private fun recomputeStartupLaunchReady() {
    val uiState = _uiState.value
    val runtime = runtimeState.value
    _uiState.update {
      uiState.copy(
        startupLaunchReady = uiState.settingsBootstrapped &&
          uiState.favoritesBootstrapped &&
          uiState.minimumSplashElapsed &&
          (uiState.initialLoadAttemptFinished || runtime.latestStationsState.stations.isNotEmpty() || runtime.latestStationsState.errorMessage != null) &&
          !(runtime.latestStationsState.isLoading && runtime.latestStationsState.stations.isEmpty()),
      )
    }
  }

  private fun maybeRefreshExperiencePrompts() {
    val uiState = _uiState.value
    if (!uiState.settingsBootstrapped || !uiState.startupLaunchReady) return
    engagementViewModel.startExperienceChecks(
      isOnboardingCompleted = runtimeState.value.latestOnboardingCompleted,
      dataFreshness = runtimeState.value.latestStationsState.freshness,
    )
  }
}
