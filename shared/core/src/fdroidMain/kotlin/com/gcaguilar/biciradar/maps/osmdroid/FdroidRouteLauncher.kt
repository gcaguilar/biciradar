package com.gcaguilar.biciradar.maps.osmdroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.PlatformBindings.Station
import com.gcaguilar.biciradar.core.RouteLauncher

/**
 * F-Droid compliant RouteLauncher that uses geo URIs instead of Google Maps
 * for launching external navigation apps (like OSMAnd, Maps.ME, etc.)
 */
class FdroidRouteLauncher(
    private val context: Context,
) : RouteLauncher {
    override fun launch(station: Station) {
        // Create a geo URI with query parameter for the station name
        val geoUri = Uri.parse("geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}")
        launchGeoUri(geoUri)
    }

    override fun launchWalkToLocation(destination: GeoPoint) {
        // For walking, we could add mode=w parameter but most apps interpret geo URIs as walking by default
        val geoUri = Uri.parse("geo:${destination.latitude},${destination.longitude}")
        launchGeoUri(geoUri)
    }

    override fun launchBikeToLocation(destination: GeoPoint) {
        // For biking, we could add mode=b parameter
        val geoUri = Uri.parse("geo:${destination.latitude},${destination.longitude}?mode=b")
        launchGeoUri(geoUri)
    }

    private fun launchGeoUri(geoUri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // Verify there's an app that can handle the geo URI
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
        // If no app can handle it, we silently fail (could show a toast in a real implementation)
    }
}