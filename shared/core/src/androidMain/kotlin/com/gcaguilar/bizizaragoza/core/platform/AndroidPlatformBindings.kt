package com.gcaguilar.bizizaragoza.core.platform

import android.Manifest
import android.annotation.SuppressLint
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
import com.gcaguilar.bizizaragoza.core.DefaultAssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.AssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.BiziHttpClientFactory
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
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okio.FileSystem

private const val REQUEST_TIMEOUT_MILLIS = 15_000L
private const val CONNECT_TIMEOUT_MILLIS = 10_000L

class AndroidPlatformBindings(
  private val context: Context,
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  override val assistantIntentResolver: AssistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val googleMapsApiKey: String? = runCatching {
    val packageManager = context.packageManager
    val applicationInfo = packageManager.getApplicationInfo(
      context.packageName,
      PackageManager.GET_META_DATA,
    )
    applicationInfo.metaData?.getString("com.google.android.geo.API_KEY")?.trim()?.takeIf { it.isNotBlank() }
  }.getOrNull()
  override val httpClientFactory: BiziHttpClientFactory = AndroidHttpClientFactory()
  private val androidLocalNotifier = AndroidLocalNotifier(context)
  override val localNotifier: LocalNotifier = androidLocalNotifier
  override val locationProvider: LocationProvider = AndroidLocationProvider(context)
  override val mapSupport: MapSupport = AndroidMapSupport(context)
  override val platform: String = "android"
  override val routeLauncher: RouteLauncher = AndroidRouteLauncher(context)
  override val secureKeyStore: SecureKeyStore = SecureKeyStore()
  override val storageDirectoryProvider: StorageDirectoryProvider = AndroidStorageDirectoryProvider(context)
  override val watchSyncBridge: WatchSyncBridge = AndroidWatchSyncBridge(context)
  override val appVersion: String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "unknown"
  }.getOrDefault("unknown")

  /** Provide an Activity-backed requester so [AndroidLocalNotifier] can actually trigger the
   *  POST_NOTIFICATIONS runtime-permission dialog on Android 13+. Call this from
   *  [androidx.activity.ComponentActivity] after registering your [ActivityResultLauncher]. */
  fun bindNotificationPermissionRequester(requester: suspend () -> Boolean) {
    androidLocalNotifier.permissionRequester = requester
  }
}

private class AndroidHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient = HttpClient(OkHttp) {
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
    val navigationUri = Uri.parse("google.navigation:q=${station.location.latitude},${station.location.longitude}&mode=w")
    val intent = Intent(Intent.ACTION_VIEW, navigationUri).apply {
      setPackage("com.google.android.apps.maps")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val fallbackIntent = Intent(
      Intent.ACTION_VIEW,
      Uri.parse("geo:${station.location.latitude},${station.location.longitude}?q=${Uri.encode(station.name)}"),
    ).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val launchIntent = if (intent.resolveActivity(context.packageManager) != null) intent else fallbackIntent
    context.startActivity(launchIntent)
  }

  override fun launchWalkToLocation(destination: GeoPoint) {
    val navigationUri = Uri.parse("google.navigation:q=${destination.latitude},${destination.longitude}&mode=w")
    val intent = Intent(Intent.ACTION_VIEW, navigationUri).apply {
      setPackage("com.google.android.apps.maps")
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val fallbackIntent = Intent(
      Intent.ACTION_VIEW,
      Uri.parse("geo:${destination.latitude},${destination.longitude}"),
    ).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val launchIntent = if (intent.resolveActivity(context.packageManager) != null) intent else fallbackIntent
    context.startActivity(launchIntent)
  }
}

private class AndroidMapSupport(
  private val context: Context,
) : MapSupport {
  override fun currentStatus(): MapSupportStatus {
    val apiKey = runCatching {
      val packageManager = context.packageManager
      val applicationInfo = packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA,
      )
      applicationInfo.metaData?.getString("com.google.android.geo.API_KEY").orEmpty().trim()
    }.getOrDefault("")

    return MapSupportStatus(
      embeddedProvider = EmbeddedMapProvider.GoogleMaps,
      googleMapsSdkLinked = true,
      googleMapsApiKeyConfigured = apiKey.isNotBlank(),
    )
  }
}

