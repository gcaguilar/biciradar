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
import platform.Foundation.NSHomeDirectory

class WatchOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val httpClientFactory: BiziHttpClientFactory = WatchOSHttpClientFactory()
  override val locationProvider: LocationProvider = WatchOSLocationProvider()
  override val routeLauncher: RouteLauncher = WatchOSRouteLauncher()
  override val storageDirectoryProvider: StorageDirectoryProvider = WatchOSStorageDirectoryProvider()
  override val watchSyncBridge: WatchSyncBridge = WatchOSSyncBridge()
}

private class WatchOSHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient = HttpClient(Darwin) {
    expectSuccess = true
    install(ContentNegotiation) {
      json(json)
    }
  }
}

private class WatchOSStorageDirectoryProvider : StorageDirectoryProvider {
  override val rootPath: String = "${NSHomeDirectory()}/Documents/bizi"
}

@OptIn(ExperimentalForeignApi::class)
private class WatchOSLocationProvider : LocationProvider {
  private val locationManager = CLLocationManager()

  override suspend fun currentLocation(): GeoPoint? {
    locationManager.requestWhenInUseAuthorization()
    return locationManager.location?.coordinate?.useContents {
      GeoPoint(latitude = latitude, longitude = longitude)
    }
  }
}

private class WatchOSRouteLauncher : RouteLauncher {
  override fun launch(station: Station) = Unit
}

private class WatchOSSyncBridge : WatchSyncBridge {
  override suspend fun pushFavoriteIds(favoriteIds: Set<String>) = Unit

  override suspend fun latestFavoriteIds(): Set<String>? = null
}
