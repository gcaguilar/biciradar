package com.gcaguilar.biciradar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class CommuteWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    WidgetRefreshWorker.reconcile(context)
    updateWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    WidgetRefreshWorker.reconcile(context)
  }

  override fun onDisabled(context: Context) {
    super.onDisabled(context)
    WidgetRefreshWorker.reconcile(context)
  }

  companion object {
    fun updateAll(context: Context) {
      val manager = AppWidgetManager.getInstance(context)
      val ids =
        manager.getAppWidgetIds(
          ComponentName(context, CommuteWidgetProvider::class.java),
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
      val views = RemoteViews(context.packageName, R.layout.widget_commute)
      val homeState =
        commutePlaceState(
          label = context.getString(R.string.widget_saved_place_home),
          station = snapshot.homeStation,
          snapshot = snapshot,
          configureSavedPlaces = context.getString(R.string.widget_configure_saved_places),
          openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
          missingTitle = context.getString(R.string.widget_saved_place_missing_title),
        )
      val workState =
        commutePlaceState(
          label = context.getString(R.string.widget_saved_place_work),
          station = snapshot.workStation,
          snapshot = snapshot,
          configureSavedPlaces = context.getString(R.string.widget_configure_saved_places),
          openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
          missingTitle = context.getString(R.string.widget_saved_place_missing_title),
        )

      views.setTextViewText(R.id.widget_commute_title, context.getString(R.string.widget_commute_title))
      bindPlaceRow(
        context = context,
        views = views,
        rowId = R.id.widget_commute_home_row,
        labelId = R.id.widget_commute_home_label,
        nameId = R.id.widget_commute_home_name,
        metaId = R.id.widget_commute_home_meta,
        state = homeState,
      )
      bindPlaceRow(
        context = context,
        views = views,
        rowId = R.id.widget_commute_work_row,
        labelId = R.id.widget_commute_work_label,
        nameId = R.id.widget_commute_work_name,
        metaId = R.id.widget_commute_work_meta,
        state = workState,
      )
      views.setOnClickPendingIntent(
        R.id.widget_commute_root,
        deepLinkPendingIntent(context, Uri.parse("biciradar://favorites")),
      )
      return views
    }

    private fun bindPlaceRow(
      context: Context,
      views: RemoteViews,
      rowId: Int,
      labelId: Int,
      nameId: Int,
      metaId: Int,
      state: AndroidCommutePlaceState,
    ) {
      views.setTextViewText(labelId, state.label)
      views.setTextViewText(nameId, state.title)
      views.setTextViewText(metaId, state.meta)
      val target =
        state.stationId?.let { Uri.parse("biciradar://station/$it") }
          ?: Uri.parse("biciradar://favorites")
      views.setOnClickPendingIntent(
        rowId,
        deepLinkPendingIntent(context, target),
      )
    }

    private fun deepLinkPendingIntent(
      context: Context,
      uri: Uri,
    ): PendingIntent {
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
