package com.gcaguilar.bizizaragoza.core.platform

import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.BiziHttpClientFactory
import com.gcaguilar.bizizaragoza.core.EmbeddedMapProvider
import com.gcaguilar.bizizaragoza.core.DefaultAssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.FavoritesSyncSnapshot
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.LocalNotifier
import com.gcaguilar.bizizaragoza.core.LocationProvider
import com.gcaguilar.bizizaragoza.core.MapSupport
import com.gcaguilar.bizizaragoza.core.MapSupportStatus
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.PreferredMapApp
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.SettingsRepository
import com.gcaguilar.bizizaragoza.core.SharedGraph
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSBundle
import platform.Foundation.NSClassFromString
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSURL
import platform.MapKit.MKLaunchOptionsDirectionsModeWalking
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.UIKit.UIApplication
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationStateActivated

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

class IOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  private val fileSystemInstance: FileSystem = FileSystem.SYSTEM
  private val storageDirectoryProviderInstance = IOSStorageDirectoryProvider()
  private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
  }

  private val iosRouteLauncher = IOSRouteLauncher()

  override val appVersion: String = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleShortVersionString")
    ?.toString()
    ?.trim()
    ?.takeIf { it.isNotBlank() } ?: "unknown"
  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = fileSystemInstance
  override val googleMapsApiKey: String? = NSBundle.mainBundle
    .objectForInfoDictionaryKey("BiziGoogleMapsApiKey")
    ?.toString()
    ?.trim()
    ?.takeUnless { it.startsWith("$(") || it.isBlank() }
  override val httpClientFactory: BiziHttpClientFactory = IOSHttpClientFactory()
  override val localNotifier: LocalNotifier = IOSLocalNotifier()
  override val locationProvider: LocationProvider = IOSLocationProvider()
  override val mapSupport: MapSupport = IOSMapSupport()
  override val platform: String = "ios"
  override val routeLauncher: RouteLauncher = iosRouteLauncher
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = storageDirectoryProviderInstance
  override val watchSyncBridge: WatchSyncBridge = IOSWatchSyncBridge()

  override fun onGraphCreated(graph: SharedGraph) {
    iosRouteLauncher.settingsRepository = graph.settingsRepository
  }
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
private class IOSRouteLauncher : RouteLauncher {
  var settingsRepository: SettingsRepository? = null

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
        MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeWalking,
      ),
    )
    if (openedInMaps) return

    val fallbackUrl = NSURL.URLWithString(
      "http://maps.apple.com/?daddr=${station.location.latitude},${station.location.longitude}&q=${station.name}&dirflg=w",
    )
    if (fallbackUrl != null && UIApplication.sharedApplication.canOpenURL(fallbackUrl)) {
      UIApplication.sharedApplication.openURL(fallbackUrl)
    }
  }

  private fun launchGoogleMaps(station: Station): Boolean {
    val googleMapsUrl = NSURL.URLWithString(
      "comgooglemaps://?daddr=${station.location.latitude},${station.location.longitude}" +
        "&directionsmode=walking&q=${station.name}",
    ) ?: return false
    val application = UIApplication.sharedApplication
    if (!application.canOpenURL(googleMapsUrl)) return false
    application.openURL(googleMapsUrl)
    return true
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    if (preferredMapApp() == PreferredMapApp.GoogleMaps) {
      val googleMapsUrl = NSURL.URLWithString(
        "comgooglemaps://?daddr=${destination.latitude},${destination.longitude}&directionsmode=walking",
      )
      val application = UIApplication.sharedApplication
      if (googleMapsUrl != null && application.canOpenURL(googleMapsUrl)) {
        application.openURL(googleMapsUrl)
        return
      }
    }
    // Apple Maps fallback
    val mapItem = MKMapItem(
      placemark = MKPlacemark(
        coordinate = destination.toCoordinate(),
        addressDictionary = null,
      ),
    ).apply {
      name = "Destino"
    }
    val opened = mapItem.openInMapsWithLaunchOptions(
      mapOf(MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeWalking),
    )
    if (!opened) {
      val fallbackUrl = NSURL.URLWithString(
        "http://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=w",
      )
      if (fallbackUrl != null && UIApplication.sharedApplication.canOpenURL(fallbackUrl)) {
        UIApplication.sharedApplication.openURL(fallbackUrl)
      }
    }
  }

  private fun preferredMapApp(): PreferredMapApp =
    settingsRepository?.currentPreferredMapApp() ?: PreferredMapApp.AppleMaps
}

private class IOSWatchSyncBridge : WatchSyncBridge {
  @OptIn(ExperimentalForeignApi::class)
  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    IOSFavoritesCache.persist(snapshot)
    if (!WCSession.isSupported()) return
    val session = WCSession.defaultSession
    if (session.activationState != WCSessionActivationStateActivated) return
    memScoped {
      val errorPtr = alloc<ObjCObjectVar<platform.Foundation.NSError?>>()
      val updated = session.updateApplicationContext(
        buildMap {
          put(IOSFavoritesCache.contextKey, snapshot.favoriteIds.toList())
          snapshot.homeStationId?.let { put(IOSFavoritesCache.homeContextKey, it) }
          snapshot.workStationId?.let { put(IOSFavoritesCache.workContextKey, it) }
        },
        error = errorPtr.ptr,
      )
      if (!updated) {
        val msg = errorPtr.value?.localizedDescription ?: "unknown error"
        println("[IOSWatchSyncBridge] updateApplicationContext failed: $msg")
      }
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

@OptIn(ExperimentalForeignApi::class)
private class IOSLocalNotifier : LocalNotifier {
  private val center = UNUserNotificationCenter.currentNotificationCenter()

  override suspend fun requestPermission(): Boolean = suspendCoroutine { cont ->
    center.requestAuthorizationWithOptions(
      options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
    ) { result, _ ->
      cont.resume(result)
    }
  }

  override suspend fun notify(title: String, body: String) {
    val content = UNMutableNotificationContent().apply {
      setTitle(title)
      setBody(body)
    }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, repeats = false)
    val request = UNNotificationRequest.requestWithIdentifier(
      identifier = "bizi_trip_${kotlin.random.Random.nextLong()}",
      content = content,
      trigger = trigger,
    )
    center.addNotificationRequest(request) { _ -> }
  }
}
