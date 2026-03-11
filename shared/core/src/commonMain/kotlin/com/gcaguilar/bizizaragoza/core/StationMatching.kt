package com.gcaguilar.bizizaragoza.core

fun findStationMatchingQuery(
  stations: List<Station>,
  query: String?,
): Station? {
  val normalizedQuery = normalizeStationSearchQuery(query)
  if (normalizedQuery.isEmpty()) return stations.firstOrNull()
  val numericQuery = query.orEmpty().filter(Char::isDigit)

  return stations.firstOrNull { station ->
    val normalizedName = normalizeStationSearchQuery(station.name)
    val normalizedAddress = normalizeStationSearchQuery(station.address)
    val normalizedId = normalizeStationSearchQuery(station.id)
    val stationNumericId = station.id.filter(Char::isDigit)
    normalizedName == normalizedQuery ||
      normalizedAddress == normalizedQuery ||
      normalizedId == normalizedQuery ||
      (!numericQuery.isEmpty() && stationNumericId == numericQuery)
  } ?: stations.firstOrNull { station ->
    val normalizedName = normalizeStationSearchQuery(station.name)
    val normalizedAddress = normalizeStationSearchQuery(station.address)
    val normalizedId = normalizeStationSearchQuery(station.id)
    val stationNumericId = station.id.filter(Char::isDigit)
    normalizedName.contains(normalizedQuery) ||
      normalizedAddress.contains(normalizedQuery) ||
      normalizedId.contains(normalizedQuery) ||
      (!numericQuery.isEmpty() && stationNumericId.contains(numericQuery))
  }
}

fun filterStationsByQuery(
  stations: List<Station>,
  query: String,
): List<Station> {
  val normalizedQuery = normalizeStationSearchQuery(query)
  if (normalizedQuery.isEmpty()) return stations
  val numericQuery = query.filter(Char::isDigit)
  return stations.filter { station ->
    val normalizedName = normalizeStationSearchQuery(station.name)
    val normalizedAddress = normalizeStationSearchQuery(station.address)
    val normalizedId = normalizeStationSearchQuery(station.id)
    val stationNumericId = station.id.filter(Char::isDigit)
    normalizedName.contains(normalizedQuery) ||
      normalizedAddress.contains(normalizedQuery) ||
      normalizedId.contains(normalizedQuery) ||
      (!numericQuery.isEmpty() && stationNumericId.contains(numericQuery))
  }
}

private fun normalizeStationSearchQuery(value: String?): String = value
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
  .replace(
    "\\s+".toRegex(),
    " ",
  )
