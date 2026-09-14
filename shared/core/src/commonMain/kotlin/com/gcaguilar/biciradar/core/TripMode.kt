package com.gcaguilar.biciradar.core

import kotlinx.serialization.Serializable

/**
 * Global travel intent that drives which nearby stations are shown and which
 * availability filter the map recommends.
 *
 * - [Pedestrian]: the user is on foot and wants to pick up a bike, so only
 *   stations with bikes available are shown.
 * - [Cyclist]: the user is already riding and wants to drop the bike off, so
 *   only stations with free slots are shown.
 */
@Serializable
enum class TripMode {
  Pedestrian,
  Cyclist,
}

/** Whether this station can serve [mode]: bikes to pick up or slots to drop off. */
fun Station.matchesTripMode(mode: TripMode): Boolean =
  when (mode) {
    TripMode.Pedestrian -> bikesAvailable > 0
    TripMode.Cyclist -> slotsFree > 0
  }

/**
 * Stations relevant for [mode] — with bikes when on foot, with free slots when
 * cycling — ordered by distance.
 *
 * Stations that cannot serve the mode are filtered out so the list always shows
 * places where you can actually get or leave a bike.
 */
fun stationsForTripMode(
  stations: List<Station>,
  mode: TripMode,
): List<Station> =
  stations
    .filter { it.matchesTripMode(mode) }
    .sortedBy(Station::distanceMeters)

/** Mode-aware variant of [selectNearbyStation]. */
fun selectNearbyStationForTripMode(
  stations: List<Station>,
  searchRadiusMeters: Int,
  mode: TripMode,
): NearbyStationSelection =
  selectNearbyStation(
    stations = stations,
    searchRadiusMeters = searchRadiusMeters,
  ) { station ->
    station.matchesTripMode(mode)
  }
