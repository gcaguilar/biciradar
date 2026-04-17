package com.gcaguilar.biciradar.core.platform

import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.BiziHttpClientFactory
import com.gcaguilar.biciradar.core.CrashlyticsReporter
import com.gcaguilar.biciradar.core.DatabaseFactory
import com.gcaguilar.biciradar.core.DefaultAssistantIntentResolver
import com.gcaguilar.biciradar.core.EmbeddedMapProvider
import com.gcaguilar.biciradar.core.ExternalLinks
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.LogLevel
import com.gcaguilar.biciradar.core.Logger
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.MapSupportStatus
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.RemoteConfigProvider
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.SharedGraph
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
import kotlinx.cinterop.value
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import platform.Foundation.NSBundle
import platform.Foundation.NSClassFromString
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUserDefaults
import platform.MapKit.MKLaunchOptionsDirectionsModeCycling
import platform.MapKit.MKLaunchOptionsDirectionsModeKey
import platform.MapKit.MKLaunchOptionsDirectionsModeWalking
import platform.MapKit.MKMapItem
import platform.MapKit.MKPlacemark
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleCancel
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter
import platform.WatchConnectivity.WCSession
import platform.WatchConnectivity.WCSessionActivationStateActivated
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private const val IOS_APP_GROUP_IDENTIFIER = "group.com.gcaguilar.biciradar"

class IOSPlatformBindings(
  override val appConfiguration: AppConfiguration = AppConfiguration(),
  private val remoteConfigBridge: IOSRemoteConfigBridge? = null,
  private val crashlyticsBridge: IOSCrashlyticsBridge? = null,
) : PlatformBindings {
  private val fileSystemInstance: FileSystem = FileSystem.SYSTEM
  private val storageDirectoryProviderInstance = IOSStorageDirectoryProvider()
  private val json =
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }

  private val iosRouteLauncher = IOSRouteLauncher()
  private val iosExperienceJson =
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }
  private val iosUpdateHttpClient = IOSHttpClientFactory().create(iosExperienceJson)

  override val appVersion: String =
    NSBundle.mainBundle
      .objectForInfoDictionaryKey("CFBundleShortVersionString")
      ?.toString()
      ?.trim()
      ?.takeIf { it.isNotBlank() } ?: "unknown"

  override val permissionPrompter: PermissionPrompter = IOSPermissionPrompterImpl()
  override val externalLinks: ExternalLinks = IOSExternalLinksImpl(appConfiguration)
  override val reviewPrompter: ReviewPrompter = IOSReviewPrompterImpl(appConfiguration)
  override val logger: Logger = AppleLogger(crashlyticsBridge)
  override val crashlyticsReporter: CrashlyticsReporter = IOSCrashlyticsReporter(crashlyticsBridge)
  override val remoteConfigProvider: RemoteConfigProvider =
    IOSRemoteConfigProvider(remoteConfigBridge, logger)
  override val appUpdatePrompter: AppUpdatePrompter =
    IOSAppUpdatePrompterImpl(
      appConfiguration = appConfiguration,
      httpClient = iosUpdateHttpClient,
      json = iosExperienceJson,
      currentAppVersion = appVersion,
    )
  override val assistantIntentResolver = DefaultAssistantIntentResolver()
  override val databaseFactory: DatabaseFactory =
    object : DatabaseFactory {
      @kotlin.concurrent.Volatile private var database: BiciRadarDatabase? = null

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
  override val fileSystem: FileSystem = fileSystemInstance
  override val googleMapsApiKey: String? =
    NSBundle.mainBundle
      .objectForInfoDictionaryKey("BiziGoogleMapsApiKey")
      ?.toString()
      ?.trim()
      ?.takeUnless { it.startsWith("$(") || it.isBlank() }
  override val httpClientFactory: BiziHttpClientFactory = IOSHttpClientFactory()
  override val localNotifier: LocalNotifier = IOSLocalNotifier()
  override val locationProvider: LocationProvider = IOSLocationProvider()
  override val mapSupport: MapSupport = IOSMapSupport()
  override val platform: String = "ios"
  override val osVersion: String = UIDevice.currentDevice.systemVersion
  override val routeLauncher: RouteLauncher = iosRouteLauncher
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = storageDirectoryProviderInstance
  override val watchSyncBridge: WatchSyncBridge = IOSWatchSyncBridge()

  /**
   * Post-wiring de dependencias del grafo hacia componentes de plataforma.
   *
   * NOTA: Idealmente IOSRouteLauncher recibiría SettingsRepository vía constructor
   * con @Inject, pero IOSRouteLauncher se crea ANTES de que el grafo exista
   * (es parte de IOSPlatformBindings que se pasa al grafo).
   *
   * La alternativa "pura" de Metro sería mover IOSRouteLauncher al grafo y que
   * IOSPlatformBindings lo reciba vía constructor, pero eso requiere refactor
   * mayor de PlatformBindings. Por ahora mantenemos este late wiring documentado.
   */
  override fun onGraphCreated(graph: SharedGraph) {
    iosRouteLauncher.settingsRepository = graph.settingsRepository
  }
}

