package com.gcaguilar.biciradar.core.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
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
import com.gcaguilar.biciradar.core.ExternalLinks
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.FdroidCrashlyticsReporter
import com.gcaguilar.biciradar.core.FdroidRemoteConfigProvider
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.LogLevel
import com.gcaguilar.biciradar.core.Logger
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.geo.currentTimeMs
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import com.gcaguilar.biciradar.core.local.createAndroidDriver
import com.gcaguilar.biciradar.maps.osmdroid.FdroidMapSupport
import com.gcaguilar.biciradar.maps.osmdroid.FdroidRouteLauncher
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.json.Json
import okio.FileSystem
import kotlin.coroutines.resume

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

class AndroidPlatformBindings(
  private val context: Context,
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  /** Activity used for in-app review / flexible updates; set from [attachExperienceActivity]. */
  var experienceActivity: Activity? = null

  private val androidPermissionPrompter = AndroidPermissionPrompter(context)
  private val androidExternalLinks = AndroidExternalLinks(context, appConfiguration)
  private val androidReviewPrompter = AndroidReviewPrompter(context) { experienceActivity }
  private val androidAppUpdatePrompter = AndroidAppUpdatePrompter(context) { experienceActivity }

  override val permissionPrompter: PermissionPrompter get() = androidPermissionPrompter
  override val externalLinks: ExternalLinks get() = androidExternalLinks
  override val reviewPrompter: ReviewPrompter get() = androidReviewPrompter
  override val appUpdatePrompter: AppUpdatePrompter get() = androidAppUpdatePrompter
  override val logger: Logger = AndroidLogger(FdroidCrashlyticsReporter())
  override val crashlyticsReporter: CrashlyticsReporter = FdroidCrashlyticsReporter()
  override val remoteConfigProvider: RemoteConfigProvider = FdroidRemoteConfigProvider()

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
  override val googleMapsApiKey: String? = null // No Google Maps in F-Droid build
  override val httpClientFactory: BiziHttpClientFactory = AndroidHttpClientFactory()
  private val androidLocalNotifier = AndroidLocalNotifier(context)
  private val androidWatchSyncBridge = AndroidWatchSyncBridge(context)
  override val localNotifier: LocalNotifier = androidLocalNotifier
  override val locationProvider: LocationProvider = AndroidLocationProvider(context)
  override val mapSupport: MapSupport = FdroidMapSupport(context)
  override val platform: String = "android"
  override val osVersion: String = "Android ${android.os.Build.VERSION.RELEASE}"
  override val routeLauncher: RouteLauncher = FdroidRouteLauncher(context)
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
    androidWatchSyncBridge.bindOnRemoteFavoritesChanged {
      graph.syncFavoritesFromPeer.execute()
    }
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

  private class AndroidLocationProvider(
    private val context: Context,
  ) : LocationProvider {
    private val locationManager by lazy(LazyThreadSafetyMode.NONE) {
      context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint? {
      if (!hasLocationPermission()) return null

      val manager = locationManager ?: return null
      val providers =
        sequenceOf(
          LocationManager.GPS_PROVIDER,
          LocationManager.NETWORK_PROVIDER,
          LocationManager.PASSIVE_PROVIDER,
        ).filter(manager::isProviderEnabled).toList()
      if (providers.isEmpty()) return null

      freshestLastKnownLocation(manager, providers)?.let { return it.toGeoPoint() }

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        providers.forEach { provider ->
          currentLocation(manager, provider)?.let { return it.toGeoPoint() }
        }
      }

      return null
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

    private fun freshestLastKnownLocation(
      manager: LocationManager,
      providers: List<String>,
    ): Location? =
      providers
        .mapNotNull { provider -> runCatching { manager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull(Location::getTime)

    private suspend fun currentLocation(
      manager: LocationManager,
      provider: String,
    ): Location? = withTimeoutOrNull(10_000L) {
      suspendCancellableCoroutine { continuation ->
        val cancellationSignal = CancellationSignal()
        continuation.invokeOnCancellation { cancellationSignal.cancel() }

        runCatching {
          manager.getCurrentLocation(provider, cancellationSignal, context.mainExecutor) { location ->
            if (continuation.isActive) {
              continuation.resume(location)
            }
          }
        }.onFailure {
          if (continuation.isActive) {
            continuation.resume(null)
          }
        }
      }
    }

    private fun Location.toGeoPoint(): GeoPoint = GeoPoint(latitude = latitude, longitude = longitude)
  }

  private class AndroidWatchSyncBridge(
    context: Context,
  ) : WatchSyncBridge {
    private var onRemoteFavoritesChanged: (suspend () -> Unit)? = null
    private var listenerRegistered = false

    fun bindOnRemoteFavoritesChanged(listener: suspend () -> Unit) {
      onRemoteFavoritesChanged = listener
      if (listenerRegistered) return
      listenerRegistered = true
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
}
