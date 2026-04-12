package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Evaluates saved-place alert rules against the current station state,
 * persists the updated rules, and returns the list of triggers.
 *
 * The caller is responsible for delivering platform notifications
 * (e.g. UNUserNotificationCenter on iOS).
 *
 * Returns an empty list when there are no enabled rules or no station
 * data is available.
 */
@SingleIn(AppScope::class)
@Inject
class EvaluateSavedPlaceAlerts(
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val stationsRepository: StationsRepository,
  private val savedPlaceAlertsEvaluator: SavedPlaceAlertsEvaluator,
) {
  suspend fun execute(nowEpoch: Long = currentTimeMs()): List<SavedPlaceAlertTrigger> {
    val rules = savedPlaceAlertsRepository.currentRules()
    if (rules.isEmpty()) return emptyList()

    stationsRepository.loadIfNeeded()
    val stationsState = stationsRepository.state.value

    val evaluation =
      savedPlaceAlertsEvaluator.evaluate(
        rules = rules,
        stationsState = stationsState,
        nowEpoch = nowEpoch,
      )

    savedPlaceAlertsRepository.replaceAll(evaluation.updatedRules)
    return evaluation.triggers
  }
}
