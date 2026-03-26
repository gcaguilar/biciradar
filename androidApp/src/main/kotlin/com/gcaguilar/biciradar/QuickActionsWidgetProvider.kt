package com.gcaguilar.biciradar

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

class QuickActionsWidgetProvider : AppWidgetProvider() {
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
        ComponentName(context, QuickActionsWidgetProvider::class.java),
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
      val views = RemoteViews(context.packageName, R.layout.widget_quick_actions)
      val actionsState = quickActionsState(snapshot)
      val monitorLabel = if (!actionsState.requiresConfiguration) {
        context.getString(R.string.widget_action_monitor)
      } else {
        context.getString(R.string.widget_action_configure)
      }

      views.setTextViewText(R.id.widget_action_monitor_text, monitorLabel)
      views.setOnClickPendingIntent(
        R.id.widget_action_map,
        deepLinkPendingIntent(context, Uri.parse("biciradar://map")),
      )
      views.setOnClickPendingIntent(
        R.id.widget_action_favorites,
        deepLinkPendingIntent(context, Uri.parse("biciradar://favorites")),
      )
      views.setOnClickPendingIntent(
        R.id.widget_action_monitor,
        deepLinkPendingIntent(context, Uri.parse(actionsState.monitorUri)),
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
