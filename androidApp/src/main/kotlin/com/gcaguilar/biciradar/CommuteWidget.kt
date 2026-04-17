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
import androidx.glance.layout.Alignment
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

class CommuteWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snapshot = AndroidSurfaceSnapshotReader.read(context)
    provideContent {
      GlanceTheme {
        CommuteContent(context, snapshot)
      }
    }
  }

  suspend fun updateAll(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(CommuteWidget::class.java)
    glanceIds.forEach { glanceId ->
      update(context, glanceId)
    }
  }
}

@Composable
private fun CommuteContent(
  context: Context,
  snapshot: AndroidSurfaceWidgetSnapshot,
) {
  val colors = GlanceTheme.colors

  val homeState = commutePlaceState(
    label = context.getString(R.string.widget_saved_place_home),
    station = snapshot.homeStation,
    snapshot = snapshot,
    configureSavedPlaces = context.getString(R.string.widget_configure_saved_places),
    openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
    missingTitle = context.getString(R.string.widget_saved_place_missing_title),
  )

  val workState = commutePlaceState(
    label = context.getString(R.string.widget_saved_place_work),
    station = snapshot.workStation,
    snapshot = snapshot,
    configureSavedPlaces = context.getString(R.string.widget_configure_saved_places),
    openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
    missingTitle = context.getString(R.string.widget_saved_place_missing_title),
  )

  Column(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(colors.surface)
      .padding(12.dp)
      .clickable(
        actionStartActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse("biciradar://favorites"))
            .setClassName(context, MainActivity::class.java.name)
        )
      ),
  ) {
    Text(
      text = context.getString(R.string.widget_commute_title),
      style = TextStyle(
        color = colors.onSurface,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      ),
      modifier = GlanceModifier.fillMaxWidth(),
    )

    Spacer(modifier = GlanceModifier.height(8.dp))

    CommutePlaceRow(
      label = homeState.label,
      name = homeState.title,
      meta = homeState.meta,
      colors = colors,
      context = context,
      targetUri = homeState.stationId?.let { "biciradar://station/$it" } ?: "biciradar://favorites",
    )

    Spacer(modifier = GlanceModifier.height(8.dp))

    CommutePlaceRow(
      label = workState.label,
      name = workState.title,
      meta = workState.meta,
      colors = colors,
      context = context,
      targetUri = workState.stationId?.let { "biciradar://station/$it" } ?: "biciradar://favorites",
    )
  }
}

@Composable
private fun CommutePlaceRow(
  label: String,
  name: String,
  meta: String,
  colors: androidx.glance.color.ColorProviders,
  context: Context,
  targetUri: String,
) {
  Column(
    modifier = GlanceModifier
      .fillMaxWidth()
      .background(colors.surface)
      .padding(10.dp)
      .clickable(
        actionStartActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse(targetUri))
            .setClassName(context, MainActivity::class.java.name)
        )
      ),
  ) {
    Text(
      text = label,
      style = TextStyle(
        color = colors.primary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
      ),
    )
    Text(
      text = name,
      style = TextStyle(
        color = colors.onSurface,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
      ),
      maxLines = 1,
    )
    Text(
      text = meta,
      style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
      maxLines = 2,
    )
  }
}

class CommuteWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = CommuteWidget()
}