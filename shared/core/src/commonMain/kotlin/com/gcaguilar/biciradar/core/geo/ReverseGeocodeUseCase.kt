package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.GoogleMapsApiKey
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.Logger
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
  private val logger: Logger = com.gcaguilar.biciradar.core.NoOpLogger,
) {
  @kotlin.concurrent.Volatile
  private var lastLocation: GeoPoint? = null

  @kotlin.concurrent.Volatile
  private var lastResult: GeoResult? = null

  /**
   * Returns the best [GeoResult] for [location], reusing the previous result
   * if the user hasn't moved more than [MIN_DISTANCE_METERS] from the last position.
   *
   * Uses [distanceBetween] from the geo package to avoid duplicating the Haversine formula.
   */
  suspend fun execute(location: GeoPoint): GeoResult? {
    val last = lastLocation
    if (last != null && distanceBetween(last, location) < MIN_DISTANCE_METERS) {
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
      logger.warn(
        "GeoReverse",
        "datosbizi reverse geocode failed without Google fallback: ${originalError::class.simpleName} ${originalError.message}",
        originalError,
      )
      return null
    }

    logger.warn(
      "GeoReverse",
      "Falling back to Google reverse geocode after ${originalError::class.simpleName}: ${originalError.message}",
      originalError,
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
    private const val MIN_DISTANCE_METERS = 50
  }
}
