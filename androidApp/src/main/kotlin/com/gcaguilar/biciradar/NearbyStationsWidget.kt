package com.gcaguilar.biciradar

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import android.content.Intent
import android.net.Uri

class NearbyStationsWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snapshot = AndroidSurfaceSnapshotReader.read(context)
    provideContent {
      GlanceTheme {
        NearbyStationsContent(context, snapshot)
      }
    }
  }

  suspend fun updateAll(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(NearbyStationsWidget::class.java)
    glanceIds.forEach { glanceId ->
      update(context, glanceId)
    }
  }
}

@Composable
private fun NearbyStationsContent(
  context: Context,
  snapshot: AndroidSurfaceWidgetSnapshot,
) {
  val stations = snapshot.nearbyStations.take(3)
  val colors = GlanceTheme.colors

  Column(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(colors.surface)
      .padding(14.dp)
      .clickable(
        actionStartActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse("biciradar://home"))
            .setClassName(context, MainActivity::class.java.name)
        )
      ),
  ) {
    Text(
      text = context.getString(R.string.widget_nearby_title),
      style = TextStyle(
        color = colors.onSurface,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      ),
      modifier = GlanceModifier.fillMaxWidth(),
    )

    if (stations.isEmpty()) {
      Spacer(modifier = GlanceModifier.height(12.dp))
      val emptyMessage = widgetEmptyMessage(
        state = nearbyWidgetEmptyState(snapshot),
        configureFavorite = context.getString(R.string.widget_configure_favorite),
        noLocationPermission = context.getString(R.string.widget_no_location_permission),
        openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
        dataUnavailable = context.getString(R.string.widget_data_unavailable),
      )
      Text(
        text = emptyMessage,
        style = TextStyle(color = colors.onSurfaceVariant, fontSize = 12.sp),
      )
    } else {
      Spacer(modifier = GlanceModifier.height(10.dp))
      stations.forEachIndexed { index, station ->
        if (index > 0) Spacer(modifier = GlanceModifier.height(8.dp))
        StationRow(
          name = station.name,
          meta = nearbyStationMeta(station),
          colors = colors,
          context = context,
          stationId = station.id,
        )
      }
    }
  }
}

@Composable
private fun StationRow(
  name: String,
  meta: String,
  colors: androidx.glance.color.ColorProviders,
  context: Context,
  stationId: String,
) {
  Column(
    modifier = GlanceModifier
      .fillMaxWidth()
      .clickable(
        actionStartActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse("biciradar://station/$stationId"))
            .setClassName(context, MainActivity::class.java.name)
        )
      ),
  ) {
    Text(
      text = name,
      style = TextStyle(
        color = colors.onSurface,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
      ),
      maxLines = 1,
    )
    Text(
      text = meta,
      style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
    )
  }
}

class NearbyStationsWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = NearbyStationsWidget()
}