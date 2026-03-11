package com.gcaguilar.bizizaragoza

import android.content.Intent
import com.gcaguilar.bizizaragoza.mobileui.AssistantLaunchRequest
import com.gcaguilar.bizizaragoza.mobileui.MobileLaunchRequest
import java.text.Normalizer
import java.util.Locale

internal const val FAVORITE_STATIONS_ACTION = "favorite_stations"
internal const val NEAREST_STATION_ACTION = "nearest_station"
internal const val NEAREST_STATION_WITH_BIKES_ACTION = "nearest_station_with_bikes"
internal const val NEAREST_STATION_WITH_SLOTS_ACTION = "nearest_station_with_slots"
internal const val OPEN_ASSISTANT_ACTION = "open_assistant"
internal const val STATION_STATUS_ACTION = "station_status"
internal const val STATION_BIKE_COUNT_ACTION = "station_bike_count"
internal const val STATION_SLOT_COUNT_ACTION = "station_slot_count"
internal const val ROUTE_TO_STATION_ACTION = "route_to_station"
internal const val SHOW_STATION_ACTION = "show_station"

internal const val ASSISTANT_ACTION_EXTRA = "assistant_action"
internal const val FEATURE_EXTRA = "feature"
internal const val STATION_ID_EXTRA = "station_id"
internal const val STATION_QUERY_EXTRA = "station_query"

internal data class AndroidLaunchPayload(
  val launchRequest: MobileLaunchRequest? = null,
  val assistantLaunchRequest: AssistantLaunchRequest? = null,
)

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
    NEAREST_STATION_WITH_BIKES_ACTION -> MobileLaunchRequest.NearestStationWithBikes
    NEAREST_STATION_WITH_SLOTS_ACTION -> MobileLaunchRequest.NearestStationWithSlots
    OPEN_ASSISTANT_ACTION -> MobileLaunchRequest.OpenAssistant
    STATION_STATUS_ACTION -> MobileLaunchRequest.StationStatus
    ROUTE_TO_STATION_ACTION -> MobileLaunchRequest.RouteToStation(normalizedStationId)
    SHOW_STATION_ACTION -> normalizedStationId?.let(MobileLaunchRequest::ShowStation)
    else -> null
  }
}

internal fun parseLaunchPayload(
  assistantAction: String? = null,
  feature: String? = null,
  stationId: String? = null,
  stationQuery: String? = null,
): AndroidLaunchPayload? {
  val normalizedStationId = stationId?.trim()?.takeIf { it.isNotEmpty() }
  val normalizedStationQuery = stationQuery?.trim()?.takeIf { it.isNotEmpty() }
  val action = sequenceOf(assistantAction, feature)
    .mapNotNull(::canonicalAction)
    .firstOrNull()

  if (action == null) {
    return normalizedStationQuery?.let { query ->
      AndroidLaunchPayload(assistantLaunchRequest = AssistantLaunchRequest.SearchStation(query))
    }
  }

  val launchRequest = parseLaunchRequest(
    assistantAction = assistantAction,
    feature = feature,
    stationId = stationId,
  )
  val assistantLaunchRequest = when (action) {
    STATION_STATUS_ACTION -> buildAssistantLaunchRequest(
      stationId = normalizedStationId,
      stationQuery = normalizedStationQuery,
      stationIdFactory = AssistantLaunchRequest::StationStatus,
      stationQueryFactory = AssistantLaunchRequest::StationStatus,
    )
    STATION_BIKE_COUNT_ACTION -> buildAssistantLaunchRequest(
      stationId = normalizedStationId,
      stationQuery = normalizedStationQuery,
      stationIdFactory = AssistantLaunchRequest::StationBikeCount,
      stationQueryFactory = AssistantLaunchRequest::StationBikeCount,
    )
    STATION_SLOT_COUNT_ACTION -> buildAssistantLaunchRequest(
      stationId = normalizedStationId,
      stationQuery = normalizedStationQuery,
      stationIdFactory = AssistantLaunchRequest::StationSlotCount,
      stationQueryFactory = AssistantLaunchRequest::StationSlotCount,
    )
    ROUTE_TO_STATION_ACTION -> if (normalizedStationId == null && normalizedStationQuery != null) {
      AssistantLaunchRequest.RouteToStation(stationQuery = normalizedStationQuery)
    } else {
      null
    }
    SHOW_STATION_ACTION -> normalizedStationQuery?.let(AssistantLaunchRequest::SearchStation)
    else -> null
  }

  return AndroidLaunchPayload(
    launchRequest = launchRequest ?: when {
      action == STATION_STATUS_ACTION && assistantLaunchRequest == null -> MobileLaunchRequest.OpenAssistant
      action == STATION_BIKE_COUNT_ACTION && assistantLaunchRequest == null -> MobileLaunchRequest.OpenAssistant
      action == STATION_SLOT_COUNT_ACTION && assistantLaunchRequest == null -> MobileLaunchRequest.OpenAssistant
      else -> null
    },
    assistantLaunchRequest = assistantLaunchRequest,
  )
}

internal fun Intent.toLaunchPayload(): AndroidLaunchPayload? = parseLaunchPayload(
  assistantAction = getStringExtra(ASSISTANT_ACTION_EXTRA) ?: data?.getQueryParameter("action"),
  feature = getStringExtra(FEATURE_EXTRA) ?: data?.getQueryParameter("feature"),
  stationId = getStringExtra(STATION_ID_EXTRA)
    ?: data?.getQueryParameter("station_id")
    ?: data?.getQueryParameter("stationId"),
  stationQuery = getStringExtra(STATION_QUERY_EXTRA)
    ?: data?.getQueryParameter(STATION_QUERY_EXTRA)
    ?: data?.getQueryParameter("query"),
)

internal fun Intent.toLaunchRequest(): MobileLaunchRequest? = toLaunchPayload()?.launchRequest

private fun canonicalAction(rawValue: String?): String? {
  val normalized = rawValue?.let(::normalizeActionToken)?.takeIf { it.isNotEmpty() } ?: return null
  return when (normalized) {
    FAVORITE_STATIONS_ACTION,
    "favorites",
    "mis favoritas" -> FAVORITE_STATIONS_ACTION
    NEAREST_STATION_ACTION,
    "estacion cercana" -> NEAREST_STATION_ACTION
    NEAREST_STATION_WITH_BIKES_ACTION,
    "estacion cercana con bicis",
    "estacion con bicis cerca" -> NEAREST_STATION_WITH_BIKES_ACTION
    NEAREST_STATION_WITH_SLOTS_ACTION,
    "estacion cercana con huecos",
    "estacion con huecos cerca" -> NEAREST_STATION_WITH_SLOTS_ACTION
    OPEN_ASSISTANT_ACTION -> OPEN_ASSISTANT_ACTION
    STATION_STATUS_ACTION,
    "estado estacion" -> STATION_STATUS_ACTION
    STATION_BIKE_COUNT_ACTION,
    "bicis estacion",
    "bicis en estacion" -> STATION_BIKE_COUNT_ACTION
    STATION_SLOT_COUNT_ACTION,
    "huecos estacion",
    "huecos en estacion" -> STATION_SLOT_COUNT_ACTION
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

private fun <T> buildAssistantLaunchRequest(
  stationId: String?,
  stationQuery: String?,
  stationIdFactory: (String?, String?) -> T,
  stationQueryFactory: (String?, String?) -> T,
): T? = when {
  stationId != null -> stationIdFactory(stationId, null)
  stationQuery != null -> stationQueryFactory(null, stationQuery)
  else -> null
}
