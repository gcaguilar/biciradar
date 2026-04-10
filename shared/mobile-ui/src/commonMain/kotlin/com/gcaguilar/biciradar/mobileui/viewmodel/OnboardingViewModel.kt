package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.mobileui.usecases.OnboardingLaunchSource
import com.gcaguilar.biciradar.mobileui.usecases.OnboardingPresentationInput
import com.gcaguilar.biciradar.mobileui.usecases.ResolveOnboardingPresentationUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para onboarding.
 */
internal data class OnboardingUiState(
  val onboardingChecklist: OnboardingChecklistSnapshot = OnboardingChecklistSnapshot(),
  val isCitySelectionRequired: Boolean = false,
  val shouldShowGuidedOnboarding: Boolean = false,
)

/**
 * ViewModel especializado en gestionar el onboarding:
 * - Flujo guiado de onboarding
 * - Decisiones de usuario (location, notifications, favorites)
 * - Checklist de onboarding
 *
 * SRP: Solo se encarga del onboarding y su estado.
 */
internal class OnboardingViewModel(
  private val startupUseCase: StartupUseCase,
  private val resolveOnboardingPresentationUseCase: ResolveOnboardingPresentationUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(OnboardingUiState())
  val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

  private val runtimeState = MutableStateFlow(
    OnboardingRuntimeState()
  )

  /**
   * Actualiza el estado de onboarding basado en el estado actual.
   */
  fun updateOnboardingState(
    latestOnboardingChecklist: OnboardingChecklistSnapshot,
    cityConfigured: Boolean,
  ) {
    recomputeOnboardingPresentation(
      checklist = latestOnboardingChecklist,
      cityConfigured = cityConfigured,
    )
  }

  fun onOpenFavoritesRequested() {
    runtimeState.update { it.copy(suppressGuidedOnboardingForNavigation = true) }
    recomputeOnboardingPresentation()
  }

  fun onOpenedFromSettings() {
    runtimeState.update { it.copy(onboardingLaunchSource = OnboardingLaunchSource.Settings) }
    recomputeOnboardingPresentation()
  }

  fun onFeatureHighlightsContinued() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist { it.copy(featureHighlightsSeen = true) }
    }
  }

  fun onLocationDecisionMade() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist { it.copy(locationDecisionMade = true) }
    }
  }

  fun onNotificationsDecisionMade() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist { it.copy(notificationsDecisionMade = true) }
    }
  }

  fun onFirstFavoriteDismissed() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist { it.copy(firstStationSaved = true) }
    }
  }

  fun onSavedPlacesDismissed() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist { it.copy(savedPlacesConfigured = true) }
    }
  }

  fun onFavoritesDismissed() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist {
        it.copy(
          firstStationSaved = true,
          savedPlacesConfigured = true,
        )
      }
    }
  }

  fun onSkipOnboarding() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist {
        it.copy(
          featureHighlightsSeen = true,
          locationDecisionMade = true,
          notificationsDecisionMade = true,
          firstStationSaved = true,
          savedPlacesConfigured = true,
          surfacesDiscovered = true,
        ).markCompleted()
      }
    }
  }

  fun onSurfacesCompleted() {
    viewModelScope.launch {
      startupUseCase.updateOnboardingChecklist { it.copy(surfacesDiscovered = true).markCompleted() }
    }
  }

  private fun recomputeOnboardingPresentation(
    checklist: OnboardingChecklistSnapshot? = null,
    cityConfigured: Boolean? = null,
  ) {
    val currentChecklist = checklist ?: startupUseCase.currentOnboardingChecklist()
    val currentCityConfigured = cityConfigured ?: currentChecklist.cityConfirmed
    val currentRuntime = runtimeState.value

    val resolved = resolveOnboardingPresentationUseCase.execute(
      OnboardingPresentationInput(
        checklist = currentChecklist,
        cityConfigured = currentCityConfigured,
        suppressGuidedOnboardingForNavigation = currentRuntime.suppressGuidedOnboardingForNavigation,
        launchSource = currentRuntime.onboardingLaunchSource,
      ),
    )

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
          suppressGuidedOnboardingForNavigation = if (resolved.shouldResetNavigationSuppression) {
            false
          } else {
            currentRuntimeCopy.suppressGuidedOnboardingForNavigation
          },
          onboardingLaunchSource = if (currentRuntimeCopy.onboardingLaunchSource == OnboardingLaunchSource.Settings &&
            !resolved.shouldShowGuidedOnboarding
          ) {
            OnboardingLaunchSource.Automatic
          } else {
            currentRuntimeCopy.onboardingLaunchSource
          },
        )
      }
    }
  }

  /**
   * Estado runtime para onboarding.
   */
  private data class OnboardingRuntimeState(
    val suppressGuidedOnboardingForNavigation: Boolean = false,
    val onboardingLaunchSource: OnboardingLaunchSource = OnboardingLaunchSource.Automatic,
  )
}
