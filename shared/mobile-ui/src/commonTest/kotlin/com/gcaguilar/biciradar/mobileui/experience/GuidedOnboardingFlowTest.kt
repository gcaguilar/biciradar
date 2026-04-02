package com.gcaguilar.biciradar.mobileui.experience

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import kotlin.test.Test
import kotlin.test.assertEquals

class GuidedOnboardingFlowTest {
  @Test
  fun `guided onboarding starts with location permission`() {
    val checklist = OnboardingChecklistSnapshot()

    assertEquals(GuidedOnboardingStep.LocationPermission, checklist.guidedOnboardingStep())
  }

  @Test
  fun `guided onboarding advances through pending steps in order`() {
    assertEquals(
      GuidedOnboardingStep.NotificationsPermission,
      OnboardingChecklistSnapshot(
        cityConfirmed = true,
        locationDecisionMade = true,
      ).guidedOnboardingStep(),
    )
    assertEquals(
      GuidedOnboardingStep.FirstFavorite,
      OnboardingChecklistSnapshot(
        cityConfirmed = true,
        locationDecisionMade = true,
        notificationsDecisionMade = true,
      ).guidedOnboardingStep(),
    )
    assertEquals(
      GuidedOnboardingStep.SavedPlaces,
      OnboardingChecklistSnapshot(
        cityConfirmed = true,
        locationDecisionMade = true,
        notificationsDecisionMade = true,
        firstStationSaved = true,
      ).guidedOnboardingStep(),
    )
    assertEquals(
      GuidedOnboardingStep.Surfaces,
      OnboardingChecklistSnapshot(
        cityConfirmed = true,
        locationDecisionMade = true,
        notificationsDecisionMade = true,
        firstStationSaved = true,
        savedPlacesConfigured = true,
      ).guidedOnboardingStep(),
    )
  }

  @Test
  fun `guided onboarding reports completed when every step is done`() {
    val checklist = OnboardingChecklistSnapshot(
      cityConfirmed = true,
      locationDecisionMade = true,
      notificationsDecisionMade = true,
      firstStationSaved = true,
      savedPlacesConfigured = true,
      surfacesDiscovered = true,
      completedAtEpoch = 123L,
    )

    assertEquals(GuidedOnboardingStep.Completed, checklist.guidedOnboardingStep())
  }
}
