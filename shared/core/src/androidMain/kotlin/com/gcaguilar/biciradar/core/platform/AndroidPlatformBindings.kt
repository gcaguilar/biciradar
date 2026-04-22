package com.gcaguilar.biciradar.core.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.AssistantIntentResolver
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
import com.gcaguilar.biciradar.core.NoOpCrashlyticsReporter
import com.gcaguilar.biciradar.core.NoOpRemoteConfigProvider
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.RemoteConfigProvider
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import com.gcaguilar.biciradar.core.local.createAndroidDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okio.FileSystem

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L
private val OPTIONAL_SERVICE_FACTORY_CLASSES =
  listOf(
    "com.gcaguilar.biciradar.AndroidOptionalServicesFactory",
    "com.gcaguilar.biciradar.wear.AndroidOptionalServicesFactory",
  )

interface AndroidOptionalServices {
  val crashlyticsReporter: CrashlyticsReporter?
  val remoteConfigProvider: RemoteConfigProvider?

  fun createReviewPrompter(activityProvider: () -> Activity?): ReviewPrompter?

  fun createAppUpdatePrompter(activityProvider: () -> Activity?): AppUpdatePrompter?

  fun createWatchSyncBridge(): WatchSyncBridge?
}

class AndroidPlatformBindings(
  private val context: Context,
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  /** Activity used for in-app review / flexible updates; set from [attachExperienceActivity]. */
  var experienceActivity: Activity? = null
  private val isFdroidBuild = context.packageName.contains(".fdroid")
  private val optionalServices = loadOptionalServices(context)

  private val androidPermissionPrompter = AndroidPermissionPrompter(context)
  private val androidExternalLinks = AndroidExternalLinks(context, appConfiguration)
  private val androidReviewPrompter =
    optionalServices?.createReviewPrompter { experienceActivity }
      ?: AndroidReviewPrompter(context) { experienceActivity }
  private val androidAppUpdatePrompter =
    optionalServices?.createAppUpdatePrompter { experienceActivity }
      ?: AndroidAppUpdatePrompter(context) { experienceActivity }

  override val permissionPrompter: PermissionPrompter get() = androidPermissionPrompter
  override val externalLinks: ExternalLinks get() = androidExternalLinks
  override val reviewPrompter: ReviewPrompter get() = androidReviewPrompter
  override val appUpdatePrompter: AppUpdatePrompter get() = androidAppUpdatePrompter
  override val crashlyticsReporter: CrashlyticsReporter =
    optionalServices?.crashlyticsReporter ?: NoOpCrashlyticsReporter
  override val logger: Logger = AndroidLogger(crashlyticsReporter)
  override val remoteConfigProvider: RemoteConfigProvider =
    optionalServices?.remoteConfigProvider ?: NoOpRemoteConfigProvider

  override val assistantIntentResolver: AssistantIntentResolver = DefaultAssistantIntentResolver()
  override val databaseFactory: DatabaseFactory =
    object : DatabaseFactory {
      private var database: BiciRadarDatabase? = null

      @Synchronized override fun create(json: Json): BiciRadarDatabase? {
        if (database == null) {
          val driver = createAndroidDriver(context)
          val db = BiciRadarDatabase(driver)
          LegacyBlobToRelationalMigration.ensure(driver, db, json)
          database = db
        }
        return database
      }
    }
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val googleMapsApiKey: String? =
    if (isFdroidBuild) {
      null
    } else {
      runCatching {
        val packageManager = context.packageManager
        val applicationInfo =
          packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
          )
        applicationInfo.metaData
          ?.getString("com.google.android.geo.API_KEY")
          ?.trim()
          ?.takeIf { it.isNotBlank() }
      }.getOrNull()
    }
  override val httpClientFactory: BiziHttpClientFactory = AndroidHttpClientFactory()
  private val androidLocalNotifier = AndroidLocalNotifier(context)
  private val androidWatchSyncBridge = optionalServices?.createWatchSyncBridge() ?: AndroidWatchSyncBridge(context)
  override val localNotifier: LocalNotifier = androidLocalNotifier
  override val locationProvider: LocationProvider = AndroidLocationProvider(context)
  override val mapSupport: MapSupport = if (isFdroidBuild) FdroidMapSupport() else AndroidMapSupport(context)
  override val platform: String = "android"
  override val osVersion: String = "Android ${android.os.Build.VERSION.RELEASE}"
  override val routeLauncher: RouteLauncher = if (isFdroidBuild) FdroidRouteLauncher(context) else AndroidRouteLauncher(context)
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = AndroidStorageDirectoryProvider(context)
  override val watchSyncBridge: WatchSyncBridge = androidWatchSyncBridge
  override val appVersion: String =
    runCatching {
      context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
    }.getOrDefault("unknown")

  /** Provide an Activity-backed requester so [AndroidLocalNotifier] can actually trigger the
   *  POST_NOTIFICATIONS runtime-permission dialog on Android 13+. Call this from
   *  [androidx.activity.ComponentActivity] after registering your [ActivityResultLauncher]. */
  fun bindNotificationPermissionRequester(requester: suspend () -> Boolean) {
    androidLocalNotifier.permissionRequester = requester
  }

  fun bindLocationPermissionRequester(requester: suspend () -> Boolean) {
    androidPermissionPrompter.locationPermissionRequester = requester
  }

  fun attachExperienceActivity(activity: Activity?) {
    experienceActivity = activity
  }

  override fun onGraphCreated(graph: SharedGraph) {
    watchSyncBridge.bindOnRemoteFavoritesChanged {
      graph.syncFavoritesFromPeer.execute()
    }
  }
}

