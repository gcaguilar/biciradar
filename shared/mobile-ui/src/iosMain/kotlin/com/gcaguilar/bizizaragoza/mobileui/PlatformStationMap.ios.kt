package com.gcaguilar.bizizaragoza.mobileui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.Station
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import platform.Foundation.NSProcessInfo
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
  onStationSelected: (Station) -> Unit,
) {
  if (isRunningOnSimulator()) {
    IOSStationMapFallback(
      modifier = modifier,
      stations = stations,
      highlightedStationId = highlightedStationId,
      onStationSelected = onStationSelected,
    )
    return
  }

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

@Composable
private fun IOSStationMapFallback(
  modifier: Modifier,
  stations: List<Station>,
  highlightedStationId: String?,
  onStationSelected: (Station) -> Unit,
) {
  val highlightedStation = stations.firstOrNull { it.id == highlightedStationId }
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF3F4F6))
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Mapa nativo no disponible en este simulador",
        style = MaterialTheme.typography.titleMedium,
        color = Color(0xFFD7191F),
      )
      highlightedStation?.let { station ->
        Text(
          text = "Estación destacada: ${station.name} · ${station.distanceMeters} m",
          style = MaterialTheme.typography.bodyMedium,
        )
      }
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(stations.take(6), key = { it.id }) { station ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onStationSelected(station) },
            colors = CardDefaults.cardColors(
              containerColor = if (station.id == highlightedStationId) {
                Color(0xFFD7191F).copy(alpha = 0.12f)
              } else {
                Color.White
              },
            ),
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
              Text(
                text = station.name,
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                text = "${station.bikesAvailable} bicis · ${station.slotsFree} libres · ${station.distanceMeters} m",
                style = MaterialTheme.typography.bodySmall,
              )
            }
          }
        }
      }
    }
  }
}

private fun isRunningOnSimulator(): Boolean = NSProcessInfo.processInfo.environment["SIMULATOR_DEVICE_NAME"] != null

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
      canShowCallout = false
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
