package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
internal actual fun PlatformStationMap(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  onStationSelected: (Station) -> Unit,
) {
  val cameraPositionState = rememberCameraPositionState()

  LaunchedEffect(userLocation, stations) {
    val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: return@LaunchedEffect
    cameraPositionState.move(
      CameraUpdateFactory.newLatLngZoom(
        LatLng(focusPoint.latitude, focusPoint.longitude),
        if (userLocation != null) 14f else 13f,
      ),
    )
  }

  GoogleMap(
    modifier = modifier,
    cameraPositionState = cameraPositionState,
    uiSettings = MapUiSettings(
      compassEnabled = false,
      mapToolbarEnabled = false,
      myLocationButtonEnabled = false,
      zoomControlsEnabled = false,
    ),
  ) {
    userLocation?.let { location ->
      Marker(
        state = remember(location) { MarkerState(position = LatLng(location.latitude, location.longitude)) },
        title = "Tu ubicación",
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
      )
    }
    stations.forEach { station ->
      Marker(
        state = remember(station.id) {
          MarkerState(position = LatLng(station.location.latitude, station.location.longitude))
        },
        title = station.name,
        snippet = "${station.bikesAvailable} bicis · ${station.slotsFree} libres",
        icon = BitmapDescriptorFactory.defaultMarker(
          if (station.id == highlightedStationId) BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_ROSE,
        ),
        onClick = {
          onStationSelected(station)
          false
        },
      )
    }
  }
}
