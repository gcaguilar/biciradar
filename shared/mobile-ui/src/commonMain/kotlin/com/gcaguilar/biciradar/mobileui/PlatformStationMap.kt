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

/**
 * Color decision for station markers on the map.
 *
 * Pure logic extracted from platform Adapters so it can be tested independently
 * of native map plumbing. Platform Adapters call this to get the right color.
 */
enum class StationMarkerColor {
  Green,
  Orange,
  Red,
  DarkGreen,
  DarkOrange,
  DarkRed,
  Blue,
}

/**
 * Determines the marker color for a station based on availability and selection state.
 */
fun determineStationMarkerColor(
  station: Station,
  highlighted: Boolean,
): StationMarkerColor =
  when {
    station.bikesAvailable > 0 && station.slotsFree > 0 ->
      if (highlighted) StationMarkerColor.DarkGreen else StationMarkerColor.Green
    station.bikesAvailable == 0 && station.slotsFree == 0 ->
      if (highlighted) StationMarkerColor.DarkRed else StationMarkerColor.Red
    else ->
      if (highlighted) StationMarkerColor.DarkOrange else StationMarkerColor.Orange
  }

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
