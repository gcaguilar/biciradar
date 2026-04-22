package com.gcaguilar.biciradar.core

import kotlinx.serialization.Serializable

@Serializable
data class AppConfiguration(
  val city: City = City.ZARAGOZA,
  val stationsApiUrl: String = "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/estacion-bicicleta.json",
  val stationsFallbackApiUrl: String = "https://api.citybik.es/v2/networks/bizi-zaragoza",
  val defaultLatitude: Double = 41.6488,
  val defaultLongitude: Double = -0.8891,
  val feedbackFormUrl: String = "https://tally.so/r/A7bYRk",
  val privacyPolicyUrl: String = "https://gcaguilar.github.io/biciradar-privacy-policy/",
  /** Numeric App Store id for lookup / write-review URLs; empty disables iOS update banner until configured. */
  val iosAppStoreId: String = "",
  val iosAppBundleId: String = "com.gcaguilar.biciradar",
) {
  val gbfsDiscoveryUrl: String get() = city.gbfsDiscoveryUrl

  val iosAppStoreUrl: String?
    get() =
      iosAppStoreId.takeIf { it.isNotBlank() }?.let { id ->
        "https://apps.apple.com/app/id$id?action=write-review"
      }

  companion object {
    fun createDefault(): AppConfiguration = AppConfiguration()
  }
}

fun AppConfiguration.stationsApiUrl(
  start: Int,
  rows: Int,
): String = "$stationsApiUrl?start=$start&rows=$rows"

fun AppConfiguration.stationAvailabilityUrl(stationId: String): String = "$stationsApiUrl/$stationId.json"

fun AppConfiguration.defaultLocation(): GeoPoint =
  GeoPoint(
    latitude = defaultLatitude,
    longitude = defaultLongitude,
  )
