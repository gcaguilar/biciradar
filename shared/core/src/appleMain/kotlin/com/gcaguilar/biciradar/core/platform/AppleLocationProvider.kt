package com.gcaguilar.biciradar.core.platform

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocationProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
internal class AppleLocationProvider : LocationProvider {
  private val locationManager = CLLocationManager()
  private var pendingContinuation: CancellableContinuation<GeoPoint?>? = null
  private val delegate = Delegate(this)

  init {
    locationManager.delegate = delegate
    locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters
  }

  override suspend fun currentLocation(): GeoPoint? =
    suspendCancellableCoroutine { continuation ->
      pendingContinuation?.resume(null)
      pendingContinuation = continuation
      continuation.invokeOnCancellation {
        if (pendingContinuation === continuation) {
          pendingContinuation = null
        }
      }

      when (locationManager.authorizationStatus) {
        kCLAuthorizationStatusAuthorizedAlways,
        kCLAuthorizationStatusAuthorizedWhenInUse,
        -> requestOrReturnCachedLocation()
        kCLAuthorizationStatusNotDetermined -> locationManager.requestWhenInUseAuthorization()
        kCLAuthorizationStatusDenied,
        kCLAuthorizationStatusRestricted,
        -> finish(null)
        else -> locationManager.requestWhenInUseAuthorization()
      }
    }

  private fun handleAuthorizationChange(manager: CLLocationManager) {
    when (manager.authorizationStatus) {
      kCLAuthorizationStatusAuthorizedAlways,
      kCLAuthorizationStatusAuthorizedWhenInUse,
      -> requestOrReturnCachedLocation()
      kCLAuthorizationStatusDenied,
      kCLAuthorizationStatusRestricted,
      -> finish(null)
      else -> Unit
    }
  }

  private fun handleUpdatedLocations(
    manager: CLLocationManager,
    didUpdateLocations: List<*>,
  ) {
    val latestLocation = didUpdateLocations.lastOrNull() as? CLLocation
    finish(latestLocation?.toGeoPoint() ?: manager.location?.toGeoPoint())
  }

  private fun handleLocationFailure(
    manager: CLLocationManager,
    didFailWithError: NSError,
  ) {
    finish(manager.location?.toGeoPoint())
  }

  private fun requestOrReturnCachedLocation() {
    val cachedLocation = locationManager.location?.toGeoPoint()
    if (cachedLocation != null) {
      finish(cachedLocation)
    } else {
      locationManager.requestLocation()
    }
  }

  private fun finish(location: GeoPoint?) {
    val continuation = pendingContinuation ?: return
    pendingContinuation = null
    continuation.resume(location)
  }

  private class Delegate(
    private val provider: AppleLocationProvider,
  ) : NSObject(),
    CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
      provider.handleAuthorizationChange(manager)
    }

    override fun locationManager(
      manager: CLLocationManager,
      didUpdateLocations: List<*>,
    ) {
      provider.handleUpdatedLocations(manager, didUpdateLocations)
    }

    override fun locationManager(
      manager: CLLocationManager,
      didFailWithError: NSError,
    ) {
      provider.handleLocationFailure(manager, didFailWithError)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun CLLocation.toGeoPoint(): GeoPoint =
  coordinate.useContents {
    GeoPoint(latitude = latitude, longitude = longitude)
  }

@OptIn(ExperimentalForeignApi::class)
internal fun GeoPoint.toCoordinate() = CLLocationCoordinate2DMake(latitude, longitude)
