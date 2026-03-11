package com.gcaguilar.bizizaragoza.core

import kotlinx.serialization.Serializable

@Serializable
data class AppConfiguration(
  val stationsApiUrl: String = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/estacion-bicicleta.json",
  val stationsFallbackApiUrl: String = "https://api.citybik.es/v2/networks/bizi-zaragoza",
  val defaultLatitude: Double = 41.6488,
  val defaultLongitude: Double = -0.8891,
)

fun AppConfiguration.stationsApiUrl(start: Int, rows: Int): String =
  "$stationsApiUrl?start=$start&rows=$rows"

fun AppConfiguration.stationAvailabilityUrl(stationId: String): String =
  "$stationsApiUrl/$stationId.json"

fun AppConfiguration.defaultLocation(): GeoPoint = GeoPoint(
  latitude = defaultLatitude,
  longitude = defaultLongitude,
)
