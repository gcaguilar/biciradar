package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.SearchTextNormalizer
import com.gcaguilar.biciradar.core.StationRequirement
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind

/** Smallest accepted value for the `limit` argument of the listing functions. */
internal const val MIN_RESULT_LIMIT = 1

/** Largest accepted value for the `limit` argument of the listing functions. */
internal const val MAX_RESULT_LIMIT = 20

/** Number of results used when the caller omits `limit`. */
internal const val DEFAULT_RESULT_LIMIT = 5

/** Duration used when the caller omits `durationMinutes` of the monitoring functions. */
internal const val DEFAULT_MONITORING_MINUTES = 10

/** Shortest accepted monitoring session in minutes. */
internal const val MIN_MONITORING_MINUTES = 1

/** Longest accepted monitoring session in minutes. */
internal const val MAX_MONITORING_MINUTES = 60

/** Seconds in a minute, used to translate the documented minutes into domain seconds. */
internal const val SECONDS_PER_MINUTE = 60

/** Threshold used by `*_AT_LEAST` alert conditions when the caller omits `count`. */
internal const val DEFAULT_ALERT_COUNT = 1

/** Smallest accepted threshold for `*_AT_LEAST` alert conditions. */
internal const val MIN_ALERT_COUNT = 1

/** Largest accepted threshold for `*_AT_LEAST` alert conditions. */
internal const val MAX_ALERT_COUNT = 100

/**
 * Queries that resolve to the user's pinned home or work station.
 *
 * The strings match the normalization applied by
 * [SearchTextNormalizer.normalizeStationSearchQuery], which is also what the
 * shared alias resolver uses.
 */
internal val PINNED_STATION_ALIASES =
  setOf("casa", "mi casa", "home", "trabajo", "mi trabajo", "work", "oficina", "mi oficina")

/**
 * Parses a `requirement` argument.
 *
 * Accepts surrounding whitespace and any letter case, so `" bikes "` and
 * `"BIKES"` are equivalent. Returns null when the value is not one of the
 * documented requirements; callers turn null into an invalid-argument error.
 */
internal fun parseStationRequirement(raw: String): StationRequirement? {
  val candidate = raw.trim()
  if (candidate.isEmpty()) return null
  return StationRequirement.entries.firstOrNull { requirement ->
    requirement.name.equals(candidate, ignoreCase = true)
  }
}

/** Whether [limit] is inside the documented 1..20 range. */
internal fun isValidResultLimit(limit: Int): Boolean = limit in MIN_RESULT_LIMIT..MAX_RESULT_LIMIT

/** Saved-place categories accepted by `createSavedPlaceAlert`. */
internal enum class AlertTargetArgument {
  FAVORITE,
  HOME,
  WORK,
}

/** Condition kinds accepted by `createSavedPlaceAlert`. */
internal enum class AlertConditionArgument {
  BIKES_AT_LEAST,
  DOCKS_AT_LEAST,
  BIKES_EQUALS_ZERO,
  DOCKS_EQUALS_ZERO,
}

/**
 * Parses the `kind` argument of `startStationMonitoring`.
 *
 * Accepts surrounding whitespace and any letter case, so `" bikes "` and
 * `"BIKES"` are equivalent. Returns null when the value is neither `BIKES` nor
 * `DOCKS`; callers turn null into an invalid-argument error.
 */
internal fun parseMonitoringKind(raw: String): SurfaceMonitoringKind? =
  when (raw.trim().uppercase()) {
    "BIKES" -> SurfaceMonitoringKind.Bikes
    "DOCKS" -> SurfaceMonitoringKind.Docks
    else -> null
  }

/** Whether [minutes] is inside the documented 1..60 monitoring range. */
internal fun isValidMonitoringDuration(minutes: Int): Boolean =
  minutes in MIN_MONITORING_MINUTES..MAX_MONITORING_MINUTES

/** Whether [count] is inside the documented 1..100 alert threshold range. */
internal fun isValidAlertCount(count: Int): Boolean = count in MIN_ALERT_COUNT..MAX_ALERT_COUNT

/**
 * Parses the `target` argument of `createSavedPlaceAlert`.
 *
 * Accepts surrounding whitespace and any letter case. Returns null when the
 * value is not one of the documented saved-place categories.
 */
internal fun parseAlertTargetArgument(raw: String): AlertTargetArgument? =
  AlertTargetArgument.entries.firstOrNull { target ->
    target.name.equals(raw.trim(), ignoreCase = true)
  }

/**
 * Parses the `condition` argument of `createSavedPlaceAlert`.
 *
 * Accepts surrounding whitespace and any letter case. Returns null when the
 * value is not one of the documented condition kinds.
 */
internal fun parseAlertConditionArgument(raw: String): AlertConditionArgument? =
  AlertConditionArgument.entries.firstOrNull { condition ->
    condition.name.equals(raw.trim(), ignoreCase = true)
  }

/** Whether [query] is an alias for the user's pinned home or work station. */
internal fun isPinnedStationAlias(query: String): Boolean =
  SearchTextNormalizer.normalizeStationSearchQuery(query) in PINNED_STATION_ALIASES

/** Result of validating the optional `latitude`/`longitude` pair of a function. */
internal sealed interface OriginArgument {
  /** The caller supplied no coordinates. */
  data object Absent : OriginArgument

  /** The caller supplied a valid coordinate pair. */
  data class Present(
    val point: GeoPoint,
  ) : OriginArgument

  /** The caller supplied incomplete or out-of-range coordinates. */
  data object Invalid : OriginArgument
}

/**
 * Validates the optional origin of a location-aware function.
 *
 * Both coordinates must be present or both absent, and each must be a finite
 * value inside its geographic range.
 */
internal fun parseOriginArgument(
  latitude: Double?,
  longitude: Double?,
): OriginArgument =
  when {
    latitude == null && longitude == null -> OriginArgument.Absent
    latitude == null || longitude == null -> OriginArgument.Invalid
    !latitude.isFinite() || !longitude.isFinite() -> OriginArgument.Invalid
    latitude !in -90.0..90.0 || longitude !in -180.0..180.0 -> OriginArgument.Invalid
    else -> OriginArgument.Present(GeoPoint(latitude = latitude, longitude = longitude))
  }
