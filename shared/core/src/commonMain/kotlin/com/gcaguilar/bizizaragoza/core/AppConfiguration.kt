package com.gcaguilar.bizizaragoza.core

import kotlinx.serialization.Serializable

@Serializable
data class AppConfiguration(
  val stationsApiUrl: String = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/estacion-bicicleta.json?rows=300",
  val stationsFallbackApiUrl: String = "https://api.citybik.es/v2/networks/bizi-zaragoza",
  val geminiProxyBaseUrl: String = "",
  val defaultLatitude: Double = 41.6488,
  val defaultLongitude: Double = -0.8891,
)

fun AppConfiguration.defaultLocation(): GeoPoint = GeoPoint(
  latitude = defaultLatitude,
  longitude = defaultLongitude,
)

fun AppConfiguration.isGeminiEnabled(): Boolean = geminiProxyBaseUrl.isNotBlank()
