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
  val iosAppStoreId: String = "6760931316",
  val iosAppBundleId: String = "com.gcaguilar.biciradar.ios",
  /**
   * BiciRadar push/monitoring backend. Empty means anonymous/local mode: the
   * app behaves exactly as before and never calls `/v1`.
   */
  val backendBaseUrl: String = "",
  /** Auth0 tenant domain, e.g. `tenant.eu.auth0.com`. Empty disables login. */
  val auth0Domain: String = "",
  val auth0ClientId: String = "",
  val auth0Audience: String = "",
  /** Custom scheme registered as an Auth0 Android callback URL. */
  val auth0Scheme: String = "biciradar",
) {
  val backendConfigured: Boolean get() = backendBaseUrl.isNotBlank()

  val auth0Configured: Boolean
    get() = auth0Domain.isNotBlank() && auth0ClientId.isNotBlank() && auth0Audience.isNotBlank()

  val auth0Issuer: String get() =
    auth0Domain
      .trim()
      .trimEnd(
        '/',
      ).let { if (it.startsWith("http")) it else "https://$it" }

  val gbfsDiscoveryUrl: String get() = city.gbfsDiscoveryUrl

  val iosAppStoreUrl: String?
    get() =
      iosAppStoreId.takeIf { it.isNotBlank() }?.let { id ->
        "https://apps.apple.com/app/id$id?action=write-review"
      }

  companion object {
    fun createDefault(): AppConfiguration = AppConfiguration()

    /**
     * Builds a configuration with the backend/Auth0 values provided at runtime
     * (Android `BuildConfig`, iOS `Info.plist`). Blank values fall back to
     * local-only/anonymous mode.
     */
    fun configured(
      backendBaseUrl: String,
      auth0Domain: String,
      auth0ClientId: String,
      auth0Audience: String,
      auth0Scheme: String = "biciradar",
    ): AppConfiguration =
      AppConfiguration(
        backendBaseUrl = backendBaseUrl.trim(),
        auth0Domain = auth0Domain.trim(),
        auth0ClientId = auth0ClientId.trim(),
        auth0Audience = auth0Audience.trim(),
        auth0Scheme = auth0Scheme.trim().ifBlank { "biciradar" },
      )
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
