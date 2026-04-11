package com.gcaguilar.biciradar

import android.app.SearchManager
import android.content.Intent
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
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
internal const val HOME_ACTION = "home"
internal const val MAP_ACTION = "map"
internal const val MONITOR_STATION_ACTION = "monitor_station"
internal const val SELECT_CITY_ACTION = "select_city"
internal const val SAVED_PLACE_ALERTS_ACTION = "saved_place_alerts"

internal const val ASSISTANT_ACTION_EXTRA = "assistant_action"
internal const val FEATURE_EXTRA = "feature"
internal const val STATION_ID_EXTRA = "station_id"
internal const val STATION_QUERY_EXTRA = "station_query"

internal data class AndroidLaunchPayload(
  val launchRequest: MobileLaunchRequest? = null,
  val assistantLaunchRequest: AssistantLaunchRequest? = null,
)

internal data class AndroidLaunchSource(
  val assistantAction: String? = null,
  val deepLinkAction: String? = null,
  val deepLinkHost: String? = null,
  val deepLinkPathSegment: String? = null,
  val feature: String? = null,
  val deepLinkFeature: String? = null,
  val stationId: String? = null,
  val deepLinkStationId: String? = null,
  val deepLinkStationIdAlias: String? = null,
  val stationQuery: String? = null,
  val deepLinkStationQuery: String? = null,
  val deepLinkQuery: String? = null,
  val searchManagerQuery: String? = null,
  val textQuery: String? = null,
)

private data class ParsedAssistantPhrase(
  val action: String,
  val stationQuery: String? = null,
)

