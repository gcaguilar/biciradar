package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import org.jetbrains.compose.resources.stringResource

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
  // Highlighted: shift slightly darker by moving hue toward a distinct anchor
  return if (highlighted) {
    when (base) {
      BitmapDescriptorFactory.HUE_GREEN -> 130f // darker green
      BitmapDescriptorFactory.HUE_RED -> 355f // deeper red
      else -> 30f // darker orange
    }
  } else {
    base
  }
}

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
  if (!isMapReady) {
    Surface(modifier = modifier) {
      Text(
        text = stringResource(Res.string.mapUnavailableGoogleApiKey),
        modifier =
          Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      )
    }
    return
  }
  val cameraPositionState = rememberCameraPositionState()
  val isDarkTheme = LocalIsDarkTheme.current
  val mapStyleOptions =
    remember(isDarkTheme) {
      if (isDarkTheme) MapStyleOptions(DARK_MAP_STYLE_JSON) else null
    }
  val mapProperties =
    remember(userLocation != null, mapStyleOptions) {
      MapProperties(
        isMyLocationEnabled = userLocation != null,
        mapStyleOptions = mapStyleOptions,
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

  LaunchedEffect(recenterRequestToken) {
    if (recenterRequestToken == 0) return@LaunchedEffect
    val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: return@LaunchedEffect
    cameraPositionState.position =
      CameraPosition.fromLatLngZoom(
        LatLng(focusPoint.latitude, focusPoint.longitude),
        if (userLocation != null) 15f else 13f,
      )
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
          environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality && zone.value <= 50 -> Color(0xFF2E7D32)
          environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality && zone.value <= 100 -> Color(0xFFEF6C00)
          environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality -> Color(0xFFC62828)
          environmentalOverlay.layer == EnvironmentalOverlayLayer.Pollen && zone.value <= 10 -> Color(0xFF2E7D32)
          environmentalOverlay.layer == EnvironmentalOverlayLayer.Pollen && zone.value <= 30 -> Color(0xFFEF6C00)
          else -> Color(0xFFC62828)
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
        ) {
          Column(
            modifier =
              Modifier
                .background(LocalBiziColors.current.surface, RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = station.name,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = stationSnippet(station),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
          }
        }
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

private const val DARK_MAP_STYLE_JSON = """
[
  {"elementType":"geometry","stylers":[{"color":"#0f172a"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#cbd5e1"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#0f172a"}]},
  {"featureType":"administrative","elementType":"geometry.stroke","stylers":[{"color":"#334155"}]},
  {"featureType":"landscape.man_made","elementType":"geometry","stylers":[{"color":"#111827"}]},
  {"featureType":"landscape.natural","elementType":"geometry","stylers":[{"color":"#0b1220"}]},
  {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#111827"}]},
  {"featureType":"poi.park","elementType":"geometry","stylers":[{"color":"#0b3b2e"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#1f2937"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#0f172a"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#334155"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1e293b"}]},
  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#172033"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#0a2540"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#93c5fd"}]}
]
"""
