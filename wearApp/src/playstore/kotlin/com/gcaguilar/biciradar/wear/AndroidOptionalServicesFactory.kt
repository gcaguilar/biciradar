package com.gcaguilar.biciradar.wear

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.CrashlyticsReporter
import com.gcaguilar.biciradar.core.FavoritesSyncCodec
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.RemoteConfigProvider
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.platform.AndroidOptionalServices
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMap
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AndroidOptionalServicesFactory {
  @JvmStatic
  fun create(context: Context): AndroidOptionalServices = WearAndroidOptionalServices(context.applicationContext)
}

private class WearAndroidOptionalServices(
  private val context: Context,
) : AndroidOptionalServices {
  override val crashlyticsReporter: CrashlyticsReporter =
    object : CrashlyticsReporter {
      override fun reportNonFatal(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
      }
    }

  override val remoteConfigProvider: RemoteConfigProvider? = null

  override fun createReviewPrompter(activityProvider: () -> android.app.Activity?): ReviewPrompter? = null

  override fun createAppUpdatePrompter(
    activityProvider: () -> android.app.Activity?,
    launcher: ActivityResultLauncher<IntentSenderRequest>?,
  ): AppUpdatePrompter? = null

  override fun createWatchSyncBridge(): WatchSyncBridge = WearPlaystoreWatchSyncBridge(context)
}

private class WearPlaystoreWatchSyncBridge(
  context: Context,
) : WatchSyncBridge {
  private val dataClient = runCatching { Wearable.getDataClient(context.applicationContext) }.getOrNull()
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

  override fun bindOnRemoteFavoritesChanged(listener: suspend () -> Unit) {
    onRemoteFavoritesChanged = listener
    if (listenerRegistered) return
    dataClient?.addListener(dataChangedListener)
    listenerRegistered = true
  }

  override suspend fun pushFavorites(snapshot: FavoritesSyncSnapshot) {
    val client = dataClient ?: return
    // The repository seals updatedAt on every local mutation; fall back to now for
    // snapshots produced by older callers that still use the default of 0.
    val updatedAt = snapshot.updatedAt.takeIf { it > 0 } ?: System.currentTimeMillis()
    val payload = FavoritesSyncCodec.encode(snapshot.copy(updatedAt = updatedAt))
    runCatching {
      withContext(Dispatchers.IO) {
        val request =
          PutDataMapRequest
            .create(FAVORITES_PATH)
            .apply {
              dataMap.putString(PAYLOAD_JSON_KEY, payload)
              dataMap.putStringArrayList(FAVORITES_KEY, ArrayList(snapshot.favoriteIds))
              snapshot.homeStationId?.let { dataMap.putString(HOME_STATION_KEY, it) }
                ?: dataMap.remove(HOME_STATION_KEY)
              snapshot.workStationId?.let { dataMap.putString(WORK_STATION_KEY, it) }
                ?: dataMap.remove(WORK_STATION_KEY)
              dataMap.putLong(UPDATED_AT_KEY, updatedAt)
            }.asPutDataRequest()
            .setUrgent()
        Tasks.await(client.putDataItem(request))
      }
    }.onFailure { error ->
      Log.w(TAG, "Failed to push favorites snapshot", error)
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? {
    val client = dataClient ?: return null
    return runCatching {
      withContext(Dispatchers.IO) {
        val dataItems = Tasks.await(client.getDataItems(buildFavoritesUri()))
        try {
          // Every node stores its own DataItem under the same path, so pick the
          // newest one by `updated_at` instead of trusting item order.
          var newestDataMap: DataMap? = null
          var newestUpdatedAt = Long.MIN_VALUE
          for (index in 0 until dataItems.count) {
            val dataMap = DataMapItem.fromDataItem(dataItems.get(index)).dataMap
            val updatedAt = dataMap.getLong(UPDATED_AT_KEY, 0L)
            if (newestDataMap == null || updatedAt > newestUpdatedAt) {
              newestDataMap = dataMap
              newestUpdatedAt = updatedAt
            }
          }
          newestDataMap?.let(::decodeSnapshot)
        } finally {
          dataItems.release()
        }
      }
    }.onFailure { error ->
      Log.w(TAG, "Failed to read favorites snapshot", error)
    }.getOrNull()
  }

  private fun decodeSnapshot(dataMap: DataMap): FavoritesSyncSnapshot {
    val updatedAt = dataMap.getLong(UPDATED_AT_KEY, 0L)
    val decoded = dataMap.getString(PAYLOAD_JSON_KEY)?.let(FavoritesSyncCodec::decode)
    if (decoded != null) {
      return if (decoded.updatedAt > 0) decoded else decoded.copy(updatedAt = updatedAt)
    }
    return FavoritesSyncSnapshot(
      favoriteIds = dataMap.getStringArrayList(FAVORITES_KEY)?.toSet().orEmpty(),
      homeStationId = dataMap.getString(HOME_STATION_KEY)?.takeIf { it.isNotBlank() },
      workStationId = dataMap.getString(WORK_STATION_KEY)?.takeIf { it.isNotBlank() },
      updatedAt = updatedAt,
    )
  }

  private fun buildFavoritesUri(): Uri =
    Uri
      .Builder()
      .scheme(PutDataRequest.WEAR_URI_SCHEME)
      .path(FAVORITES_PATH)
      .build()

  private companion object {
    const val TAG = "WatchSyncBridge"
    const val FAVORITES_PATH = "/bizi/favorites"
    const val FAVORITES_KEY = "favorite_ids"
    const val HOME_STATION_KEY = "home_station_id"
    const val WORK_STATION_KEY = "work_station_id"
    const val UPDATED_AT_KEY = "updated_at"
    const val PAYLOAD_JSON_KEY = "payload_json"
  }
}
