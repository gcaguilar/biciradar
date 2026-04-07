package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val REVIEW_MIN_INSTALL_AGE_MILLIS = 7L * 24L * 60L * 60L * 1000L
private const val REVIEW_MIN_COOLDOWN_MILLIS = 120L * 24L * 60L * 60L * 1000L

@Serializable
enum class StationDataSource {
  Network,
  Cache,
  Unavailable,
}

@Serializable
enum class DataFreshness {
  Fresh,
  StaleUsable,
  Expired,
  Unavailable,
}

@Serializable
data class OnboardingChecklistSnapshot(
  val cityConfirmed: Boolean = false,
  val featureHighlightsSeen: Boolean = false,
  val locationDecisionMade: Boolean = false,
  val notificationsDecisionMade: Boolean = false,
  /** User has at least one favorite (synced from favorites repo during onboarding). */
  val firstStationSaved: Boolean = false,
  val savedPlacesConfigured: Boolean = false,
  val surfacesDiscovered: Boolean = false,
  val completedAtEpoch: Long? = null,
) {
  fun isCompleted(): Boolean = completedAtEpoch != null

  fun markCompleted(nowEpoch: Long = currentTimeMs()): OnboardingChecklistSnapshot = copy(completedAtEpoch = nowEpoch)

  fun clearCompleted(): OnboardingChecklistSnapshot = copy(completedAtEpoch = null)

  /**
   * Post-onboarding nudge on Profile when useful milestones are still missing.
   * Permission flags come from the platform layer.
   */
  fun needsProfileSetupCard(
    hasLocationPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasFavoriteStations: Boolean,
    hasHomeStation: Boolean,
    hasWorkStation: Boolean,
  ): Boolean {
    if (!isCompleted()) return false
    return !hasLocationPermission ||
      !hasNotificationPermission ||
      !hasFavoriteStations ||
      !hasHomeStation ||
      !hasWorkStation
  }
}

@Serializable
data class EngagementSnapshot(
  val installedAtEpoch: Long = 0L,
  val lastSessionEpoch: Long? = null,
  val usefulSessionsCount: Int = 0,
  val favoritesSavedCount: Int = 0,
  val routesOpenedCount: Int = 0,
  val monitoringsCompletedCount: Int = 0,
  val repeatedErrorCount: Int = 0,
  val expiredDataEventsCount: Int = 0,
  val unavailableDataEventsCount: Int = 0,
  val lastFeedbackNudgeAtEpoch: Long? = null,
  val lastFeedbackDismissedAtEpoch: Long? = null,
  val lastFeedbackOpenedAtEpoch: Long? = null,
  val lastFeedbackNudgedVersion: String? = null,
  val lastReviewRequestedAtEpoch: Long? = null,
  val lastReviewRequestedVersion: String? = null,
  val dismissedUpdateVersion: String? = null,
  val lastUpdateCheckAtEpoch: Long? = null,
  val lastUpdateBannerDismissedAtEpoch: Long? = null,
) {
  fun positiveSignalsCount(): Int = usefulSessionsCount + favoritesSavedCount + routesOpenedCount + monitoringsCompletedCount

  fun withInstallEpochIfMissing(nowEpoch: Long = currentTimeMs()): EngagementSnapshot =
    if (installedAtEpoch > 0L) this else copy(installedAtEpoch = nowEpoch)

  fun markSessionStarted(nowEpoch: Long = currentTimeMs()): EngagementSnapshot =
    withInstallEpochIfMissing(nowEpoch).copy(lastSessionEpoch = nowEpoch)

  fun markUsefulSession(nowEpoch: Long = currentTimeMs()): EngagementSnapshot =
    copy(usefulSessionsCount = usefulSessionsCount + 1, lastSessionEpoch = nowEpoch)

  fun markFavoriteSaved(nowEpoch: Long = currentTimeMs()): EngagementSnapshot =
    copy(favoritesSavedCount = favoritesSavedCount + 1, lastSessionEpoch = nowEpoch)

  fun markRouteOpened(nowEpoch: Long = currentTimeMs()): EngagementSnapshot =
    copy(routesOpenedCount = routesOpenedCount + 1, lastSessionEpoch = nowEpoch)

  fun markMonitoringCompleted(nowEpoch: Long = currentTimeMs()): EngagementSnapshot =
    copy(monitoringsCompletedCount = monitoringsCompletedCount + 1, lastSessionEpoch = nowEpoch)

  fun markRepeatedError(): EngagementSnapshot = copy(repeatedErrorCount = repeatedErrorCount + 1)

  fun markExpiredDataEvent(): EngagementSnapshot = copy(expiredDataEventsCount = expiredDataEventsCount + 1)

  fun markUnavailableDataEvent(): EngagementSnapshot = copy(unavailableDataEventsCount = unavailableDataEventsCount + 1)

  fun markFeedbackNudged(appVersion: String, nowEpoch: Long = currentTimeMs()): EngagementSnapshot = copy(
    lastFeedbackNudgeAtEpoch = nowEpoch,
    lastFeedbackNudgedVersion = appVersion,
  )

  fun markFeedbackDismissed(nowEpoch: Long = currentTimeMs()): EngagementSnapshot = copy(
    lastFeedbackDismissedAtEpoch = nowEpoch,
  )

  fun markFeedbackOpened(nowEpoch: Long = currentTimeMs()): EngagementSnapshot = copy(
    lastFeedbackOpenedAtEpoch = nowEpoch,
  )

  fun markReviewRequested(appVersion: String, nowEpoch: Long = currentTimeMs()): EngagementSnapshot = copy(
    lastReviewRequestedAtEpoch = nowEpoch,
    lastReviewRequestedVersion = appVersion,
  )

  fun markUpdateChecked(nowEpoch: Long = currentTimeMs()): EngagementSnapshot = copy(
    lastUpdateCheckAtEpoch = nowEpoch,
  )

  fun markUpdateBannerDismissed(version: String, nowEpoch: Long = currentTimeMs()): EngagementSnapshot = copy(
    dismissedUpdateVersion = version,
    lastUpdateBannerDismissedAtEpoch = nowEpoch,
  )
}

