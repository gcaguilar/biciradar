package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.Station

internal fun sortWearFavoriteStations(
  stations: List<Station>,
  homeStationId: String?,
  workStationId: String?,
): List<Station> = stations.sortedWith(
  compareByDescending<Station> { it.id == homeStationId }
    .thenByDescending { it.id == workStationId }
    .thenBy { it.distanceMeters }
    .thenBy { it.name },
)

internal fun wearSavedPlaceLabel(
  stationId: String,
  homeStationId: String?,
  workStationId: String?,
): String? = when (stationId) {
  homeStationId -> "Casa"
  workStationId -> "Trabajo"
  else -> null
}
