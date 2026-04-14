package com.gcaguilar.biciradar.wear.complications

import android.app.PendingIntent
import android.content.Intent
import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.LongTextComplicationData
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.wear.WearActivity
import com.gcaguilar.biciradar.wear.WearAppGraph

class StationComplicationProvider : SuspendingComplicationDataSourceService() {
  override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    val graph = WearAppGraph.graph
    graph.syncFavoritesFromPeer.execute()
    val snapshot = graph.refreshStationDataIfNeeded.execute(forceRefresh = true)

    return when (request.complicationType) {
      ComplicationType.SHORT_TEXT -> createShortTextComplicationData(snapshot)
      ComplicationType.LONG_TEXT -> createLongTextComplicationData(snapshot)
      else -> null
    }
  }

  override fun getPreviewData(type: ComplicationType): ComplicationData? =
    when (type) {
      ComplicationType.SHORT_TEXT -> createShortTextComplicationData(null)
      ComplicationType.LONG_TEXT -> createLongTextComplicationData(null)
      else -> null
    }

  private fun createShortTextComplicationData(snapshot: SurfaceSnapshotBundle?): ComplicationData {
    val tapIntent = createTapIntent()
    val favorite = snapshot?.favoriteStation
    val text = favorite?.let { "${it.bikesAvailable} bicis" } ?: "Bici"
    return ShortTextComplicationData
      .Builder(
        text = PlainComplicationText.Builder(text).build(),
        contentDescription = PlainComplicationText.Builder("Estación de bici").build(),
      ).setTapAction(tapIntent)
      .build()
  }

  private fun createLongTextComplicationData(snapshot: SurfaceSnapshotBundle?): ComplicationData {
    val tapIntent = createTapIntent()
    val favorite = snapshot?.favoriteStation
    val body =
      favorite?.let { "${it.nameShort}\n${it.bikesAvailable} bicis · ${it.docksAvailable} anclajes" } ?: "Sin datos"
    return LongTextComplicationData
      .Builder(
        text = PlainComplicationText.Builder(body).build(),
        contentDescription = PlainComplicationText.Builder("Detalles de la estación").build(),
      ).setTapAction(tapIntent)
      .build()
  }

  private fun createTapIntent(): PendingIntent {
    val intent =
      Intent(this, WearActivity::class.java).apply {
        action = WearActivity.ACTION_OPEN_STATION
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
      }
    return PendingIntent.getActivity(
      this,
      0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }
}