private class AppleLogger(
  private val crashlyticsBridge: IOSCrashlyticsBridge?,
) : Logger {
  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?,
  ) {
    val suffix = throwable?.let { " | ${it::class.simpleName}: ${it.message}" }.orEmpty()
    platform.Foundation.NSLog("[$tag][${level.name}] $message$suffix")
    if (level == LogLevel.Error && throwable != null) {
      crashlyticsBridge?.reportNonFatal(throwable)
    }
  }
}

interface IOSCrashlyticsBridge {
  fun reportNonFatal(throwable: Throwable)
}

private class IOSCrashlyticsReporter(
  private val bridge: IOSCrashlyticsBridge?,
) : CrashlyticsReporter {
  override fun reportNonFatal(throwable: Throwable) {
    bridge?.reportNonFatal(throwable)
  }
}

interface IOSRemoteConfigBridge {
  fun getString(
    key: String,
    completionHandler: (String?) -> Unit,
  )
}

private class IOSRemoteConfigProvider(
  private val remoteConfigBridge: IOSRemoteConfigBridge?,
  private val logger: Logger,
) : RemoteConfigProvider {
  override suspend fun getString(key: String): String? {
    val bridge = remoteConfigBridge ?: return null
    return suspendCoroutine { continuation ->
      runCatching {
        bridge.getString(key) { value ->
          continuation.resume(value)
        }
      }.onFailure { error ->
        logger.warn("RemoteConfig", "Unable to fetch remote config key=$key", error)
        continuation.resume(null)
      }
    }
  }
}

private class IOSMapSupport : MapSupport {
  override fun currentStatus(): MapSupportStatus {
    val apiKey =
      NSBundle.mainBundle
        .objectForInfoDictionaryKey("BiziGoogleMapsApiKey")
        ?.toString()
        ?.trim()
        ?.takeUnless { it.startsWith("$(") }
        .orEmpty()
    val googleMapsUrl = NSURL.URLWithString("comgooglemaps://")
    val googleMapsInstalled =
      googleMapsUrl != null &&
        UIApplication.sharedApplication.canOpenURL(googleMapsUrl)
    return MapSupportStatus(
      embeddedProvider = EmbeddedMapProvider.AppleMapKit,
      googleMapsSdkLinked = NSClassFromString("GMSServices") != null && NSClassFromString("GMSMapView") != null,
      googleMapsApiKeyConfigured = apiKey.isNotBlank(),
      googleMapsAppInstalled = googleMapsInstalled,
    )
  }
}

private class IOSHttpClientFactory : BiziHttpClientFactory {
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

private class IOSStorageDirectoryProvider : StorageDirectoryProvider {
  override val rootPath: String = iosSharedStorageRootPath() ?: "${NSHomeDirectory()}/Documents/bizi"
}

private fun iosSharedStorageRootPath(): String? {
  val containerUrl =
    NSFileManager.defaultManager.containerURLForSecurityApplicationGroupIdentifier(
      IOS_APP_GROUP_IDENTIFIER,
    ) ?: return null
  val path = containerUrl.path ?: return null
  return "$path/bizi"
}

private class IOSLocationProvider : LocationProvider {
  private val delegate = AppleLocationProvider()

