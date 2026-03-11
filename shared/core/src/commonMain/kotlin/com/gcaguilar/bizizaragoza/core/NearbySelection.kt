package com.gcaguilar.bizizaragoza.core

const val DEFAULT_SEARCH_RADIUS_METERS = 500

val SEARCH_RADIUS_OPTIONS_METERS = listOf(300, 500, 750, 1000)

data class NearbyStationSelection(
  val withinRadiusStation: Station?,
  val fallbackStation: Station?,
  val radiusMeters: Int,
) {
  val highlightedStation: Station? = withinRadiusStation ?: fallbackStation
  val usesFallback: Boolean = withinRadiusStation == null && fallbackStation != null
}

fun normalizeSearchRadiusMeters(searchRadiusMeters: Int): Int = searchRadiusMeters
  .takeIf { it in SEARCH_RADIUS_OPTIONS_METERS }
  ?: DEFAULT_SEARCH_RADIUS_METERS

fun selectNearbyStation(
  stations: List<Station>,
  searchRadiusMeters: Int,
  predicate: (Station) -> Boolean = { true },
): NearbyStationSelection {
  val normalizedRadius = normalizeSearchRadiusMeters(searchRadiusMeters)
  val fallbackStation = stations.firstOrNull(predicate)
  val withinRadiusStation = stations.firstOrNull { station ->
    station.distanceMeters <= normalizedRadius && predicate(station)
  }
  return NearbyStationSelection(
    withinRadiusStation = withinRadiusStation,
    fallbackStation = fallbackStation,
    radiusMeters = normalizedRadius,
  )
}
