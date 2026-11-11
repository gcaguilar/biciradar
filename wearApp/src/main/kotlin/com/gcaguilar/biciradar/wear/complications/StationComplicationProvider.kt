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
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.wear.WearActivity
import com.gcaguilar.biciradar.wear.ContextWearText
import com.gcaguilar.biciradar.wear.R
import com.gcaguilar.biciradar.wear.WearAppGraph
import com.gcaguilar.biciradar.wear.WearText
import com.gcaguilar.biciradar.wear.statusLabel

class StationComplicationProvider : SuspendingComplicationDataSourceService() {
  override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData? {
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(applicationContext as android.app.Application)
    }

    val graph = WearAppGraph.graph
    graph.syncFavoritesFromPeer.execute()
    val snapshot = graph.getCachedStationSnapshot.execute()
    // Sin favorita (p. ej. reloj sin móvil emparejado) se muestra la estación más cercana.
    val station =
      snapshot?.favoriteStation ?: graph.getNearestStations.execute(limit = 1).firstOrNull()
    val wearText = ContextWearText(this)

    return when (request.complicationType) {
      ComplicationType.SHORT_TEXT -> createShortTextComplicationData(wearText, station)
      ComplicationType.LONG_TEXT -> createLongTextComplicationData(wearText, station)
      else -> null
    }
  }

  override fun getPreviewData(type: ComplicationType): ComplicationData? =
    when (type) {
      ComplicationType.SHORT_TEXT -> createShortTextComplicationData(ContextWearText(this), PREVIEW_STATION)
      ComplicationType.LONG_TEXT -> createLongTextComplicationData(ContextWearText(this), PREVIEW_STATION)
      else -> null
    }

  private fun createShortTextComplicationData(
    wearText: WearText,
    favorite: SurfaceStationSnapshot?,
  ): ComplicationData {
    val tapIntent = createTapIntent(favorite)
    val text = favorite?.let { "🚲 ${it.bikesAvailable}" } ?: wearText.get(R.string.wear_complication_short_default)
    val title =
      favorite?.let { station ->
        buildString {
          station.distanceMeters?.let {
            append(formatDistance(it))
            append(" · ")
          }
          append(wearText.statusLabel(station.statusLevel))
        }
      } ?: wearText.get(R.string.wear_complication_open_app)
    return ShortTextComplicationData
      .Builder(
        text = PlainComplicationText.Builder(text).build(),
        contentDescription = PlainComplicationText.Builder(wearText.get(R.string.wear_complication_cd_short)).build(),
      ).setTitle(PlainComplicationText.Builder(title).build())
      .setTapAction(tapIntent)
      .build()
  }

  private fun createLongTextComplicationData(
    wearText: WearText,
    favorite: SurfaceStationSnapshot?,
  ): ComplicationData {
    val tapIntent = createTapIntent(favorite)
    val body =
      favorite?.let { station ->
        buildString {
          append(station.nameShort)
          append("\n")
          station.distanceMeters?.let {
            append(formatDistance(it))
            append(" · ")
          }
          append(wearText.statusLabel(station.statusLevel))
          append(" · 🚲 ")
          append(station.bikesAvailable)
          append(" · 🅿 ")
          append(station.docksAvailable)
        }
      } ?: wearText.get(R.string.wear_complication_no_data)
    return LongTextComplicationData
      .Builder(
        text = PlainComplicationText.Builder(body).build(),
        contentDescription = PlainComplicationText.Builder(wearText.get(R.string.wear_complication_cd_long)).build(),
      ).setTapAction(tapIntent)
      .build()
  }

  private fun createTapIntent(station: SurfaceStationSnapshot?): PendingIntent {
    val favoriteStationId = station?.id
    val intent =
      Intent(this, WearActivity::class.java).apply {
        action = if (favoriteStationId != null) WearActivity.ACTION_OPEN_STATION else Intent.ACTION_MAIN
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        favoriteStationId?.let { putExtra(WearActivity.EXTRA_OPEN_STATION_ID, it) }
      }
    return PendingIntent.getActivity(
      this,
      favoriteStationId?.hashCode() ?: 0,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private companion object {
    // Datos de ejemplo para el selector de complications del reloj.
    val PREVIEW_STATION =
      SurfaceStationSnapshot(
        id = "preview",
        nameShort = "BiciRadar",
        nameFull = "BiciRadar",
        cityId = "preview",
        latitude = 0.0,
        longitude = 0.0,
        bikesAvailable = 8,
        docksAvailable = 12,
        statusTextShort = "Disponible",
        statusLevel = SurfaceStatusLevel.Good,
        lastUpdatedEpoch = 0L,
        distanceMeters = 120,
      )
  }
}
