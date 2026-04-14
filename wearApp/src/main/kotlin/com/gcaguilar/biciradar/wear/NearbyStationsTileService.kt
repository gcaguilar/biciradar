package com.gcaguilar.biciradar.wear

import android.content.Context
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService

class NearbyStationsTileService : Material3TileService() {
  override suspend fun MaterialScope.tileResponse(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    val graph = WearAppGraph.graph
    graph.syncFavoritesFromPeer.execute()
    val snapshot = graph.getCachedStationSnapshot.execute()
    val nearbyStations = snapshot?.nearbyStations?.take(4) ?: emptyList()

    val content =
      buildString {
        if (nearbyStations.isEmpty()) {
          append("Sin datos. Abre la app")
        } else {
          append("Cercanas:\n")
          nearbyStations.forEach { station ->
            append("${station.nameShort}: 🚲${station.bikesAvailable} 🅿${station.docksAvailable}\n")
          }
        }
      }.trimEnd()

    val root = text(content.layoutString)

    return TileBuilders.Tile
      .Builder()
      .setResourcesVersion(snapshot?.generatedAtEpoch?.toString() ?: "empty")
      .setFreshnessIntervalMillis(60_000L)
      .setTileTimeline(
        TimelineBuilders.Timeline
          .Builder()
          .addTimelineEntry(
            TimelineBuilders.TimelineEntry
              .Builder()
              .setLayout(
                LayoutElementBuilders.Layout
                  .Builder()
                  .setRoot(root)
                  .build(),
              ).build(),
          ).build(),
      ).build()
  }

  companion object {
    fun requestUpdate(context: Context) {
      runCatching {
        TileService.getUpdater(context.applicationContext).requestUpdate(NearbyStationsTileService::class.java)
      }
    }
  }
}