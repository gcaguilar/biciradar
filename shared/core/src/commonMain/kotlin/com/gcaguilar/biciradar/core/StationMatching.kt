package com.gcaguilar.biciradar.core

fun findStationMatchingQuery(
  stations: List<Station>,
  query: String?,
): Station? {
  val normalizedQuery = normalizeStationSearchQuery(query)
  if (normalizedQuery.isEmpty()) return stations.firstOrNull()
  val numericQuery = query.orEmpty().filter(Char::isDigit)

  return rankStationsForQuery(stations, normalizedQuery, numericQuery).firstOrNull()
}

fun findStationMatchingQueryOrPinnedAlias(
  stations: List<Station>,
  query: String?,
  homeStationId: String?,
  workStationId: String?,
): Station? {
  val pinnedStationId =
    pinnedAliasStationId(
      query = query,
      homeStationId = homeStationId,
      workStationId = workStationId,
    )
  return pinnedStationId?.let { id ->
    stations.firstOrNull { station -> station.id == id }
  } ?: findStationMatchingQuery(stations, query)
}

fun filterStationsByQuery(
  stations: List<Station>,
  query: String,
): List<Station> {
  val normalizedQuery = normalizeStationSearchQuery(query)
  if (normalizedQuery.isEmpty()) return stations
  val numericQuery = query.filter(Char::isDigit)
  return rankStationsForQuery(stations, normalizedQuery, numericQuery)
}

internal fun normalizeStationSearchQuery(value: String?): String =
  value
    .orEmpty()
    .trim()
    .lowercase()
    .replace("á", "a")
    .replace("à", "a")
    .replace("ä", "a")
    .replace("é", "e")
    .replace("è", "e")
    .replace("ë", "e")
    .replace("í", "i")
    .replace("ì", "i")
    .replace("ï", "i")
    .replace("ó", "o")
    .replace("ò", "o")
    .replace("ö", "o")
    .replace("ú", "u")
    .replace("ù", "u")
    .replace("ü", "u")
    .replace("ñ", "n")
    .replace("\\bc/\\s*".toRegex(), " calle ")
    .replace("\\bpza\\.?\\b".toRegex(), " plaza ")
    .replace("\\bavda\\.?\\b".toRegex(), " avenida ")
    .replace("\\bav\\.?\\b".toRegex(), " avenida ")
    .replace("[^a-z0-9 ]".toRegex(), " ")
    .replace(
      "\\s+".toRegex(),
      " ",
    ).split(' ')
    .filter { token -> token.isNotBlank() && token !in STATION_STOPWORDS }
    .joinToString(" ")

private fun pinnedAliasStationId(
  query: String?,
  homeStationId: String?,
  workStationId: String?,
): String? =
  when (normalizeStationSearchQuery(query)) {
    "casa", "mi casa", "home" -> homeStationId
    "trabajo", "mi trabajo", "work", "oficina", "mi oficina" -> workStationId
    else -> null
  }

private val STATION_STOPWORDS =
  setOf(
    "de",
    "del",
    "la",
    "las",
    "el",
    "los",
  )

private data class RankedStation(
  val station: Station,
  val score: Int,
)

private fun rankStationsForQuery(
  stations: List<Station>,
  normalizedQuery: String,
  numericQuery: String,
): List<Station> =
  stations
    .mapNotNull { station ->
      stationSearchScore(station, normalizedQuery, numericQuery)?.let { score ->
        RankedStation(station = station, score = score)
      }
    }.sortedWith(
      compareByDescending<RankedStation> { it.score }
        .thenBy { it.station.distanceMeters }
        .thenBy { it.station.name },
    ).map(RankedStation::station)

private fun stationSearchScore(
  station: Station,
  normalizedQuery: String,
  numericQuery: String,
): Int? {
  val normalizedName = normalizeStationSearchQuery(station.name)
  val normalizedAddress = normalizeStationSearchQuery(station.address)
  val normalizedId = normalizeStationSearchQuery(station.id)
  val stationNumericId = station.id.filter(Char::isDigit)

  if (normalizedName.isEmpty() && normalizedAddress.isEmpty() && normalizedId.isEmpty()) return null

  if (normalizedQuery == normalizedId) return 10_000
  if (numericQuery.isNotEmpty() && stationNumericId == numericQuery) return 9_500
  if (normalizedQuery == normalizedName) return 9_000
  if (normalizedQuery == normalizedAddress) return 8_500

  val queryTokens = normalizedQuery.split(' ').filter(String::isNotBlank)
  val nameTokens = normalizedName.split(' ').filter(String::isNotBlank)
  val addressTokens = normalizedAddress.split(' ').filter(String::isNotBlank)
  val nameAndAddressTokens = (nameTokens + addressTokens).distinct()

  if (normalizedName.startsWith("$normalizedQuery ")) return 8_000
  if (normalizedAddress.startsWith("$normalizedQuery ")) return 7_700

  if (queryTokens.isNotEmpty() && queryTokens.all(nameTokens::contains)) {
    return 7_200 + queryTokens.size
  }
  if (queryTokens.isNotEmpty() && queryTokens.all(nameAndAddressTokens::contains)) {
    return 6_700 + queryTokens.size
  }

  if (normalizedName.contains(normalizedQuery)) return 6_200
  if (normalizedAddress.contains(normalizedQuery)) return 5_700
  if (normalizedId.contains(normalizedQuery)) return 5_200
  if (numericQuery.isNotEmpty() && stationNumericId.contains(numericQuery)) return 5_000

  val overlappingNameTokens = queryTokens.count(nameAndAddressTokens::contains)
  if (overlappingNameTokens > 0) {
    return 4_000 + overlappingNameTokens
  }

  return null
}
