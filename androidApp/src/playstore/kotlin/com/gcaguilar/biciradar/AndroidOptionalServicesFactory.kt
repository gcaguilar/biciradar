package com.gcaguilar.biciradar

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.WatchMonitoringBridge
import com.gcaguilar.biciradar.core.WatchMonitoringCommandResult
import com.gcaguilar.biciradar.core.WatchMonitoringProtocol
import com.gcaguilar.biciradar.core.WatchMonitoringResultPayload
import com.gcaguilar.biciradar.core.WatchMonitoringSessionsPayload
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
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object AndroidOptionalServicesFactory {
  @JvmStatic
  fun create(context: Context): AndroidOptionalServices = PhoneAndroidOptionalServices(context.applicationContext)
}

private class PhoneAndroidOptionalServices(
  private val context: Context,
) : AndroidOptionalServices {
  override val crashlyticsReporter: CrashlyticsReporter =
    object : CrashlyticsReporter {
      override fun reportNonFatal(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
      }
    }

  override val remoteConfigProvider: RemoteConfigProvider =
    object : RemoteConfigProvider {
      override suspend fun getString(key: String): String? =
        runCatching {
          val remoteConfig = FirebaseRemoteConfig.getInstance()
          val settings =
            FirebaseRemoteConfigSettings
              .Builder()
              .setMinimumFetchIntervalInSeconds(3_600)
              .build()
          Tasks.await(remoteConfig.setConfigSettingsAsync(settings))
          Tasks.await(remoteConfig.fetchAndActivate())
          remoteConfig.getString(key).takeIf { it.isNotBlank() }
        }.getOrNull()
    }

  override fun createReviewPrompter(activityProvider: () -> Activity?): ReviewPrompter =
    PlaystoreReviewPrompter(context, activityProvider)

  override fun createAppUpdatePrompter(
    activityProvider: () -> Activity?,
    launcher: ActivityResultLauncher<IntentSenderRequest>?,
  ): AppUpdatePrompter = PlaystoreAppUpdatePrompter(context, activityProvider, launcher)

  override fun createWatchSyncBridge(): WatchSyncBridge = PlaystoreWatchSyncBridge(context)

  override fun createWatchMonitoringBridge(): WatchMonitoringBridge = PlaystoreWatchMonitoringBridge(context)

  override fun createDeviceTokenProvider(): com.gcaguilar.biciradar.core.push.DeviceTokenProvider =
    FcmDeviceTokenProvider(context)
}

private class PlaystoreReviewPrompter(
  private val context: Context,
  private val activityProvider: () -> Activity?,
) : ReviewPrompter {
  override suspend fun requestInAppReview(): Boolean {
    val activity = activityProvider() ?: return false
    val manager = ReviewManagerFactory.create(context)
    // requestReviewFlow only performs a lightweight in-task check; keep it off the main thread.
    val info =
      withContext(Dispatchers.IO) {
        runCatching { Tasks.await(manager.requestReviewFlow()) }.getOrNull()
      } ?: return false
    // launchReviewFlow shows a UI and must run on the main thread. Do not await it: the task
    // completes only after the user dismisses the (optional) dialog.
    return withContext(Dispatchers.Main) {
      runCatching { manager.launchReviewFlow(activity, info) }.isSuccess
    }
  }

  override fun openStoreWriteReview() {
    openStoreUri("market://details?id=${context.packageName}&showAllReviews=true")
  }

  override suspend fun requestInAppReviewOrStoreFallback() {
    val activity = activityProvider()
    if (activity == null) {
      openStoreWriteReview()
      return
    }
    val manager = ReviewManagerFactory.create(context)
    val info =
      withContext(Dispatchers.IO) {
        runCatching { Tasks.await(manager.requestReviewFlow()) }.getOrNull()
      }
    if (info == null) {
      openStoreWriteReview()
      return
    }
    val launched =
      withContext(Dispatchers.Main) {
        runCatching { manager.launchReviewFlow(activity, info) }.isSuccess
      }
    if (!launched) openStoreWriteReview()
  }

  private fun openStoreUri(uriString: String) {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.android.vending")
      }
    val fallback =
      Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"),
      ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    val target = if (intent.resolveActivity(context.packageManager) != null) intent else fallback
    context.startActivity(target)
  }
}

