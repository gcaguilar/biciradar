package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.compositionLocalOf
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import platform.UIKit.UIView

interface StationMapViewFactory {
  fun createView(): UIView
  fun updateView(
    view: UIView,
    stations: List<Station>,
    userLocation: GeoPoint?,
    highlightedStationId: String?,
    onStationSelected: (Station) -> Unit,
    recenterRequestToken: Int,
  )
}

val LocalStationMapViewFactory = compositionLocalOf<StationMapViewFactory?> { null }
