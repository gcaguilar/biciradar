package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.EngagementRepository
import com.gcaguilar.biciradar.core.ReviewEligibility
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.compareAppVersionStrings
import com.gcaguilar.biciradar.core.normalizeAppVersionForCatalog
import com.gcaguilar.biciradar.core.pendingChangelogVersion
import com.gcaguilar.biciradar.mobileui.experience.ChangelogCatalog
import com.gcaguilar.biciradar.mobileui.viewmodel.ChangelogPresentation

/**
 * Use case that handles app lifecycle-related operations.
 * Groups engagement, updates, reviews, and changelog logic.
 */
internal class AppLifecycleUseCase(
  private val engagementRepository: EngagementRepository,
  private val appUpdatePrompter: AppUpdatePrompter,
  private val reviewPrompter: ReviewPrompter,
  private val settingsAggregationUseCase: SettingsAggregationUseCase,
  private val appVersion: String,
) {

  // region App Updates

  /**
   * Checks for available app updates.
   */
  suspend fun checkForUpdate(): UpdateAvailabilityState {
    return appUpdatePrompter.checkForUpdate()
  }

  /**
   * Starts a flexible update if available.
   * @return true if the update was started successfully
   */
  suspend fun startFlexibleUpdate(): Boolean {
    return appUpdatePrompter.startFlexibleUpdate()
  }

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

  // endregion

  // region Reviews

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
  ): ReviewEligibility {
    return engagementRepository.reviewEligibility(
      appVersion = appVersion,
      onboardingCompleted = onboardingCompleted,
      currentFreshness = currentFreshness,
      nowEpoch = nowEpoch,
    )
  }

  /**
   * Marks that a review was prompted.
   */
  suspend fun markReviewPrompted(appVersion: String, nowEpoch: Long) {
    engagementRepository.markReviewPrompted(appVersion, nowEpoch)
  }

  // endregion

  // region Feedback / Engagement

  /**
   * Marks that an update check was performed.
   */
  suspend fun markUpdateChecked(nowEpoch: Long) {
    engagementRepository.markUpdateChecked(nowEpoch)
  }

  /**
   * Marks that an update banner was dismissed for a specific version.
   */
  suspend fun markUpdateBannerDismissed(version: String, nowEpoch: Long) {
    engagementRepository.markUpdateBannerDismissed(version, nowEpoch)
  }

  /**
   * Marks that a feedback nudge was shown.
   */
  suspend fun markFeedbackNudgeShown(appVersion: String, nowEpoch: Long) {
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
  fun shouldShowFeedbackNudge(appVersion: String, nowEpoch: Long): Boolean {
    return engagementRepository.shouldShowFeedbackNudge(appVersion, nowEpoch)
  }

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

  // endregion

  // region Changelog

  /**
   * Gets the pending changelog if one should be shown to the user.
   * @return ChangelogPresentation if there's a pending changelog, null otherwise
   */
  fun getPendingChangelog(): ChangelogPresentation? {
    val suppression = checkChangelogSuppression()
    if (suppression.suppressed) return null

    val lastSeen = settingsAggregationUseCase.currentLastSeenChangelogAppVersion() ?: "0.0.0"
    val pending = pendingChangelogVersion(
      appVersion,
      lastSeen,
      ChangelogCatalog.catalogVersionSet(),
    )
    val entries = pending?.let { ChangelogCatalog.entriesFor(it) }.orEmpty()

    return if (pending != null && entries.isNotEmpty()) {
      ChangelogPresentation(
        sections = ChangelogCatalog.history(),
        highlightedVersion = pending,
        persistSeenVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion,
      )
    } else null
  }

  /**
   * Gets the full changelog history for manual viewing.
   */
  fun getChangelogHistory(): ChangelogPresentation? {
    val sections = ChangelogCatalog.history()
    if (sections.isEmpty()) return null

    return ChangelogPresentation(
      sections = sections,
      highlightedVersion = ChangelogCatalog.latestVersionAtOrBefore(appVersion),
      persistSeenVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion,
    )
  }

  /**
   * Marks the changelog as seen for the given version.
   */
  suspend fun markChangelogSeen(version: String) {
    settingsAggregationUseCase.setLastSeenChangelogAppVersion(version)
  }

  /**
   * Checks if changelog should be suppressed and if current version should be marked as seen.
   * This is used when onboarding is pending.
   */
  fun checkChangelogSuppression(): ChangelogSuppressionResult {
    val onboardingCompleted = settingsAggregationUseCase.isOnboardingCompleted()
    if (onboardingCompleted) {
      return ChangelogSuppressionResult(suppressed = false)
    }

    val normalizedCurrentVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion
    val normalizedLastSeen = normalizeAppVersionForCatalog(
      settingsAggregationUseCase.currentLastSeenChangelogAppVersion(),
    )

    val shouldMarkSeen = normalizedLastSeen == null ||
      compareAppVersionStrings(normalizedLastSeen, normalizedCurrentVersion) < 0

    return ChangelogSuppressionResult(
      suppressed = true,
      shouldMarkCurrentVersionSeen = shouldMarkSeen,
      currentVersionToMark = normalizedCurrentVersion,
    )
  }

  /**
   * Ensures the changelog baseline is set for first-time users.
   * Should be called during initialization.
   */
  suspend fun ensureChangelogBaseline() {
    settingsAggregationUseCase.ensureChangelogStringBaseline(appVersion)
  }

  // endregion
}
