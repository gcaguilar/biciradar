package com.gcaguilar.biciradar.core

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

enum class EmbeddedMapProvider {
  None,
  AppleMapKit,
  GoogleMaps,
}

@Immutable
data class MapSupportStatus(
  val embeddedProvider: EmbeddedMapProvider,
  val googleMapsSdkLinked: Boolean,
  val googleMapsApiKeyConfigured: Boolean,
)

@Stable
interface MapSupport {
  fun currentStatus(): MapSupportStatus
}

fun MapSupportStatus.isGoogleMapsReady(): Boolean = googleMapsSdkLinked && googleMapsApiKeyConfigured
