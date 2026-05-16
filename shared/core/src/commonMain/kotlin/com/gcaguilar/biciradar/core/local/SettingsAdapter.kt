package com.gcaguilar.biciradar.core.local

import com.gcaguilar.biciradar.core.App_settings
import com.gcaguilar.biciradar.core.EngagementSnapshot
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsSnapshot
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.core.geo.currentTimeMs

/**
 * Deep adapter for app_settings table CRUD.
 *
 * Consolidates the 35-column bidirectional mapping between SQLDelight's
 * [App_settings] row and domain-level [SettingsSnapshot].
 *
 * This is a deep seam: callers never touch individual columns. Instead they
 * call [toSnapshot] or [toUpsertArgs], and any schema change requires updating
 * only this adapter. Tests exercise one interface method at a time.
 *
 * The migration path for legacy blob tables uses this same adapter via
 * [normalizeLegacyOnboardingForMigration] so that blob→relational
 * conversions exercise the identical mapping code path as regular reads/writes.
 */
internal interface SettingsAdapter {
  /** Convert a SQLDelight row + map filter names into a domain snapshot. */
  fun toSnapshot(
    row: App_settings,
    mapFilterNames: Set<String>,
  ): SettingsSnapshot

  /** Convert a domain snapshot into upsert argument tuples for SQLDelight queries. */
  fun toUpsertArgs(snapshot: SettingsSnapshot): UpsertSettingsArgs

  /** Normalize legacy onboarding flags when migrating from blob format. */
  fun normalizeLegacyOnboarding(snapshot: SettingsSnapshot): SettingsSnapshot
}

/**
 * Intermediate type for upsert arguments.
 *
 * Decouples [SettingsAdapter.toUpsertArgs] from SQLDelight query signatures
 * so that the adapter can be tested without depending on [BiciRadarDatabase].
 *
 * All 35+ columns are captured here as one record instead of passing
 * individual arguments through multiple layers.
 */
data class UpsertSettingsArgs(
  val id: Long,
  val searchRadiusMeters: Long,
  val preferredMapApp: String,
  val lastSeenChangelogVersion: Long,
  val lastSeenChangelogAppVersion: String?,
  val themePreference: String,
  val selectedCityId: String,
  val hasCompletedOnboarding: Long,
  val onboardingCityConfirmed: Long,
  val onboardingFeatureHighlightsSeen: Long,
  val onboardingLocationDecisionMade: Long,
  val onboardingNotificationsDecisionMade: Long,
  val onboardingFirstStationSaved: Long,
  val onboardingSavedPlacesConfigured: Long,
  val onboardingSurfacesDiscovered: Long,
  val onboardingCompletedAtEpoch: Long?,
  val engagementInstalledAtEpoch: Long,
  val engagementLastSessionEpoch: Long?,
  val engagementUsefulSessionsCount: Long,
  val engagementFavoritesSavedCount: Long,
  val engagementRoutesOpenedCount: Long,
  val engagementMonitoringsCompletedCount: Long,
  val engagementRepeatedErrorCount: Long,
  val engagementExpiredDataEventsCount: Long,
  val engagementUnavailableDataEventsCount: Long,
  val engagementLastFeedbackNudgeAtEpoch: Long?,
  val engagementLastFeedbackDismissedAtEpoch: Long?,
  val engagementLastFeedbackOpenedAtEpoch: Long?,
  val engagementLastFeedbackNudgedVersion: String?,
  val engagementLastReviewRequestedAtEpoch: Long?,
  val engagementLastReviewRequestedVersion: String?,
  val engagementDismissedUpdateVersion: String?,
  val engagementLastUpdateCheckAtEpoch: Long?,
  val engagementLastUpdateBannerDismissedAtEpoch: Long?,
  val preferredMonitoringDurationSeconds: Long?,
)

/**
 * Default implementation of [SettingsAdapter].
 */
internal class DefaultSettingsAdapter : SettingsAdapter {
  override fun toSnapshot(
    row: App_settings,
    mapFilterNames: Set<String>,
  ): SettingsSnapshot =
    SettingsSnapshot(
      searchRadiusMeters = row.search_radius_meters.toInt(),
      preferredMapApp = PreferredMapApp.valueOf(row.preferred_map_app),
      lastSeenChangelogVersion = row.last_seen_changelog_version.toInt(),
      lastSeenChangelogAppVersion = row.last_seen_changelog_app_version,
      themePreference = ThemePreference.valueOf(row.theme_preference),
      selectedCityId = row.selected_city_id,
      hasCompletedOnboarding = row.has_completed_onboarding != 0L,
      onboardingChecklist =
        OnboardingChecklistSnapshot(
          cityConfirmed = row.onboarding_city_confirmed != 0L,
          featureHighlightsSeen = row.onboarding_feature_highlights_seen != 0L,
          locationDecisionMade = row.onboarding_location_decision_made != 0L,
          notificationsDecisionMade = row.onboarding_notifications_decision_made != 0L,
          firstStationSaved = row.onboarding_first_station_saved != 0L,
          savedPlacesConfigured = row.onboarding_saved_places_configured != 0L,
          surfacesDiscovered = row.onboarding_surfaces_discovered != 0L,
          completedAtEpoch = row.onboarding_completed_at_epoch,
        ),
      engagementSnapshot =
        EngagementSnapshot(
          installedAtEpoch = row.engagement_installed_at_epoch,
          lastSessionEpoch = row.engagement_last_session_epoch,
          usefulSessionsCount = row.engagement_useful_sessions_count.toInt(),
          favoritesSavedCount = row.engagement_favorites_saved_count.toInt(),
          routesOpenedCount = row.engagement_routes_opened_count.toInt(),
          monitoringsCompletedCount = row.engagement_monitorings_completed_count.toInt(),
          repeatedErrorCount = row.engagement_repeated_error_count.toInt(),
          expiredDataEventsCount = row.engagement_expired_data_events_count.toInt(),
          unavailableDataEventsCount = row.engagement_unavailable_data_events_count.toInt(),
          lastFeedbackNudgeAtEpoch = row.engagement_last_feedback_nudge_at_epoch,
          lastFeedbackDismissedAtEpoch = row.engagement_last_feedback_dismissed_at_epoch,
          lastFeedbackOpenedAtEpoch = row.engagement_last_feedback_opened_at_epoch,
          lastFeedbackNudgedVersion = row.engagement_last_feedback_nudged_version,
          lastReviewRequestedAtEpoch = row.engagement_last_review_requested_at_epoch,
          lastReviewRequestedVersion = row.engagement_last_review_requested_version,
          dismissedUpdateVersion = row.engagement_dismissed_update_version,
          lastUpdateCheckAtEpoch = row.engagement_last_update_check_at_epoch,
          lastUpdateBannerDismissedAtEpoch = row.engagement_last_update_banner_dismissed_at_epoch,
        ),
      mapFilterNames = mapFilterNames,
      preferredMonitoringDurationSeconds = row.preferred_monitoring_duration_seconds?.toInt(),
    )

