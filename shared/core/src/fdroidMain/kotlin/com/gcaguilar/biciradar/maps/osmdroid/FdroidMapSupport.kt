package com.gcaguilar.biciradar.maps.osmdroid

import android.content.Context
import com.gcaguilar.biciradar.core.EmbeddedMapProvider
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.MapSupportStatus

/**
 * F-Droid compliant MapSupport implementation with no embedded map provider.
 * External navigation is supported without linking Google Maps.
 */
class FdroidMapSupport(
    private val context: Context,
) : MapSupport {
    override fun currentStatus(): MapSupportStatus {
        return MapSupportStatus(
            embeddedProvider = EmbeddedMapProvider.None,
            googleMapsSdkLinked = false,
            googleMapsApiKeyConfigured = false
        )
    }
}
