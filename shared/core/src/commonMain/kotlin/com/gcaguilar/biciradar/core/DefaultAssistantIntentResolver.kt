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
        spokenResponse = "Tienes ${favoriteIds.size} estaciones guardadas en Bici Radar.",
      )
      AssistantAction.NearestStation -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters),
        emptyMessage = "No tengo datos de estaciones cercanas ahora mismo.",
        withinRadiusFormatter = { station ->
          "La estación más cercana es ${station.name} con ${station.bikesAvailable} bicis y ${station.slotsFree} anclajes."
        },
        fallbackFormatter = { station, radiusMeters ->
          "No he encontrado ninguna estación dentro de ${radiusMeters} m. La más cercana está a ${station.distanceMeters} m: ${station.name}, con ${station.bikesAvailable} bicis y ${station.slotsFree} anclajes."
        },
      )
      AssistantAction.NearestStationWithBikes -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters) { station ->
          station.bikesAvailable > 0
        },
        emptyMessage = "No he encontrado estaciones cercanas con bicis disponibles ahora mismo.",
        withinRadiusFormatter = { station ->
          "La estación más cercana con bicis disponibles es ${station.name} con ${station.bikesAvailable} bicis y ${station.slotsFree} anclajes."
        },
        fallbackFormatter = { station, radiusMeters ->
          "No he encontrado estaciones con bicis disponibles dentro de ${radiusMeters} m. La más cercana está a ${station.distanceMeters} m: ${station.name}, con ${station.bikesAvailable} bicis y ${station.slotsFree} anclajes."
        },
      )
      AssistantAction.NearestStationWithSlots -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters) { station ->
          station.slotsFree > 0
        },
        emptyMessage = "No he encontrado estaciones cercanas con huecos libres ahora mismo.",
        withinRadiusFormatter = { station ->
          "La estación más cercana con huecos libres es ${station.name} con ${station.slotsFree} huecos y ${station.bikesAvailable} bicis."
        },
        fallbackFormatter = { station, radiusMeters ->
          "No he encontrado estaciones con huecos libres dentro de ${radiusMeters} m. La más cercana está a ${station.distanceMeters} m: ${station.name}, con ${station.slotsFree} huecos y ${station.bikesAvailable} bicis."
        },
      )
      is AssistantAction.StationBikeCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            "${it.name} tiene ${it.bikesAvailable} bicis disponibles."
          } ?: "No he encontrado esa estación.",
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.StationSlotCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            "${it.name} tiene ${it.slotsFree} huecos libres."
          } ?: "No he encontrado esa estación.",
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.RouteToStation -> AssistantResolution(
        spokenResponse = "Abriendo la ruta a la estación seleccionada.",
        highlightedStationId = action.stationId,
      )
      is AssistantAction.StationStatus -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            "${it.name} tiene ${it.bikesAvailable} bicis disponibles y ${it.slotsFree} huecos libres."
          } ?: "No he encontrado esa estación.",
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
