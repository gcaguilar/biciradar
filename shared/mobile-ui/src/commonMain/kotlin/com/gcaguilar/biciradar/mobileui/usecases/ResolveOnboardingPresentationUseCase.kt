package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import dev.zacsweers.metro.Inject

internal enum class OnboardingLaunchSource {
  Automatic,
  Settings,
}

internal data class OnboardingPresentationInput(
  val checklist: OnboardingChecklistSnapshot,
  val cityConfigured: Boolean,
  val launchSource: OnboardingLaunchSource,
)

internal data class OnboardingPresentationResult(
  val onboardingChecklist: OnboardingChecklistSnapshot,
  val isCitySelectionRequired: Boolean,
  val shouldShowGuidedOnboarding: Boolean,
)

internal class ResolveOnboardingPresentationUseCase
  @Inject
  constructor() {
    fun execute(input: OnboardingPresentationInput): OnboardingPresentationResult =
      OnboardingPresentationResult(
        onboardingChecklist = input.checklist,
        isCitySelectionRequired = !input.cityConfigured,
        shouldShowGuidedOnboarding =
          input.cityConfigured && !input.checklist.isCompleted(),
      )
  }
