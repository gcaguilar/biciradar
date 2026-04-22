package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveOnboardingPresentationUseCaseTest {
  private val useCase = ResolveOnboardingPresentationUseCase()

  @Test
  fun `requires city selection when city is not configured`() {
    val result =
      useCase.execute(
        OnboardingPresentationInput(
          checklist = OnboardingChecklistSnapshot(),
          cityConfigured = false,
          launchSource = OnboardingLaunchSource.Automatic,
        ),
      )

    assertEquals(true, result.isCitySelectionRequired)
    assertEquals(false, result.shouldShowGuidedOnboarding)
  }

  @Test
  fun `shows guided onboarding while city is configured and checklist is pending`() {
    val result =
      useCase.execute(
        OnboardingPresentationInput(
          checklist =
            OnboardingChecklistSnapshot(
              cityConfirmed = true,
              featureHighlightsSeen = true,
              locationDecisionMade = true,
              notificationsDecisionMade = true,
            ),
          cityConfigured = true,
          launchSource = OnboardingLaunchSource.Automatic,
        ),
      )

    assertEquals(false, result.isCitySelectionRequired)
    assertEquals(true, result.shouldShowGuidedOnboarding)
  }

  @Test
  fun `settings launch keeps guided onboarding visible`() {
    val result =
      useCase.execute(
        OnboardingPresentationInput(
          checklist =
            OnboardingChecklistSnapshot(
              cityConfirmed = true,
              featureHighlightsSeen = true,
              locationDecisionMade = true,
              notificationsDecisionMade = true,
            ),
          cityConfigured = true,
          launchSource = OnboardingLaunchSource.Settings,
        ),
      )

    assertEquals(true, result.shouldShowGuidedOnboarding)
  }

  @Test
  fun `hides guided onboarding when checklist is completed`() {
    val result =
      useCase.execute(
        OnboardingPresentationInput(
          checklist =
            OnboardingChecklistSnapshot(
              cityConfirmed = true,
              featureHighlightsSeen = true,
              locationDecisionMade = true,
              notificationsDecisionMade = true,
              surfacesDiscovered = true,
              completedAtEpoch = 123L,
            ),
          cityConfigured = true,
          launchSource = OnboardingLaunchSource.Automatic,
        ),
      )

    assertEquals(false, result.shouldShowGuidedOnboarding)
  }
}
