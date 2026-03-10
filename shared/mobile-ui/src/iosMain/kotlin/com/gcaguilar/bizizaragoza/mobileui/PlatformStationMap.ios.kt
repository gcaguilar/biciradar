package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformStationMap(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  onStationSelected: (Station) -> Unit,
) {
  UIKitView(
    modifier = modifier,
    factory = {
      MKMapView().apply {
        rotateEnabled = false
        pitchEnabled = false
      }
    },
    update = { mapView ->
      mapView.removeAnnotations(mapView.annotations)
      val focusPoint = userLocation ?: stations.firstOrNull()?.location
      if (focusPoint != null) {
        mapView.setRegion(
          MKCoordinateRegionMakeWithDistance(
            CLLocationCoordinate2DMake(focusPoint.latitude, focusPoint.longitude),
            1_500.0,
            1_500.0,
          ),
          animated = false,
        )
      }
      userLocation?.let { location ->
        mapView.addAnnotation(
          MKPointAnnotation().apply {
            setCoordinate(CLLocationCoordinate2DMake(location.latitude, location.longitude))
            setTitle("Tu ubicación")
          },
        )
      }
      stations.forEach { station ->
        mapView.addAnnotation(
          MKPointAnnotation().apply {
            setCoordinate(CLLocationCoordinate2DMake(station.location.latitude, station.location.longitude))
            setTitle(station.name)
            setSubtitle("${station.bikesAvailable} bicis · ${station.slotsFree} libres")
          },
        )
      }
    },
  )
}
