package com.gcaguilar.biciradar.core

// TODO: Migrate to Compose Resources when available
class DefaultAssistantIntentResolver : AssistantIntentResolver {
  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution {
    return when (action) {
      AssistantAction.FavoriteStations -> AssistantResolution(
        spokenResponse = "You have ${favoriteIds.size} favorite stations",
      )
      AssistantAction.NearestStation -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters),
        emptyMessage = "No nearby stations found",
        withinRadiusFormatter = { station ->
          "Nearest station: ${station.name}. ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free"
        },
        fallbackFormatter = { station, radiusMeters ->
          "No stations within ${radiusMeters}m. Nearest is ${station.distanceMeters}m away: ${station.name}. ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free"
        },
      )
      AssistantAction.NearestStationWithBikes -> nearestStationResolution(
        selection = selectNearbyStationWithBikes(stationsState.stations, searchRadiusMeters),
        emptyMessage = "No nearby stations with bikes",
        withinRadiusFormatter = { station ->
          "Nearest station with bikes: ${station.name}. ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free"
        },
        fallbackFormatter = { station, radiusMeters ->
          "No stations with bikes within ${radiusMeters}m. Nearest is ${station.distanceMeters}m away: ${station.name}. ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free"
        },
      )
      AssistantAction.NearestStationWithSlots -> nearestStationResolution(
        selection = selectNearbyStationWithSlots(stationsState.stations, searchRadiusMeters),
        emptyMessage = "No nearby stations with free slots",
        withinRadiusFormatter = { station ->
          "Nearest station with slots: ${station.name}. ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free"
        },
        fallbackFormatter = { station, radiusMeters ->
          "No stations with slots within ${radiusMeters}m. Nearest is ${station.distanceMeters}m away: ${station.name}. ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free"
        },
      )
      is AssistantAction.RouteToStation -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        if (station != null) {
          AssistantResolution(
            spokenResponse = "Opening route to ${station.name}",
            highlightedStationId = station.id,
          )
        } else {
          AssistantResolution(
            spokenResponse = "Station not found",
          )
        }
      }
      is AssistantAction.StationBikeCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        if (station != null) {
          AssistantResolution(
            spokenResponse = "${station.name} has ${station.bikesAvailable} bikes available",
            highlightedStationId = station.id,
          )
        } else {
          AssistantResolution(
            spokenResponse = "Station not found",
          )
        }
      }
      is AssistantAction.StationSlotCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        if (station != null) {
          AssistantResolution(
            spokenResponse = "${station.name} has ${station.slotsFree} free slots",
            highlightedStationId = station.id,
          )
        } else {
          AssistantResolution(
            spokenResponse = "Station not found",
          )
        }
      }
      is AssistantAction.StationStatus -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        if (station != null) {
          AssistantResolution(
            spokenResponse = "${station.name}: ${station.bikesAvailable} bikes available, ${station.slotsFree} slots free",
            highlightedStationId = station.id,
          )
        } else {
          AssistantResolution(
            spokenResponse = "Station not found",
          )
        }
      }
    }
  }

  private fun formatStationList(
    stations: List<Station>,
    limit: Int,
  ): List<Pair<String, String>> = stations
    .take(limit)
    .map { station ->
      station.name to "${station.bikesAvailable} bikes, ${station.slotsFree} slots"
    }

  private fun nearestStationResolution(
    selection: NearbyStationSelection,
    emptyMessage: String,
    withinRadiusFormatter: (Station) -> String,
    fallbackFormatter: (Station, Int) -> String,
  ): AssistantResolution = AssistantResolution(
    spokenResponse = when {
      selection.withinRadiusStation != null -> withinRadiusFormatter(selection.withinRadiusStation)
      selection.fallbackStation != null -> fallbackFormatter(selection.fallbackStation, selection.radiusMeters)
      else -> emptyMessage
    },
    highlightedStationId = selection.highlightedStation?.id,
  )
}
