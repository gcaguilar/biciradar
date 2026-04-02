@file:OptIn(ExperimentalForeignApi::class)

package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station


import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKCircle
import platform.MapKit.MKCircleRenderer
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKMapView
import platform.MapKit.MKMarkerAnnotationView
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
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
  recenterRequestToken: Int,
  environmentalOverlay: EnvironmentalOverlayData?,
  stationSnippet: (Station) -> String,
  pinTitle: String,
) {
  val factory = LocalStationMapViewFactory.current
  val isDarkTheme = LocalIsDarkTheme.current

  if (isMapReady && factory != null) {
    val view = remember { factory.createView() }
    UIKitView(
      modifier = modifier,
      factory = { view },
      update = {
        factory.updateView(
          view = it,
          stations = stations,
          userLocation = userLocation,
          highlightedStationId = highlightedStationId,
          isDarkTheme = isDarkTheme,
          onStationSelected = onStationSelected,
          recenterRequestToken = recenterRequestToken,
          environmentalOverlay = environmentalOverlay,
        )
      },
    )
  } else {
    AppleMapKitView(
      modifier = modifier,
      stations = stations,
      userLocation = userLocation,
      highlightedStationId = highlightedStationId,
      isDarkTheme = isDarkTheme,
      onStationSelected = onStationSelected,
      onMapClick = onMapClick,
      pinLocation = pinLocation,
      recenterRequestToken = recenterRequestToken,
      environmentalOverlay = environmentalOverlay,
      stationSnippet = stationSnippet,
      pinTitle = pinTitle,
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
  isDarkTheme: Boolean,
  onStationSelected: (Station) -> Unit,
  onMapClick: ((GeoPoint) -> Unit)?,
  pinLocation: GeoPoint?,
  recenterRequestToken: Int,
  environmentalOverlay: EnvironmentalOverlayData?,
  stationSnippet: (Station) -> String,
  pinTitle: String,
) {
  val coordinator = remember { IOSStationMapCoordinator(stationSnippet = stationSnippet, pinTitle = pinTitle) }.apply {
    selectionHandler = onStationSelected
    mapClickHandler = onMapClick
  }

  UIKitView(
    modifier = modifier,
    factory = {
      coordinator.mapView
    },
    update = { mapView ->
      coordinator.update(
        mapView = mapView,
        stations = stations,
        userLocation = userLocation,
        highlightedStationId = highlightedStationId,
        isDarkTheme = isDarkTheme,
        pinLocation = pinLocation,
        recenterRequestToken = recenterRequestToken,
        environmentalOverlay = environmentalOverlay,
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
private class IOSStationMapCoordinator(
  private val stationSnippet: (Station) -> String = { "" },
  private val pinTitle: String = "Destino",
) {
  var selectionHandler: (Station) -> Unit = {}
  var mapClickHandler: ((GeoPoint) -> Unit)? = null
  var highlightedStationId: String? = null
  private var hasZoomed = false
  private var lastRecenterRequestToken = 0
  private var lastStations: List<Station> = emptyList()
  private var lastHighlightedStationId: String? = null
  private var pinAnnotation: MKPointAnnotation? = null
  private val environmentalOverlays = mutableListOf<MKCircle>()
  private val environmentalOverlayValues = mutableMapOf<MKCircle, Int>()
  private var currentEnvironmentalLayer: EnvironmentalOverlayLayer? = null

  private val stationAnnotations = mutableMapOf<MKPointAnnotation, Station>()
  // Reverse map to update annotation views by station id
  private val annotationByStationId = mutableMapOf<String, MKPointAnnotation>()
  private val delegate = StationMapDelegate(
    stationForAnnotation = { annotation -> stationAnnotations[annotation] },
    highlightedStationId = { highlightedStationId },
    currentEnvironmentalLayer = { currentEnvironmentalLayer },
    overlayValueForCircle = { circle -> environmentalOverlayValues[circle] },
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
    isDarkTheme: Boolean,
    pinLocation: GeoPoint?,
    recenterRequestToken: Int,
    environmentalOverlay: EnvironmentalOverlayData?,
  ) {
    this.highlightedStationId = highlightedStationId
    currentEnvironmentalLayer = environmentalOverlay?.layer
    tapRecognizer.mapView = mapView

    // Zoom to user location only on first load
    if (!hasZoomed) {
      val focusPoint = userLocation ?: stations.firstOrNull()?.location
      if (focusPoint != null) {
        centerMap(mapView, focusPoint, animated = false, userLocation != null)
        hasZoomed = true
      }
    }

    if (recenterRequestToken != lastRecenterRequestToken) {
      val focusPoint = userLocation ?: stations.firstOrNull()?.location
      if (focusPoint != null) {
        centerMap(mapView, focusPoint, animated = true, userLocation != null)
      }
      lastRecenterRequestToken = recenterRequestToken
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
          setSubtitle(stationSnippet(station))
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

    environmentalOverlays.forEach { overlay ->
      mapView.performSelector(
        aSelector = platform.objc.sel_registerName("removeOverlay:"),
        withObject = overlay,
      )
    }
    environmentalOverlays.clear()
    environmentalOverlayValues.clear()
    environmentalOverlay?.zones?.forEach { zone ->
      val circle = MKCircle.circleWithCenterCoordinate(
        coord = CLLocationCoordinate2DMake(zone.center.latitude, zone.center.longitude),
        radius = 450.0,
      )
      environmentalOverlays += circle
      environmentalOverlayValues[circle] = zone.value
      mapView.performSelector(
        aSelector = platform.objc.sel_registerName("addOverlay:"),
        withObject = circle,
      )
    }

    // Update destination pin
    val currentPin = pinAnnotation
    if (pinLocation != null) {
      if (currentPin != null) {
        currentPin.setCoordinate(CLLocationCoordinate2DMake(pinLocation.latitude, pinLocation.longitude))
      } else {
        val newPin = MKPointAnnotation().apply {
          setCoordinate(CLLocationCoordinate2DMake(pinLocation.latitude, pinLocation.longitude))
          setTitle(pinTitle)
        }
        pinAnnotation = newPin
        mapView.addAnnotation(newPin)
      }
    } else if (currentPin != null) {
      mapView.removeAnnotation(currentPin)
      pinAnnotation = null
    }
  }

  private fun centerMap(
    mapView: MKMapView,
    focusPoint: GeoPoint,
    animated: Boolean,
    prefersTightZoom: Boolean,
  ) {
    mapView.setRegion(
      MKCoordinateRegionMakeWithDistance(
        CLLocationCoordinate2DMake(focusPoint.latitude, focusPoint.longitude),
        if (prefersTightZoom) 900.0 else 1_200.0,
        if (prefersTightZoom) 900.0 else 1_200.0,
      ),
      animated = animated,
    )
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

  @OptIn(kotlinx.cinterop.BetaInteropApi::class)
  @ObjCAction
  @Suppress("unused")
  fun handleTap(recognizer: UITapGestureRecognizer) {
    val mv = mapView ?: return
    val touchPoint = recognizer.locationInView(mv)
    // Ignore taps that land on an annotation view (station pin / callout) so that
    // tapping a station triggers onStationSelected (via MKMapViewDelegate) rather
    // than also firing onMapClick here.
    val hitView = mv.hitTest(touchPoint, withEvent = null)
    if (hitView is platform.MapKit.MKAnnotationView ||
      hitView?.superview() is platform.MapKit.MKAnnotationView
    ) return
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
  private val currentEnvironmentalLayer: () -> EnvironmentalOverlayLayer?,
  private val overlayValueForCircle: (MKCircle) -> Int?,
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

  @ObjCSignatureOverride
  override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
    val circle = rendererForOverlay as? MKCircle ?: return MKOverlayRenderer(rendererForOverlay)
    val renderer = MKCircleRenderer(circle)
    val value = overlayValueForCircle(circle) ?: return renderer
    val tone = environmentalTone(
      layer = currentEnvironmentalLayer(),
      value = value,
    )
    renderer.fillColor = tone.colorWithAlphaComponent(0.22)
    renderer.strokeColor = tone.colorWithAlphaComponent(0.45)
    renderer.lineWidth = 1.0
    return renderer
  }
}

private fun environmentalTone(layer: EnvironmentalOverlayLayer?, value: Int): UIColor = when {
  layer == EnvironmentalOverlayLayer.AirQuality && value <= 50 -> UIColor.colorWithRed(0.20, 0.72, 0.20, 1.0)
  layer == EnvironmentalOverlayLayer.AirQuality && value <= 100 -> UIColor.colorWithRed(0.95, 0.50, 0.00, 1.0)
  layer == EnvironmentalOverlayLayer.AirQuality -> UIColor.colorWithRed(0.84, 0.10, 0.12, 1.0)
  layer == EnvironmentalOverlayLayer.Pollen && value <= 10 -> UIColor.colorWithRed(0.20, 0.72, 0.20, 1.0)
  layer == EnvironmentalOverlayLayer.Pollen && value <= 30 -> UIColor.colorWithRed(0.95, 0.50, 0.00, 1.0)
  else -> UIColor.colorWithRed(0.84, 0.10, 0.12, 1.0)
}
