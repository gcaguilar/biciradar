package com.gcaguilar.biciradar.wear

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService

class NearbyStationsTileService : Material3TileService() {
  private var pendingIntentCounter = 3000

  override suspend fun MaterialScope.tileResponse(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    val graph = WearAppGraph.graph
    graph.syncFavoritesFromPeer.execute()
    val snapshot = graph.getCachedStationSnapshot.execute()
    val nearbyStations = snapshot?.nearbyStations?.take(4) ?: emptyList()

    val mainContent: LayoutElementBuilders.LayoutElement =
      if (nearbyStations.isEmpty()) {
        text("Sin datos cercanas.\nAbre la app".layoutString)
      } else {
        val column =
          LayoutElementBuilders.Column.Builder()
            .setWidth(expand())
            .setHeight(expand())
        nearbyStations.forEach { station ->
          val stationClickable =
            protoLayoutScope.clickable(
              pendingIntent = openStationPendingIntent(station.id),
              id = "nearby_${station.id}",
            )
          val row =
            LayoutElementBuilders.Box.Builder()
              .setWidth(expand())
              .setModifiers(
                ModifiersBuilders.Modifiers
                  .Builder()
                  .setClickable(stationClickable)
                  .build(),
              ).addContent(
                text("${station.nameShort}: \uD83D\uDEB2${station.bikesAvailable} \uD83C\uDD7F\uFE0F${station.docksAvailable}".layoutString),
              ).build()
          column.addContent(row)
        }
        column.build()
      }

    val root = primaryLayout(mainSlot = { mainContent })

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

  private fun openStationPendingIntent(stationId: String): PendingIntent {
    val intent =
      Intent(this, WearActivity::class.java).apply {
        action = WearActivity.ACTION_OPEN_STATION
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        putExtra(WearActivity.EXTRA_OPEN_STATION_ID, stationId)
      }
    val requestCode = ++pendingIntentCounter
    return PendingIntent.getActivity(
      this,
      requestCode,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  companion object {
    fun requestUpdate(context: Context) {
      runCatching {
        TileService.getUpdater(context.applicationContext).requestUpdate(NearbyStationsTileService::class.java)
      }
    }
  }
}
