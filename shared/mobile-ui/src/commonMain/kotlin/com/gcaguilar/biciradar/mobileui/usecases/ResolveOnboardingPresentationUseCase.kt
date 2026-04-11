package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.mobileui.experience.GuidedOnboardingStep
import com.gcaguilar.biciradar.mobileui.experience.guidedOnboardingStep

internal enum class OnboardingLaunchSource {
  Automatic,
  Settings,
}

internal data class OnboardingPresentationInput(
  val checklist: OnboardingChecklistSnapshot,
  val cityConfigured: Boolean,
  val suppressGuidedOnboardingForNavigation: Boolean,
  val launchSource: OnboardingLaunchSource,
)

internal data class OnboardingPresentationResult(
  val onboardingChecklist: OnboardingChecklistSnapshot,
  val isCitySelectionRequired: Boolean,
  val shouldShowGuidedOnboarding: Boolean,
  val shouldResetNavigationSuppression: Boolean,
)

internal class ResolveOnboardingPresentationUseCase {
  fun execute(input: OnboardingPresentationInput): OnboardingPresentationResult {
    val onboardingStep = input.checklist.guidedOnboardingStep()
    val shouldSuppressForStep =
      input.launchSource != OnboardingLaunchSource.Settings &&
        input.suppressGuidedOnboardingForNavigation &&
        onboardingStep == GuidedOnboardingStep.SavedPlaces

    return OnboardingPresentationResult(
      onboardingChecklist = input.checklist,
      isCitySelectionRequired = !input.cityConfigured,
      shouldShowGuidedOnboarding =
        input.cityConfigured &&
          !input.checklist.isCompleted() &&
          !shouldSuppressForStep,
      shouldResetNavigationSuppression =
        input.suppressGuidedOnboardingForNavigation &&
          onboardingStep != GuidedOnboardingStep.SavedPlaces,
    )
  }
}
