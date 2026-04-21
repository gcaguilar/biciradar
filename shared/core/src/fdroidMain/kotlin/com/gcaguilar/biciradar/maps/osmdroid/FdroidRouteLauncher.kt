package com.gcaguilar.biciradar.maps.osmdroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.Station

/**
 * F-Droid compliant RouteLauncher that uses geo URIs instead of Google Maps
 * for launching external navigation apps (like OSMAnd, Maps.ME, etc.)
 */
class FdroidRouteLauncher(
  private val context: Context,
) : RouteLauncher {
  override fun launch(station: Station) {
    val geoUri =
      Uri.parse(
        "geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}",
      )
    launchGeoUri(geoUri)
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    launchGeoUri(Uri.parse("geo:${destination.latitude},${destination.longitude}"))
  }

  override fun launchBikeToLocation(destination: GeoPoint) {
    launchGeoUri(Uri.parse("geo:${destination.latitude},${destination.longitude}?mode=b"))
  }

  private fun launchGeoUri(geoUri: Uri) {
    val intent =
      Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    if (intent.resolveActivity(context.packageManager) != null) {
      context.startActivity(intent)
    }
  }
}