@Serializable
enum class ReviewEligibilityReason {
  Eligible,
  OnboardingIncomplete,
  InstallTooRecent,
  NotEnoughPositiveSignals,
  UnavailableDataThisSession,
  CooldownActive,
  AlreadyRequestedForVersion,
}

@Serializable
data class ReviewEligibility(
  val isEligible: Boolean,
  val reason: ReviewEligibilityReason,
  val positiveSignals: Int,
)

fun reviewEligibility(
  engagement: EngagementSnapshot,
  appVersion: String,
  onboardingCompleted: Boolean,
  currentFreshness: DataFreshness,
  nowEpoch: Long = currentTimeMs(),
): ReviewEligibility {
  val positiveSignals = engagement.positiveSignalsCount()
  if (!onboardingCompleted) {
    return ReviewEligibility(false, ReviewEligibilityReason.OnboardingIncomplete, positiveSignals)
  }
  if (engagement.installedAtEpoch <= 0L || nowEpoch - engagement.installedAtEpoch < REVIEW_MIN_INSTALL_AGE_MILLIS) {
    return ReviewEligibility(false, ReviewEligibilityReason.InstallTooRecent, positiveSignals)
  }
  if (positiveSignals < 5) {
    return ReviewEligibility(false, ReviewEligibilityReason.NotEnoughPositiveSignals, positiveSignals)
  }
  if (currentFreshness == DataFreshness.Unavailable) {
    return ReviewEligibility(false, ReviewEligibilityReason.UnavailableDataThisSession, positiveSignals)
  }
  if (engagement.lastReviewRequestedVersion == appVersion) {
    return ReviewEligibility(false, ReviewEligibilityReason.AlreadyRequestedForVersion, positiveSignals)
  }
  if (engagement.lastReviewRequestedAtEpoch != null &&
    nowEpoch - engagement.lastReviewRequestedAtEpoch < REVIEW_MIN_COOLDOWN_MILLIS
  ) {
    return ReviewEligibility(false, ReviewEligibilityReason.CooldownActive, positiveSignals)
  }
  return ReviewEligibility(true, ReviewEligibilityReason.Eligible, positiveSignals)
}

@Serializable
sealed interface UpdateAvailabilityState {
  @Serializable
  @SerialName("unknown")
  data object Unknown : UpdateAvailabilityState

  @Serializable
  @SerialName("unavailable")
  data object Unavailable : UpdateAvailabilityState

  @Serializable
  @SerialName("available")
  data class Available(
    val versionName: String,
    val storeUrl: String? = null,
    val isFlexibleAllowed: Boolean = false,
  ) : UpdateAvailabilityState

  @Serializable
  @SerialName("downloaded")
  data class Downloaded(
    val versionName: String,
    val storeUrl: String? = null,
  ) : UpdateAvailabilityState
}

@Serializable
enum class SavedPlaceKind {
  Favorite,
  Home,
  Work,
  Category,
}

@Serializable
sealed interface SavedPlaceAlertTarget {
  val stationId: String
  val cityId: String
  val stationName: String?
  val kind: SavedPlaceKind

  fun identityKey(): String = when (kind) {
    SavedPlaceKind.Favorite -> "favorite:$stationId:$cityId"
    SavedPlaceKind.Home -> "home:$stationId:$cityId"
    SavedPlaceKind.Work -> "work:$stationId:$cityId"
    SavedPlaceKind.Category -> {
      val categoryId = (this as? CategoryStation)?.categoryId ?: "unknown"
      "category:$categoryId:$stationId:$cityId"
    }
  }

  @Serializable
  @SerialName("favorite_station")
  data class FavoriteStation(
    override val stationId: String,
    override val cityId: String,
    override val stationName: String? = null,
  ) : SavedPlaceAlertTarget {
    override val kind: SavedPlaceKind = SavedPlaceKind.Favorite
  }