private fun loadOptionalServices(context: Context): AndroidOptionalServices? {
  OPTIONAL_SERVICE_FACTORY_CLASSES.forEach { className ->
    val factoryClass =
      runCatching { Class.forName(className) }
        .getOrNull()
        ?: return@forEach
    val instance =
      runCatching {
        factoryClass
          .getMethod("create", Context::class.java)
          .invoke(null, context.applicationContext) as? AndroidOptionalServices
      }.getOrNull()
    if (instance != null) return instance
  }
  return null
}

private class AndroidLogger(
  private val crashlyticsReporter: CrashlyticsReporter,
) : Logger {
  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?,
  ) {
    when (level) {
      LogLevel.Debug -> android.util.Log.d(tag, message, throwable)
      LogLevel.Info -> android.util.Log.i(tag, message, throwable)
      LogLevel.Warning -> android.util.Log.w(tag, message, throwable)
      LogLevel.Error -> {
        android.util.Log.e(tag, message, throwable)
        throwable?.let { crashlyticsReporter.reportNonFatal(it) }
      }
    }
  }
}

private class AndroidHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient =
    HttpClient(OkHttp) {
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

private class AndroidStorageDirectoryProvider(
  context: Context,
) : StorageDirectoryProvider {
  override val rootPath: String = "${context.filesDir.absolutePath}/bizi"
}

private class AndroidRouteLauncher(
  private val context: Context,
) : RouteLauncher {
  override fun launch(station: Station) {
    launchGoogleMapsRoute(
      destination = station.location,
      mode = "w",
      fallbackUri =
        Uri.parse(
          "geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}",
        ),
    )
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    launchGoogleMapsRoute(
      destination = destination,
      mode = "w",
      fallbackUri = Uri.parse("geo:${destination.latitude},${destination.longitude}"),
    )
  }

  override fun launchBikeToLocation(destination: GeoPoint) {
    launchGoogleMapsRoute(
      destination = destination,
      mode = "b",
      fallbackUri = Uri.parse("geo:${destination.latitude},${destination.longitude}"),
    )
  }

  private fun launchGoogleMapsRoute(
    destination: GeoPoint,
    mode: String,
    fallbackUri: Uri,
  ) {
    val navigationUri =
      Uri.parse(
        "google.navigation:q=${destination.latitude},${destination.longitude}&mode=$mode",
      )
    val intent =
      Intent(Intent.ACTION_VIEW, navigationUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    val fallbackIntent =
      Intent(Intent.ACTION_VIEW, fallbackUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    val hasGoogleMaps = intent.resolveActivity(context.packageManager) != null
    val hasFallback = fallbackIntent.resolveActivity(context.packageManager) != null

    when {
      hasGoogleMaps -> context.startActivity(intent)
      hasFallback -> context.startActivity(fallbackIntent)
    }
  }
}

private class AndroidMapSupport(
  private val context: Context,
) : MapSupport {
  override fun currentStatus(): MapSupportStatus {
    val apiKey =
      runCatching {
        val packageManager = context.packageManager
        val applicationInfo =
          packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA,
          )
        applicationInfo.metaData
          ?.getString("com.google.android.geo.API_KEY")
          .orEmpty()
          .trim()
      }.getOrDefault("")

    return MapSupportStatus(
      embeddedProvider = EmbeddedMapProvider.GoogleMaps,
      googleMapsSdkLinked = true,
      googleMapsApiKeyConfigured = apiKey.isNotBlank(),
    )
  }
}

private class FdroidMapSupport : MapSupport {
  override fun currentStatus(): MapSupportStatus =
    MapSupportStatus(
      embeddedProvider = EmbeddedMapProvider.Osmdroid,
      googleMapsSdkLinked = false,
      googleMapsApiKeyConfigured = false,
    )
}

private class FdroidRouteLauncher(
  private val context: Context,
) : RouteLauncher {
  override fun launch(station: Station) {
    launchFirstSupportedGeoUri(
      Uri.parse(
        "geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}",
      ),
      Uri.parse(
        "geo:0,0?q=${station.location.latitude},${station.location.longitude}(${Uri.encode(station.name)})",
      ),
    )
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    launchFirstSupportedIntent(
      navigationIntent(destination, mode = "w"),
      geoIntent(Uri.parse("geo:${destination.latitude},${destination.longitude}")),
      geoIntent(Uri.parse("geo:0,0?q=${destination.latitude},${destination.longitude}")),
    )
  }

  override fun launchBikeToLocation(destination: GeoPoint) {
    launchFirstSupportedIntent(
      navigationIntent(destination, mode = "b"),
      geoIntent(Uri.parse("geo:${destination.latitude},${destination.longitude}?mode=b")),
      geoIntent(Uri.parse("geo:${destination.latitude},${destination.longitude}")),
      geoIntent(Uri.parse("geo:0,0?q=${destination.latitude},${destination.longitude}")),
    )
  }

  private fun launchFirstSupportedGeoUri(vararg geoUris: Uri) {
    launchFirstSupportedIntent(*geoUris.map(::geoIntent).toTypedArray())
  }

  private fun launchFirstSupportedIntent(vararg intents: Intent) {
    intents.forEach { intent ->
      try {
        context.startActivity(intent)
        return
      } catch (_: ActivityNotFoundException) {
        // Try the next URI variant.
      } catch (_: SecurityException) {
        // Try the next URI variant.
      }
    }
  }

  private fun navigationIntent(
    destination: GeoPoint,
    mode: String,
  ): Intent =
    Intent(
      Intent.ACTION_VIEW,
      Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=$mode"),
    ).apply {
      setPackage("com.google.android.apps.maps")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

  private fun geoIntent(geoUri: Uri): Intent =
    Intent(Intent.ACTION_VIEW, geoUri).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

private class AndroidLocationProvider(
  private val context: Context,
) : LocationProvider {
  private val locationManager by lazy(LazyThreadSafetyMode.NONE) {
    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  }

  @SuppressLint("MissingPermission")
  override suspend fun currentLocation(): GeoPoint? {
    if (!hasLocationPermission()) return null
    val bestLocation =
      locationManager
        .getProviders(true)
        .asSequence()
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull { location -> location.time }

    return bestLocation?.let { location ->
      GeoPoint(latitude = location.latitude, longitude = location.longitude)
    }
  }

  private fun hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
      ) == PackageManager.PERMISSION_GRANTED
}

private class AndroidWatchSyncBridge(
  context: Context,
) : WatchSyncBridge {
  init {
    context.applicationContext
  }

  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) = Unit

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? = null
}

private class AndroidLocalNotifier(
  private val context: Context,
) : LocalNotifier {
  private val channelId = "bizi_trip"
  private val notificationManager: NotificationManagerCompat = NotificationManagerCompat.from(context)

  /** Injected by [AndroidPlatformBindings.bindNotificationPermissionRequester] once the Activity
   *  has registered its [ActivityResultLauncher]. When null, falls back to a check-only path. */
  var permissionRequester: (suspend () -> Boolean)? = null

  init {
    ensureChannel()
  }

  override suspend fun hasPermission(): Boolean =
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      notificationManager.areNotificationsEnabled()
    } else {
      notificationManager.areNotificationsEnabled() &&
        ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

  override suspend fun requestPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return hasPermission()
    // If an Activity-backed requester is available, use it to show the system dialog.
    permissionRequester?.let { return it() }
    // Fallback: check current grant state without prompting.
    return hasPermission()
  }

  @SuppressLint("MissingPermission")
  override suspend fun notify(
    title: String,
    body: String,
  ) {
    if (!requestPermission()) return
    val notification =
      NotificationCompat
        .Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)
        .build()
    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }

  private fun ensureChannel() {
    val channel =
      NotificationChannel(
        channelId,
        "Bizi Viaje",
        NotificationManager.IMPORTANCE_HIGH,
      ).apply {
        description = "Notificaciones de monitorización de viaje en Bizi"
      }
    notificationManager.createNotificationChannel(channel)
  }
}