private class AndroidLocationProvider(
  private val context: Context,
) : LocationProvider {
  private val fusedLocationClient by lazy(LazyThreadSafetyMode.NONE) {
    LocationServices.getFusedLocationProviderClient(context)
  }

  @SuppressLint("MissingPermission")
  override suspend fun currentLocation(): GeoPoint? {
    if (!hasLocationPermission()) return null
    val currentLocation = runCatching {
      val cancellationTokenSource = CancellationTokenSource()
      fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        cancellationTokenSource.token,
      ).await()
    }.getOrNull()

    val bestLocation = currentLocation ?: runCatching {
      fusedLocationClient.lastLocation.await()
    }.getOrNull()

    return bestLocation?.let { location ->
      GeoPoint(latitude = location.latitude, longitude = location.longitude)
    }
  }

  private fun hasLocationPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
      ) == PackageManager.PERMISSION_GRANTED
  }
}

private class AndroidWatchSyncBridge(
  context: Context,
) : WatchSyncBridge {
  private val dataClient = runCatching { Wearable.getDataClient(context) }.getOrNull()

  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    val client = dataClient ?: return
    runCatching {
      val request = PutDataMapRequest.create(FAVORITES_PATH).apply {
        dataMap.putStringArrayList(FAVORITES_KEY, ArrayList(snapshot.favoriteIds))
        snapshot.homeStationId?.let { dataMap.putString(HOME_STATION_KEY, it) } ?: dataMap.remove(HOME_STATION_KEY)
        snapshot.workStationId?.let { dataMap.putString(WORK_STATION_KEY, it) } ?: dataMap.remove(WORK_STATION_KEY)
        dataMap.putLong(UPDATED_AT_KEY, System.currentTimeMillis())
      }.asPutDataRequest().setUrgent()
      client.putDataItem(request).await()
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? {
    val client = dataClient ?: return null
    return runCatching {
      val dataItems = client.getDataItems(buildFavoritesUri()).await()
      try {
        if (dataItems.count == 0) return@runCatching null
        val dataMap = DataMapItem.fromDataItem(dataItems.get(0)).dataMap
        FavoritesSyncSnapshot(
          favoriteIds = dataMap.getStringArrayList(FAVORITES_KEY)?.toSet().orEmpty(),
          homeStationId = dataMap.getString(HOME_STATION_KEY)?.takeIf { it.isNotBlank() },
          workStationId = dataMap.getString(WORK_STATION_KEY)?.takeIf { it.isNotBlank() },
        ).takeIf { it.favoriteIds.isNotEmpty() || it.homeStationId != null || it.workStationId != null }
      } finally {
        dataItems.release()
      }
    }.getOrNull()
  }

  private fun buildFavoritesUri(): Uri = Uri.Builder()
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

  override suspend fun requestPermission(): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    // If an Activity-backed requester is available, use it to show the system dialog.
    permissionRequester?.let { return it() }
    // Fallback: check current grant state without prompting.
    return ContextCompat.checkSelfPermission(
      context,
      Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
  }

  @SuppressLint("MissingPermission")
  override suspend fun notify(title: String, body: String) {
    if (!requestPermission()) return
    val notification = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(android.R.drawable.ic_dialog_info)
      .setContentTitle(title)
      .setContentText(body)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .build()
    notificationManager.notify(System.currentTimeMillis().toInt(), notification)
  }

  private fun ensureChannel() {
    val channel = NotificationChannel(
      channelId,
      "Bizi Viaje",
      NotificationManager.IMPORTANCE_HIGH,
    ).apply {
      description = "Notificaciones de monitorización de viaje en Bizi"
    }
    notificationManager.createNotificationChannel(channel)
  }
}
