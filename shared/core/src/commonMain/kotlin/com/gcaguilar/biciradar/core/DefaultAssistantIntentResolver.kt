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
        spokenResponse = localizedText("Tienes %s estaciones guardadas en Bici Radar.", favoriteIds.size),
      )
      AssistantAction.NearestStation -> nearestStationResolution(
        selection = selectNearbyStation(stationsState.stations, searchRadiusMeters),
        emptyMessage = localizedText("No tengo datos de estaciones cercanas ahora mismo."),
        withinRadiusFormatter = { station ->
          localizedText("La estación más cercana es %s con %s bicis y %s anclajes.", station.name, station.bikesAvailable, station.slotsFree)
        },
        fallbackFormatter = { station, radiusMeters ->
          localizedText(
            "No he encontrado ninguna estación dentro de %s m. La más cercana está a %s m: %s, con %s bicis y %s anclajes.",
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
        emptyMessage = localizedText("No he encontrado estaciones cercanas con bicis disponibles ahora mismo."),
        withinRadiusFormatter = { station ->
          localizedText("La estación más cercana con bicis disponibles es %s con %s bicis y %s anclajes.", station.name, station.bikesAvailable, station.slotsFree)
        },
        fallbackFormatter = { station, radiusMeters ->
          localizedText(
            "No he encontrado estaciones con bicis disponibles dentro de %s m. La más cercana está a %s m: %s, con %s bicis y %s anclajes.",
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
        emptyMessage = localizedText("No he encontrado estaciones cercanas con huecos libres ahora mismo."),
        withinRadiusFormatter = { station ->
          localizedText("La estación más cercana con huecos libres es %s con %s huecos y %s bicis.", station.name, station.slotsFree, station.bikesAvailable)
        },
        fallbackFormatter = { station, radiusMeters ->
          localizedText(
            "No he encontrado estaciones con huecos libres dentro de %s m. La más cercana está a %s m: %s, con %s huecos y %s bicis.",
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
            localizedText("%s tiene %s bicis disponibles.", it.name, it.bikesAvailable)
          } ?: localizedText("No he encontrado esa estación."),
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.StationSlotCount -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            localizedText("%s tiene %s huecos libres.", it.name, it.slotsFree)
          } ?: localizedText("No he encontrado esa estación."),
          highlightedStationId = station?.id,
        )
      }
      is AssistantAction.RouteToStation -> AssistantResolution(
        spokenResponse = localizedText("Abriendo la ruta a la estación seleccionada."),
        highlightedStationId = action.stationId,
      )
      is AssistantAction.StationStatus -> {
        val station = stationsState.stations.firstOrNull { it.id == action.stationId }
        AssistantResolution(
          spokenResponse = station?.let {
            localizedText("%s tiene %s bicis disponibles y %s huecos libres.", it.name, it.bikesAvailable, it.slotsFree)
          } ?: localizedText("No he encontrado esa estación."),
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