internal fun parseLaunchRequest(
  assistantAction: String? = null,
  feature: String? = null,
  stationId: String? = null,
): MobileLaunchRequest? {
  val action =
    sequenceOf(assistantAction, feature)
      .mapNotNull { parseAssistantPhrase(it)?.action ?: canonicalAction(it) }
      .firstOrNull()
      ?: return null
  val normalizedStationId = stationId?.trim()?.takeIf { it.isNotEmpty() }
  return when (action) {
    HOME_ACTION -> MobileLaunchRequest.Home
    MAP_ACTION -> MobileLaunchRequest.Map
    FAVORITE_STATIONS_ACTION -> MobileLaunchRequest.Favorites
    NEAREST_STATION_ACTION -> MobileLaunchRequest.NearestStation
    NEAREST_STATION_WITH_BIKES_ACTION -> MobileLaunchRequest.NearestStationWithBikes
    NEAREST_STATION_WITH_SLOTS_ACTION -> MobileLaunchRequest.NearestStationWithSlots
    OPEN_ASSISTANT_ACTION -> MobileLaunchRequest.OpenAssistant
    STATION_STATUS_ACTION -> MobileLaunchRequest.StationStatus
    MONITOR_STATION_ACTION -> normalizedStationId?.let(MobileLaunchRequest::MonitorStation)
    SELECT_CITY_ACTION -> normalizedStationId?.let(MobileLaunchRequest::SelectCity)
    ROUTE_TO_STATION_ACTION -> MobileLaunchRequest.RouteToStation(normalizedStationId)
    SHOW_STATION_ACTION -> normalizedStationId?.let(MobileLaunchRequest::ShowStation)
    SAVED_PLACE_ALERTS_ACTION -> MobileLaunchRequest.SavedPlaceAlerts
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
  val parsedPhrase =
    sequenceOf(assistantAction, feature)
      .mapNotNull(::parseAssistantPhrase)
      .firstOrNull()
  val normalizedStationQuery =
    stationQuery
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
      ?.let(::cleanStationQuery)
      ?: parsedPhrase?.stationQuery
  val action =
    parsedPhrase?.action ?: sequenceOf(assistantAction, feature)
      .mapNotNull(::canonicalAction)
      .firstOrNull()

  if (action == null) {
    val fallbackQuery =
      normalizedStationQuery ?: sequenceOf(assistantAction, feature)
        .mapNotNull { rawValue ->
          rawValue
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let(::normalizeActionToken)
            ?.let(::stripAssistantAppName)
            ?.let(::cleanStationQuery)
            ?.takeUnless(::isGenericStationPlaceholder)
            ?.takeIf { it.isNotEmpty() }
        }.firstOrNull()

    return fallbackQuery?.let { query ->
      AndroidLaunchPayload(assistantLaunchRequest = AssistantLaunchRequest.SearchStation(query))
    }
  }

  val launchRequest =
    parseLaunchRequest(
      assistantAction = assistantAction,
      feature = feature,
      stationId = stationId,
    )
  val assistantLaunchRequest =
    when (action) {
      STATION_STATUS_ACTION ->
        buildAssistantLaunchRequest(
          stationId = normalizedStationId,
          stationQuery = normalizedStationQuery,
          stationIdFactory = AssistantLaunchRequest::StationStatus,
          stationQueryFactory = AssistantLaunchRequest::StationStatus,
        )
      STATION_BIKE_COUNT_ACTION ->
        buildAssistantLaunchRequest(
          stationId = normalizedStationId,
          stationQuery = normalizedStationQuery,
          stationIdFactory = AssistantLaunchRequest::StationBikeCount,
          stationQueryFactory = AssistantLaunchRequest::StationBikeCount,
        )
      STATION_SLOT_COUNT_ACTION ->
        buildAssistantLaunchRequest(
          stationId = normalizedStationId,
          stationQuery = normalizedStationQuery,
          stationIdFactory = AssistantLaunchRequest::StationSlotCount,
          stationQueryFactory = AssistantLaunchRequest::StationSlotCount,
        )
      ROUTE_TO_STATION_ACTION ->
        if (normalizedStationId == null && normalizedStationQuery != null) {
          AssistantLaunchRequest.RouteToStation(stationQuery = normalizedStationQuery)
        } else {
          null
        }
      SHOW_STATION_ACTION -> normalizedStationQuery?.let(AssistantLaunchRequest::SearchStation)
      else -> null
    }

  return AndroidLaunchPayload(
    launchRequest =
      launchRequest ?: when {
        action == STATION_STATUS_ACTION && assistantLaunchRequest == null -> MobileLaunchRequest.OpenAssistant
        action == STATION_BIKE_COUNT_ACTION && assistantLaunchRequest == null -> MobileLaunchRequest.OpenAssistant
        action == STATION_SLOT_COUNT_ACTION && assistantLaunchRequest == null -> MobileLaunchRequest.OpenAssistant
        else -> null
      },
    assistantLaunchRequest = assistantLaunchRequest,
  )
}

internal fun Intent.toLaunchPayload(): AndroidLaunchPayload? =
  parseLaunchPayload(
    source =
      AndroidLaunchSource(
        assistantAction = getStringExtra(ASSISTANT_ACTION_EXTRA),
        deepLinkAction = data?.getQueryParameter("action"),
        deepLinkHost = data?.host,
        deepLinkPathSegment = data?.pathSegments?.firstOrNull(),
        feature = getStringExtra(FEATURE_EXTRA),
        deepLinkFeature = data?.getQueryParameter("feature"),
        stationId = getStringExtra(STATION_ID_EXTRA),
        deepLinkStationId = data?.getQueryParameter("station_id"),
        deepLinkStationIdAlias = data?.getQueryParameter("stationId"),
        stationQuery = getStringExtra(STATION_QUERY_EXTRA),
        deepLinkStationQuery = data?.getQueryParameter(STATION_QUERY_EXTRA),
        deepLinkQuery = data?.getQueryParameter("query"),
        searchManagerQuery = getStringExtra(SearchManager.QUERY),
        textQuery = getStringExtra(Intent.EXTRA_TEXT),
      ),
  )

internal fun Intent.toLaunchRequest(): MobileLaunchRequest? = toLaunchPayload()?.launchRequest

internal fun parseLaunchPayload(source: AndroidLaunchSource): AndroidLaunchPayload? =
  parseLaunchPayload(
    assistantAction =
      source.assistantAction
        ?: source.deepLinkAction
        ?: source.searchManagerQuery
        ?: source.textQuery,
    feature = source.feature ?: source.deepLinkFeature ?: canonicalDeepLinkAction(source.deepLinkHost),
    stationId =
      source.stationId ?: source.deepLinkStationId ?: source.deepLinkStationIdAlias ?: source.deepLinkPathSegment,
    stationQuery =
      source.stationQuery
        ?: source.deepLinkStationQuery
        ?: source.deepLinkQuery,
  )

private fun canonicalDeepLinkAction(host: String?): String? =
  when (host?.trim()?.lowercase(Locale.ROOT)) {
    "home", "nearby" -> HOME_ACTION
    "map" -> MAP_ACTION
    "favorites" -> FAVORITE_STATIONS_ACTION
    "alerts" -> SAVED_PLACE_ALERTS_ACTION
    "station" -> SHOW_STATION_ACTION
    "monitor" -> MONITOR_STATION_ACTION
    "city" -> SELECT_CITY_ACTION
    else -> null
  }

private fun canonicalAction(rawValue: String?): String? {
  val normalized =
    rawValue?.let(::normalizeActionToken)?.let(::stripAssistantAppName)?.takeIf { it.isNotEmpty() } ?: return null
  return when (normalized) {
    FAVORITE_STATIONS_ACTION,
    "favorites",
    "favoritas",
    "mis favoritas",
    "abre favoritas",
    "abre mis estaciones favoritas",
    "ensename mis favoritas",
    "abre mis favoritas",
    -> FAVORITE_STATIONS_ACTION
    NEAREST_STATION_ACTION,
    "estacion cercana",
    "cual es la estacion mas cercana",
    "que estacion tengo mas cerca",
    "que estacion esta mas cerca",
    "estacion mas cercana",
    -> NEAREST_STATION_ACTION
    NEAREST_STATION_WITH_BIKES_ACTION,
    "estacion cercana con bicis",
    "estacion con bicis cerca",
    "donde hay bicis cerca",
    "dime donde hay bicis cerca",
    "quiero saber donde hay bicis cerca",
    "donde puedo coger una bici",
    "donde puedo sacar una bici",
    "hay bicis cerca",
    "quiero coger una bici",
    -> NEAREST_STATION_WITH_BIKES_ACTION
    NEAREST_STATION_WITH_SLOTS_ACTION,
    "estacion cercana con huecos",
    "estacion con huecos cerca",
    "donde hay huecos cerca",
    "dime donde hay huecos cerca",
    "quiero saber donde hay huecos cerca",
    "donde puedo dejar la bici",
    "donde puedo aparcar la bici",
    "donde puedo anclar la bici",
    "hay huecos cerca",
    "quiero dejar la bici",
    -> NEAREST_STATION_WITH_SLOTS_ACTION
    OPEN_ASSISTANT_ACTION -> OPEN_ASSISTANT_ACTION
    HOME_ACTION,
    "home",
    "inicio",
    -> HOME_ACTION
    MAP_ACTION,
    "mapa",
    -> MAP_ACTION
    MONITOR_STATION_ACTION,
    "monitorizar estacion",
    "vigilar estacion",
    -> MONITOR_STATION_ACTION
    SELECT_CITY_ACTION,
    "cambiar ciudad",
    -> SELECT_CITY_ACTION
    STATION_STATUS_ACTION,
    "estado estacion",
    "como esta una estacion",
    "como esta estacion",
    "estado de una estacion",
    -> STATION_STATUS_ACTION
    STATION_BIKE_COUNT_ACTION,
    "bicis estacion",
    "bicis en estacion",
    "cuantas bicis hay en una estacion",
    "hay bicis en una estacion",
    -> STATION_BIKE_COUNT_ACTION
    STATION_SLOT_COUNT_ACTION,
    "huecos estacion",
    "huecos en estacion",
    "cuantos huecos hay en una estacion",
    "hay huecos en una estacion",
    -> STATION_SLOT_COUNT_ACTION
    ROUTE_TO_STATION_ACTION,
    "ruta a estacion",
    "llevame a una estacion",
    "como llego a una estacion",
    "quiero ir a una estacion",
    -> ROUTE_TO_STATION_ACTION
    SHOW_STATION_ACTION -> SHOW_STATION_ACTION
    SAVED_PLACE_ALERTS_ACTION,
    "alerts",
    "alertas",
    -> SAVED_PLACE_ALERTS_ACTION
    else -> null
  }
}

private fun parseAssistantPhrase(rawValue: String?): ParsedAssistantPhrase? {
  val normalized =
    rawValue?.let(::normalizeActionToken)?.let(::stripAssistantAppName)?.takeIf { it.isNotEmpty() }
      ?: return null

  canonicalAction(normalized)?.let { action ->
    return ParsedAssistantPhrase(action = action)
  }

  extractParameterizedAction(
    normalized = normalized,
    prefixes = listOf("como esta ", "como esta la estacion ", "estado de ", "que tal esta "),
    action = STATION_STATUS_ACTION,
  )?.let { return it }

  extractParameterizedAction(
    normalized = normalized,
    prefixes =
      listOf(
        "cuantas bicis hay en ",
        "dime cuantas bicis hay en ",
        "quiero saber cuantas bicis hay en ",
        "cuantas bicis quedan en ",
        "bicis en ",
        "bicis disponibles en ",
        "hay bicis en ",
        "cuantas bicis tiene ",
      ),
    action = STATION_BIKE_COUNT_ACTION,
  )?.let { return it }

  extractParameterizedAction(
    normalized = normalized,
    prefixes =
      listOf(
        "cuantos huecos hay en ",
        "dime cuantos huecos hay en ",
        "quiero saber cuantos huecos hay en ",
        "cuantos huecos quedan en ",
        "huecos en ",
        "huecos libres en ",
        "hay huecos en ",
        "cuantos anclajes libres hay en ",
      ),
    action = STATION_SLOT_COUNT_ACTION,
  )?.let { return it }

  extractParameterizedAction(
    normalized = normalized,
    prefixes =
      listOf(
        "llevame a ",
        "llevame al ",
        "llevame hasta ",
        "ruta a ",
        "ruta al ",
        "abre ruta a ",
        "abre ruta al ",
        "navega a ",
        "navega al ",
        "como llego a ",
        "como llego al ",
        "guiame a ",
        "guiame al ",
        "quiero ir a ",
        "quiero ir al ",
      ),
    action = ROUTE_TO_STATION_ACTION,
  )?.let { return it }

  extractParameterizedAction(
    normalized = normalized,
    prefixes = listOf("muestrame ", "ensename ", "busca ", "abre "),
    action = SHOW_STATION_ACTION,
  )?.let { return it }

  return null
}

private fun extractParameterizedAction(
  normalized: String,
  prefixes: List<String>,
  action: String,
): ParsedAssistantPhrase? {
  val stationQuery =
    prefixes
      .firstNotNullOfOrNull { prefix ->
        normalized
          .removePrefix(prefix)
          .takeIf { normalized.startsWith(prefix) }
          ?.trim()
      }?.let(::cleanStationQuery)
      ?.takeUnless(::isGenericStationPlaceholder)

  return stationQuery?.let { ParsedAssistantPhrase(action = action, stationQuery = it) }
}

private fun isGenericStationPlaceholder(value: String): Boolean =
  value in
    setOf(
      "estacion",
      "la estacion",
      "una estacion",
      "mi estacion",
    )

private fun stripAssistantAppName(value: String): String =
  value
    .replace("\\b(con|en|de) bici radar\\b".toRegex(), "")
    .replace("\\b(con|en|de) biciradar\\b".toRegex(), "")
    .replace("\\bbici radar\\b".toRegex(), "")
    .replace("\\bbiciradar\\b".toRegex(), "")
    .replace(WHITESPACE_REGEX, " ")
    .trim()

private fun cleanStationQuery(value: String): String =
  value
    .replace(
      "^(la |el )?(estacion|parada|bizi)( numero| n)? ".toRegex(),
      "",
    ).replace("^(por favor )".toRegex(), "")
    .replace("^(dime |quiero saber |necesito saber )".toRegex(), "")
    .replace("^(la |el )".toRegex(), "")
    .replace("\\bpor favor\\b".toRegex(), "")
    .replace("\\bahora mismo\\b".toRegex(), "")
    .replace("\\bporfa\\b".toRegex(), "")
    .replace(WHITESPACE_REGEX, " ")
    .trim()

private fun normalizeActionToken(value: String): String {
  val withoutDiacritics =
    Normalizer
      .normalize(value.trim(), Normalizer.Form.NFD)
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
): T? =
  when {
    stationId != null -> stationIdFactory(stationId, null)
    stationQuery != null -> stationQueryFactory(null, stationQuery)
    else -> null
  }
