package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class ResolveOnboardingPresentationUseCaseTest {
  private val useCase = ResolveOnboardingPresentationUseCase()

  @Test
  fun `requires city selection when city is not configured`() {
    val result = useCase.execute(
      OnboardingPresentationInput(
        checklist = OnboardingChecklistSnapshot(),
        cityConfigured = false,
        suppressGuidedOnboardingForNavigation = false,
        launchSource = OnboardingLaunchSource.Automatic,
      ),
    )

    assertEquals(true, result.isCitySelectionRequired)
    assertEquals(false, result.shouldShowGuidedOnboarding)
  }

  @Test
  fun `suppresses guided onboarding for automatic flow in favorites steps`() {
    val result = useCase.execute(
      OnboardingPresentationInput(
        checklist = OnboardingChecklistSnapshot(
          cityConfirmed = true,
          featureHighlightsSeen = true,
          locationDecisionMade = true,
          notificationsDecisionMade = true,
        ),
        cityConfigured = true,
        suppressGuidedOnboardingForNavigation = true,
        launchSource = OnboardingLaunchSource.Automatic,
      ),
    )

    assertEquals(false, result.isCitySelectionRequired)
    assertEquals(false, result.shouldShowGuidedOnboarding)
    assertEquals(false, result.shouldResetNavigationSuppression)
  }

  @Test
  fun `settings launch ignores temporary navigation suppression`() {
    val result = useCase.execute(
      OnboardingPresentationInput(
        checklist = OnboardingChecklistSnapshot(
          cityConfirmed = true,
          featureHighlightsSeen = true,
          locationDecisionMade = true,
          notificationsDecisionMade = true,
        ),
        cityConfigured = true,
        suppressGuidedOnboardingForNavigation = true,
        launchSource = OnboardingLaunchSource.Settings,
      ),
    )

    assertEquals(true, result.shouldShowGuidedOnboarding)
    assertEquals(false, result.shouldResetNavigationSuppression)
  }

  @Test
  fun `requests suppression reset when flow moves out of favorites steps`() {
    val result = useCase.execute(
      OnboardingPresentationInput(
        checklist = OnboardingChecklistSnapshot(
          cityConfirmed = true,
          featureHighlightsSeen = true,
          locationDecisionMade = true,
          notificationsDecisionMade = true,
          firstStationSaved = true,
          savedPlacesConfigured = true,
        ),
        cityConfigured = true,
        suppressGuidedOnboardingForNavigation = true,
        launchSource = OnboardingLaunchSource.Automatic,
      ),
    )

    assertEquals(true, result.shouldShowGuidedOnboarding)
    assertEquals(true, result.shouldResetNavigationSuppression)
  }
}
