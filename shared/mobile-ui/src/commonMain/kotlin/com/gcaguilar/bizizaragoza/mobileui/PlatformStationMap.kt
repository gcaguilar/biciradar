package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station

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
)
