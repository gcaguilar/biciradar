package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station

interface AndroidStationMapRenderer {
  @Composable
  fun Render(
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
  )
}

val LocalAndroidStationMapRenderer = compositionLocalOf<AndroidStationMapRenderer?> { null }
