package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
private const val REVIEW_COOLDOWN_MILLIS = 120L * DAY_MILLIS
private const val FEEDBACK_NUDGE_COOLDOWN_MILLIS = 30L * DAY_MILLIS

interface EngagementRepository {
  val snapshot: StateFlow<EngagementSnapshot>
  suspend fun bootstrap()
  suspend fun markSessionStarted(nowEpoch: Long = currentTimeMs())
  suspend fun markUsefulSession(nowEpoch: Long = currentTimeMs())
  suspend fun markFavoriteCreated(nowEpoch: Long = currentTimeMs())
  suspend fun markRouteOpened(nowEpoch: Long = currentTimeMs())
  suspend fun markMonitoringCompleted(nowEpoch: Long = currentTimeMs())
  suspend fun markDataFreshnessObserved(freshness: DataFreshness)
  suspend fun markFeedbackNudgeShown(appVersion: String, nowEpoch: Long = currentTimeMs())
  suspend fun markFeedbackOpened(nowEpoch: Long = currentTimeMs())
  suspend fun markFeedbackDismissed(nowEpoch: Long = currentTimeMs())
  suspend fun markReviewPrompted(appVersion: String, nowEpoch: Long = currentTimeMs())
  suspend fun markUpdateChecked(nowEpoch: Long = currentTimeMs())
  suspend fun markUpdateBannerDismissed(version: String, nowEpoch: Long = currentTimeMs())
  fun shouldShowFeedbackNudge(appVersion: String, nowEpoch: Long = currentTimeMs()): Boolean
  fun reviewEligibility(
    appVersion: String,
    onboardingCompleted: Boolean,
    currentFreshness: DataFreshness,
    nowEpoch: Long = currentTimeMs(),
  ): ReviewEligibility
}

/**
 * Implementación de EngagementRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class EngagementRepositoryImpl(
  private val settingsRepository: SettingsRepository,
) : EngagementRepository {
  override val snapshot: StateFlow<EngagementSnapshot> = settingsRepository.engagementSnapshot

  override suspend fun bootstrap() {
    settingsRepository.bootstrap()
  }

  override suspend fun markSessionStarted(nowEpoch: Long) {
    update { it.markSessionStarted(nowEpoch) }
  }

  override suspend fun markUsefulSession(nowEpoch: Long) {
    update { it.markUsefulSession(nowEpoch) }
  }

  override suspend fun markFavoriteCreated(nowEpoch: Long) {
    update { it.markFavoriteSaved(nowEpoch) }
  }

  override suspend fun markRouteOpened(nowEpoch: Long) {
    update { it.markRouteOpened(nowEpoch) }
  }

  override suspend fun markMonitoringCompleted(nowEpoch: Long) {
    update { it.markMonitoringCompleted(nowEpoch) }
  }

  override suspend fun markDataFreshnessObserved(freshness: DataFreshness) {
    update {
      when (freshness) {
        DataFreshness.StaleUsable -> it
        DataFreshness.Expired -> it.markExpiredDataEvent()
        DataFreshness.Unavailable -> it.markUnavailableDataEvent().markRepeatedError()
        DataFreshness.Fresh -> it
      }
    }
  }

  override suspend fun markFeedbackNudgeShown(appVersion: String, nowEpoch: Long) {
    update { it.markFeedbackNudged(appVersion, nowEpoch) }
  }

  override suspend fun markFeedbackOpened(nowEpoch: Long) {
    update { it.markFeedbackOpened(nowEpoch) }
  }

  override suspend fun markFeedbackDismissed(nowEpoch: Long) {
    update { it.markFeedbackDismissed(nowEpoch) }
  }

  override suspend fun markReviewPrompted(appVersion: String, nowEpoch: Long) {
    update { it.markReviewRequested(appVersion, nowEpoch) }
  }

  override suspend fun markUpdateChecked(nowEpoch: Long) {
    update { it.markUpdateChecked(nowEpoch) }
  }

  override suspend fun markUpdateBannerDismissed(version: String, nowEpoch: Long) {
    update { it.markUpdateBannerDismissed(version, nowEpoch) }
  }

  override fun shouldShowFeedbackNudge(appVersion: String, nowEpoch: Long): Boolean {
    val current = snapshot.value
    val cooldownSatisfied = current.lastFeedbackDismissedAtEpoch
      ?.let { nowEpoch - it >= FEEDBACK_NUDGE_COOLDOWN_MILLIS } ?: true
    val notRecentlyShown = current.lastFeedbackNudgeAtEpoch?.let { nowEpoch - it >= 7L * DAY_MILLIS } ?: true
    val notSameVersion = current.lastFeedbackNudgedVersion != appVersion
    return cooldownSatisfied && notRecentlyShown && notSameVersion && current.usefulSessionsCount >= 3
  }

  override fun reviewEligibility(
    appVersion: String,
    onboardingCompleted: Boolean,
    currentFreshness: DataFreshness,
    nowEpoch: Long,
  ): ReviewEligibility {
    return reviewEligibility(
      engagement = snapshot.value,
      appVersion = appVersion,
      onboardingCompleted = onboardingCompleted,
      currentFreshness = currentFreshness,
      nowEpoch = nowEpoch,
    )
  }

  private suspend fun update(transform: (EngagementSnapshot) -> EngagementSnapshot) {
    settingsRepository.setEngagementSnapshot(transform(snapshot.value))
  }
}