  override suspend fun currentLocation(): GeoPoint? = delegate.currentLocation()
}

@OptIn(ExperimentalForeignApi::class)
private class IOSRouteLauncher : RouteLauncher {
  var settingsRepository: SettingsRepository? = null

  override fun launch(station: Station) {
    if (preferredMapApp() == PreferredMapApp.GoogleMaps) {
      launchGoogleMapsWithFallback(
        destination = station.location,
        googleMapsMode = "walking",
        onAllGoogleFallbacksFailed = { launchAppleMaps(station) },
      )
      return
    }
    launchAppleMaps(station)
  }

  private fun launchAppleMaps(station: Station) {
    val mapItem =
      MKMapItem(
        placemark =
          MKPlacemark(
            coordinate = station.location.toCoordinate(),
            addressDictionary = null,
          ),
      ).apply {
        name = station.name
      }

    val openedInMaps =
      mapItem.openInMapsWithLaunchOptions(
        mapOf(
          MKLaunchOptionsDirectionsModeKey to MKLaunchOptionsDirectionsModeWalking,
        ),
      )
    if (openedInMaps) return

    val fallbackUrl =
      NSURL.URLWithString(
        "http://maps.apple.com/?daddr=${station.location.latitude},${station.location.longitude}&q=${station.name}&dirflg=w",
      )
    if (fallbackUrl != null && UIApplication.sharedApplication.canOpenURL(fallbackUrl)) {
      UIApplication.sharedApplication.openURL(
        url = fallbackUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = null,
      )
    }
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    if (preferredMapApp() == PreferredMapApp.GoogleMaps) {
      launchGoogleMapsWithFallback(
        destination = destination,
        googleMapsMode = "walking",
        onAllGoogleFallbacksFailed = {
          launchAppleMapsToLocation(
            destination = destination,
            launchMode = MKLaunchOptionsDirectionsModeWalking,
            fallbackFlag = "w",
          )
        },
      )
      return
    }
    launchAppleMapsToLocation(
      destination = destination,
      launchMode = MKLaunchOptionsDirectionsModeWalking,
      fallbackFlag = "w",
    )
  }

  override fun launchBikeToLocation(destination: GeoPoint) {
    if (preferredMapApp() == PreferredMapApp.GoogleMaps) {
      launchGoogleMapsWithFallback(
        destination = destination,
        googleMapsMode = "bicycling",
        onAllGoogleFallbacksFailed = {
          launchAppleMapsToLocation(
            destination = destination,
            launchMode = MKLaunchOptionsDirectionsModeCycling,
            fallbackFlag = "b",
          )
        },
      )
      return
    }
    launchAppleMapsToLocation(
      destination = destination,
      launchMode = MKLaunchOptionsDirectionsModeCycling,
      fallbackFlag = "b",
    )
  }

  private fun launchGoogleMapsWithFallback(
    destination: GeoPoint,
    googleMapsMode: String,
    onAllGoogleFallbacksFailed: () -> Unit,
  ) {
    val googleMapsUrl =
      NSURL.URLWithString(
        "comgooglemaps://?daddr=${destination.latitude},${destination.longitude}&directionsmode=$googleMapsMode",
      )
    val app = UIApplication.sharedApplication
    if (googleMapsUrl == null) {
      if (!launchGoogleMapsWeb(destination, googleMapsMode)) onAllGoogleFallbacksFailed()
      return
    }

    // Check if Google Maps app is installed
    if (!app.canOpenURL(googleMapsUrl)) {
      // Show alert and fallback to Apple Maps
      showGoogleMapsNotInstalledAlert()
      onAllGoogleFallbacksFailed()
      return
    }

    app.openURL(
      url = googleMapsUrl,
      options = emptyMap<Any?, Any>(),
      completionHandler = { opened ->
        if (!opened) {
          showGoogleMapsNotInstalledAlert()
          val openedWeb = launchGoogleMapsWeb(destination, googleMapsMode)
          if (!openedWeb) onAllGoogleFallbacksFailed()
        }
      },
    )
  }