  override fun toUpsertArgs(snapshot: SettingsSnapshot): UpsertSettingsArgs {
    val s = normalizeLegacyOnboarding(snapshot)
    val e = s.engagementSnapshot
    val o = s.onboardingChecklist
    return UpsertSettingsArgs(
      id = 1L,
      searchRadiusMeters = s.searchRadiusMeters.toLong(),
      preferredMapApp = s.preferredMapApp.name,
      lastSeenChangelogVersion = s.lastSeenChangelogVersion.toLong(),
      lastSeenChangelogAppVersion = s.lastSeenChangelogAppVersion,
      themePreference = s.themePreference.name,
      selectedCityId = s.selectedCityId,
      hasCompletedOnboarding = if (s.hasCompletedOnboarding) 1L else 0L,
      onboardingCityConfirmed = if (o.cityConfirmed) 1L else 0L,
      onboardingFeatureHighlightsSeen = if (o.featureHighlightsSeen) 1L else 0L,
      onboardingLocationDecisionMade = if (o.locationDecisionMade) 1L else 0L,
      onboardingNotificationsDecisionMade = if (o.notificationsDecisionMade) 1L else 0L,
      onboardingFirstStationSaved = if (o.firstStationSaved) 1L else 0L,
      onboardingSavedPlacesConfigured = if (o.savedPlacesConfigured) 1L else 0L,
      onboardingSurfacesDiscovered = if (o.surfacesDiscovered) 1L else 0L,
      onboardingCompletedAtEpoch = o.completedAtEpoch,
      engagementInstalledAtEpoch = e.installedAtEpoch,
      engagementLastSessionEpoch = e.lastSessionEpoch,
      engagementUsefulSessionsCount = e.usefulSessionsCount.toLong(),
      engagementFavoritesSavedCount = e.favoritesSavedCount.toLong(),
      engagementRoutesOpenedCount = e.routesOpenedCount.toLong(),
      engagementMonitoringsCompletedCount = e.monitoringsCompletedCount.toLong(),
      engagementRepeatedErrorCount = e.repeatedErrorCount.toLong(),
      engagementExpiredDataEventsCount = e.expiredDataEventsCount.toLong(),
      engagementUnavailableDataEventsCount = e.unavailableDataEventsCount.toLong(),
      engagementLastFeedbackNudgeAtEpoch = e.lastFeedbackNudgeAtEpoch,
      engagementLastFeedbackDismissedAtEpoch = e.lastFeedbackDismissedAtEpoch,
      engagementLastFeedbackOpenedAtEpoch = e.lastFeedbackOpenedAtEpoch,
      engagementLastFeedbackNudgedVersion = e.lastFeedbackNudgedVersion,
      engagementLastReviewRequestedAtEpoch = e.lastReviewRequestedAtEpoch,
      engagementLastReviewRequestedVersion = e.lastReviewRequestedVersion,
      engagementDismissedUpdateVersion = e.dismissedUpdateVersion,
      engagementLastUpdateCheckAtEpoch = e.lastUpdateCheckAtEpoch,
      engagementLastUpdateBannerDismissedAtEpoch = e.lastUpdateBannerDismissedAtEpoch,
      preferredMonitoringDurationSeconds = s.preferredMonitoringDurationSeconds?.toLong(),
    )
  }

  override fun normalizeLegacyOnboarding(snapshot: SettingsSnapshot): SettingsSnapshot {
    var checklist = snapshot.onboardingChecklist
    if (snapshot.hasCompletedOnboarding && !checklist.isCompleted()) {
      checklist =
        OnboardingChecklistSnapshot(
          cityConfirmed = true,
          featureHighlightsSeen = true,
          locationDecisionMade = true,
          notificationsDecisionMade = true,
          firstStationSaved = true,
          savedPlacesConfigured = true,
          surfacesDiscovered = true,
          completedAtEpoch = checklist.completedAtEpoch ?: currentTimeMs(),
        )
    }
    return snapshot.copy(
      onboardingChecklist = checklist,
      hasCompletedOnboarding = checklist.isCompleted(),
    )
  }
}
