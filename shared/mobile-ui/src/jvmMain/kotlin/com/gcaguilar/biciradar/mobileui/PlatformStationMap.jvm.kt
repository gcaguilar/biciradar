package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station

@Composable
internal actual fun PlatformStationMap(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  isMapReady: Boolean,
  onStationSelected: (Station) -> Unit,
  onMapClick: ((GeoPoint) -> Unit)?,
  pinLocation: GeoPoint?,
  recenterRequestToken: Int,
  environmentalOverlay: EnvironmentalOverlayData?,
  stationSnippet: (Station) -> String,
  pinTitle: String,
) {
  StationListFallback(
    stations = stations,
    highlightedStationId = highlightedStationId,
    onStationSelected = onStationSelected,
    onMapClick = onMapClick,
    pinLocation = pinLocation,
    userLocation = userLocation,
    stationSnippet = stationSnippet,
  )
}
