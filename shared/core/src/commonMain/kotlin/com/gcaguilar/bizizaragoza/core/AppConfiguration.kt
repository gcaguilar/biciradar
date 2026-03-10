package com.gcaguilar.bizizaragoza.core

import kotlinx.serialization.Serializable

@Serializable
data class AppConfiguration(
  val cityBikesNetworkUrl: String = "https://api.citybik.es/v2/networks/bizi-zaragoza",
  val geminiProxyBaseUrl: String = "http://127.0.0.1:8080",
  val defaultLatitude: Double = 41.6488,
  val defaultLongitude: Double = -0.8891,
)

fun AppConfiguration.defaultLocation(): GeoPoint = GeoPoint(
  latitude = defaultLatitude,
  longitude = defaultLongitude,
)
