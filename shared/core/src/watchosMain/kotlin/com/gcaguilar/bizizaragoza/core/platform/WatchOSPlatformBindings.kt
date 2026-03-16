package com.gcaguilar.bizizaragoza.core.platform

import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.BiziHttpClientFactory
import com.gcaguilar.bizizaragoza.core.DefaultAssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.EmbeddedMapProvider
import com.gcaguilar.bizizaragoza.core.FavoritesSyncSnapshot
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.LocalNotifier
import com.gcaguilar.bizizaragoza.core.LocationProvider
import com.gcaguilar.bizizaragoza.core.MapSupport
import com.gcaguilar.bizizaragoza.core.MapSupportStatus
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StorageDirectoryProvider
import com.gcaguilar.bizizaragoza.core.WatchSyncBridge
import com.gcaguilar.bizizaragoza.core.crypto.SecureKeyStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.Foundation.NSBundle
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSURL
import platform.WatchConnectivity.WCSession
import platform.WatchKit.WKExtension

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

class WatchOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  override val appVersion: String = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleShortVersionString")
    ?.toString()
    ?.trim()
    ?.takeIf { it.isNotBlank() } ?: "unknown"
  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val googleMapsApiKey: String? = null
  override val httpClientFactory: BiziHttpClientFactory = WatchOSHttpClientFactory()
  override val localNotifier: LocalNotifier = WatchOSLocalNotifier()
  override val locationProvider: LocationProvider = WatchOSLocationProvider()
  override val mapSupport: MapSupport = WatchOSMapSupport()
  override val platform: String = "watchos"
  override val routeLauncher: RouteLauncher = WatchOSRouteLauncher()
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = WatchOSStorageDirectoryProvider()
  override val watchSyncBridge: WatchSyncBridge = WatchOSSyncBridge()
}

private class WatchOSHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient = HttpClient(Darwin) {
    expectSuccess = true
    install(HttpTimeout) {
      requestTimeoutMillis = REQUEST_TIMEOUT_MILLIS
      connectTimeoutMillis = CONNECT_TIMEOUT_MILLIS
      socketTimeoutMillis = REQUEST_TIMEOUT_MILLIS
    }
    install(ContentNegotiation) {
      json(json)
    }
  }
}

private class WatchOSStorageDirectoryProvider : StorageDirectoryProvider {
  override val rootPath: String = "${NSHomeDirectory()}/Documents/bizi"
}

private class WatchOSLocationProvider : LocationProvider {
  private val delegate = AppleLocationProvider()

  override suspend fun currentLocation(): GeoPoint? = delegate.currentLocation()
}

private class WatchOSMapSupport : MapSupport {
  override fun currentStatus(): MapSupportStatus = MapSupportStatus(
    embeddedProvider = EmbeddedMapProvider.None,
    googleMapsSdkLinked = false,
    googleMapsApiKeyConfigured = false,
  )
}

private class WatchOSRouteLauncher : RouteLauncher {
  override fun launch(station: Station) {
    val routeUrl = NSURL.URLWithString(
      "http://maps.apple.com/?daddr=${station.location.latitude},${station.location.longitude}&q=${station.name}&dirflg=w",
    ) ?: return
    WKExtension.sharedExtension().openSystemURL(routeUrl)
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    val routeUrl = NSURL.URLWithString(
      "http://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=w",
    ) ?: return
    WKExtension.sharedExtension().openSystemURL(routeUrl)
  }
}

private class WatchOSSyncBridge : WatchSyncBridge {
  @OptIn(ExperimentalForeignApi::class)
  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    WatchOSFavoritesCache.persist(snapshot)
    val session = WCSession.defaultSession
    memScoped {
      session.updateApplicationContext(
        buildMap {
          put(WatchOSFavoritesCache.contextKey, snapshot.favoriteIds.toList())
          snapshot.homeStationId?.let { put(WatchOSFavoritesCache.homeContextKey, it) }
          snapshot.workStationId?.let { put(WatchOSFavoritesCache.workContextKey, it) }
        },
        error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>().ptr,
      )
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? = WatchOSFavoritesCache.read()
    .takeIf { it.favoriteIds.isNotEmpty() || it.homeStationId != null || it.workStationId != null }
}

private object WatchOSFavoritesCache {
  const val cacheKey = "bizizaragoza.watch.favorite_ids"
  const val contextKey = "favorite_ids"
  const val homeCacheKey = "bizizaragoza.watch.home_station_id"
  const val workCacheKey = "bizizaragoza.watch.work_station_id"
  const val homeContextKey = "home_station_id"
  const val workContextKey = "work_station_id"

  fun read(): FavoritesSyncSnapshot = FavoritesSyncSnapshot(
    favoriteIds = NSUserDefaults.standardUserDefaults.arrayForKey(cacheKey)
      .orEmpty()
      .filterIsInstance<String>()
      .toSet(),
    homeStationId = NSUserDefaults.standardUserDefaults.stringForKey(homeCacheKey),
    workStationId = NSUserDefaults.standardUserDefaults.stringForKey(workCacheKey),
  )

  fun persist(snapshot: FavoritesSyncSnapshot) {
    NSUserDefaults.standardUserDefaults.setObject(snapshot.favoriteIds.toList(), forKey = cacheKey)
    NSUserDefaults.standardUserDefaults.setObject(snapshot.homeStationId, forKey = homeCacheKey)
    NSUserDefaults.standardUserDefaults.setObject(snapshot.workStationId, forKey = workCacheKey)
  }
}

private class WatchOSLocalNotifier : LocalNotifier {
  override suspend fun requestPermission(): Boolean = false
  override suspend fun notify(title: String, body: String) = Unit
}
