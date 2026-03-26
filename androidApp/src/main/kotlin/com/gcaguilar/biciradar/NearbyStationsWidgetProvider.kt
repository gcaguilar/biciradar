package com.gcaguilar.biciradar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class NearbyStationsWidgetProvider : AppWidgetProvider() {
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
        ComponentName(context, NearbyStationsWidgetProvider::class.java),
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
      val views = RemoteViews(context.packageName, R.layout.widget_nearby_stations)
      val stations = snapshot.nearbyStations.take(3)
      if (stations.isEmpty()) {
        views.setTextViewText(R.id.widget_nearby_title, context.getString(R.string.widget_nearby_title))
        views.setTextViewText(
          R.id.widget_nearby_empty,
          widgetEmptyMessage(
            state = widgetEmptyState(snapshot),
            configureFavorite = context.getString(R.string.widget_configure_favorite),
            openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
            dataUnavailable = context.getString(R.string.widget_data_unavailable),
          ),
        )
        views.setOnClickPendingIntent(
          R.id.widget_nearby_root,
          deepLinkPendingIntent(context, Uri.parse("biciradar://home")),
        )
        return views
      }

      bindStationRow(context, views, R.id.widget_row_one, R.id.widget_row_one_name, R.id.widget_row_one_meta, stations.getOrNull(0))
      bindStationRow(context, views, R.id.widget_row_two, R.id.widget_row_two_name, R.id.widget_row_two_meta, stations.getOrNull(1))
      bindStationRow(context, views, R.id.widget_row_three, R.id.widget_row_three_name, R.id.widget_row_three_meta, stations.getOrNull(2))
      views.setTextViewText(R.id.widget_nearby_empty, "")
      views.setOnClickPendingIntent(
        R.id.widget_nearby_root,
        deepLinkPendingIntent(context, Uri.parse("biciradar://home")),
      )
      return views
    }

    private fun bindStationRow(
      context: Context,
      views: RemoteViews,
      rowId: Int,
      nameId: Int,
      metaId: Int,
      station: AndroidSurfaceNearbyStation?,
    ) {
      if (station == null) {
        views.setViewVisibility(rowId, android.view.View.GONE)
        return
      }
      views.setViewVisibility(rowId, android.view.View.VISIBLE)
      views.setTextViewText(nameId, station.name)
      views.setTextViewText(metaId, nearbyStationMeta(station))
      views.setOnClickPendingIntent(
        rowId,
        deepLinkPendingIntent(context, Uri.parse("biciradar://station/${station.id}")),
      )
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
