package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.distanceBetween

const val DEFAULT_SEARCH_RADIUS_METERS = 500

val SEARCH_RADIUS_OPTIONS_METERS = listOf(300, 500, 750, 1000, 1500, 2000, 3000, 5000)

fun formatDistance(meters: Int): String =
  when {
    meters >= 1000 -> {
      val km = meters / 1000.0
      val rounded = (km * 10).toInt() / 10.0
      "$rounded km"
    }

    else -> {
      "$meters m"
    }
  }

data class NearbyStationSelection(
  val withinRadiusStation: Station?,
  val fallbackStation: Station?,
  val radiusMeters: Int,
) {
  val highlightedStation: Station? = withinRadiusStation ?: fallbackStation
  val usesFallback: Boolean = withinRadiusStation == null && fallbackStation != null
}

fun normalizeSearchRadiusMeters(searchRadiusMeters: Int): Int =
  searchRadiusMeters
    .takeIf { it in SEARCH_RADIUS_OPTIONS_METERS }
    ?: DEFAULT_SEARCH_RADIUS_METERS

fun selectNearbyStation(
  stations: List<Station>,
  searchRadiusMeters: Int,
  predicate: (Station) -> Boolean = { true },
): NearbyStationSelection {
  val normalizedRadius = normalizeSearchRadiusMeters(searchRadiusMeters)
  val fallbackStation = stations.firstOrNull(predicate)
  val withinRadiusStation =
    stations.firstOrNull { station ->
      station.distanceMeters <= normalizedRadius && predicate(station)
    }
  return NearbyStationSelection(
    withinRadiusStation = withinRadiusStation,
    fallbackStation = fallbackStation,
    radiusMeters = normalizedRadius,
  )
}

fun selectNearbyStationWithBikes(
  stations: List<Station>,
  searchRadiusMeters: Int,
): NearbyStationSelection =
  selectNearbyStation(
    stations = stations,
    searchRadiusMeters = searchRadiusMeters,
  ) { station ->
    station.bikesAvailable > 0
  }

fun selectNearbyStationWithSlots(
  stations: List<Station>,
  searchRadiusMeters: Int,
): NearbyStationSelection =
  selectNearbyStation(
    stations = stations,
    searchRadiusMeters = searchRadiusMeters,
  ) { station ->
    station.slotsFree > 0
  }

/**
 * Returns copies of [stations] ordered by their distance to [origin], closest first.
 *
 * Every copy gets `distanceMeters` recomputed with [distanceBetween], so the stored
 * value (relative to the app's foreground location) never leaks into the result.
 * Ties are broken by station name and then by id, which makes the ordering stable and
 * independent of the input order. The input list and its items are never mutated.
 */
fun stationsSortedByDistanceFrom(
  stations: List<Station>,
  origin: GeoPoint,
): List<Station> {
  val rebased =
    stations.map { station ->
      station.copy(distanceMeters = distanceBetween(origin, station.location))
    }
  return rebased.sortedWith(
    compareBy<Station> { it.distanceMeters }
      .thenBy { it.name }
      .thenBy { it.id },
  )
}