  @Serializable
  @SerialName("home")
  data class Home(
    override val stationId: String,
    override val cityId: String,
    override val stationName: String? = null,
  ) : SavedPlaceAlertTarget {
    override val kind: SavedPlaceKind = SavedPlaceKind.Home
  }

  @Serializable
  @SerialName("work")
  data class Work(
    override val stationId: String,
    override val cityId: String,
    override val stationName: String? = null,
  ) : SavedPlaceAlertTarget {
    override val kind: SavedPlaceKind = SavedPlaceKind.Work
  }

  @Serializable
  @SerialName("category_station")
  data class CategoryStation(
    override val stationId: String,
    override val cityId: String,
    override val stationName: String? = null,
    val categoryId: String,
    val categoryLabel: String? = null,
  ) : SavedPlaceAlertTarget {
    override val kind: SavedPlaceKind = SavedPlaceKind.Category
  }
}

@Serializable
sealed interface SavedPlaceAlertCondition {
  @Serializable
  @SerialName("bikes_at_least")
  data class BikesAtLeast(val count: Int) : SavedPlaceAlertCondition

  @Serializable
  @SerialName("docks_at_least")
  data class DocksAtLeast(val count: Int) : SavedPlaceAlertCondition

  @Serializable
  @SerialName("bikes_equals_zero")
  data object BikesEqualsZero : SavedPlaceAlertCondition

  @Serializable
  @SerialName("docks_equals_zero")
  data object DocksEqualsZero : SavedPlaceAlertCondition
}

@Serializable
data class SavedPlaceAlertRule(
  val id: String,
  val target: SavedPlaceAlertTarget,
  val condition: SavedPlaceAlertCondition,
  val isEnabled: Boolean = true,
  val lastTriggeredEpoch: Long? = null,
  val lastObservedValue: Int? = null,
  val lastConditionMatched: Boolean = false,
) {
  fun metricValue(station: Station): Int = when (condition) {
    is SavedPlaceAlertCondition.BikesAtLeast -> station.bikesAvailable
    is SavedPlaceAlertCondition.DocksAtLeast -> station.slotsFree
    SavedPlaceAlertCondition.BikesEqualsZero -> station.bikesAvailable
    SavedPlaceAlertCondition.DocksEqualsZero -> station.slotsFree
  }

  fun matches(station: Station): Boolean = when (val currentCondition = condition) {
    is SavedPlaceAlertCondition.BikesAtLeast -> station.bikesAvailable >= currentCondition.count
    is SavedPlaceAlertCondition.DocksAtLeast -> station.slotsFree >= currentCondition.count
    SavedPlaceAlertCondition.BikesEqualsZero -> station.bikesAvailable == 0
    SavedPlaceAlertCondition.DocksEqualsZero -> station.slotsFree == 0
  }
}

@Serializable
data class SavedPlaceAlertTrigger(
  val ruleId: String,
  val target: SavedPlaceAlertTarget,
  val stationId: String,
  val stationName: String,
  val cityId: String,
  val condition: SavedPlaceAlertCondition,
  val triggeredAtEpoch: Long,
  val bikesAvailable: Int,
  val docksAvailable: Int,
)

@Serializable
data class SavedPlaceAlertEvaluationResult(
  val updatedRules: List<SavedPlaceAlertRule>,
  val triggers: List<SavedPlaceAlertTrigger>,
)

fun evaluateSavedPlaceAlerts(
  rules: List<SavedPlaceAlertRule>,
  stations: Map<String, Station>,
  nowEpoch: Long = currentTimeMs(),
  minimumRetriggerIntervalMillis: Long = 60L * 60L * 1000L,
): SavedPlaceAlertEvaluationResult {
  val updatedRules = mutableListOf<SavedPlaceAlertRule>()
  val triggers = mutableListOf<SavedPlaceAlertTrigger>()

  rules.forEach { rule ->
    val station = stations[rule.target.stationId]
    if (station == null || !rule.isEnabled) {
      updatedRules += rule.copy(lastConditionMatched = false)
      return@forEach
    }

    val metricValue = rule.metricValue(station)
    val matches = rule.matches(station)
    val withinCooldown = rule.lastTriggeredEpoch?.let { nowEpoch - it < minimumRetriggerIntervalMillis } == true
    val crossedThreshold = !rule.lastConditionMatched && matches
    val shouldTrigger = crossedThreshold && !withinCooldown

    updatedRules += rule.copy(
      lastObservedValue = metricValue,
      lastConditionMatched = matches,
      lastTriggeredEpoch = if (shouldTrigger) nowEpoch else rule.lastTriggeredEpoch,
    )

    if (shouldTrigger) {
      triggers += SavedPlaceAlertTrigger(
        ruleId = rule.id,
        target = rule.target,
        stationId = station.id,
        stationName = station.name,
        cityId = rule.target.cityId,
        condition = rule.condition,
        triggeredAtEpoch = nowEpoch,
        bikesAvailable = station.bikesAvailable,
        docksAvailable = station.slotsFree,
      )
    }
  }

  return SavedPlaceAlertEvaluationResult(updatedRules = updatedRules, triggers = triggers)
}
