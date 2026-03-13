package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerInfoWindowContent
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private fun stationMarkerHue(station: Station, highlighted: Boolean): Float {
  val base = when {
    station.bikesAvailable > 0 && station.slotsFree > 0 -> BitmapDescriptorFactory.HUE_GREEN
    station.bikesAvailable == 0 && station.slotsFree == 0 -> BitmapDescriptorFactory.HUE_RED
    else -> BitmapDescriptorFactory.HUE_ORANGE
  }
  // Highlighted: shift slightly darker by moving hue toward a distinct anchor
  return if (highlighted) {
    when (base) {
      BitmapDescriptorFactory.HUE_GREEN -> 130f  // darker green
      BitmapDescriptorFactory.HUE_RED -> 355f    // deeper red
      else -> 30f                                // darker orange
    }
  } else base
}

@Composable
internal actual fun PlatformStationMap(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  isMapReady: Boolean,
  onStationSelected: (Station) -> Unit,
) {
  if (!isMapReady) {
    Surface(modifier = modifier) {
      Text(
        text = "Mapa no disponible. Configura una API key de Google Maps.",
        modifier = Modifier
          .fillMaxSize()
          .wrapContentSize(Alignment.Center),
        style = MaterialTheme.typography.bodyMedium,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
      )
    }
    return
  }
  val cameraPositionState = rememberCameraPositionState()
  var hasZoomed by remember { mutableStateOf(false) }

  LaunchedEffect(userLocation, stations) {
    if (hasZoomed) return@LaunchedEffect
    val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: return@LaunchedEffect
    cameraPositionState.position = CameraPosition.fromLatLngZoom(
      LatLng(focusPoint.latitude, focusPoint.longitude),
      if (userLocation != null) 15f else 13f,
    )
    hasZoomed = true
  }

  GoogleMap(
    modifier = modifier,
    cameraPositionState = cameraPositionState,
    properties = MapProperties(isMyLocationEnabled = userLocation != null),
    uiSettings = MapUiSettings(
      compassEnabled = false,
      mapToolbarEnabled = false,
      myLocationButtonEnabled = false,
      zoomControlsEnabled = false,
    ),
  ) {
    stations.forEach { station ->
      MarkerInfoWindowContent(
        state = remember(station.id) {
          MarkerState(position = LatLng(station.location.latitude, station.location.longitude))
        },
        title = station.name,
        snippet = "${station.bikesAvailable} bicis · ${station.slotsFree} libres",
        icon = BitmapDescriptorFactory.defaultMarker(
          stationMarkerHue(station, station.id == highlightedStationId),
        ),
        onClick = {
          onStationSelected(station)
          false
        },
      ) {
        Column(
          modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = station.name,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "${station.bikesAvailable} bicis · ${station.slotsFree} huecos · ${station.distanceMeters} m",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64748B),
          )
        }
      }
    }
  }
}
