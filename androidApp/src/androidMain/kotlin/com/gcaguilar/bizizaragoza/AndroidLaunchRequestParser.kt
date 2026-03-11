package com.gcaguilar.bizizaragoza

import android.content.Intent
import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest
import java.text.Normalizer
import java.util.Locale

internal const val FAVORITE_STATIONS_ACTION = "favorite_stations"
internal const val NEAREST_STATION_ACTION = "nearest_station"
internal const val OPEN_ASSISTANT_ACTION = "open_assistant"
internal const val STATION_STATUS_ACTION = "station_status"
internal const val ROUTE_TO_STATION_ACTION = "route_to_station"
internal const val SHOW_STATION_ACTION = "show_station"

internal const val ASSISTANT_ACTION_EXTRA = "assistant_action"
internal const val FEATURE_EXTRA = "feature"
internal const val STATION_ID_EXTRA = "station_id"

internal fun parseLaunchRequest(
  assistantAction: String? = null,
  feature: String? = null,
  stationId: String? = null,
): MobileLaunchRequest? {
  val action = sequenceOf(assistantAction, feature)
    .mapNotNull(::canonicalAction)
    .firstOrNull()
    ?: return null
  val normalizedStationId = stationId?.trim()?.takeIf { it.isNotEmpty() }
  return when (action) {
    FAVORITE_STATIONS_ACTION -> MobileLaunchRequest.Favorites
    NEAREST_STATION_ACTION -> MobileLaunchRequest.NearestStation
    OPEN_ASSISTANT_ACTION -> MobileLaunchRequest.OpenAssistant
    STATION_STATUS_ACTION -> MobileLaunchRequest.StationStatus
    ROUTE_TO_STATION_ACTION -> MobileLaunchRequest.RouteToStation(normalizedStationId)
    SHOW_STATION_ACTION -> normalizedStationId?.let(MobileLaunchRequest::ShowStation)
    else -> null
  }
}

internal fun Intent.toLaunchRequest(): MobileLaunchRequest? = parseLaunchRequest(
  assistantAction = getStringExtra(ASSISTANT_ACTION_EXTRA) ?: data?.getQueryParameter("action"),
  feature = getStringExtra(FEATURE_EXTRA) ?: data?.getQueryParameter("feature"),
  stationId = getStringExtra(STATION_ID_EXTRA)
    ?: data?.getQueryParameter("station_id")
    ?: data?.getQueryParameter("stationId"),
)

private fun canonicalAction(rawValue: String?): String? {
  val normalized = rawValue?.let(::normalizeActionToken)?.takeIf { it.isNotEmpty() } ?: return null
  return when (normalized) {
    FAVORITE_STATIONS_ACTION,
    "favorites",
    "mis favoritas" -> FAVORITE_STATIONS_ACTION
    NEAREST_STATION_ACTION,
    "estacion cercana" -> NEAREST_STATION_ACTION
    OPEN_ASSISTANT_ACTION -> OPEN_ASSISTANT_ACTION
    STATION_STATUS_ACTION,
    "estado estacion" -> STATION_STATUS_ACTION
    ROUTE_TO_STATION_ACTION,
    "ruta a estacion" -> ROUTE_TO_STATION_ACTION
    SHOW_STATION_ACTION -> SHOW_STATION_ACTION
    else -> null
  }
}

private fun normalizeActionToken(value: String): String {
  val withoutDiacritics = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
    .replace(DIACRITICS_REGEX, "")
  return withoutDiacritics
    .lowercase(Locale.ROOT)
    .replace(WHITESPACE_REGEX, " ")
}

private val DIACRITICS_REGEX = "\\p{Mn}+".toRegex()
private val WHITESPACE_REGEX = "\\s+".toRegex()
