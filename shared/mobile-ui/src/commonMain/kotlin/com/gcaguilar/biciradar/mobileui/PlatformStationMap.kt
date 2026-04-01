package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station

enum class EnvironmentalOverlayLayer {
  AirQuality,
  Pollen,
}

data class EnvironmentalOverlayZone(
  val center: GeoPoint,
  val value: Int,
)

data class EnvironmentalOverlayData(
  val layer: EnvironmentalOverlayLayer,
  val zones: List<EnvironmentalOverlayZone>,
)

@Composable
internal expect fun PlatformStationMap(
  modifier: Modifier = Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  isMapReady: Boolean,
  onStationSelected: (Station) -> Unit,
  onMapClick: ((GeoPoint) -> Unit)? = null,
  pinLocation: GeoPoint? = null,
  recenterRequestToken: Int = 0,
  environmentalOverlay: EnvironmentalOverlayData? = null,
  stationSnippet: (Station) -> String = { "${it.bikesAvailable} bicis · ${it.slotsFree} libres" },
  pinTitle: String = "Destino",
)
