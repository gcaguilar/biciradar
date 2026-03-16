package com.gcaguilar.bizizaragoza.core.geo

import com.gcaguilar.bizizaragoza.core.GeoPoint
import dev.zacsweers.metro.Inject

/**
 * Wraps [GeoApi.reverseGeocode] with a simple position-change guard.
 *
 * Reverse geocoding is only repeated if the user has moved more than
 * [MIN_DISTANCE_METERS] from the last successfully reverse-geocoded position,
 * preventing a flood of requests when the device is slowly drifting.
 */
@Inject
class ReverseGeocodeUseCase(
    private val geoApi: GeoApi,
) {
    @kotlin.concurrent.Volatile
    private var lastLocation: GeoPoint? = null

    @kotlin.concurrent.Volatile
    private var lastResult: GeoResult? = null

    /**
     * Returns the best [GeoResult] for [location], reusing the previous result
     * if the user hasn't moved more than [MIN_DISTANCE_METERS].
     */
    suspend fun execute(location: GeoPoint): GeoResult? {
        val last = lastLocation
        if (last != null && haversineMeters(last, location) < MIN_DISTANCE_METERS) {
            return lastResult
        }
        val result = geoApi.reverseGeocode(location)
        lastLocation = location
        lastResult = result
        return result
    }

    companion object {
        private const val MIN_DISTANCE_METERS = 50.0
    }
}

// Haversine formula — reuse of distanceBetween from BiziApi.kt is not possible
// here because that function is private. A local copy is acceptable.
private fun haversineMeters(a: GeoPoint, b: GeoPoint): Double {
    val r = 6_371_000.0
    val dLat = (b.latitude - a.latitude) * kotlin.math.PI / 180.0
    val dLon = (b.longitude - a.longitude) * kotlin.math.PI / 180.0
    val sinDLat = kotlin.math.sin(dLat / 2)
    val sinDLon = kotlin.math.sin(dLon / 2)
    val h = sinDLat * sinDLat +
        kotlin.math.cos(a.latitude * kotlin.math.PI / 180.0) *
        kotlin.math.cos(b.latitude * kotlin.math.PI / 180.0) *
        sinDLon * sinDLon
    return 2 * r * kotlin.math.asin(kotlin.math.sqrt(h))
}
