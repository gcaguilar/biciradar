package com.gcaguilar.biciradar.core

/**
 * Canonical action identifiers understood by BiciRadar.
 *
 * These constants are shared across androidApp, wearApp, apple, and any future platform
 * so that deep links, assistants, and shortcuts all resolve to the same semantic actions.
 */
object ActionIds {
  const val HOME_ACTION = "home"
  const val MAP_ACTION = "map"
  const val FAVORITE_STATIONS_ACTION = "favorite_stations"
  const val NEAREST_STATION_ACTION = "nearest_station"
  const val NEAREST_STATION_WITH_BIKES_ACTION = "nearest_station_with_bikes"
  const val NEAREST_STATION_WITH_SLOTS_ACTION = "nearest_station_with_slots"
  const val OPEN_ASSISTANT_ACTION = "open_assistant"
  const val STATION_STATUS_ACTION = "station_status"
  const val STATION_BIKE_COUNT_ACTION = "station_bike_count"
  const val STATION_SLOT_COUNT_ACTION = "station_slot_count"
  const val ROUTE_TO_STATION_ACTION = "route_to_station"
  const val SHOW_STATION_ACTION = "show_station"
  const val MONITOR_STATION_ACTION = "monitor_station"
  const val SELECT_CITY_ACTION = "select_city"
  const val SAVED_PLACE_ALERTS_ACTION = "saved_place_alerts"
}

/**
 * Resolves a raw phrase (from Assistant, deep link, or shortcut) into a canonical action
 * and an optional station query extracted by stripping known prefixes.
 *
 * This is the deep domain seam: platform code calls [resolvePhrase] and receives a
 * platform-agnostic [PhraseResolution] without dealing with Spanish-language pattern
 * matching or diacritics removal.
 */
interface AssistantPhraseResolver {
  fun resolvePhrase(raw: String?): PhraseResolution?
}

/**
 * Result of resolving a raw phrase.
 *
 * @param action canonical action ID (one of [ActionIds] constants), or null when the phrase
 *   doesn't match any known action (may still carry a [stationQuery] for search fallback).
 * @param stationQuery extracted station name/ID, already cleaned and normalized.
 */
data class PhraseResolution(
  val action: String?,
  val stationQuery: String? = null,
)

/**
 * Shared phrase-to-action resolver using Spanish-language natural language patterns.
 *
 * The canonical pattern list is the single source of truth for all platforms (Android,
 * Wear, watchOS, iOS). To add a new phrase, update the [canonicalMap] below.
 * To add a new prefix-based pattern, add to the relevant [prefixMap] entry.
 */
