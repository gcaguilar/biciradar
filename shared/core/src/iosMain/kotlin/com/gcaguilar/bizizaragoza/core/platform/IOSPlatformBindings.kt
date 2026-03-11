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
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSURL
import platform.MapKit.MKLaunchOptionsDirectionsModeDriving
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.UIKit.UIApplication
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationStateActivated

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

private class IOSLocationProvider : LocationProvider {
  private val delegate = AppleLocationProvider()

  override suspend fun currentLocation(): GeoPoint? = delegate.currentLocation()
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
  @OptIn(ExperimentalForeignApi::class)
  override suspend fun pushFavoriteIds(favoriteIds: Set<String>) {
    IOSFavoritesCache.persist(favoriteIds)
    val session = WCSession.defaultSession
    if (session.activationState != WCSessionActivationStateActivated) return
    memScoped {
      session.updateApplicationContext(
        mapOf(IOSFavoritesCache.contextKey to favoriteIds.toList()),
        error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>().ptr,
      )
    }
  }

  override suspend fun latestFavoriteIds(): Set<String>? = IOSFavoritesCache.read().takeIf { it.isNotEmpty() }
}

private object IOSFavoritesCache {
  const val cacheKey = "bizizaragoza.watch.favorite_ids"
  const val contextKey = "favorite_ids"

  fun read(): Set<String> = NSUserDefaults.standardUserDefaults.arrayForKey(cacheKey)
    .orEmpty()
    .filterIsInstance<String>()
    .toSet()

  fun persist(favoriteIds: Set<String>) {
    NSUserDefaults.standardUserDefaults.setObject(favoriteIds.toList(), forKey = cacheKey)
  }
}
