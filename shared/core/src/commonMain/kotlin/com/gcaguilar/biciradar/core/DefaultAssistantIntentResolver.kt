package com.gcaguilar.biciradar.core

class DefaultAssistantIntentResolver : AssistantIntentResolver {
  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution {
    return when (action) {
      AssistantAction.FavoriteStations -> AssistantResolution(
        spokenResponse = sharedString(SharedString.FAVORITE_COUNT, favoriteIds.size),
      )
      AssistantAction.NearestStation -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters),
        emptyMessage = sharedString(SharedString.NO_NEARBY_STATIONS),
        withinRadiusFormatter = { station ->
          sharedString(SharedString.NEAREST_STATION, station.name, station.bikesAvailable, station.slotsFree)
        },
        fallbackFormatter = { station, radiusMeters ->
          sharedString(
            SharedString.NEAREST_STATION_FALLBACK,
            radiusMeters,
            station.distanceMeters,
            station.name,
            station.bikesAvailable,
            station.slotsFree,
          )
        },
      )
      AssistantAction.NearestStationWithBikes -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters) { station ->
          station.bikesAvailable > 0
        },
        emptyMessage = sharedString(SharedString.NO_NEARBY_BIKES),
        withinRadiusFormatter = { station ->
          sharedString(SharedString.NEAREST_WITH_BIKES, station.name, station.bikesAvailable, station.slotsFree)
        },
        fallbackFormatter = { station, radiusMeters ->
          sharedString(
            SharedString.NEAREST_WITH_BIKES_FALLBACK,
            radiusMeters,
            station.distanceMeters,
            station.name,
            station.bikesAvailable,
            station.slotsFree,
          )
        },
      )
      AssistantAction.NearestStationWithSlots -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters) { station ->
          station.slotsFree > 0
        },
        emptyMessage = sharedString(SharedString.NO_NEARBY_SLOTS),
        withinRadiusFormatter = { station ->
          sharedString(SharedString.NEAREST_WITH_SLOTS, station.name, station.slotsFree, station.bikesAvailable)
        },
        fallbackFormatter = { station, radiusMeters ->
          sharedString(
            SharedString.NEAREST_WITH_SLOTS_FALLBACK,
            radiusMeters,
            station.distanceMeters,
            station.name,
            station.slotsFree,
            station.bikesAvailable,
          )
        },
      )
      is AssistantAction.StationBikeCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            sharedString(SharedString.STATION_BIKES, it.name, it.bikesAvailable)
          } ?: sharedString(SharedString.UNKNOWN_STATION),
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.StationSlotCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            sharedString(SharedString.STATION_SLOTS, it.name, it.slotsFree)
          } ?: sharedString(SharedString.UNKNOWN_STATION),
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.RouteToStation -> AssistantResolution(
        spokenResponse = sharedString(SharedString.ROUTE_TO_SELECTED_STATION),
        highlightedStationId = action.stationId,
      )
      is AssistantAction.StationStatus -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            sharedString(SharedString.STATION_STATUS, it.name, it.bikesAvailable, it.slotsFree)
          } ?: sharedString(SharedString.UNKNOWN_STATION),
          highlightedStationId = station?.id,
        )
      }
    }
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
