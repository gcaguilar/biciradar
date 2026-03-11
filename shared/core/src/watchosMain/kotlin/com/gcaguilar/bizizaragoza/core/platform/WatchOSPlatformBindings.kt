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
import kotlinx.cinterop.useContents
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.CoreLocation.CLLocationManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSUserDefaults
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationStateActivated

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
  @OptIn(ExperimentalForeignApi::class)
  override suspend fun pushFavoriteIds(favoriteIds: Set<String>) {
    WatchOSFavoritesCache.persist(favoriteIds)
    val session = WCSession.defaultSession
    if (session.activationState != WCSessionActivationStateActivated) return
    memScoped {
      session.updateApplicationContext(
        mapOf(WatchOSFavoritesCache.contextKey to favoriteIds.toList()),
        error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>().ptr,
      )
    }
  }

  override suspend fun latestFavoriteIds(): Set<String>? = WatchOSFavoritesCache.read().takeIf { it.isNotEmpty() }
}

private object WatchOSFavoritesCache {
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