class DefaultAssistantPhraseResolver : AssistantPhraseResolver {
  private val canonicalMap: Map<String, String> =
    buildMap {
      put(ActionIds.FAVORITE_STATIONS_ACTION, ActionIds.FAVORITE_STATIONS_ACTION)
      put("favorites", ActionIds.FAVORITE_STATIONS_ACTION)
      put("favoritas", ActionIds.FAVORITE_STATIONS_ACTION)
      put("mis favoritas", ActionIds.FAVORITE_STATIONS_ACTION)
      put("abre favoritas", ActionIds.FAVORITE_STATIONS_ACTION)
      put("abre mis estaciones favoritas", ActionIds.FAVORITE_STATIONS_ACTION)
      put("ensename mis favoritas", ActionIds.FAVORITE_STATIONS_ACTION)
      put("abre mis favoritas", ActionIds.FAVORITE_STATIONS_ACTION)

      put(ActionIds.NEAREST_STATION_ACTION, ActionIds.NEAREST_STATION_ACTION)
      put("estacion cercana", ActionIds.NEAREST_STATION_ACTION)
      put("cual es la estacion mas cercana", ActionIds.NEAREST_STATION_ACTION)
      put("que estacion tengo mas cerca", ActionIds.NEAREST_STATION_ACTION)
      put("que estacion esta mas cerca", ActionIds.NEAREST_STATION_ACTION)
      put("estacion mas cercana", ActionIds.NEAREST_STATION_ACTION)

      put(ActionIds.NEAREST_STATION_WITH_BIKES_ACTION, ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("estacion cercana con bicis", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("estacion con bicis cerca", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("donde hay bicis cerca", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("dime donde hay bicis cerca", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("quiero saber donde hay bicis cerca", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("donde puedo coger una bici", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("donde puedo sacar una bici", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("hay bicis cerca", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)
      put("quiero coger una bici", ActionIds.NEAREST_STATION_WITH_BIKES_ACTION)

      put(ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION, ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("estacion cercana con huecos", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("estacion con huecos cerca", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("donde hay huecos cerca", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("dime donde hay huecos cerca", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("quiero saber donde hay huecos cerca", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("donde puedo dejar la bici", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("donde puedo aparcar la bici", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("donde puedo anclar la bici", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("hay huecos cerca", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)
      put("quiero dejar la bici", ActionIds.NEAREST_STATION_WITH_SLOTS_ACTION)

      put(ActionIds.OPEN_ASSISTANT_ACTION, ActionIds.OPEN_ASSISTANT_ACTION)
      put(ActionIds.HOME_ACTION, ActionIds.HOME_ACTION)
      put("inicio", ActionIds.HOME_ACTION)
      put(ActionIds.MAP_ACTION, ActionIds.MAP_ACTION)
      put("mapa", ActionIds.MAP_ACTION)
      put(ActionIds.MONITOR_STATION_ACTION, ActionIds.MONITOR_STATION_ACTION)
      put("monitorizar estacion", ActionIds.MONITOR_STATION_ACTION)
      put("vigilar estacion", ActionIds.MONITOR_STATION_ACTION)
      put(ActionIds.SELECT_CITY_ACTION, ActionIds.SELECT_CITY_ACTION)
      put("cambiar ciudad", ActionIds.SELECT_CITY_ACTION)
      put(ActionIds.STATION_STATUS_ACTION, ActionIds.STATION_STATUS_ACTION)
      put("estado estacion", ActionIds.STATION_STATUS_ACTION)
      put("como esta una estacion", ActionIds.STATION_STATUS_ACTION)
      put("como esta estacion", ActionIds.STATION_STATUS_ACTION)
      put("estado de una estacion", ActionIds.STATION_STATUS_ACTION)
      put(ActionIds.STATION_BIKE_COUNT_ACTION, ActionIds.STATION_BIKE_COUNT_ACTION)
      put("bicis estacion", ActionIds.STATION_BIKE_COUNT_ACTION)
      put("bicis en estacion", ActionIds.STATION_BIKE_COUNT_ACTION)
      put("cuantas bicis hay en una estacion", ActionIds.STATION_BIKE_COUNT_ACTION)
      put("hay bicis en una estacion", ActionIds.STATION_BIKE_COUNT_ACTION)
      put(ActionIds.STATION_SLOT_COUNT_ACTION, ActionIds.STATION_SLOT_COUNT_ACTION)
      put("huecos estacion", ActionIds.STATION_SLOT_COUNT_ACTION)
      put("huecos en estacion", ActionIds.STATION_SLOT_COUNT_ACTION)
      put("cuantos huecos hay en una estacion", ActionIds.STATION_SLOT_COUNT_ACTION)
      put("hay huecos en una estacion", ActionIds.STATION_SLOT_COUNT_ACTION)
      put(ActionIds.ROUTE_TO_STATION_ACTION, ActionIds.ROUTE_TO_STATION_ACTION)
      put("ruta a estacion", ActionIds.ROUTE_TO_STATION_ACTION)
      put("llevame a una estacion", ActionIds.ROUTE_TO_STATION_ACTION)
      put("como llego a una estacion", ActionIds.ROUTE_TO_STATION_ACTION)
      put("quiero ir a una estacion", ActionIds.ROUTE_TO_STATION_ACTION)
      put(ActionIds.SHOW_STATION_ACTION, ActionIds.SHOW_STATION_ACTION)
      put(ActionIds.SAVED_PLACE_ALERTS_ACTION, ActionIds.SAVED_PLACE_ALERTS_ACTION)
      put("alerts", ActionIds.SAVED_PLACE_ALERTS_ACTION)
      put("alertas", ActionIds.SAVED_PLACE_ALERTS_ACTION)
    }

  private val prefixMap: Map<String, List<String>> =
    mapOf(
      ActionIds.STATION_STATUS_ACTION to
        listOf(
          "como esta ",
          "como esta la estacion ",
          "estado de ",
          "que tal esta ",
        ),
      ActionIds.STATION_BIKE_COUNT_ACTION to
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
      ActionIds.STATION_SLOT_COUNT_ACTION to
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
      ActionIds.ROUTE_TO_STATION_ACTION to
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
      ActionIds.SHOW_STATION_ACTION to
        listOf(
          "muestrame ",
          "ensename ",
          "busca ",
          "abre ",
        ),
    )

  override fun resolvePhrase(raw: String?): PhraseResolution? {
    val normalized = normalize(raw) ?: return null

    canonicalAction(normalized)?.let { action ->
      return PhraseResolution(action = action)
    }

    for ((action, prefixes) in prefixMap) {
      val stationQuery = extractParameterizedStationQuery(normalized, prefixes)
      if (stationQuery != null) {
        return PhraseResolution(action = action, stationQuery = stationQuery)
      }
    }

    return null
  }

  private fun normalize(value: String?): String? {
    val withoutDiacritics = value?.let(::stripDiacritics)
    return withoutDiacritics
      ?.lowercase()
      ?.replace(WHITESPACE_REGEX, " ")
      ?.trim()
      ?.takeIf { it.isNotEmpty() }
  }

  private fun stripDiacritics(input: String): String {
    val result = StringBuilder(input.length)
    for (char in input) {
      result.append(
        when (char) {
          'á', 'à', 'ä', 'â', 'ã', 'å' -> 'a'
          'é', 'è', 'ë', 'ê' -> 'e'
          'í', 'ì', 'ï', 'î' -> 'i'
          'ó', 'ò', 'ö', 'ô', 'õ' -> 'o'
          'ú', 'ù', 'ü', 'û' -> 'u'
          'ñ' -> 'n'
          'ý', 'ÿ' -> 'y'
          else -> char
        },
      )
    }
    return result.toString()
  }

  fun canonicalAction(rawValue: String?): String? {
    val normalized =
      normalize(rawValue)
        ?.let(::stripAssistantAppName)
        .takeIf { it?.isNotEmpty() == true } ?: return null
    return canonicalMap[normalized]
  }

  private fun stripAssistantAppName(value: String): String =
    value
      .replace("\\b(con|en|de) bici radar\\b".toRegex(), "")
      .replace("\\b(con|en|de) biciradar\\b".toRegex(), "")
      .replace("\\bbici radar\\b".toRegex(), "")
      .replace("\\bbiciradar\\b".toRegex(), "")
      .replace(WHITESPACE_REGEX, " ")
      .trim()

  private fun extractParameterizedStationQuery(
    normalized: String,
    prefixes: List<String>,
  ): String? {
    val stationQuery =
      prefixes
        .firstNotNullOfOrNull { prefix ->
          normalized
            .removePrefix(prefix)
            .takeIf { normalized.startsWith(prefix) }
            ?.trim()
        }?.let(::cleanStationQuery)
        ?.takeUnless(::isGenericStationPlaceholder)

    return stationQuery
  }

  private fun isGenericStationPlaceholder(value: String): Boolean =
    value in setOf("estacion", "la estacion", "una estacion", "mi estacion")

  private fun cleanStationQuery(value: String): String =
    value
      .replace("^(la |el )?(estacion|parada|bizi)( numero| n)? ".toRegex(), "")
      .replace("^(por favor )".toRegex(), "")
      .replace("^(dime |quiero saber |necesito saber )".toRegex(), "")
      .replace("^(la |el )".toRegex(), "")
      .replace("\\bpor favor\\b".toRegex(), "")
      .replace("\\bahora mismo\\b".toRegex(), "")
      .replace("\\bporfa\\b".toRegex(), "")
      .replace("\\b(con|en|de) (bici radar|biciradar)\\b".toRegex(), "")
      .replace("\\bbici radar\\b".toRegex(), "")
      .replace("\\bbiciradar\\b".toRegex(), "")
      .replace(WHITESPACE_REGEX, " ")
      .trim()

  companion object {
    private val DIACRITICS_REGEX = "\\p{Mn}+".toRegex()
    private val WHITESPACE_REGEX = "\\s+".toRegex()
  }
}
