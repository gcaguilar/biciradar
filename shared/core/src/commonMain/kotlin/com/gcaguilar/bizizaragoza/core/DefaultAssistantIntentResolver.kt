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
      AssistantAction.NearestStation -> {
        val station = stationsState.stations.firstOrNull()
        AssistantResolution(
          spokenResponse = station?.let {
            "La estación más cercana es ${it.name} con ${it.bikesAvailable} bicis y ${it.slotsFree} anclajes."
          } ?: "No tengo datos de estaciones cercanas ahora mismo.",
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
}
