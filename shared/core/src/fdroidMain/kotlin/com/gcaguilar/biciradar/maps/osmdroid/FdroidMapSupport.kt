package com.gcaguilar.biciradar.maps.osmdroid

import android.content.Context
import com.gcaguilar.biciradar.core.EmbeddedMapProvider
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.MapSupportStatus

/**
 * F-Droid compliant MapSupport implementation using OSMdroid
 * instead of Google Maps
 */
class FdroidMapSupport(
    private val context: Context,
) : MapSupport {
    override fun currentStatus(): MapSupportStatus {
        return MapSupportStatus(
            embeddedProvider = EmbeddedMapProvider.Osmdroid,
            googleMapsSdkLinked = false, // Not using Google Maps SDK
            googleMapsApiKeyConfigured = false // No API key needed for OSMdroid
        )
    }
}