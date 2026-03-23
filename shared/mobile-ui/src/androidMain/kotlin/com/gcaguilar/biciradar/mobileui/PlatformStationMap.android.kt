package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.MR
import com.gcaguilar.biciradar.core.Station
import com.google.android.gms.maps.model.CameraPosition
import dev.icerock.moko.resources.compose.localized
import dev.icerock.moko.resources.compose.stringResource
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.ResourceFormatted
import dev.icerock.moko.resources.desc.StringDesc
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
  onMapClick: ((GeoPoint) -> Unit)?,
  pinLocation: GeoPoint?,
  recenterRequestToken: Int,
) {
  if (!isMapReady) {
    Surface(modifier = modifier) {
      Text(
        text = stringResource(MR.strings.mapUnavailableGoogleApiKey),
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
  val mapProperties = remember(userLocation != null) {
    MapProperties(isMyLocationEnabled = userLocation != null)
  }
  val mapUiSettings = remember {
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
    cameraPositionState.position = CameraPosition.fromLatLngZoom(
      LatLng(focusPoint.latitude, focusPoint.longitude),
      if (userLocation != null) 15f else 13f,
    )
    hasZoomed = true
  }

  LaunchedEffect(recenterRequestToken) {
    if (recenterRequestToken == 0) return@LaunchedEffect
    val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: return@LaunchedEffect
    cameraPositionState.position = CameraPosition.fromLatLngZoom(
      LatLng(focusPoint.latitude, focusPoint.longitude),
      if (userLocation != null) 15f else 13f,
    )
  }

  GoogleMap(
    modifier = modifier,
    cameraPositionState = cameraPositionState,
    properties = mapProperties,
    uiSettings = mapUiSettings,
    onMapClick = if (onMapClick != null) {
      { latLng -> onMapClick(GeoPoint(latLng.latitude, latLng.longitude)) }
    } else null,
  ) {
    stations.forEach { station ->
      key(station.id) {
        val markerState = remember(station.location.latitude, station.location.longitude) {
          MarkerState(position = LatLng(station.location.latitude, station.location.longitude))
        }
        MarkerInfoWindowContent(
          state = markerState,
          title = station.name,
          snippet = StringDesc.ResourceFormatted(MR.strings.mapStationBikesFree, station.bikesAvailable, station.slotsFree).localized(),
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
              text = stringResource(MR.strings.mapStationBikesSlotsDistance, station.bikesAvailable, station.slotsFree, station.distanceMeters),
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
          state = remember(pinLocation.latitude, pinLocation.longitude) {
            MarkerState(position = LatLng(pinLocation.latitude, pinLocation.longitude))
          },
          title = StringDesc.Resource(MR.strings.destination).localized(),
          icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
        ) {}
      }
    }
  }
}