  @OptIn(ExperimentalForeignApi::class)
  private fun showGoogleMapsNotInstalledAlert() {
    val alertController =
      UIAlertController.alertControllerWithTitle(
        title = "Google Maps no instalado",
        message =
          "Para obtener la mejor experiencia de navegación, instala Google Maps desde el App Store. " +
            "Se abrirá Apple Maps como alternativa.",
        preferredStyle = UIAlertControllerStyleAlert,
      )

    alertController.addAction(
      UIAlertAction.actionWithTitle(
        title = "Abrir App Store",
        style = UIAlertActionStyleDefault,
        handler = { _ ->
          val appStoreUrl = NSURL.URLWithString("https://apps.apple.com/app/google-maps/id585027354")
          if (appStoreUrl != null && UIApplication.sharedApplication.canOpenURL(appStoreUrl)) {
            UIApplication.sharedApplication.openURL(
              url = appStoreUrl,
              options = emptyMap<Any?, Any>(),
              completionHandler = null,
            )
          }
        },
      ),
    )

    alertController.addAction(
      UIAlertAction.actionWithTitle(
        title = "Continuar con Apple Maps",
        style = UIAlertActionStyleCancel,
        handler = null,
      ),
    )

    // Get the top view controller to present the alert
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootViewController = keyWindow?.rootViewController
    rootViewController?.presentViewController(
      alertController,
      animated = true,
      completion = null,
    )
  }

  private fun launchAppleMapsToLocation(
    destination: GeoPoint,
    launchMode: String,
    fallbackFlag: String,
  ) {
    val mapItem =
      MKMapItem(
        placemark =
          MKPlacemark(
            coordinate = destination.toCoordinate(),
            addressDictionary = null,
          ),
      ).apply {
        name = "Destino"
      }
    val opened =
      mapItem.openInMapsWithLaunchOptions(
        mapOf(MKLaunchOptionsDirectionsModeKey to launchMode),
      )
    if (!opened) {
      val fallbackUrl =
        NSURL.URLWithString(
          "http://maps.apple.com/?daddr=${destination.latitude},${destination.longitude}&dirflg=$fallbackFlag",
        )
      if (fallbackUrl != null && UIApplication.sharedApplication.canOpenURL(fallbackUrl)) {
        UIApplication.sharedApplication.openURL(
          url = fallbackUrl,
          options = emptyMap<Any?, Any>(),
          completionHandler = null,
        )
      }
    }
  }

  private fun preferredMapApp(): PreferredMapApp =
    when (settingsRepository?.currentPreferredMapApp() ?: PreferredMapApp.AppleMaps) {
      PreferredMapApp.GoogleMaps -> {
        if (isGoogleMapsInstalled()) PreferredMapApp.GoogleMaps else PreferredMapApp.AppleMaps
      }
      PreferredMapApp.AppleMaps -> PreferredMapApp.AppleMaps
    }

  private fun isGoogleMapsInstalled(): Boolean {
    val url = NSURL.URLWithString("comgooglemaps://") ?: return false
    return UIApplication.sharedApplication.canOpenURL(url)
  }

  private fun launchGoogleMapsWeb(
    destination: GeoPoint,
    travelMode: String,
  ): Boolean {
    val url =
      NSURL.URLWithString(
        "https://www.google.com/maps/dir/?api=1&destination=${destination.latitude},${destination.longitude}&travelmode=$travelMode",
      ) ?: return false
    val app = UIApplication.sharedApplication
    if (!app.canOpenURL(url)) return false
    app.openURL(
      url = url,
      options = emptyMap<Any?, Any>(),
      completionHandler = null,
    )
    return true
  }
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
      val updated =
        session.updateApplicationContext(
          buildMap {
            put(IOSFavoritesCache.CONTEXT_KEY, snapshot.favoriteIds.toList())
            snapshot.homeStationId?.let { put(IOSFavoritesCache.HOME_CONTEXT_KEY, it) }
            snapshot.workStationId?.let { put(IOSFavoritesCache.WORK_CONTEXT_KEY, it) }
            runCatching { Json { ignoreUnknownKeys = true }.encodeToString(snapshot) }
              .getOrNull()
              ?.let { put(IOSFavoritesCache.SNAPSHOT_CONTEXT_KEY, it) }
          },
          error = errorPtr.ptr,
        )
      if (!updated) {
        val msg = errorPtr.value?.localizedDescription ?: "unknown error"
        AppleLogger(crashlyticsBridge = null).warn("IOSWatchSyncBridge", "updateApplicationContext failed: $msg")
      }
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? =
    IOSFavoritesCache
      .read()
      .takeIf {
        it.favoriteIds.isNotEmpty() ||
          it.homeStationId != null ||
          it.workStationId != null ||
          it.stationCategory.isNotEmpty()
      }
}

private object IOSFavoritesCache {
  const val CACHE_KEY = "bizizaragoza.watch.favorite_ids"
  const val CONTEXT_KEY = "favorite_ids"
  const val HOME_CACHE_KEY = "bizizaragoza.watch.home_station_id"
  const val WORK_CACHE_KEY = "bizizaragoza.watch.work_station_id"
  const val HOME_CONTEXT_KEY = "home_station_id"
  const val WORK_CONTEXT_KEY = "work_station_id"
  const val SNAPSHOT_CACHE_KEY = "bizizaragoza.watch.favorite_categories_v2"
  const val SNAPSHOT_CONTEXT_KEY = "favorite_categories_v2"

