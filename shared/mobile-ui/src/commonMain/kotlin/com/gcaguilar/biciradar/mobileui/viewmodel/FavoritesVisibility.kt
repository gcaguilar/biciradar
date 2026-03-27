package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.Station

internal fun visibleFavoriteStations(
  stations: List<Station>,
  favoriteIds: Set<String>,
  homeStationId: String?,
  workStationId: String?,
): List<Station> {
  val pinnedIds = setOfNotNull(homeStationId, workStationId)
  return stations.filter { station ->
    station.id in favoriteIds && station.id !in pinnedIds
  }.distinctBy { it.id }
}
