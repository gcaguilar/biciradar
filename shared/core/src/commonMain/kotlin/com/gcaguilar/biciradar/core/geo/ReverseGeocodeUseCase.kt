package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.GoogleMapsApiKey
import com.gcaguilar.biciradar.core.GooglePlacesApi
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException

/**
 * Wraps [GeoApi.reverseGeocode] with a simple position-change guard.
 *
 * Reverse geocoding is only repeated if the user has moved more than
 * [MIN_DISTANCE_METERS] from the last successfully reverse-geocoded position,
 * preventing a flood of requests when the device is slowly drifting.
 */
@SingleIn(AppScope::class)
@Inject
class ReverseGeocodeUseCase(
  private val geoApi: GeoApi,
  private val googlePlacesApi: GooglePlacesApi,
  @param:GoogleMapsApiKey private val googleMapsApiKey: String?,
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
    val result =
      try {
        geoApi.reverseGeocode(location)
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Throwable) {
        fallbackToGoogleReverseGeocode(location, error)
      }
    lastLocation = location
    lastResult = result
    return result
  }

  private suspend fun fallbackToGoogleReverseGeocode(
    location: GeoPoint,
    originalError: Throwable,
  ): GeoResult? {
    val apiKey = googleMapsApiKey?.takeIf { it.isNotBlank() }
    if (apiKey == null) {
      println(
        "[GeoReverse] datosbizi reverse geocode failed without Google fallback: ${originalError::class.simpleName} ${originalError.message}",
      )
      return null
    }

    println(
      "[GeoReverse] Falling back to Google reverse geocode after ${originalError::class.simpleName}: ${originalError.message}",
    )
    val formattedAddress = googlePlacesApi.reverseGeocode(location, apiKey) ?: return null
    val primaryLabel = formattedAddress.substringBefore(',').trim().ifBlank { formattedAddress }
    return GeoResult(
      id = "${location.latitude},${location.longitude}",
      name = primaryLabel,
      address = formattedAddress,
      latitude = location.latitude,
      longitude = location.longitude,
    )
  }

  companion object {
    private const val MIN_DISTANCE_METERS = 50.0
  }
}

// Haversine formula — reuse of distanceBetween from BiziApi.kt is not possible
// here because that function is private. A local copy is acceptable.
private fun haversineMeters(
  a: GeoPoint,
  b: GeoPoint,
): Double {
  val r = 6_371_000.0
  val dLat = (b.latitude - a.latitude) * kotlin.math.PI / 180.0
  val dLon = (b.longitude - a.longitude) * kotlin.math.PI / 180.0
  val sinDLat = kotlin.math.sin(dLat / 2)
  val sinDLon = kotlin.math.sin(dLon / 2)
  val h =
    sinDLat * sinDLat +
      kotlin.math.cos(a.latitude * kotlin.math.PI / 180.0) *
      kotlin.math.cos(b.latitude * kotlin.math.PI / 180.0) *
      sinDLon * sinDLon
  return 2 * r * kotlin.math.asin(kotlin.math.sqrt(h))
}
