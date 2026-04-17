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
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import android.content.Intent
import android.net.Uri

class QuickActionsWidget : GlanceAppWidget() {
  override suspend fun provideGlance(context: Context, id: GlanceId) {
    val snapshot = AndroidSurfaceSnapshotReader.read(context)
    provideContent {
      GlanceTheme {
        QuickActionsContent(context, snapshot)
      }
    }
  }

  suspend fun updateAll(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    val glanceIds = manager.getGlanceIds(QuickActionsWidget::class.java)
    glanceIds.forEach { glanceId ->
      update(context, glanceId)
    }
  }
}

@Composable
private fun QuickActionsContent(
  context: Context,
  snapshot: AndroidSurfaceWidgetSnapshot,
) {
  val actionsState = quickActionsState(snapshot)
  val colors = GlanceTheme.colors

  val monitorLabel = when {
    actionsState.requiresConfiguration -> context.getString(R.string.widget_action_configure)
    actionsState.requiresNotificationPermission -> context.getString(R.string.widget_action_notifications)
    else -> context.getString(R.string.widget_action_monitor)
  }

  Row(
    modifier = GlanceModifier
      .fillMaxSize()
      .background(colors.surface)
      .padding(12.dp),
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    ActionButton(
      label = "Mapa",
      context = context,
      uri = "biciradar://map",
    )
    Spacer(modifier = GlanceModifier.width(8.dp))
    ActionButton(
      label = "Favoritas",
      context = context,
      uri = "biciradar://favorites",
    )
    Spacer(modifier = GlanceModifier.width(8.dp))
    ActionButton(
      label = monitorLabel,
      context = context,
      uri = actionsState.monitorUri,
    )
  }
}

@Composable
private fun ActionButton(
  label: String,
  context: Context,
  uri: String,
) {
  val colors = GlanceTheme.colors
  Column(
    modifier = GlanceModifier
      .fillMaxWidth()
      .height(72.dp)
      .background(colors.surfaceVariant)
      .clickable(
        actionStartActivity(
          Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            .setClassName(context, MainActivity::class.java.name)
        )
      ),
    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = TextStyle(
        color = colors.primary,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
      ),
    )
  }
}

class QuickActionsWidgetReceiver : GlanceAppWidgetReceiver() {
  override val glanceAppWidget: GlanceAppWidget = QuickActionsWidget()
}