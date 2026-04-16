package com.gcaguilar.biciradar

import android.app.Application
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.RemoteViews
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import kotlinx.coroutines.runBlocking

class FavoriteStationWidgetProvider : AppWidgetProvider() {
  override fun onUpdate(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetIds: IntArray,
  ) {
    updateWidgets(context, appWidgetManager, appWidgetIds)
  }

  override fun onEnabled(context: Context) {
    super.onEnabled(context)
    WidgetRefreshWorker.reconcile(context)
    WidgetRefreshWorker.scheduleImmediate(context)
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
          widgetEmptyMessage(
            state = widgetEmptyState(snapshot),
            configureFavorite = context.getString(R.string.widget_configure_favorite),
            noLocationPermission = context.getString(R.string.widget_no_location_permission),
            openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
            dataUnavailable = context.getString(R.string.widget_data_unavailable),
          ),
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

internal data class AndroidSurfaceWidgetSnapshot(
  val favoriteStation: AndroidSurfaceFavoriteStation? = null,
  val homeStation: AndroidSurfaceSavedPlaceStation? = null,
  val workStation: AndroidSurfaceSavedPlaceStation? = null,
  val nearbyStations: List<AndroidSurfaceNearbyStation> = emptyList(),
  val hasFavoriteStation: Boolean? = null,
  val isDataFresh: Boolean? = null,
  val hasLocationPermission: Boolean? = null,
  val hasNotificationPermission: Boolean? = null,
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

internal data class AndroidSurfaceSavedPlaceStation(
  val id: String,
  val name: String,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val statusText: String,
)

internal object AndroidSurfaceSnapshotReader {
  fun read(context: Context): AndroidSurfaceWidgetSnapshot {
    return runCatching {
      if (!BiziAppGraph.isInitialized()) {
        BiziAppGraph.initialize(context.applicationContext as Application)
      }
      val bundle =
        runBlocking {
          BiziAppGraph.graph.getCachedStationSnapshot.execute()
        }
      bundle?.toAndroidSurfaceWidgetSnapshot() ?: AndroidSurfaceWidgetSnapshot(isDataFresh = false)
    }.getOrElse {
      AndroidSurfaceWidgetSnapshot()
    }
  }
}

internal fun SurfaceSnapshotBundle.toAndroidSurfaceWidgetSnapshot(): AndroidSurfaceWidgetSnapshot =
  AndroidSurfaceWidgetSnapshot(
    favoriteStation = favoriteStation?.toAndroidFavoriteStation(),
    homeStation = homeStation?.toAndroidSavedPlaceStation(),
    workStation = workStation?.toAndroidSavedPlaceStation(),
    nearbyStations = nearbyStations.map(SurfaceStationSnapshot::toAndroidNearbyStation),
    hasFavoriteStation = state.hasFavoriteStation,
    isDataFresh = state.isDataFresh,
    hasLocationPermission = state.hasLocationPermission,
    hasNotificationPermission = state.hasNotificationPermission,
  )

private fun SurfaceStationSnapshot.toAndroidFavoriteStation(): AndroidSurfaceFavoriteStation =
  AndroidSurfaceFavoriteStation(
    id = id,
    name = nameShort.ifBlank { nameFull },
    bikesAvailable = bikesAvailable,
    docksAvailable = docksAvailable,
    statusText = statusTextShort,
    lastUpdatedEpoch = lastUpdatedEpoch.takeIf { it > 0L },
  )

private fun SurfaceStationSnapshot.toAndroidNearbyStation(): AndroidSurfaceNearbyStation =
  AndroidSurfaceNearbyStation(
    id = id,
    name = nameShort.ifBlank { nameFull },
    bikesAvailable = bikesAvailable,
    docksAvailable = docksAvailable,
    distanceMeters = distanceMeters?.takeIf { it > 0 },
    statusText = statusTextShort,
  )

private fun SurfaceStationSnapshot.toAndroidSavedPlaceStation(): AndroidSurfaceSavedPlaceStation =
  AndroidSurfaceSavedPlaceStation(
    id = id,
    name = nameShort.ifBlank { nameFull },
    bikesAvailable = bikesAvailable,
    docksAvailable = docksAvailable,
    statusText = statusTextShort,
  )
