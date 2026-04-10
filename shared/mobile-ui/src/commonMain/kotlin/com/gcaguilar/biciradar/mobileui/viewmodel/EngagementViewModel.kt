package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para experiencia de usuario (engagement).
 */
internal data class EngagementUiState(
  val topUpdateBanner: TopUpdateBanner = TopUpdateBanner.Hidden,
  val showFeedbackNudge: Boolean = false,
)

/**
 * ViewModel especializado en gestionar la experiencia de usuario:
 * - Solicitudes de review
 * - Feedback nudges  
 * - Actualizaciones de app
 *
 * SRP: Solo se encarga de engagement y prompts de experiencia.
 */
internal class EngagementViewModel(
  private val appLifecycleUseCase: AppLifecycleUseCase,
  private val appVersion: String,
  private val clock: () -> Long = ::epochMillisForUi,
) : ViewModel() {

  private val _uiState = MutableStateFlow(EngagementUiState())
  val uiState: StateFlow<EngagementUiState> = _uiState.asStateFlow()

  private val runtimeState = MutableStateFlow(
    EngagementRuntimeState()
  )

  /**
   * Inicia los checks de experiencia (llamar cuando el startup esté listo).
   */
  fun startExperienceChecks(
    isOnboardingCompleted: Boolean,
    dataFreshness: DataFreshness,
  ) {
    maybeCheckForUpdates()
    maybeShowFeedbackNudge(isOnboardingCompleted)
    maybeRequestInAppReview(isOnboardingCompleted, dataFreshness)
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

  override fun onCleared() {
    runtimeState.value.updatePollJob?.cancel()
    super.onCleared()
  }

  private fun maybeCheckForUpdates() {
    if (runtimeState.value.updateCheckInFlight) return

    runtimeState.update { it.copy(updateCheckInFlight = true) }
    viewModelScope.launch {
      try {
        appLifecycleUseCase.markUpdateChecked(clock())
        when (val update = appLifecycleUseCase.checkForUpdate()) {
          is UpdateAvailabilityState.Available -> {
            _uiState.update {
              it.copy(
                topUpdateBanner = TopUpdateBanner.Available(
                  version = update.versionName,
                  flexible = update.isFlexibleAllowed,
                  storeUrl = update.storeUrl,
                ),
              )
            }
          }
          is UpdateAvailabilityState.Downloaded -> {
            _uiState.update { it.copy(topUpdateBanner = TopUpdateBanner.Downloaded(update.versionName)) }
          }
          else -> Unit
        }
      } finally {
        runtimeState.update { it.copy(updateCheckInFlight = false) }
      }
    }
  }

  private fun maybeShowFeedbackNudge(isOnboardingCompleted: Boolean) {
    if (_uiState.value.showFeedbackNudge || runtimeState.value.feedbackNudgeInFlight) return
    if (!isOnboardingCompleted) return
    if (!appLifecycleUseCase.shouldShowFeedbackNudge(appVersion, clock())) return

    runtimeState.update { it.copy(feedbackNudgeInFlight = true) }
    _uiState.update { it.copy(showFeedbackNudge = true) }

    viewModelScope.launch {
      try {
        appLifecycleUseCase.markFeedbackNudgeShown(appVersion, clock())
      } finally {
        runtimeState.update { it.copy(feedbackNudgeInFlight = false) }
      }
    }
  }

  private fun maybeRequestInAppReview(
    isOnboardingCompleted: Boolean,
    dataFreshness: DataFreshness,
  ) {
    if (runtimeState.value.inAppReviewRequested) return
    if (!isOnboardingCompleted) return
    if (dataFreshness == DataFreshness.Unavailable) return

    val eligibility = appLifecycleUseCase.checkReviewEligibility(
      appVersion = appVersion,
      onboardingCompleted = isOnboardingCompleted,
      currentFreshness = dataFreshness,
      nowEpoch = clock(),
    )
    if (!eligibility.isEligible) return

    runtimeState.update { it.copy(inAppReviewRequested = true) }
    viewModelScope.launch {
      delay(4_000)
      appLifecycleUseCase.markReviewPrompted(appVersion, clock())
      appLifecycleUseCase.requestInAppReview()
    }
  }

  private fun startUpdatePolling() {
    runtimeState.value.updatePollJob?.cancel()
    val job = viewModelScope.launch {
      repeat(10) {
        delay(8_000)
        when (val update = appLifecycleUseCase.checkForUpdate()) {
          is UpdateAvailabilityState.Downloaded -> {
            _uiState.update { it.copy(topUpdateBanner = TopUpdateBanner.Downloaded(update.versionName)) }
            return@launch
          }
          else -> Unit
        }
      }
    }
    runtimeState.update { it.copy(updatePollJob = job) }
  }

  /**
   * Estado runtime para engagement.
   */
  private data class EngagementRuntimeState(
    val inAppReviewRequested: Boolean = false,
    val updateCheckInFlight: Boolean = false,
    val feedbackNudgeInFlight: Boolean = false,
    val updatePollJob: Job? = null,
  )
}
