package com.gcaguilar.biciradar.core

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc

class DefaultAssistantIntentResolver : AssistantIntentResolver {
  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution {
    return when (action) {
      AssistantAction.FavoriteStations -> AssistantResolution(
        spokenResponse = StringDesc.ResourceFormatted(MR.strings.favoriteCount, favoriteIds.size),
      )
      AssistantAction.NearestStation -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters),
        emptyMessage = StringDesc.Resource(MR.strings.noNearbyStations),
        withinRadiusFormatter = { station ->
          StringDesc.ResourceFormatted(MR.strings.nearestStation, station.name, station.bikesAvailable, station.slotsFree)
        },
        fallbackFormatter = { station, radiusMeters ->
          StringDesc.ResourceFormatted(
            MR.strings.nearestStationFallback,
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
        emptyMessage = StringDesc.Resource(MR.strings.noNearbyBikes),
        withinRadiusFormatter = { station ->
          StringDesc.ResourceFormatted(MR.strings.nearestWithBikes, station.name, station.bikesAvailable, station.slotsFree)
        },
        fallbackFormatter = { station, radiusMeters ->
          StringDesc.ResourceFormatted(
            MR.strings.nearestWithBikesFallback,
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
        emptyMessage = StringDesc.Resource(MR.strings.noNearbySlots),
        withinRadiusFormatter = { station ->
          StringDesc.ResourceFormatted(MR.strings.nearestWithSlots, station.name, station.slotsFree, station.bikesAvailable)
        },
        fallbackFormatter = { station, radiusMeters ->
          StringDesc.ResourceFormatted(
            MR.strings.nearestWithSlotsFallback,
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
            StringDesc.ResourceFormatted(MR.strings.stationBikes, it.name, it.bikesAvailable)
          } ?: StringDesc.Resource(MR.strings.unknownStation),
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.StationSlotCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            StringDesc.ResourceFormatted(MR.strings.stationSlots, it.name, it.slotsFree)
          } ?: StringDesc.Resource(MR.strings.unknownStation),
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.RouteToStation -> AssistantResolution(
        spokenResponse = StringDesc.Resource(MR.strings.routeToSelectedStation),
        highlightedStationId = action.stationId,
      )
      is AssistantAction.StationStatus -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            StringDesc.ResourceFormatted(MR.strings.stationStatus, it.name, it.bikesAvailable, it.slotsFree)
          } ?: StringDesc.Resource(MR.strings.unknownStation),
          highlightedStationId = station?.id,
        )
      }
    }
  }

  private fun nearestStationResolution(
    selection: NearbyStationSelection,
    emptyMessage: StringDesc,
    withinRadiusFormatter: (Station) -> StringDesc,
    fallbackFormatter: (Station, Int) -> StringDesc,
  ): AssistantResolution = AssistantResolution(
    spokenResponse = when {
      selection.withinRadiusStation != null -> withinRadiusFormatter(selection.withinRadiusStation)
      selection.fallbackStation != null -> fallbackFormatter(selection.fallbackStation, selection.radiusMeters)
      else -> emptyMessage
    },
    highlightedStationId = selection.highlightedStation?.id,
  )
}
