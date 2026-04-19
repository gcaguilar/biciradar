package com.gcaguilar.biciradar.core

enum class EmbeddedMapProvider {
  None,
  AppleMapKit,
  GoogleMaps,
  Osmdroid,
}

data class MapSupportStatus(
  val embeddedProvider: EmbeddedMapProvider,
  val googleMapsSdkLinked: Boolean,
  val googleMapsApiKeyConfigured: Boolean,
  val googleMapsAppInstalled: Boolean = false,
)

interface MapSupport {
  fun currentStatus(): MapSupportStatus
}

fun MapSupportStatus.isGoogleMapsReady(): Boolean = googleMapsSdkLinked && googleMapsApiKeyConfigured
