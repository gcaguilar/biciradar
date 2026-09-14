package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewEligibilityTest {
  private val now = 1_000_000_000_000L
  private val oneDayMillis = 24L * 60 * 60 * 1000
  private val eightDaysMillis = 8L * oneDayMillis

  private fun eligibleSnapshot(): EngagementSnapshot =
    EngagementSnapshot(
      installedAtEpoch = now - eightDaysMillis,
      usefulSessionsCount = 5,
    )

  @Test
  fun `eligible after install age and enough positive signals`() {
    val result =
      reviewEligibility(
        engagement = eligibleSnapshot(),
        appVersion = "1.0.0",
        onboardingCompleted = true,
        currentFreshness = DataFreshness.Fresh,
        nowEpoch = now,
      )

    assertTrue(result.isEligible)
    assertEquals(ReviewEligibilityReason.Eligible, result.reason)
  }

  @Test
  fun `not eligible when already requested for the same version`() {
    val result =
      reviewEligibility(
        engagement =
          eligibleSnapshot().copy(
            lastReviewRequestedVersion = "1.0.0",
            lastReviewRequestedAtEpoch = now - 1_000L,
          ),
        appVersion = "1.0.0",
        onboardingCompleted = true,
        currentFreshness = DataFreshness.Fresh,
        nowEpoch = now,
      )

    assertFalse(result.isEligible)
    assertEquals(ReviewEligibilityReason.AlreadyRequestedForVersion, result.reason)
  }

  @Test
  fun `eligible again on a new version even right after a previous request`() {
    val result =
      reviewEligibility(
        engagement =
          eligibleSnapshot().copy(
            lastReviewRequestedVersion = "1.0.0",
            lastReviewRequestedAtEpoch = now - 1_000L,
          ),
        appVersion = "1.1.0",
        onboardingCompleted = true,
        currentFreshness = DataFreshness.Fresh,
        nowEpoch = now,
      )

    assertTrue(result.isEligible)
    assertEquals(ReviewEligibilityReason.Eligible, result.reason)
  }

  @Test
  fun `not eligible when install is too recent`() {
    val result =
      reviewEligibility(
        engagement = eligibleSnapshot().copy(installedAtEpoch = now - oneDayMillis),
        appVersion = "1.0.0",
        onboardingCompleted = true,
        currentFreshness = DataFreshness.Fresh,
        nowEpoch = now,
      )

    assertFalse(result.isEligible)
    assertEquals(ReviewEligibilityReason.InstallTooRecent, result.reason)
  }

  @Test
  fun `not eligible with fewer than five positive signals`() {
    val result =
      reviewEligibility(
        engagement = eligibleSnapshot().copy(usefulSessionsCount = 4),
        appVersion = "1.0.0",
        onboardingCompleted = true,
        currentFreshness = DataFreshness.Fresh,
        nowEpoch = now,
      )

    assertFalse(result.isEligible)
    assertEquals(ReviewEligibilityReason.NotEnoughPositiveSignals, result.reason)
  }

  @Test
  fun `not eligible while onboarding is incomplete`() {
    val result =
      reviewEligibility(
        engagement = eligibleSnapshot(),
        appVersion = "1.0.0",
        onboardingCompleted = false,
        currentFreshness = DataFreshness.Fresh,
        nowEpoch = now,
      )

    assertFalse(result.isEligible)
    assertEquals(ReviewEligibilityReason.OnboardingIncomplete, result.reason)
  }

  @Test
  fun `not eligible when data is unavailable this session`() {
    val result =
      reviewEligibility(
        engagement = eligibleSnapshot(),
        appVersion = "1.0.0",
        onboardingCompleted = true,
        currentFreshness = DataFreshness.Unavailable,
        nowEpoch = now,
      )

    assertFalse(result.isEligible)
    assertEquals(ReviewEligibilityReason.UnavailableDataThisSession, result.reason)
  }
}
