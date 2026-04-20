package com.gcaguilar.biciradar.core.platform

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.gcaguilar.biciradar.core.FdroidCrashlyticsReporter
import com.gcaguilar.biciradar.core.FdroidRemoteConfigProvider
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.LocationProvider
import com.gcaguilar.biciradar.core.LogLevel
import com.gcaguilar.biciradar.core.Logger
import com.gcaguilar.biciradar.core.MapSupport
import com.gcaguilar.biciradar.core.MapSupportStatus
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okio.FileSystem

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

  private class AndroidRouteLauncher(
    private val context: Context,
  ) : RouteLauncher {
    // This will be overridden by FdroidRouteLauncher in the actual implementation
    override fun launch(station: Station) {
      // Fallback to geo URI
      val fallbackUri = Uri.parse("geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}")
      val intent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }

    override fun launchWalkToLocation(destination: GeoPoint) {
      val fallbackUri = Uri.parse("geo:${destination.latitude},${destination.longitude}")
      val intent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }

    override fun launchBikeToLocation(destination: GeoPoint) {
      val fallbackUri = Uri.parse("geo:${destination.latitude},${destination.longitude}")
      val intent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    }
  }

  private class AndroidMapSupport(
    private val context: Context,
  ) : MapSupport {
    // This will be overridden by FdroidMapSupport in the actual implementation
    override fun currentStatus(): MapSupportStatus {
      return MapSupportStatus(
        embeddedProvider = EmbeddedMapProvider.Osmdroid, // Using OSMdroid instead
        googleMapsSdkLinked = false, // No Google Maps SDK
        googleMapsApiKeyConfigured = false // No API key
      )
    }
  }

  private class AndroidLocationProvider(
    private val context: Context,
  ) : LocationProvider {
    private val fusedLocationClient by lazy(LazyThreadSafetyMode.NONE) {
      // Since we're not using Play Services Location in F-Droid, we'll need an alternative
      // For now, we'll return null to indicate location is not available
      // A proper implementation would use android.location.LocationManager
      return object : com.google.android.gms.location.FusedLocationProviderClient(context) {
        // This is a placeholder - in a real implementation we'd use LocationManager
      }
    }

    @SuppressLint("MissingPermission")
    override suspend fun currentLocation(): GeoPoint? {
      // Return null as we don't have location implementation for F-Droid yet
      // A proper implementation would use android.location.LocationManager
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
  }

  private class AndroidWatchSyncBridge(
    context: Context,
  ) : WatchSyncBridge {
    // Since we're not using Play Services Wearable in F-Droid
    private val dataClient = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var onRemoteFavoritesChanged: (suspend () -> Unit)? = null
    private var listenerRegistered = false
    private val dataChangedListener =
      DataClient.OnDataChangedListener { dataEvents ->
        val callback = onRemoteFavoritesChanged ?: return@OnDataChangedListener
        val hasFavoritesUpdate =
          dataEvents.any { event ->
            event.type == DataEvent.TYPE_CHANGED && event.dataItem.uri.path == FAVORITES_PATH
          }
        if (hasFavoritesUpdate) {
          scope.launch { callback() }
        }
      }

    fun bindOnRemoteFavoritesChanged(listener: suspend () -> Unit) {
      onRemoteFavoritesChanged = listener
      if (listenerRegistered) return
      // No-op since dataClient is null
      listenerRegistered = true
    }

    override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
      // No-op since we don't have wearable support
    }

    override suspend fun latestFavorites(): FavoritesSyncSnapshot? {
      return null // No wearable support
    }

    private fun buildFavoritesUri(): Uri =
      Uri
        .Builder()
        .scheme(PutDataRequest.WEAR_URI_SCHEME)
        .path(FAVORITES_PATH)
        .build()

    private companion object {
      const val FAVORITES_PATH = "/bizi/favorites"
      const val FAVORITES_KEY = "favorite_ids"
      const val HOME_STATION_KEY = "home_station_id"
      const val WORK_STATION_KEY = "work_station_id"
      const val UPDATED_AT_KEY = "updated_at"
    }
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