private class PlaystoreAppUpdatePrompter(
  private val context: Context,
  private val activityProvider: () -> Activity?,
  private val launcher: ActivityResultLauncher<IntentSenderRequest>?,
) : AppUpdatePrompter {
  private val appUpdateManager: AppUpdateManager by lazy(LazyThreadSafetyMode.NONE) {
    AppUpdateManagerFactory.create(context)
  }

  override suspend fun checkForUpdate(): UpdateAvailabilityState {
    val info =
      runCatching { Tasks.await(appUpdateManager.appUpdateInfo) }.getOrNull()
        ?: return UpdateAvailabilityState.Unknown
    val isDownloaded = info.installStatus() == InstallStatus.DOWNLOADED
    val isAvailable =
      info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
    val versionName = info.availableVersionCode().toString()
    return when {
      isDownloaded -> {
        UpdateAvailabilityState.Downloaded(versionName)
      }

      isAvailable -> {
        UpdateAvailabilityState.Available(versionName, "market://details?id=${context.packageName}", true)
      }

      info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
        UpdateAvailabilityState.Available(versionName, "market://details?id=${context.packageName}", false)
      }

      else -> {
        UpdateAvailabilityState.Unavailable
      }
    }
  }

  override suspend fun startFlexibleUpdate(): Boolean {
    val activity = activityProvider() ?: return false
    val info = runCatching { Tasks.await(appUpdateManager.appUpdateInfo) }.getOrNull() ?: return false
    if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return false
    val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
    return if (launcher != null) {
      runCatching {
        appUpdateManager.startUpdateFlowForResult(
          info,
          launcher!!,
          options,
        )
        true
      }.getOrDefault(false)
    } else {
      runCatching {
        appUpdateManager.startUpdateFlowForResult(
          info,
          AppUpdateType.FLEXIBLE,
          activity,
          ANDROID_FLEXIBLE_UPDATE_REQUEST_CODE,
        )
        true
      }.getOrDefault(false)
    }
  }

  override suspend fun completeFlexibleUpdateIfReady(): Boolean {
    val info = runCatching { Tasks.await(appUpdateManager.appUpdateInfo) }.getOrNull() ?: return false
    if (info.installStatus() != InstallStatus.DOWNLOADED) return false
    return runCatching {
      Tasks.await(appUpdateManager.completeUpdate())
      true
    }.getOrDefault(false)
  }

  override fun openStoreListing() {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        setPackage("com.android.vending")
      }
    val fallback =
      Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"),
      ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    val target = if (intent.resolveActivity(context.packageManager) != null) intent else fallback
    context.startActivity(target)
  }

  private companion object {
    const val ANDROID_FLEXIBLE_UPDATE_REQUEST_CODE = 1207
  }
}

private class PlaystoreWatchSyncBridge(
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

/**
 * Responde al reloj con el resultado de un comando de monitorización. El
 * reloj pide la vigilancia por mensaje; el teléfono la ejecuta en
 * [WearMonitoringCommandService].
 */
private class PlaystoreWatchMonitoringBridge(
  context: Context,
) : WatchMonitoringBridge {
  private val appContext = context.applicationContext
  private val messageClient by lazy { runCatching { Wearable.getMessageClient(appContext) }.getOrNull() }
  private val nodeClient by lazy { runCatching { Wearable.getNodeClient(appContext) }.getOrNull() }
  private val dataClient by lazy { runCatching { Wearable.getDataClient(appContext) }.getOrNull() }

  override suspend fun publishSessions(sessions: List<SurfaceMonitoringSession>) {
    val client = dataClient ?: return
    val payload =
      WatchMonitoringProtocol.encodeSessions(
        WatchMonitoringSessionsPayload(
          updatedAt = System.currentTimeMillis(),
          sessions = sessions,
        ),
      )
    runCatching {
      withContext(Dispatchers.IO) {
        val request =
          PutDataRequest
            .create(WatchMonitoringProtocol.SESSIONS_PATH)
            .setData(payload)
            .setUrgent()
        Tasks.await(client.putDataItem(request))
      }
    }.onFailure { error ->
      Log.w(TAG, "Failed to publish monitoring sessions", error)
    }
  }

  override suspend fun sendResult(result: WatchMonitoringCommandResult) {
    val client = messageClient ?: return
    val payload =
      WatchMonitoringProtocol.encodeResult(
        WatchMonitoringResultPayload(
          stationId = result.stationId,
          success = result.success,
          failure = result.failure?.name,
        ),
      )
    runCatching {
      withContext(Dispatchers.IO) {
        val nodes = nodeClient?.let { Tasks.await(it.connectedNodes) }.orEmpty()
        nodes.forEach { node ->
          runCatching { Tasks.await(client.sendMessage(node.id, WatchMonitoringProtocol.RESULT_PATH, payload)) }
        }
      }
    }.onFailure { error ->
      Log.w(TAG, "Failed to send monitoring result", error)
    }
  }

  private companion object {
    const val TAG = "WatchMonitoringBridge"
  }
}
