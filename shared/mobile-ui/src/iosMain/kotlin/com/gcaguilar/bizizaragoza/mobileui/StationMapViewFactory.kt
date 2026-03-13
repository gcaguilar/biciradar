package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.compositionLocalOf
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station
import platform.UIKit.UIView

interface StationMapViewFactory {
  fun createView(): UIView
  fun updateView(
    view: UIView,
    stations: List<Station>,
    userLocation: GeoPoint?,
    highlightedStationId: String?,
    onStationSelected: (Station) -> Unit,
  )
}

val LocalStationMapViewFactory = compositionLocalOf<StationMapViewFactory?> { null }