  fun read(): FavoritesSyncSnapshot =
    FavoritesSyncSnapshot(
      favoriteIds =
        NSUserDefaults(suiteName = "group.com.gcaguilar.biciradar")!!
          .arrayForKey(CACHE_KEY)
          .orEmpty()
          .filterIsInstance<String>()
          .toSet(),
      homeStationId = NSUserDefaults(suiteName = "group.com.gcaguilar.biciradar")!!.stringForKey(HOME_CACHE_KEY),
      workStationId = NSUserDefaults(suiteName = "group.com.gcaguilar.biciradar")!!.stringForKey(WORK_CACHE_KEY),
    ).let { legacy ->
      val encoded =
        NSUserDefaults(suiteName = "group.com.gcaguilar.biciradar")!!.stringForKey(SNAPSHOT_CACHE_KEY)
          ?: return@let legacy
      runCatching { Json { ignoreUnknownKeys = true }.decodeFromString<FavoritesSyncSnapshot>(encoded) }
        .getOrNull() ?: legacy
    }

  fun persist(snapshot: FavoritesSyncSnapshot) {
    NSUserDefaults(
      suiteName = "group.com.gcaguilar.biciradar",
    )!!.setObject(snapshot.favoriteIds.toList(), forKey = CACHE_KEY)
    NSUserDefaults(
      suiteName = "group.com.gcaguilar.biciradar",
    )!!.setObject(snapshot.homeStationId, forKey = HOME_CACHE_KEY)
    NSUserDefaults(
      suiteName = "group.com.gcaguilar.biciradar",
    )!!.setObject(snapshot.workStationId, forKey = WORK_CACHE_KEY)
    val encoded = runCatching { Json { ignoreUnknownKeys = true }.encodeToString(snapshot) }.getOrNull()
    NSUserDefaults(suiteName = "group.com.gcaguilar.biciradar")!!.setObject(encoded, forKey = SNAPSHOT_CACHE_KEY)
  }
}

@OptIn(ExperimentalForeignApi::class)
private class IOSLocalNotifier : LocalNotifier {
  private val center = UNUserNotificationCenter.currentNotificationCenter()

  override suspend fun hasPermission(): Boolean =
    suspendCoroutine { cont ->
      center.getNotificationSettingsWithCompletionHandler { settings ->
        cont.resume(settings?.authorizationStatus == UNAuthorizationStatusAuthorized)
      }
    }

  override suspend fun requestPermission(): Boolean =
    suspendCoroutine { cont ->
      center.requestAuthorizationWithOptions(
        options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
      ) { result, _ ->
        cont.resume(result)
      }
    }

  override suspend fun notify(
    title: String,
    body: String,
  ) {
    val content =
      UNMutableNotificationContent().apply {
        setTitle(title)
        setBody(body)
      }
    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(0.1, repeats = false)
    val request =
      UNNotificationRequest.requestWithIdentifier(
        identifier = "bizi_trip_${kotlin.random.Random.nextLong()}",
        content = content,
        trigger = trigger,
      )
    center.addNotificationRequest(request) { _ -> }
  }
}
