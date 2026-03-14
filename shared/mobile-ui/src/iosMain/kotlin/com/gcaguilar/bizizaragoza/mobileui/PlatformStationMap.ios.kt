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
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMapView
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKPointAnnotation
import platform.UIKit.UIColor
import platform.UIKit.UITapGestureRecognizer
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
  onMapClick: ((GeoPoint) -> Unit)?,
  pinLocation: GeoPoint?,
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
      onMapClick = onMapClick,
      pinLocation = pinLocation,
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
  onMapClick: ((GeoPoint) -> Unit)?,
  pinLocation: GeoPoint?,
) {
  val coordinator = remember { IOSStationMapCoordinator() }.apply {
    selectionHandler = onStationSelected
    mapClickHandler = onMapClick
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
        pinLocation = pinLocation,
      )
    },
  )
}

@OptIn(ExperimentalForeignApi::class)
private fun stationMarkerColor(station: Station, highlighted: Boolean): UIColor = when {
  station.bikesAvailable > 0 && station.slotsFree > 0 ->
    if (highlighted) UIColor.colorWithRed(0.10, 0.50, 0.10, 1.0)  // dark green
    else UIColor.colorWithRed(0.20, 0.72, 0.20, 1.0)              // green
  station.bikesAvailable == 0 && station.slotsFree == 0 ->
    if (highlighted) UIColor.colorWithRed(0.66, 0.08, 0.10, 1.0)  // dark red
    else UIColor.colorWithRed(0.84, 0.10, 0.12, 1.0)              // red
  else ->
    if (highlighted) UIColor.colorWithRed(0.70, 0.35, 0.00, 1.0)  // dark orange
    else UIColor.colorWithRed(0.95, 0.50, 0.00, 1.0)              // orange
}

@OptIn(ExperimentalForeignApi::class)
private class IOSStationMapCoordinator {
  var selectionHandler: (Station) -> Unit = {}
  var mapClickHandler: ((GeoPoint) -> Unit)? = null
  var highlightedStationId: String? = null
  private var hasZoomed = false
  private var lastStations: List<Station> = emptyList()
  private var lastHighlightedStationId: String? = null
  private var pinAnnotation: MKPointAnnotation? = null

  private val stationAnnotations = mutableMapOf<MKPointAnnotation, Station>()
  // Reverse map to update annotation views by station id
  private val annotationByStationId = mutableMapOf<String, MKPointAnnotation>()
  private val delegate = StationMapDelegate(
    stationForAnnotation = { annotation -> stationAnnotations[annotation] },
    highlightedStationId = { highlightedStationId },
    onStationSelected = { station -> selectionHandler(station) },
  )
  private val tapRecognizer = MapTapRecognizer { point ->
    mapClickHandler?.invoke(point)
  }

  val mapView = MKMapView().apply {
    rotateEnabled = false
    pitchEnabled = false
    delegate = this@IOSStationMapCoordinator.delegate
    addGestureRecognizer(tapRecognizer.recognizer)
  }

  fun update(
    mapView: MKMapView,
    stations: List<Station>,
    userLocation: GeoPoint?,
    highlightedStationId: String?,
    pinLocation: GeoPoint?,
  ) {
    this.highlightedStationId = highlightedStationId
    tapRecognizer.mapView = mapView

    // Zoom to user location only on first load
    if (!hasZoomed) {
      val focusPoint = userLocation ?: stations.firstOrNull()?.location
      if (focusPoint != null) {
        mapView.setRegion(
          MKCoordinateRegionMakeWithDistance(
            CLLocationCoordinate2DMake(focusPoint.latitude, focusPoint.longitude),
            1_200.0,
            1_200.0,
          ),
          animated = false,
        )
        hasZoomed = true
      }
    }

    // Use native blue dot for user location
    mapView.showsUserLocation = userLocation != null

    val stationsChanged = stations.map { it.id } != lastStations.map { it.id } ||
      stations.zip(lastStations).any { (a, b) ->
        a.bikesAvailable != b.bikesAvailable || a.slotsFree != b.slotsFree
      }

    if (stationsChanged) {
      // Full redraw: station list or availability changed
      stationAnnotations.clear()
      annotationByStationId.clear()
      // Remove only station annotations (keep pin if present)
      val toRemove = mapView.annotations.filterIsInstance<MKPointAnnotation>().filter { it != pinAnnotation }
      mapView.removeAnnotations(toRemove)

      stations.forEach { station ->
        val annotation = MKPointAnnotation().apply {
          setCoordinate(CLLocationCoordinate2DMake(station.location.latitude, station.location.longitude))
          setTitle(station.name)
          setSubtitle("${station.bikesAvailable} bicis · ${station.slotsFree} libres")
        }
        stationAnnotations[annotation] = station
        annotationByStationId[station.id] = annotation
        mapView.addAnnotation(annotation)
      }
      lastStations = stations
    } else if (highlightedStationId != lastHighlightedStationId) {
      // Only selection changed — update marker colors in-place without removing annotations
      val affectedIds = listOfNotNull(lastHighlightedStationId, highlightedStationId)
      affectedIds.forEach { stationId ->
        val annotation = annotationByStationId[stationId] ?: return@forEach
        val station = lastStations.firstOrNull { it.id == stationId } ?: return@forEach
        val annotationView = mapView.viewForAnnotation(annotation) as? MKMarkerAnnotationView ?: return@forEach
        annotationView.markerTintColor = stationMarkerColor(station, highlighted = stationId == highlightedStationId)
      }
    }

    lastHighlightedStationId = highlightedStationId

    // Update destination pin
    val currentPin = pinAnnotation
    if (pinLocation != null) {
      if (currentPin != null) {
        currentPin.setCoordinate(CLLocationCoordinate2DMake(pinLocation.latitude, pinLocation.longitude))
      } else {
        val newPin = MKPointAnnotation().apply {
          setCoordinate(CLLocationCoordinate2DMake(pinLocation.latitude, pinLocation.longitude))
          setTitle("Destino")
        }
        pinAnnotation = newPin
        mapView.addAnnotation(newPin)
      }
    } else if (currentPin != null) {
      mapView.removeAnnotation(currentPin)
      pinAnnotation = null
    }
  }
}

/** Wraps a UITapGestureRecognizer to forward taps on the map to a GeoPoint handler. */
@OptIn(ExperimentalForeignApi::class)
private class MapTapRecognizer(
  private val onTap: (GeoPoint) -> Unit,
) : NSObject() {
  var mapView: MKMapView? = null

  val recognizer: UITapGestureRecognizer = UITapGestureRecognizer(
    target = this,
    action = platform.objc.sel_registerName("handleTap:"),
  )

  @Suppress("unused")
  fun handleTap(recognizer: UITapGestureRecognizer) {
    val mv = mapView ?: return
    val touchPoint = recognizer.locationInView(mv)
    val coordinate = mv.convertPoint(touchPoint, toCoordinateFromView = mv)
    coordinate.useContents {
      onTap(GeoPoint(latitude = latitude, longitude = longitude))
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
        station.id == highlightedStationId() -> stationMarkerColor(station, highlighted = true)
        else -> stationMarkerColor(station, highlighted = false)
      }
    }
  }

  @ObjCSignatureOverride
  override fun mapView(mapView: MKMapView, didSelectAnnotation: MKAnnotationProtocol) {
    val pointAnnotation = didSelectAnnotation as? MKPointAnnotation ?: return
    stationForAnnotation(pointAnnotation)?.let(onStationSelected)
  }
}


