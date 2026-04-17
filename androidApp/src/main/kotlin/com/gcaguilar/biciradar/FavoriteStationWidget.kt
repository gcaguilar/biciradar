package com.gcaguilar.biciradar

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class FavoriteStationWidget : GlanceAppWidget() {
  override suspend fun provideGlance(
    context: Context,
    id: GlanceId,
  ) {
    val snapshot = AndroidSurfaceSnapshotReader.read(context)
    provideContent {
      GlanceTheme {
        FavoriteStationContent(context, snapshot)
      }
    }
  }

  suspend fun updateAll(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(FavoriteStationWidget::class.java)
    glanceIds.forEach { glanceId ->
      update(context, glanceId)
    }
  }
}

@Composable
private fun FavoriteStationContent(
  context: Context,
  snapshot: AndroidSurfaceWidgetSnapshot,
) {
  val favorite = snapshot.favoriteStation
  val colors = GlanceTheme.colors

  Column(
    modifier =
      GlanceModifier
        .fillMaxSize()
        .background(colors.surface)
        .padding(14.dp)
        .clickable(
          actionStartActivity(
            Intent(
              Intent.ACTION_VIEW,
              Uri.parse(favorite?.let { "biciradar://station/${it.id}" } ?: "biciradar://favorites"),
            ).setClassName(context, MainActivity::class.java.name),
          ),
        ),
  ) {
    if (favorite == null) {
      Text(
        text = context.getString(R.string.widget_favorite_empty_title),
        style =
          TextStyle(
            color = colors.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
          ),
        maxLines = 2,
      )
      Spacer(modifier = GlanceModifier.height(4.dp))
      Text(
        text =
          widgetEmptyMessage(
            state = widgetEmptyState(snapshot),
            configureFavorite = context.getString(R.string.widget_configure_favorite),
            noLocationPermission = context.getString(R.string.widget_no_location_permission),
            openAppToRefresh = context.getString(R.string.widget_open_app_to_refresh),
            dataUnavailable = context.getString(R.string.widget_data_unavailable),
          ),
        style = TextStyle(color = colors.onSurfaceVariant, fontSize = 12.sp),
      )
      Spacer(modifier = GlanceModifier.height(12.dp))
      Row(modifier = GlanceModifier.fillMaxWidth()) {
        Column(modifier = GlanceModifier.defaultWeight()) {
          Text(
            text = "Bicis",
            style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
          )
          Text(
            text = "--",
            style = TextStyle(color = colors.primary, fontSize = 26.sp, fontWeight = FontWeight.Bold),
          )
        }
        Column(modifier = GlanceModifier.defaultWeight()) {
          Text(
            text = "Huecos",
            style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
          )
          Text(
            text = "--",
            style = TextStyle(color = colors.primary, fontSize = 26.sp, fontWeight = FontWeight.Bold),
          )
        }
      }
      Spacer(modifier = GlanceModifier.height(8.dp))
      Text(
        text = context.getString(R.string.widget_open_app_to_refresh),
        style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
      )
    } else {
      Text(
        text = favorite.name,
        style =
          TextStyle(
            color = colors.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
          ),
        maxLines = 2,
      )
      Text(
        text = favorite.statusText,
        style = TextStyle(color = colors.onSurfaceVariant, fontSize = 12.sp),
      )
      Spacer(modifier = GlanceModifier.height(12.dp))
      Row(modifier = GlanceModifier.fillMaxWidth()) {
        Column(modifier = GlanceModifier.defaultWeight()) {
          Text(
            text = "Bicis",
            style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
          )
          Text(
            text = favorite.bikesAvailable.toString(),
            style = TextStyle(color = colors.primary, fontSize = 26.sp, fontWeight = FontWeight.Bold),
          )
        }
        Column(modifier = GlanceModifier.defaultWeight()) {
          Text(
            text = "Huecos",
            style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
          )
          Text(
            text = favorite.docksAvailable.toString(),
            style = TextStyle(color = colors.primary, fontSize = 26.sp, fontWeight = FontWeight.Bold),
          )
        }
      }
      Spacer(modifier = GlanceModifier.height(8.dp))
      Text(
        text =
          favorite.lastUpdatedEpoch?.let {
            DateUtils
              .getRelativeTimeSpanString(
                it,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE,
              ).toString()
          } ?: context.getString(R.string.widget_data_unavailable),
        style = TextStyle(color = colors.onSurfaceVariant, fontSize = 11.sp),
      )
    }
  }
}

class FavoriteStationWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = FavoriteStationWidget()
}
