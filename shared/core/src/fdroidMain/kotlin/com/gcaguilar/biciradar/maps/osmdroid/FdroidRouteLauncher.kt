package com.gcaguilar.biciradar.maps.osmdroid

import android.content.ActivityNotFoundException
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
    launchFirstSupportedGeoUri(
      Uri.parse(
        "geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}",
      ),
      Uri.parse(
        "geo:0,0?q=${station.location.latitude},${station.location.longitude}(${Uri.encode(station.name)})",
      ),
    )
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    launchFirstSupportedIntent(
      navigationIntent(destination, mode = "w"),
      geoIntent(Uri.parse("geo:${destination.latitude},${destination.longitude}")),
      geoIntent(Uri.parse("geo:0,0?q=${destination.latitude},${destination.longitude}")),
    )
  }

  override fun launchBikeToLocation(destination: GeoPoint) {
    launchFirstSupportedIntent(
      navigationIntent(destination, mode = "b"),
      geoIntent(Uri.parse("geo:${destination.latitude},${destination.longitude}?mode=b")),
      geoIntent(Uri.parse("geo:${destination.latitude},${destination.longitude}")),
      geoIntent(Uri.parse("geo:0,0?q=${destination.latitude},${destination.longitude}")),
    )
  }

  private fun launchFirstSupportedGeoUri(vararg geoUris: Uri) {
    launchFirstSupportedIntent(*geoUris.map(::geoIntent).toTypedArray())
  }

  private fun launchFirstSupportedIntent(vararg intents: Intent) {
    intents.forEach { intent ->
      try {
        context.startActivity(intent)
        return
      } catch (_: ActivityNotFoundException) {
        // Try the next URI variant.
      } catch (_: SecurityException) {
        // Try the next URI variant.
      }
    }
  }

  private fun navigationIntent(
    destination: GeoPoint,
    mode: String,
  ): Intent =
    Intent(
      Intent.ACTION_VIEW,
      Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=$mode"),
    ).apply {
      setPackage("com.google.android.apps.maps")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

  private fun geoIntent(geoUri: Uri): Intent =
    Intent(Intent.ACTION_VIEW, geoUri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
