package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.StationHourlyPattern

/**
 * Public usage-pattern endpoint of the BiciRadar backend. Implemented by
 * [BiciRadarApiClient] and consumed through `RoutingDatosBiziApi`, which keeps
 * Zaragoza on datosbizi.com.
 */
interface UsagePatternsBackendApi {
  val isConfigured: Boolean

  suspend fun fetchPatterns(
    cityId: String,
    stationId: String,
  ): List<StationHourlyPattern>
}

/**
 * The backend does not serve usage history for [cityId] (its feed cannot supply
 * the counters, or the feed is dead). Callers hide the patterns surface instead
 * of showing an empty card forever.
 */
class PatternsNotOfferedException(
  val cityId: String,
  cause: Throwable? = null,
) : Exception("Usage patterns are not offered for $cityId", cause)

/**
 * Maps a failed `/patterns` call: HTTP 404 means the city has no history,
 * anything else is a real error and must stay visible.
 */
internal fun BackendException.toPatternsFailure(cityId: String): Throwable =
  if (status == 404) PatternsNotOfferedException(cityId, this) else this

/**
 * Maps the documented wire shape to the app model. The fields are identical on
 * purpose; keeping the mapping explicit means a backend rename fails here
 * instead of silently producing empty patterns.
 */
internal fun UsagePatternDto.toStationHourlyPattern(): StationHourlyPattern =
  StationHourlyPattern(
    stationId = stationId,
    dayType = dayType,
    hour = hour,
    bikesAvg = bikesAvg,
    anchorsAvg = anchorsAvg,
    occupancyAvg = occupancyAvg,
    sampleCount = sampleCount,
  )
