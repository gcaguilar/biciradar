package com.gcaguilar.biciradar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.AndroidStationMapRenderer
import com.gcaguilar.biciradar.mobileui.BiziDataColors
import com.gcaguilar.biciradar.mobileui.EnvironmentalOverlayData
import com.gcaguilar.biciradar.mobileui.EnvironmentalOverlayLayer
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.util.GeoPoint as OsmGeoPoint

class FdroidAndroidStationMapRendererProvider : AndroidStationMapRenderer {
  @Composable
  override fun Render(
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
  ) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    remember(context) { configureOsmdroid(context.applicationContext) }
    var hasZoomed by remember { mutableStateOf(false) }
    var lastHandledRecenterToken by remember { mutableIntStateOf(0) }
    val mapView =
      remember(context) {
        MapView(context).apply {
          setTileSource(TileSourceFactory.MAPNIK)
          setMultiTouchControls(true)
          setUseDataConnection(true)
          setBuiltInZoomControls(false)
          minZoomLevel = 4.0
          maxZoomLevel = 19.0
        }
      }

    LaunchedEffect(userLocation, stations) {
      if (hasZoomed) return@LaunchedEffect
      val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: pinLocation ?: return@LaunchedEffect
      mapView.controller.setZoom(if (userLocation != null) 15.0 else 13.0)
      mapView.controller.setCenter(focusPoint.toOsmGeoPoint())
      hasZoomed = true
    }

    LaunchedEffect(recenterRequestToken, userLocation, stations, pinLocation) {
      if (recenterRequestToken == 0) return@LaunchedEffect
      if (recenterRequestToken == lastHandledRecenterToken) return@LaunchedEffect
      val focusPoint = userLocation ?: stations.firstOrNull()?.location ?: pinLocation ?: return@LaunchedEffect
      mapView.controller.animateTo(focusPoint.toOsmGeoPoint())
      mapView.controller.setZoom(if (userLocation != null) 15.0 else 13.0)
      lastHandledRecenterToken = recenterRequestToken
    }

    DisposableEffect(mapView, lifecycleOwner) {
      val observer =
        LifecycleEventObserver { _, event ->
          when (event) {
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            else -> Unit
          }
        }
      lifecycleOwner.lifecycle.addObserver(observer)
      onDispose {
        lifecycleOwner.lifecycle.removeObserver(observer)
        mapView.onPause()
        mapView.onDetach()
      }
    }

    AndroidView(
      modifier = modifier,
      factory = { mapView },
      update = { view ->
        view.overlays.clear()

        if (onMapClick != null) {
          view.overlays +=
            MapEventsOverlay(
              object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: OsmGeoPoint): Boolean {
                  onMapClick(GeoPoint(latitude = p.latitude, longitude = p.longitude))
                  return true
                }

                override fun longPressHelper(p: OsmGeoPoint): Boolean = false
              },
            )
        }

        environmentalOverlay?.zones?.forEach { zone ->
          val tone =
            when {
              environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality &&
                zone.value <= 50 -> BiziDataColors.AqiGood
              environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality &&
                zone.value <= 100 -> BiziDataColors.AqiModerate
              environmentalOverlay.layer == EnvironmentalOverlayLayer.AirQuality ->
                BiziDataColors.AqiBad
              environmentalOverlay.layer == EnvironmentalOverlayLayer.Pollen &&
                zone.value <= 10 -> BiziDataColors.PollenLow
              environmentalOverlay.layer == EnvironmentalOverlayLayer.Pollen &&
                zone.value <= 30 -> BiziDataColors.PollenMedium
              else -> BiziDataColors.PollenHigh
            }
          view.overlays +=
            Polygon().apply {
              points = Polygon.pointsAsCircle(zone.center.toOsmGeoPoint(), 450.0)
              fillColor = tone.copy(alpha = 0.22f).toArgb()
              strokeColor = tone.copy(alpha = 0.45f).toArgb()
              strokeWidth = 2f
            }
        }

        userLocation?.let { location ->
          view.overlays +=
            Marker(view).apply {
              position = location.toOsmGeoPoint()
              title = "Tu ubicación"
              setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
              icon = circularMarkerDrawable(context, sizeDp = 18, fillColor = BiziDataColors.AqiGood.toArgb())
            }
        }

        stations.forEach { station ->
          view.overlays +=
            Marker(view).apply {
              position = station.location.toOsmGeoPoint()
              title = station.name
              subDescription = stationSnippet(station)
              setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
              icon =
                circularMarkerDrawable(
                  context = context,
                  sizeDp = if (station.id == highlightedStationId) 34 else 28,
                  fillColor = stationMarkerColor(station).toArgb(),
                  label = station.bikesAvailable.toString(),
                )
              setOnMarkerClickListener { _, _ ->
                onStationSelected(station)
                true
              }
            }
        }

        pinLocation?.let { destination ->
          view.overlays +=
            Marker(view).apply {
              position = destination.toOsmGeoPoint()
              title = pinTitle
              setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
              icon = circularMarkerDrawable(context, sizeDp = 30, fillColor = BiziDataColors.PollenLow.toArgb())
            }
        }

        view.invalidate()
      },
    )
  }

  private fun stationMarkerColor(station: Station) =
    when {
      station.ebikesAvailable > 0 && station.regularBikesAvailable == 0 -> BiziDataColors.PollenMedium
      station.regularBikesAvailable > 0 && station.ebikesAvailable == 0 -> BiziDataColors.PollenHigh
      station.bikesAvailable > 0 && station.slotsFree > 0 -> BiziDataColors.AqiGood
      station.bikesAvailable > 0 && station.slotsFree == 0 -> BiziDataColors.PollenLow
      station.bikesAvailable == 0 && station.slotsFree > 0 -> BiziDataColors.AqiBad
      else -> BiziDataColors.AqiBad
    }

  private fun GeoPoint.toOsmGeoPoint(): OsmGeoPoint = OsmGeoPoint(latitude, longitude)

  private fun circularMarkerDrawable(
    context: Context,
    sizeDp: Int,
    fillColor: Int,
    label: String? = null,
  ): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt().coerceAtLeast(24)
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val circlePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = fillColor
        style = Paint.Style.FILL
      }
    val strokePaint =
      Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.STROKE
        strokeWidth = sizePx * 0.10f
      }
    val radius = (sizePx / 2f) - strokePaint.strokeWidth
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, circlePaint)
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, radius, strokePaint)

    if (!label.isNullOrBlank()) {
      val textPaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = 0xFFFFFFFF.toInt()
          textAlign = Paint.Align.CENTER
          textSize = sizePx * if (label.length >= 2) 0.42f else 0.5f
          typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }
      val bounds = Rect()
      textPaint.getTextBounds(label, 0, label.length, bounds)
      val baseline = (sizePx / 2f) - bounds.exactCenterY()
      canvas.drawText(label, sizePx / 2f, baseline, textPaint)
    }

    return BitmapDrawable(context.resources, bitmap)
  }

  private fun configureOsmdroid(context: Context) {
    val configuration = Configuration.getInstance()
    val preferences = context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
    val basePath = context.cacheDir.resolve("osmdroid").apply { mkdirs() }
    val tileCache = basePath.resolve("tiles").apply { mkdirs() }
    configuration.load(context, preferences)
    configuration.userAgentValue = context.packageName
    configuration.osmdroidBasePath = basePath
    configuration.osmdroidTileCache = tileCache
  }
}
