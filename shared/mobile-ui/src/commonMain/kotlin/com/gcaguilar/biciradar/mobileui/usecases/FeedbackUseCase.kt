package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.EngagementRepository
import com.gcaguilar.biciradar.core.ReviewEligibility
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.UpdateAvailabilityState

/**
 * Use case that handles user feedback prompts including app updates and reviews.
 * Groups AppUpdatePrompter and ReviewPrompter operations.
 */
internal class FeedbackUseCase(
  private val appUpdatePrompter: AppUpdatePrompter,
  private val reviewPrompter: ReviewPrompter,
  private val engagementRepository: EngagementRepository,
) {
  /**
   * Checks for available app updates.
   */
  suspend fun checkForUpdate(): UpdateAvailabilityState = appUpdatePrompter.checkForUpdate()

  /**
   * Starts a flexible update if available.
   * @return true if the update was started successfully
   */
  suspend fun startFlexibleUpdate(): Boolean = appUpdatePrompter.startFlexibleUpdate()

  /**
   * Completes a flexible update if it's ready to install.
   */
  suspend fun completeFlexibleUpdateIfReady() {
    appUpdatePrompter.completeFlexibleUpdateIfReady()
  }

  /**
   * Opens the store listing for manual update.
   */
  fun openStoreListing() {
    appUpdatePrompter.openStoreListing()
  }

  /**
   * Requests an in-app review from the user.
   */
  suspend fun requestInAppReview() {
    reviewPrompter.requestInAppReview()
  }

  /**
   * Checks review eligibility based on engagement metrics.
   */
  fun checkReviewEligibility(
    appVersion: String,
    onboardingCompleted: Boolean,
    currentFreshness: DataFreshness,
    nowEpoch: Long,
  ): ReviewEligibility =
    engagementRepository.reviewEligibility(
      appVersion = appVersion,
      onboardingCompleted = onboardingCompleted,
      currentFreshness = currentFreshness,
      nowEpoch = nowEpoch,
    )

  /**
   * Marks that an update check was performed.
   */
  suspend fun markUpdateChecked(nowEpoch: Long) {
    engagementRepository.markUpdateChecked(nowEpoch)
  }

  /**
   * Marks that an update banner was dismissed for a specific version.
   */
  suspend fun markUpdateBannerDismissed(
    version: String,
    nowEpoch: Long,
  ) {
    engagementRepository.markUpdateBannerDismissed(version, nowEpoch)
  }

  /**
   * Marks that a review was prompted.
   */
  suspend fun markReviewPrompted(
    appVersion: String,
    nowEpoch: Long,
  ) {
    engagementRepository.markReviewPrompted(appVersion, nowEpoch)
  }

  /**
   * Marks that a feedback nudge was shown.
   */
  suspend fun markFeedbackNudgeShown(
    appVersion: String,
    nowEpoch: Long,
  ) {
    engagementRepository.markFeedbackNudgeShown(appVersion, nowEpoch)
  }

  /**
   * Marks that feedback was opened.
   */
  suspend fun markFeedbackOpened(nowEpoch: Long) {
    engagementRepository.markFeedbackOpened(nowEpoch)
  }

  /**
   * Marks that feedback was dismissed.
   */
  suspend fun markFeedbackDismissed(nowEpoch: Long) {
    engagementRepository.markFeedbackDismissed(nowEpoch)
  }

  /**
   * Checks if a feedback nudge should be shown.
   */
  fun shouldShowFeedbackNudge(
    appVersion: String,
    nowEpoch: Long,
  ): Boolean = engagementRepository.shouldShowFeedbackNudge(appVersion, nowEpoch)

  /**
   * Marks that data freshness was observed.
   */
  suspend fun markDataFreshnessObserved(freshness: DataFreshness) {
    engagementRepository.markDataFreshnessObserved(freshness)
  }

  /**
   * Marks that a favorite was created.
   */
  suspend fun markFavoriteCreated(nowEpoch: Long) {
    engagementRepository.markFavoriteCreated(nowEpoch)
  }

  /**
   * Marks that a session was started.
   */
  suspend fun markSessionStarted(nowEpoch: Long) {
    engagementRepository.markSessionStarted(nowEpoch)
  }

  /**
   * Marks that a useful session occurred.
   */
  suspend fun markUsefulSession(nowEpoch: Long) {
    engagementRepository.markUsefulSession(nowEpoch)
  }
}
