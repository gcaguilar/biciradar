package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Creates or replaces the saved-place alert rule for [target].
 *
 * Rules are keyed by the target identity, so calling this twice for the same
 * station and target updates the condition instead of adding a duplicate.
 * [SavedPlaceAlertsRepositoryImpl] persists the rule and syncs it with the
 * backend subscription service.
 */
@SingleIn(AppScope::class)
@Inject
class UpsertSavedPlaceAlertRule(
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
) {
  suspend fun execute(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ): SavedPlaceAlertRule {
    val rule =
      SavedPlaceAlertRule(
        id = target.identityKey(),
        target = target,
        condition = condition,
        isEnabled = true,
      )
    savedPlaceAlertsRepository.upsertRule(target, condition)
    return rule
  }
}
