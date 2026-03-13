package com.gcaguilar.bizizaragoza.core.platform

import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.BiziHttpClientFactory
import com.gcaguilar.bizizaragoza.core.EmbeddedMapProvider
import com.gcaguilar.bizizaragoza.core.DefaultAssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.FavoritesSyncSnapshot
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.LocationProvider
import com.gcaguilar.bizizaragoza.core.MapSupport
import com.gcaguilar.bizizaragoza.core.MapSupportStatus
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.PreferredMapApp
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.SettingsSnapshot
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StorageDirectoryProvider
import com.gcaguilar.bizizaragoza.core.WatchSyncBridge
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
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSClassFromString
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSURL
import platform.MapKit.MKLaunchOptionsDirectionsModeDriving
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.UIKit.UIApplication
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationStateActivated

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

class IOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  private val fileSystemInstance: FileSystem = FileSystem.SYSTEM
  private val storageDirectoryProviderInstance = IOSStorageDirectoryProvider()
  private val json = Json

  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = fileSystemInstance
  override val httpClientFactory: BiziHttpClientFactory = IOSHttpClientFactory()
  override val locationProvider: LocationProvider = IOSLocationProvider()
  override val mapSupport: MapSupport = IOSMapSupport()
  override val routeLauncher: RouteLauncher = IOSRouteLauncher(
    fileSystem = fileSystemInstance,
    json = json,
    storageDirectoryProvider = storageDirectoryProviderInstance,
  )
  override val storageDirectoryProvider: StorageDirectoryProvider = storageDirectoryProviderInstance
  override val watchSyncBridge: WatchSyncBridge = IOSWatchSyncBridge()
}

private class IOSMapSupport : MapSupport {
  override fun currentStatus(): MapSupportStatus {
    val apiKey = NSBundle.mainBundle.objectForInfoDictionaryKey("BiziGoogleMapsApiKey")
      ?.toString()
      ?.trim()
      ?.takeUnless { it.startsWith("$(") }
      .orEmpty()
    return MapSupportStatus(
      embeddedProvider = EmbeddedMapProvider.AppleMapKit,
      googleMapsSdkLinked = NSClassFromString("GMSServices") != null && NSClassFromString("GMSMapView") != null,
      googleMapsApiKeyConfigured = apiKey.isNotBlank(),
    )
  }
}

private class IOSHttpClientFactory : BiziHttpClientFactory {
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

private class IOSStorageDirectoryProvider : StorageDirectoryProvider {
  override val rootPath: String = "${NSHomeDirectory()}/Documents/bizi"
}

private class IOSLocationProvider : LocationProvider {
  private val delegate = AppleLocationProvider()

  override suspend fun currentLocation(): GeoPoint? = delegate.currentLocation()
}

@OptIn(ExperimentalForeignApi::class)
private class IOSRouteLauncher(
  private val fileSystem: FileSystem,
  private val json: Json,
  private val storageDirectoryProvider: StorageDirectoryProvider,
) : RouteLauncher {
  override fun launch(station: Station) {
    if (preferredMapApp() == PreferredMapApp.GoogleMaps && launchGoogleMaps(station)) {
      return
    }
    launchAppleMaps(station)
  }

  private fun launchAppleMaps(station: Station) {
    val mapItem = MKMapItem(
      placemark = MKPlacemark(
        coordinate = station.location.toCoordinate(),
        addressDictionary = null,
      ),
    ).apply {
      name = station.name
    }

    val openedInMaps = mapItem.openInMapsWithLaunchOptions(
      mapOf(
        MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeDriving,
      ),
    )
    if (openedInMaps) return

    val fallbackUrl = NSURL.URLWithString(
      "http://maps.apple.com/?daddr=${station.location.latitude},${station.location.longitude}&q=${station.name}",
    )
    if (fallbackUrl != null && UIApplication.sharedApplication.canOpenURL(fallbackUrl)) {
      UIApplication.sharedApplication.openURL(fallbackUrl)
    }
  }

  private fun launchGoogleMaps(station: Station): Boolean {
    val googleMapsUrl = NSURL.URLWithString(
      "comgooglemaps://?daddr=${station.location.latitude},${station.location.longitude}" +
        "&directionsmode=driving&q=${station.name}",
    ) ?: return false
    val application = UIApplication.sharedApplication
    if (!application.canOpenURL(googleMapsUrl)) return false
    application.openURL(googleMapsUrl)
    return true
  }

  private fun preferredMapApp(): PreferredMapApp {
    val path = "${storageDirectoryProvider.rootPath}/settings.json".toPath()
    if (!fileSystem.exists(path)) return PreferredMapApp.AppleMaps
    return runCatching {
      fileSystem.read(path) { readUtf8() }
    }.mapCatching { raw ->
      json.decodeFromString<SettingsSnapshot>(raw).preferredMapApp
    }.getOrDefault(PreferredMapApp.AppleMaps)
  }
}

private class IOSWatchSyncBridge : WatchSyncBridge {
  @OptIn(ExperimentalForeignApi::class)
  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    IOSFavoritesCache.persist(snapshot)
    val session = WCSession.defaultSession
    if (session.activationState != WCSessionActivationStateActivated) return
    memScoped {
      session.updateApplicationContext(
        buildMap {
          put(IOSFavoritesCache.contextKey, snapshot.favoriteIds.toList())
          snapshot.homeStationId?.let { put(IOSFavoritesCache.homeContextKey, it) }
          snapshot.workStationId?.let { put(IOSFavoritesCache.workContextKey, it) }
        },
        error = alloc<ObjCObjectVar<platform.Foundation.NSError?>>().ptr,
      )
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? = IOSFavoritesCache.read()
    .takeIf { it.favoriteIds.isNotEmpty() || it.homeStationId != null || it.workStationId != null }
}

private object IOSFavoritesCache {
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
