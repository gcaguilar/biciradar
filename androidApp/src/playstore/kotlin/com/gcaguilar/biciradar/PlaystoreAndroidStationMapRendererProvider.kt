package com.gcaguilar.biciradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.AndroidStationMapRenderer
import com.gcaguilar.biciradar.mobileui.BiziDataColors
import com.gcaguilar.biciradar.mobileui.EnvironmentalOverlayData
import com.gcaguilar.biciradar.mobileui.EnvironmentalOverlayLayer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

class PlaystoreAndroidStationMapRendererProvider : AndroidStationMapRenderer {
  @Composable
  override fun Render(
    modifier: Modifier,
    stations: List<Station>,
    userLocation: GeoPoint?,
    highlightedStationId: String?,
    onStationSelected: (Station) -> Unit,
    onMapClick: ((GeoPoint) -> Unit)?,
    pinLocation: GeoPoint?,
    recenterRequestToken: Int,
    environmentalOverlay: EnvironmentalOverlayData?,
    stationSnippet: (Station) -> String,
    pinTitle: String,
  ) {
    val cameraPositionState = rememberCameraPositionState()
    val mapProperties =
      remember(userLocation != null) {
        MapProperties(
          isMyLocationEnabled = userLocation != null,
        )
      }
    val mapUiSettings =
      remember {
        MapUiSettings(
          compassEnabled = false,
          mapToolbarEnabled = false,
          myLocationButtonEnabled = false,
          zoomControlsEnabled = false,
        )
      }
    var hasZoomed by remember { mutableStateOf(false) }
    var lastHandledRecenterToken by remember { mutableIntStateOf(0) }

    LaunchedEffect(userLocation, stations) {
      if (hasZoomed) return@LaunchedEffect
      val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: return@LaunchedEffect
      cameraPositionState.position =
        CameraPosition.fromLatLngZoom(
          LatLng(focusPoint.latitude, focusPoint.longitude),
          if (userLocation != null) 15f else 13f,
        )
      hasZoomed = true
    }

    LaunchedEffect(recenterRequestToken, userLocation, stations) {
      if (recenterRequestToken == 0) return@LaunchedEffect
      if (recenterRequestToken == lastHandledRecenterToken) return@LaunchedEffect
      val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: return@LaunchedEffect
      cameraPositionState.position =
        CameraPosition.fromLatLngZoom(
          LatLng(focusPoint.latitude, focusPoint.longitude),
          if (userLocation != null) 15f else 13f,
        )
      lastHandledRecenterToken = recenterRequestToken
    }

    GoogleMap(
      modifier = modifier,
      cameraPositionState = cameraPositionState,
      properties = mapProperties,
      uiSettings = mapUiSettings,
      onMapClick =
        if (onMapClick != null) {
          { latLng -> onMapClick(GeoPoint(latLng.latitude, latLng.longitude)) }
        } else {
          null
        },
    ) {
      environmentalOverlay?.zones?.forEach { zone ->
        val tone =
          when {
            environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality &&
              zone.value <= 50 -> BiziDataColors.AqiGood
            environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality &&
              zone.value <= 100 -> BiziDataColors.AqiModerate
            environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality ->
              BiziDataColors.AqiBad
            environmentalOverlay.layer == EnvironmentalOverlayLayer.Pollen &&
              zone.value <= 10 -> BiziDataColors.PollenLow
            environmentalOverlay.layer == EnvironmentalOverlayLayer.Pollen &&
              zone.value <= 30 -> BiziDataColors.PollenMedium
            else -> BiziDataColors.PollenHigh
          }
        Circle(
          center = LatLng(zone.center.latitude, zone.center.longitude),
          radius = 450.0,
          fillColor = tone.copy(alpha = 0.22f),
          strokeColor = tone.copy(alpha = 0.45f),
          strokeWidth = 1f,
        )
      }

      stations.forEach { station ->
        key(station.id) {
          val markerState =
            remember(station.location.latitude, station.location.longitude) {
              MarkerState(position = LatLng(station.location.latitude, station.location.longitude))
            }
          MarkerInfoWindowContent(
            state = markerState,
            title = station.name,
            snippet = stationSnippet(station),
            icon =
              BitmapDescriptorFactory.defaultMarker(
                stationMarkerHue(station, station.id == highlightedStationId),
              ),
            onClick = {
              onStationSelected(station)
              false
            },
          ) {}
        }
      }
      if (pinLocation != null) {
        key("destination-pin") {
          MarkerInfoWindowContent(
            state =
              remember(pinLocation.latitude, pinLocation.longitude) {
                MarkerState(position = LatLng(pinLocation.latitude, pinLocation.longitude))
              },
            title = pinTitle,
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
          ) {}
        }
      }
    }
  }

  private fun stationMarkerHue(
    station: Station,
    highlighted: Boolean,
  ): Float {
    val base =
      when {
        station.bikesAvailable > 0 && station.slotsFree > 0 -> BitmapDescriptorFactory.HUE_GREEN
        station.bikesAvailable == 0 && station.slotsFree == 0 -> BitmapDescriptorFactory.HUE_RED
        else -> BitmapDescriptorFactory.HUE_ORANGE
      }
    return if (highlighted) {
      when (base) {
        BitmapDescriptorFactory.HUE_GREEN -> 130f
        BitmapDescriptorFactory.HUE_RED -> 355f
        else -> 30f
      }
    } else {
      base
    }
  }
}
