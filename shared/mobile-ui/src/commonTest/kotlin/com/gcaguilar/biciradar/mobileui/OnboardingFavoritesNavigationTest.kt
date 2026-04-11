package com.gcaguilar.biciradar.mobileui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingFavoritesNavigationTest {
  @Test
  fun shouldNavigate_whenPendingAndOnboardingHidden() {
    assertTrue(
      shouldNavigateToFavoritesAfterOnboarding(
        hasPendingFavoritesNavigation = true,
        shouldShowGuidedOnboarding = false,
      ),
    )
  }

  @Test
  fun shouldNotNavigate_whenPendingButOnboardingStillVisible() {
    assertFalse(
      shouldNavigateToFavoritesAfterOnboarding(
        hasPendingFavoritesNavigation = true,
        shouldShowGuidedOnboarding = true,
      ),
    )
  }

  @Test
  fun shouldNotNavigate_whenNotPending() {
    assertFalse(
      shouldNavigateToFavoritesAfterOnboarding(
        hasPendingFavoritesNavigation = false,
        shouldShowGuidedOnboarding = false,
      ),
    )
  }
}
