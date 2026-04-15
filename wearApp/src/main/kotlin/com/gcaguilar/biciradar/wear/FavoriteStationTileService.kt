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

class FavoriteStationTileService : Material3TileService() {
  override suspend fun MaterialScope.tileResponse(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    val graph = WearAppGraph.graph
    graph.syncFavoritesFromPeer.execute()
    val snapshot = graph.getCachedStationSnapshot.execute()
    val tileState = wearFavoriteTileState(snapshot)

    val labelSlot: (MaterialScope.() -> LayoutElementBuilders.LayoutElement)? =
      tileState.label?.takeIf { it.isNotBlank() }?.let { label ->
        { text(label.layoutString) }
      }
    val timeSlot: (MaterialScope.() -> LayoutElementBuilders.LayoutElement)? =
      tileState.updatedText?.takeIf { it.isNotBlank() }?.let { updated ->
        { text(updated.layoutString) }
      }
    val root =
      primaryLayout(
        mainSlot = {
          appCard(
            onClick =
              protoLayoutScope.clickable(
                pendingIntent = openStationPendingIntent(tileState.stationId),
                id = CLICKABLE_OPEN_FAVORITE,
                fallbackAction = openAppAction(tileState.stationId),
              ),
            modifier = LayoutModifier.contentDescription(tileContentDescription(tileState)),
            height = expand(),
            title = { text(tileState.title.layoutString) },
            label = labelSlot,
            time = timeSlot,
            content = { text(tileState.body.layoutString) },
          )
        },
      )

    return TileBuilders.Tile
      .Builder()
      .setResourcesVersion(snapshot?.generatedAtEpoch?.toString() ?: EMPTY_TILE_RESOURCES_VERSION)
      .setFreshnessIntervalMillis(TILE_FRESHNESS_INTERVAL_MILLIS)
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

  private fun openAppAction(stationId: String?): ActionBuilders.Action {
    val activityBuilder =
      ActionBuilders.AndroidActivity
        .Builder()
        .setPackageName(packageName)
        .setClassName(WearActivity::class.java.name)

    stationId?.let {
      activityBuilder.addKeyToExtraMapping(
        WearActivity.EXTRA_OPEN_STATION_ID,
        ActionBuilders.AndroidStringExtra.Builder().setValue(it).build(),
      )
    }

    return ActionBuilders.LaunchAction
      .Builder()
      .setAndroidActivity(activityBuilder.build())
      .build()
  }

  private fun openStationPendingIntent(stationId: String?): PendingIntent {
    val intent =
      Intent(this, WearActivity::class.java).apply {
        action = WearActivity.ACTION_OPEN_STATION
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        stationId?.let { putExtra(WearActivity.EXTRA_OPEN_STATION_ID, it) }
      }
    return PendingIntent.getActivity(
      this,
      PENDING_INTENT_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun tileContentDescription(state: WearFavoriteTileState): String =
    buildString {
      append(state.title)
      state.label?.takeIf { it.isNotBlank() }?.let {
        append(", ")
        append(it)
      }
      if (state.body.isNotBlank()) {
        append(", ")
        append(state.body)
      }
      state.updatedText?.takeIf { it.isNotBlank() }?.let {
        append(", ")
        append(it)
      }
    }

  companion object {
    private const val CLICKABLE_OPEN_FAVORITE = "open_favorite"
    private const val PENDING_INTENT_REQUEST_CODE = 2000
    private const val EMPTY_TILE_RESOURCES_VERSION = "empty"
    private const val TILE_FRESHNESS_INTERVAL_MILLIS = 60_000L

    fun requestUpdate(context: Context) {
      runCatching {
        TileService.getUpdater(context.applicationContext).requestUpdate(FavoriteStationTileService::class.java)
      }
    }
  }
}
