package com.gcaguilar.bizizaragoza.core

class DefaultAssistantIntentResolver : AssistantIntentResolver {
  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
  ): AssistantResolution {
    return when (action) {
      AssistantAction.FavoriteStations -> AssistantResolution(
        spokenResponse = "Tienes ${favoriteIds.size} estaciones guardadas en Zaragoza Bizi.",
      )
      AssistantAction.NearestStation -> nearestStationResolution(
        station = stationsState.stations.firstOrNull(),
        emptyMessage = "No tengo datos de estaciones cercanas ahora mismo.",
        formatter = { station ->
          "La estación más cercana es ${station.name} con ${station.bikesAvailable} bicis y ${station.slotsFree} anclajes."
        },
      )
      AssistantAction.NearestStationWithBikes -> nearestStationResolution(
        station = stationsState.stations.firstOrNull { station -> station.bikesAvailable > 0 },
        emptyMessage = "No he encontrado estaciones cercanas con bicis disponibles ahora mismo.",
        formatter = { station ->
          "La estación más cercana con bicis disponibles es ${station.name} con ${station.bikesAvailable} bicis y ${station.slotsFree} anclajes."
        },
      )
      AssistantAction.NearestStationWithSlots -> nearestStationResolution(
        station = stationsState.stations.firstOrNull { station -> station.slotsFree > 0 },
        emptyMessage = "No he encontrado estaciones cercanas con huecos libres ahora mismo.",
        formatter = { station ->
          "La estación más cercana con huecos libres es ${station.name} con ${station.slotsFree} huecos y ${station.bikesAvailable} bicis."
        },
      )
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
    station: Station?,
    emptyMessage: String,
    formatter: (Station) -> String,
  ): AssistantResolution = AssistantResolution(
    spokenResponse = station?.let(formatter) ?: emptyMessage,
    highlightedStationId = station?.id,
  )
}
