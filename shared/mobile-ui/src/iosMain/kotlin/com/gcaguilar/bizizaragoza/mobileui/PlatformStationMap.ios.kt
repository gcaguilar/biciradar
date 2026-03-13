package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMapView
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIColor
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun PlatformStationMap(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  isMapReady: Boolean,
  onStationSelected: (Station) -> Unit,
) {
  val factory = LocalStationMapViewFactory.current

  if (isMapReady && factory != null) {
    val view = remember { factory.createView() }
    UIKitView(
      modifier = modifier,
      factory = { view },
      properties = UIKitInteropProperties(),
      update = {
        factory.updateView(
          view = it,
          stations = stations,
          userLocation = userLocation,
          highlightedStationId = highlightedStationId,
          onStationSelected = onStationSelected,
        )
      },
    )
  } else {
    AppleMapKitView(
      modifier = modifier,
      stations = stations,
      userLocation = userLocation,
      highlightedStationId = highlightedStationId,
      onStationSelected = onStationSelected,
    )
  }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
private fun AppleMapKitView(
  modifier: Modifier,
  stations: List<Station>,
  userLocation: GeoPoint?,
  highlightedStationId: String?,
  onStationSelected: (Station) -> Unit,
) {
  val coordinator = remember { IOSStationMapCoordinator() }.apply {
    selectionHandler = onStationSelected
  }

  UIKitView(
    modifier = modifier,
    factory = {
      coordinator.mapView
    },
    properties = UIKitInteropProperties(),
    update = { mapView ->
      coordinator.update(
        mapView = mapView,
        stations = stations,
        userLocation = userLocation,
        highlightedStationId = highlightedStationId,
      )
    },
  )
}

@OptIn(ExperimentalForeignApi::class)
private class IOSStationMapCoordinator {
  var selectionHandler: (Station) -> Unit = {}
  var highlightedStationId: String? = null

  private val stationAnnotations = mutableMapOf<MKPointAnnotation, Station>()
  private val delegate = StationMapDelegate(
    stationForAnnotation = { annotation -> stationAnnotations[annotation] },
    highlightedStationId = { highlightedStationId },
    onStationSelected = { station -> selectionHandler(station) },
  )

  val mapView = MKMapView().apply {
    rotateEnabled = false
    pitchEnabled = false
    delegate = this@IOSStationMapCoordinator.delegate
  }

  fun update(
    mapView: MKMapView,
    stations: List<Station>,
    userLocation: GeoPoint?,
    highlightedStationId: String?,
  ) {
    this.highlightedStationId = highlightedStationId
    stationAnnotations.clear()
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
      val annotation = MKPointAnnotation().apply {
        setCoordinate(CLLocationCoordinate2DMake(station.location.latitude, station.location.longitude))
        setTitle(station.name)
        setSubtitle("${station.bikesAvailable} bicis · ${station.slotsFree} libres")
      }
      stationAnnotations[annotation] = station
      mapView.addAnnotation(annotation)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private class StationMapDelegate(
  private val stationForAnnotation: (MKPointAnnotation) -> Station?,
  private val highlightedStationId: () -> String?,
  private val onStationSelected: (Station) -> Unit,
) : NSObject(), MKMapViewDelegateProtocol {
  @ObjCSignatureOverride
  override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): platform.MapKit.MKAnnotationView? {
    val pointAnnotation = viewForAnnotation as? MKPointAnnotation ?: return null
    val station = stationForAnnotation(pointAnnotation)
    return MKMarkerAnnotationView(annotation = pointAnnotation, reuseIdentifier = "bizi.station").apply {
      canShowCallout = true
      markerTintColor = when {
        station == null -> UIColor.blueColor
        station.id == highlightedStationId() -> UIColor.colorWithRed(
          red = 0.66,
          green = 0.08,
          blue = 0.10,
          alpha = 1.0,
        )
        else -> UIColor.colorWithRed(
          red = 0.84,
          green = 0.10,
          blue = 0.12,
          alpha = 1.0,
        )
      }
    }
  }

  @ObjCSignatureOverride
  override fun mapView(mapView: MKMapView, didSelectAnnotation: MKAnnotationProtocol) {
    val pointAnnotation = didSelectAnnotation as? MKPointAnnotation ?: return
    stationForAnnotation(pointAnnotation)?.let(onStationSelected)
  }
}

