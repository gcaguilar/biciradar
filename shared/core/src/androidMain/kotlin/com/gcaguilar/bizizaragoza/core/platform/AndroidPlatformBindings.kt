package com.gcaguilar.bizizaragoza.core.platform

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.gcaguilar.bizizaragoza.core.DefaultAssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.AppConfiguration
import com.gcaguilar.bizizaragoza.core.AssistantIntentResolver
import com.gcaguilar.bizizaragoza.core.BiziHttpClientFactory
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.LocationProvider
import com.gcaguilar.bizizaragoza.core.PlatformBindings
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StorageDirectoryProvider
import com.gcaguilar.bizizaragoza.core.WatchSyncBridge
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okio.FileSystem

class AndroidPlatformBindings(
  private val context: Context,
  override val appConfiguration: AppConfiguration = AppConfiguration(),
) : PlatformBindings {
  override val assistantIntentResolver: AssistantIntentResolver = DefaultAssistantIntentResolver()
  override val fileSystem: FileSystem = FileSystem.SYSTEM
  override val httpClientFactory: BiziHttpClientFactory = AndroidHttpClientFactory()
  override val locationProvider: LocationProvider = AndroidLocationProvider(context)
  override val routeLauncher: RouteLauncher = AndroidRouteLauncher(context)
  override val storageDirectoryProvider: StorageDirectoryProvider = AndroidStorageDirectoryProvider(context)
  override val watchSyncBridge: WatchSyncBridge = AndroidWatchSyncBridge(context)
}

private class AndroidHttpClientFactory : BiziHttpClientFactory {
  override fun create(json: Json): HttpClient = HttpClient(OkHttp) {
    expectSuccess = true
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
    val navigationUri = Uri.parse("google.navigation:q=${station.location.latitude},${station.location.longitude}")
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
  private val dataClient = Wearable.getDataClient(context)

  override suspend fun pushFavoriteIds(favoriteIds: Set<String>) {
    val request = PutDataMapRequest.create(FAVORITES_PATH).apply {
      dataMap.putStringArrayList(FAVORITES_KEY, ArrayList(favoriteIds))
      dataMap.putLong(UPDATED_AT_KEY, System.currentTimeMillis())
    }.asPutDataRequest().setUrgent()
    dataClient.putDataItem(request).await()
  }

  override suspend fun latestFavoriteIds(): Set<String>? {
    val dataItems = dataClient.getDataItems(buildFavoritesUri()).await()
    try {
      if (dataItems.count == 0) return null
      val dataItem = dataItems.get(0)
      return DataMapItem.fromDataItem(dataItem)
        .dataMap
        .getStringArrayList(FAVORITES_KEY)
        ?.toSet()
    } finally {
      dataItems.release()
    }
  }

  private fun buildFavoritesUri(): Uri = Uri.Builder()
    .scheme(PutDataRequest.WEAR_URI_SCHEME)
    .path(FAVORITES_PATH)
    .build()

  private companion object {
    const val FAVORITES_PATH = "/bizi/favorites"
    const val FAVORITES_KEY = "favorite_ids"
    const val UPDATED_AT_KEY = "updated_at"
  }
}
