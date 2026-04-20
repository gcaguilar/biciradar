package com.gcaguilar.biciradar.wear

import android.content.Context
import android.net.Uri
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.CrashlyticsReporter
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.RemoteConfigProvider
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.platform.AndroidOptionalServices
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.PutDataRequest
import com.google.android.gms.wearable.Wearable
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

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

  override fun createAppUpdatePrompter(activityProvider: () -> android.app.Activity?): AppUpdatePrompter? = null

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
    runCatching {
      val request =
        PutDataMapRequest
          .create(FAVORITES_PATH)
          .apply {
            dataMap.putStringArrayList(FAVORITES_KEY, ArrayList(snapshot.favoriteIds))
            snapshot.homeStationId?.let { dataMap.putString(HOME_STATION_KEY, it) } ?: dataMap.remove(HOME_STATION_KEY)
            snapshot.workStationId?.let { dataMap.putString(WORK_STATION_KEY, it) } ?: dataMap.remove(WORK_STATION_KEY)
            dataMap.putLong(UPDATED_AT_KEY, System.currentTimeMillis())
          }.asPutDataRequest()
          .setUrgent()
      Tasks.await(client.putDataItem(request))
    }
  }

  override suspend fun latestFavorites(): FavoritesSyncSnapshot? {
    val client = dataClient ?: return null
    return runCatching {
      val dataItems = Tasks.await(client.getDataItems(buildFavoritesUri()))
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
