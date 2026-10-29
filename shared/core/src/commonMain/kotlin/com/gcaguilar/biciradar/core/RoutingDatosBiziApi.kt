package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.backend.UsagePatternsBackendApi

/**
 * Routes usage-pattern reads to the provider that owns the city.
 *
 * Zaragoza keeps datosbizi.com; every other city is served by the BiciRadar
 * backend once it is configured. Without a backend URL the app stays in
 * anonymous/local mode: those cities simply report no patterns and the UI
 * hides the section.
 */
class RoutingDatosBiziApi(
  private val datosBiziApi: DatosBiziApi,
  private val backendPatternsApi: UsagePatternsBackendApi,
) : DatosBiziApi {
  override fun supports(cityId: String): Boolean = datosBiziApi.supports(cityId) || backendPatternsApi.isConfigured

  override suspend fun fetchPatterns(
    cityId: String,
    stationId: String,
  ): List<StationHourlyPattern> =
    when {
      datosBiziApi.supports(cityId) -> datosBiziApi.fetchPatterns(cityId, stationId)
      backendPatternsApi.isConfigured -> backendPatternsApi.fetchPatterns(cityId, stationId)
      else -> emptyList()
    }
}
