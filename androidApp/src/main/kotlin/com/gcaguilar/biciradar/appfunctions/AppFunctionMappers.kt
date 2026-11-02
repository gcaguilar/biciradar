package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceKind
import com.gcaguilar.biciradar.core.Station
import java.time.Instant

/**
 * Maps a domain [City] to the AppFunctions [CityInfo] contract.
 *
 * Pure on purpose: the AppFunctions surface must not leak domain models or
 * perform I/O in the mapping step.
 */
internal fun City.toCityInfo(): CityInfo =
  CityInfo(
    id = id,
    displayName = displayName,
    supportsEbikes = supportsEbikes,
  )

/**
 * Maps a domain [Station] to the AppFunctions [StationInfo] contract.
 *
 * @param isFavorite Whether the user marked this station as favorite.
 * @param isHome Whether this station is the user's pinned home station.
 * @param isWork Whether this station is the user's pinned work station.
 * @param lastUpdatedEpoch Repository timestamp of the availability snapshot, in epoch millis.
 * @param distanceIsUserRelative Whether [Station.distanceMeters] was measured from a real
 * user location (or from coordinates supplied by the caller). Repository distances fall back
 * to the city centre when the app has no location, so they are reported as -1 instead.
 */
internal fun Station.toStationInfo(
  isFavorite: Boolean,
  isHome: Boolean,
  isWork: Boolean,
  lastUpdatedEpoch: Long?,
  distanceIsUserRelative: Boolean,
): StationInfo =
  StationInfo(
    id = id,
    name = name,
    address = address,
    bikesAvailable = bikesAvailable,
    ebikesAvailable = ebikesAvailable,
    regularBikesAvailable = regularBikesAvailable,
    freeDocks = slotsFree,
    distanceMeters = if (distanceIsUserRelative) distanceMeters else UNKNOWN_DISTANCE_METERS,
    latitude = location.latitude,
    longitude = location.longitude,
    isFavorite = isFavorite,
    isHome = isHome,
    isWork = isWork,
    updatedAt = lastUpdatedEpoch?.let(Instant::ofEpochMilli),
  )

/** Sentinel used by [StationInfo.distanceMeters] when the user's location is unknown. */
internal const val UNKNOWN_DISTANCE_METERS = -1

/**
 * Maps a persisted [SavedPlaceAlertRule] to the AppFunctions
 * [SavedPlaceAlertInfo] contract.
 *
 * The rule ID is derived from the target identity, so the caller can report it
 * as stable across repeated creations of the same station and target.
 */
internal fun SavedPlaceAlertRule.toSavedPlaceAlertInfo(): SavedPlaceAlertInfo =
  SavedPlaceAlertInfo(
    id = id,
    stationId = target.stationId,
    stationName = target.stationName ?: target.stationId,
    target = target.kind.toAlertTargetName(),
    condition = condition.toAlertConditionName(),
    count = condition.thresholdCount(),
    isEnabled = isEnabled,
  )

/** Maps a saved-place category to the documented `target` argument value. */
internal fun SavedPlaceKind.toAlertTargetName(): String =
  when (this) {
    SavedPlaceKind.Favorite -> "FAVORITE"
    SavedPlaceKind.Home -> "HOME"
    SavedPlaceKind.Work -> "WORK"
    SavedPlaceKind.Category -> "CATEGORY"
  }

/** Maps an alert condition to the documented `condition` argument value. */
internal fun SavedPlaceAlertCondition.toAlertConditionName(): String =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> "BIKES_AT_LEAST"
    is SavedPlaceAlertCondition.DocksAtLeast -> "DOCKS_AT_LEAST"
    SavedPlaceAlertCondition.BikesEqualsZero -> "BIKES_EQUALS_ZERO"
    SavedPlaceAlertCondition.DocksEqualsZero -> "DOCKS_EQUALS_ZERO"
  }

/** Reports the threshold of an alert condition; zero for `*_EQUALS_ZERO`. */
internal fun SavedPlaceAlertCondition.thresholdCount(): Int =
  when (this) {
    is SavedPlaceAlertCondition.BikesAtLeast -> count
    is SavedPlaceAlertCondition.DocksAtLeast -> count
    SavedPlaceAlertCondition.BikesEqualsZero -> 0
    SavedPlaceAlertCondition.DocksEqualsZero -> 0
  }
