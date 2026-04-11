package com.gcaguilar.biciradar.core.platform

import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.BiziHttpClientFactory
import com.gcaguilar.biciradar.core.DatabaseFactory
import com.gcaguilar.biciradar.core.DefaultAssistantIntentResolver
import com.gcaguilar.biciradar.core.EmbeddedMapProvider
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.MapSupportStatus
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import com.gcaguilar.biciradar.core.local.createNativeDriver
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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.Foundation.NSBundle
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.WatchConnectivity.WCSession
import platform.WatchKit.WKExtension
import platform.WatchKit.WKInterfaceDevice

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val WATCH_SCREENSHOT_MODE_KEY = "bizizaragoza.watch.screenshot_mode"
private const val WATCH_SCREENSHOT_LATITUDE_KEY = "bizizaragoza.watch.screenshot_latitude"
private const val WATCH_SCREENSHOT_LONGITUDE_KEY = "bizizaragoza.watch.screenshot_longitude"
private val DEFAULT_WATCH_SCREENSHOT_LOCATION = GeoPoint(latitude = 41.6488, longitude = -0.8891)

class WatchOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  override val appVersion: String =
    NSBundle.mainBundle
      .objectForInfoDictionaryKey("CFBundleShortVersionString")
      ?.toString()
      ?.trim()
      ?.takeIf { it.isNotBlank() } ?: "unknown"
  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  private var database: BiciRadarDatabase? = null
  override val databaseFactory: DatabaseFactory =
    object : DatabaseFactory {
      override fun create(json: Json): BiciRadarDatabase? {
        if (database == null) {
          val driver = createNativeDriver()
          val db = BiciRadarDatabase(driver)
          LegacyBlobToRelationalMigration.ensure(driver, db, json)
          database = db
        }
        return database
      }
    }
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val googleMapsApiKey: String? = null
  override val httpClientFactory: BiziHttpClientFactory = WatchOSHttpClientFactory()
  override val localNotifier: LocalNotifier = WatchOSLocalNotifier()
  override val locationProvider: LocationProvider = WatchOSLocationProvider()
  override val mapSupport: MapSupport = WatchOSMapSupport()
  override val platform: String = "watchos"
  override val osVersion: String = WKInterfaceDevice.currentDevice().systemVersion()
  override val routeLauncher: RouteLauncher = WatchOSRouteLauncher()
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = WatchOSStorageDirectoryProvider()
  override val watchSyncBridge: WatchSyncBridge = WatchOSSyncBridge()
}

private class WatchOSHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient =
    HttpClient(Darwin) {
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

  override suspend fun currentLocation(): GeoPoint? = screenshotOverrideLocation() ?: delegate.currentLocation()

  private fun screenshotOverrideLocation(): GeoPoint? {
    val defaults = NSUserDefaults.standardUserDefaults
    if (!defaults.boolForKey(WATCH_SCREENSHOT_MODE_KEY)) {
      return null
    }
    val latitude =
      if (defaults.objectForKey(WATCH_SCREENSHOT_LATITUDE_KEY) != null) {
        defaults.doubleForKey(WATCH_SCREENSHOT_LATITUDE_KEY)
      } else {
        DEFAULT_WATCH_SCREENSHOT_LOCATION.latitude
      }
    val longitude =
      if (defaults.objectForKey(WATCH_SCREENSHOT_LONGITUDE_KEY) != null) {
        defaults.doubleForKey(WATCH_SCREENSHOT_LONGITUDE_KEY)
      } else {
        DEFAULT_WATCH_SCREENSHOT_LOCATION.longitude
      }
    return GeoPoint(latitude = latitude, longitude = longitude)
  }
}

private class WatchOSMapSupport : MapSupport {
  override fun currentStatus(): MapSupportStatus =
    MapSupportStatus(
      embeddedProvider = EmbeddedMapProvider.None,
      googleMapsSdkLinked = false,
      googleMapsApiKeyConfigured = false,
    )
}

private class WatchOSRouteLauncher : RouteLauncher {
  override fun launch(station: Station) {
    val routeUrl =
      NSURL.URLWithString(
        "http://maps.apple.com/?daddr=${station.location.latitude},${station.location.longitude}&q=${station.name}&dirflg=w",
      ) ?: return
    WKExtension.sharedExtension().openSystemURL(routeUrl)
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    val routeUrl =
      NSURL.URLWithString(
        "http://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=w",
      ) ?: return
    WKExtension.sharedExtension().openSystemURL(routeUrl)
  }

  override fun launchBikeToLocation(destination: GeoPoint) {
    val routeUrl =
      NSURL.URLWithString(
        "http://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=b",
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
          runCatching { Json { ignoreUnknownKeys = true }.encodeToString(snapshot) }
            .getOrNull()
            ?.let { put(WatchOSFavoritesCache.snapshotContextKey, it) }
        },
        error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>().ptr,
      )
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? =
    WatchOSFavoritesCache
      .read()
      .takeIf {
        it.favoriteIds.isNotEmpty() ||
          it.homeStationId != null ||
          it.workStationId != null ||
          it.stationCategory.isNotEmpty()
      }
}

private object WatchOSFavoritesCache {
  const val cacheKey = "bizizaragoza.watch.favorite_ids"
  const val contextKey = "favorite_ids"
  const val homeCacheKey = "bizizaragoza.watch.home_station_id"
  const val workCacheKey = "bizizaragoza.watch.work_station_id"
  const val homeContextKey = "home_station_id"
  const val workContextKey = "work_station_id"
  const val snapshotCacheKey = "bizizaragoza.watch.favorite_categories_v2"
  const val snapshotContextKey = "favorite_categories_v2"

  fun read(): FavoritesSyncSnapshot =
    FavoritesSyncSnapshot(
      favoriteIds =
        NSUserDefaults.standardUserDefaults
          .arrayForKey(cacheKey)
          .orEmpty()
          .filterIsInstance<String>()
          .toSet(),
      homeStationId = NSUserDefaults.standardUserDefaults.stringForKey(homeCacheKey),
      workStationId = NSUserDefaults.standardUserDefaults.stringForKey(workCacheKey),
    ).let { legacy ->
      val encoded = NSUserDefaults.standardUserDefaults.stringForKey(snapshotCacheKey) ?: return@let legacy
      runCatching { Json { ignoreUnknownKeys = true }.decodeFromString<FavoritesSyncSnapshot>(encoded) }
        .getOrNull() ?: legacy
    }

  fun persist(snapshot: FavoritesSyncSnapshot) {
    NSUserDefaults.standardUserDefaults.setObject(snapshot.favoriteIds.toList(), forKey = cacheKey)
    NSUserDefaults.standardUserDefaults.setObject(snapshot.homeStationId, forKey = homeCacheKey)
    NSUserDefaults.standardUserDefaults.setObject(snapshot.workStationId, forKey = workCacheKey)
    val encoded = runCatching { Json { ignoreUnknownKeys = true }.encodeToString(snapshot) }.getOrNull()
    NSUserDefaults.standardUserDefaults.setObject(encoded, forKey = snapshotCacheKey)
  }
}

private class WatchOSLocalNotifier : LocalNotifier {
  override suspend fun hasPermission(): Boolean = false

  override suspend fun requestPermission(): Boolean = false

  override suspend fun notify(
    title: String,
    body: String,
  ) = Unit
}
