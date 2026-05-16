package com.gcaguilar.biciradar

import android.app.SearchManager
import android.content.Intent
import com.gcaguilar.biciradar.core.ActionIds
import com.gcaguilar.biciradar.core.ActionIds.FAVORITE_STATIONS_ACTION
import com.gcaguilar.biciradar.core.ActionIds.HOME_ACTION
import com.gcaguilar.biciradar.core.ActionIds.MAP_ACTION
import com.gcaguilar.biciradar.core.ActionIds.MONITOR_STATION_ACTION
import com.gcaguilar.biciradar.core.ActionIds.NEAREST_STATION_ACTION
import com.gcaguilar.biciradar.core.ActionIds.NEAREST_STATION_WITH_BIKES_ACTION
import com.gcaguilar.biciradar.core.ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION
import com.gcaguilar.biciradar.core.ActionIds.OPEN_ASSISTANT_ACTION
import com.gcaguilar.biciradar.core.ActionIds.ROUTE_TO_STATION_ACTION
import com.gcaguilar.biciradar.core.ActionIds.SAVED_PLACE_ALERTS_ACTION
import com.gcaguilar.biciradar.core.ActionIds.SELECT_CITY_ACTION
import com.gcaguilar.biciradar.core.ActionIds.SHOW_STATION_ACTION
import com.gcaguilar.biciradar.core.ActionIds.STATION_BIKE_COUNT_ACTION
import com.gcaguilar.biciradar.core.ActionIds.STATION_SLOT_COUNT_ACTION
import com.gcaguilar.biciradar.core.ActionIds.STATION_STATUS_ACTION
import com.gcaguilar.biciradar.core.DefaultAssistantPhraseResolver
import com.gcaguilar.biciradar.core.PhraseResolution
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest

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

private val sharedPhraseResolver = DefaultAssistantPhraseResolver()

private fun resolvePhrase(raw: String?): PhraseResolution? = sharedPhraseResolver.resolvePhrase(raw)

internal fun parseLaunchRequest(
  assistantAction: String? = null,
  feature: String? = null,
  stationId: String? = null,
): MobileLaunchRequest? {
  val action =
    sequenceOf(assistantAction, feature)
      .mapNotNull { resolvePhrase(it)?.action ?: sharedPhraseResolver.canonicalAction(it) }
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
      .mapNotNull(::resolvePhrase)
      .firstOrNull()
  val normalizedStationQuery =
    stationQuery
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
      ?: parsedPhrase?.stationQuery
  val action =
    parsedPhrase?.action ?: sequenceOf(assistantAction, feature)
      .mapNotNull { sharedPhraseResolver.canonicalAction(it) }
      .firstOrNull()

  if (action == null) {
    val fallbackQuery =
      normalizedStationQuery ?: sequenceOf(assistantAction, feature)
        .mapNotNull { it?.trim()?.takeIf { s -> s.isNotEmpty() } }
        .mapNotNull { rawValue ->
          sharedPhraseResolver.resolvePhrase(rawValue)?.stationQuery
            ?: sharedPhraseResolver.canonicalAction(rawValue)?.let { rawValue }
        }.mapNotNull { it.let(::cleanStationQuery) }
        .takeUnless(::isGenericStationPlaceholder)
        .takeIf { it.isNotEmpty() }
        ?.firstOrNull()

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
    "home", "nearby" -> ActionIds.HOME_ACTION
    "map" -> ActionIds.MAP_ACTION
    "favorites" -> ActionIds.FAVORITE_STATIONS_ACTION
    "alerts" -> ActionIds.SAVED_PLACE_ALERTS_ACTION
    "station" -> ActionIds.SHOW_STATION_ACTION
    "monitor" -> ActionIds.MONITOR_STATION_ACTION
    "city" -> ActionIds.SELECT_CITY_ACTION
    else -> null
  }

// Delegated to shared — kept for backward compatibility with existing callers
private fun canonicalAction(rawValue: String?): String? = sharedPhraseResolver.canonicalAction(rawValue)

private fun parseAssistantPhrase(rawValue: String?): PhraseResolution? = sharedPhraseResolver.resolvePhrase(rawValue)

// Delegated to shared — kept for backward compatibility with existing callers
private fun extractParameterizedAction(
  normalized: String,
  prefixes: List<String>,
  action: String,
): PhraseResolution? {
  val stationQuery =
    prefixes
      .firstNotNullOfOrNull { prefix ->
        normalized
          .removePrefix(prefix)
          .takeIf { normalized.startsWith(prefix) }
          ?.trim()
      }?.let(::cleanStationQuery)
      ?.takeUnless(::isGenericStationPlaceholder)

  return stationQuery?.let { PhraseResolution(action = action, stationQuery = it) }
}

// Delegated to shared — kept for backward compatibility with existing callers
private fun isGenericStationPlaceholder(value: String): Boolean =
  value in
    setOf(
      "estacion",
      "la estacion",
      "una estacion",
      "mi estacion",
    )

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
