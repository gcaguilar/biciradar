package com.gcaguilar.biciradar.wear

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.appCard
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.contentDescription
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.gcaguilar.biciradar.core.formatDistance

class NearbyStationsTileService : Material3TileService() {
  private var pendingIntentCounter = 3000

  override suspend fun MaterialScope.tileResponse(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    val graph = WearAppGraph.graph
    graph.syncFavoritesFromPeer.execute()
    val snapshot = graph.getCachedStationSnapshot.execute()
    val nearbyStation = graph.getNearestStations.execute(limit = 1).firstOrNull()

    val mainContent: LayoutElementBuilders.LayoutElement =
      if (nearbyStation == null) {
        text("Sin datos cercanas.\nAbre la app".layoutString)
      } else {
        appCard(
          onClick =
            protoLayoutScope.clickable(
              pendingIntent = openStationPendingIntent(nearbyStation.id),
              id = "nearby_${nearbyStation.id}",
              fallbackAction = openAppAction(nearbyStation.id),
            ),
          modifier = LayoutModifier.contentDescription(tileContentDescription(nearbyStation)),
          height = expand(),
          label = { text(nearbyStation.statusTextShort.layoutString) },
          time = {
            text(
              nearbyStation.distanceMeters
                ?.let(::formatDistance)
                ?.layoutString ?: "".layoutString,
            )
          },
          title = { text(nearbyStation.nameShort.layoutString) },
          content = {
            text(
              buildString {
                append("Bicis: ")
                append(nearbyStation.bikesAvailable)
                append(" · Huecos: ")
                append(nearbyStation.docksAvailable)
              }.layoutString,
            )
          },
        )
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

  private fun openAppAction(stationId: String): ActionBuilders.Action {
    val activityBuilder =
      ActionBuilders.AndroidActivity
        .Builder()
        .setPackageName(packageName)
        .setClassName(WearActivity::class.java.name)
        .addKeyToExtraMapping(
          WearActivity.EXTRA_OPEN_STATION_ID,
          ActionBuilders.AndroidStringExtra.Builder().setValue(stationId).build(),
        )

    return ActionBuilders.LaunchAction
      .Builder()
      .setAndroidActivity(activityBuilder.build())
      .build()
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

  private fun tileContentDescription(station: com.gcaguilar.biciradar.core.SurfaceStationSnapshot): String =
    buildString {
      append(station.nameShort)
      append(", ")
      append("${station.bikesAvailable} bicis")
      append(", ")
      append("${station.docksAvailable} anclajes")
      station.distanceMeters?.let {
        append(", ")
        append(formatDistance(it))
      }
    }

  companion object {
    fun requestUpdate(context: Context) {
      runCatching {
        TileService.getUpdater(context.applicationContext).requestUpdate(NearbyStationsTileService::class.java)
      }
    }
  }
}
