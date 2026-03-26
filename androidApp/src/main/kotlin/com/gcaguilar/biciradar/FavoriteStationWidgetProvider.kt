package com.gcaguilar.biciradar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.RemoteViews
import java.io.File
import org.json.JSONObject

class FavoriteStationWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    updateWidgets(context, appWidgetManager, appWidgetIds)
  }

  companion object {
    fun updateAll(context: Context) {
      val manager = AppWidgetManager.getInstance(context)
      val ids = manager.getAppWidgetIds(
        ComponentName(context, FavoriteStationWidgetProvider::class.java),
      )
      if (ids.isNotEmpty()) {
        updateWidgets(context, manager, ids)
      }
    }

    private fun updateWidgets(
      context: Context,
      appWidgetManager: AppWidgetManager,
      appWidgetIds: IntArray,
    ) {
      val snapshot = AndroidSurfaceSnapshotReader.read(context)
      appWidgetIds.forEach { appWidgetId ->
        appWidgetManager.updateAppWidget(appWidgetId, buildRemoteViews(context, snapshot))
      }
    }

    private fun buildRemoteViews(
      context: Context,
      snapshot: AndroidSurfaceWidgetSnapshot,
    ): RemoteViews {
      val views = RemoteViews(context.packageName, R.layout.widget_favorite_station)
      val favorite = snapshot.favoriteStation
      if (favorite == null) {
        views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_favorite_empty_title))
        views.setTextViewText(
          R.id.widget_status,
          snapshot.emptyMessage ?: context.getString(R.string.widget_data_unavailable),
        )
        views.setTextViewText(R.id.widget_bikes_value, "--")
        views.setTextViewText(R.id.widget_docks_value, "--")
        views.setTextViewText(R.id.widget_updated, context.getString(R.string.widget_open_app_to_refresh))
        views.setOnClickPendingIntent(
          R.id.widget_root,
          deepLinkPendingIntent(context, Uri.parse("biciradar://favorites")),
        )
        return views
      }

      views.setTextViewText(R.id.widget_title, favorite.name)
      views.setTextViewText(R.id.widget_status, favorite.statusText)
      views.setTextViewText(R.id.widget_bikes_value, favorite.bikesAvailable.toString())
      views.setTextViewText(R.id.widget_docks_value, favorite.docksAvailable.toString())
      views.setTextViewText(
        R.id.widget_updated,
        favorite.lastUpdatedEpoch?.let {
          DateUtils.getRelativeTimeSpanString(
            it,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE,
          )
        } ?: context.getString(R.string.widget_data_unavailable),
      )
      views.setOnClickPendingIntent(
        R.id.widget_root,
        deepLinkPendingIntent(context, Uri.parse("biciradar://station/${favorite.id}")),
      )
      return views
    }

    private fun deepLinkPendingIntent(context: Context, uri: Uri): PendingIntent {
      val intent = Intent(Intent.ACTION_VIEW, uri, context, MainActivity::class.java)
      return PendingIntent.getActivity(
        context,
        uri.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )
    }
  }
}

internal data class AndroidSurfaceWidgetSnapshot(
  val favoriteStation: AndroidSurfaceFavoriteStation? = null,
  val nearbyStations: List<AndroidSurfaceNearbyStation> = emptyList(),
  val emptyMessage: String? = null,
)

internal data class AndroidSurfaceFavoriteStation(
  val id: String,
  val name: String,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val statusText: String,
  val lastUpdatedEpoch: Long?,
)

internal data class AndroidSurfaceNearbyStation(
  val id: String,
  val name: String,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val distanceMeters: Int?,
  val statusText: String,
)

internal object AndroidSurfaceSnapshotReader {
  private const val SNAPSHOT_PATH = "bizi/surface_snapshot.json"

  fun read(context: Context): AndroidSurfaceWidgetSnapshot {
    val file = File(context.filesDir, SNAPSHOT_PATH)
    if (!file.exists()) {
      return AndroidSurfaceWidgetSnapshot(
        emptyMessage = context.getString(R.string.widget_open_app_to_refresh),
      )
    }
    return runCatching {
      parse(context, JSONObject(file.readText()))
    }.getOrElse {
      AndroidSurfaceWidgetSnapshot(
        emptyMessage = context.getString(R.string.widget_data_unavailable),
      )
    }
  }

  private fun parse(context: Context, root: JSONObject): AndroidSurfaceWidgetSnapshot {
    val state = root.optJSONObject("state")
    val favorite = root.optJSONObject("favoriteStation")
    val nearby = root.optJSONArray("nearbyStations")
    val nearbyStations = buildList {
      for (index in 0 until (nearby?.length() ?: 0)) {
        val station = nearby?.optJSONObject(index) ?: continue
        add(
          AndroidSurfaceNearbyStation(
            id = station.optString("id"),
            name = station.optString("nameShort", station.optString("nameFull")),
            bikesAvailable = station.optInt("bikesAvailable"),
            docksAvailable = station.optInt("docksAvailable"),
            distanceMeters = station.optInt("distanceMeters").takeIf { it > 0 },
            statusText = station.optString("statusTextShort"),
          ),
        )
      }
    }
    if (favorite == null) {
      return AndroidSurfaceWidgetSnapshot(
        nearbyStations = nearbyStations,
        emptyMessage = when {
          state?.optBoolean("hasFavoriteStation") == false -> context.getString(R.string.widget_configure_favorite)
          state?.optBoolean("isDataFresh") == false -> context.getString(R.string.widget_open_app_to_refresh)
          else -> context.getString(R.string.widget_data_unavailable)
        },
      )
    }
    return AndroidSurfaceWidgetSnapshot(
      favoriteStation = AndroidSurfaceFavoriteStation(
        id = favorite.optString("id"),
        name = favorite.optString("nameShort", favorite.optString("nameFull")),
        bikesAvailable = favorite.optInt("bikesAvailable"),
        docksAvailable = favorite.optInt("docksAvailable"),
        statusText = favorite.optString("statusTextShort"),
        lastUpdatedEpoch = favorite.optLong("lastUpdatedEpoch").takeIf { it > 0L },
      ),
      nearbyStations = nearbyStations,
    )
  }
}
