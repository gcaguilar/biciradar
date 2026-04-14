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
import com.gcaguilar.biciradar.wear.WearActivity

class StationComplicationProvider : SuspendingComplicationDataSourceService() {
  override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    return when (request.complicationType) {
      ComplicationType.SHORT_TEXT -> createShortTextComplicationData()
      ComplicationType.LONG_TEXT -> createLongTextComplicationData()
      else -> null
    }
  }

  override fun getPreviewData(type: ComplicationType): ComplicationData? {
    return when (type) {
      ComplicationType.SHORT_TEXT -> createShortTextComplicationData()
      ComplicationType.LONG_TEXT -> createLongTextComplicationData()
      else -> null
    }
  }

  private fun createShortTextComplicationData(): ComplicationData {
    val tapIntent = createTapIntent()
    return ShortTextComplicationData.Builder(
      text = PlainComplicationText.Builder("Bici").build(),
      contentDescription = PlainComplicationText.Builder("Estación más cercana").build(),
    )
      .setTapAction(tapIntent)
      .build()
  }

  private fun createLongTextComplicationData(): ComplicationData {
    val tapIntent = createTapIntent()
    return LongTextComplicationData.Builder(
      text = PlainComplicationText.Builder("Sin datos").build(),
      contentDescription = PlainComplicationText.Builder("Sin datos disponibles").build(),
    )
      .setTapAction(tapIntent)
      .build()
  }

  private fun createTapIntent(): PendingIntent {
    val intent = Intent(this, WearActivity::class.java).apply {
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