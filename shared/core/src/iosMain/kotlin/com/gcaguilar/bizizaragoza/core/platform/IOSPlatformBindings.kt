package com.gcaguilar.bizizaragoza.core.platform

import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.BiziHttpClientFactory
import com.gcaguilar.bizizaragoza.core.DefaultAssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.LocationProvider
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StorageDirectoryProvider
import com.gcaguilar.bizizaragoza.core.WatchSyncBridge
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.MapKit.MKLaunchOptionsDirectionsModeDriving
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.UIKit.UIApplication

class IOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val httpClientFactory: BiziHttpClientFactory = IOSHttpClientFactory()
  override val locationProvider: LocationProvider = IOSLocationProvider()
  override val routeLauncher: RouteLauncher = IOSRouteLauncher()
  override val storageDirectoryProvider: StorageDirectoryProvider = IOSStorageDirectoryProvider()
  override val watchSyncBridge: WatchSyncBridge = IOSWatchSyncBridge()
}

private class IOSHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient = HttpClient(Darwin) {
    expectSuccess = true
    install(ContentNegotiation) {
      json(json)
    }
  }
}

private class IOSStorageDirectoryProvider : StorageDirectoryProvider {
  override val rootPath: String = "${NSHomeDirectory()}/Documents/bizi"
}

@OptIn(ExperimentalForeignApi::class)
private class IOSLocationProvider : LocationProvider {
  private val locationManager = CLLocationManager()

  override suspend fun currentLocation(): GeoPoint? {
    locationManager.requestWhenInUseAuthorization()
    return locationManager.location?.coordinate?.useContents {
      GeoPoint(latitude = latitude, longitude = longitude)
    }
  }
}

@OptIn(ExperimentalForeignApi::class)
private class IOSRouteLauncher : RouteLauncher {
  override fun launch(station: Station) {
    val mapItem = MKMapItem(
      placemark = MKPlacemark(
        coordinate = station.location.toCoordinate(),
        addressDictionary = null,
      ),
    ).apply {
      name = station.name
    }

    mapItem.openInMapsWithLaunchOptions(
      mapOf(
        MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeDriving,
      ),
    )

    val fallbackUrl = NSURL.URLWithString(
      "http://maps.apple.com/?daddr=${station.location.latitude},${station.location.longitude}&q=${station.name}",
    )
    if (fallbackUrl != null && UIApplication.sharedApplication.canOpenURL(fallbackUrl)) {
      UIApplication.sharedApplication.openURL(fallbackUrl)
    }
  }
}

private class IOSWatchSyncBridge : WatchSyncBridge {
  override suspend fun pushFavoriteIds(favoriteIds: Set<String>) = Unit

  override suspend fun latestFavoriteIds(): Set<String>? = null
}

@OptIn(ExperimentalForeignApi::class)
private fun GeoPoint.toCoordinate() = CLLocationCoordinate2DMake(latitude, longitude)
