package com.gcaguilar.biciradar.appfunctions

import androidx.appfunctions.AppFunctionSerializable
import java.time.Instant

/**
 * The city currently selected in BiciRadar.
 *
 * Every other BiciRadar function reads and writes data for this city. The city
 * is a user setting: the functions never change it.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class CityInfo(
  /** Stable city ID such as "zaragoza"; use it only for display or logging. */
  val id: String,
  /** Human-readable city name to show to the user. */
  val displayName: String,
  /** Whether this city has electric bikes, so e-bike counts are meaningful. */
  val supportsEbikes: Boolean,
)

/**
 * A public bike-sharing station with its current availability.
 *
 * Values are a live snapshot: availability changes every few minutes, so a
 * `StationInfo` must not be cached across conversations.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class StationInfo(
  /** Stable station ID; pass it to the other BiciRadar station functions. */
  val id: String,
  /** Human-readable station name to show to the user. */
  val name: String,
  /** Street address of the station. */
  val address: String,
  /** Bikes available to rent right now, including electric bikes. */
  val bikesAvailable: Int,
  /** Electric bikes available to rent; 0 when the city has no e-bikes or none are left. */
  val ebikesAvailable: Int,
  /**
   * Mechanical bikes available to rent when the provider reports per-type counts;
   * 0 when it does not. Use "bikesAvailable" for the total.
   */
  val regularBikesAvailable: Int,
  /** Free docks where the user can return a bike right now. */
  val freeDocks: Int,
  /** Distance from the user in meters, or -1 when the user's location is unknown. */
  val distanceMeters: Int,
  /** Latitude of the station in degrees. */
  val latitude: Double,
  /** Longitude of the station in degrees. */
  val longitude: Double,
  /** Whether the user marked this station as favorite. */
  val isFavorite: Boolean,
  /** Whether this station is the user's pinned home station. */
  val isHome: Boolean,
  /** Whether this station is the user's pinned work station. */
  val isWork: Boolean,
  /** When the availability snapshot was last refreshed; null when never refreshed. */
  val updatedAt: Instant?,
)

/**
 * Result of starting a station monitoring session.
 *
 * Monitoring is time-boxed and ends automatically after
 * `durationMinutes`. A `started` value of false means no alert was scheduled.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class MonitoringInfo(
  /** Station being monitored. */
  val stationId: String,
  /** Whether the monitoring session started and alerts will be delivered. */
  val started: Boolean,
  /** Total duration of the session in minutes. */
  val durationMinutes: Int,
  /** What the session watches: "BIKES" or "DOCKS". */
  val monitors: String,
  /** Actionable explanation for the agent. */
  val message: String,
)

/**
 * A saved-place availability alert rule.
 *
 * Rules are keyed by station and target, so creating an alert again for the same
 * pair replaces its condition instead of adding a duplicate.
 */
@AppFunctionSerializable(isDescribedByKDoc = true)
data class SavedPlaceAlertInfo(
  /** Stable rule ID; repeating the same station and target replaces this rule. */
  val id: String,
  /** Station the alert watches. */
  val stationId: String,
  /** Station display name. */
  val stationName: String,
  /** Saved-place kind: "FAVORITE", "HOME" or "WORK". */
  val target: String,
  /** Condition kind, e.g. "DOCKS_AT_LEAST". */
  val condition: String,
  /** Threshold for *_AT_LEAST conditions; 0 for *_EQUALS_ZERO. */
  val count: Int,
  /** Whether the rule is enabled; newly created rules are enabled. */
  val isEnabled: Boolean,
)
