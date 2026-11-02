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
  val renderer = LocalAndroidStationMapRenderer.current

  // On Android the Play Store renderer is the authoritative source of embedded map support.
  if (renderer != null) {
    renderer.Render(
      modifier = modifier,
      stations = stations,
      userLocation = userLocation,
      highlightedStationId = highlightedStationId,
      onStationSelected = onStationSelected,
      onMapClick = onMapClick,
      pinLocation = pinLocation,
      recenterRequestToken = recenterRequestToken,
      environmentalOverlay = environmentalOverlay,
      stationSnippet = stationSnippet,
      pinTitle = pinTitle,
    )
    return
  }

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
