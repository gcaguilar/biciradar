package com.gcaguilar.biciradar

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.CrashlyticsReporter
import com.gcaguilar.biciradar.core.FavoritesSyncSnapshot
import com.gcaguilar.biciradar.core.RemoteConfigProvider
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.WatchSyncBridge
import com.gcaguilar.biciradar.core.platform.AndroidOptionalServices
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
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

  override fun createAppUpdatePrompter(activityProvider: () -> Activity?): AppUpdatePrompter =
    PlaystoreAppUpdatePrompter(context, activityProvider)

  override fun createWatchSyncBridge(): WatchSyncBridge = PlaystoreWatchSyncBridge(context)
}

private class PlaystoreReviewPrompter(
  private val context: Context,
  private val activityProvider: () -> Activity?,
) : ReviewPrompter {
  override suspend fun requestInAppReview() {
    val activity = activityProvider() ?: return
    val manager = ReviewManagerFactory.create(context)
    val info = runCatching { Tasks.await(manager.requestReviewFlow()) }.getOrNull() ?: return
    runCatching { Tasks.await(manager.launchReviewFlow(activity, info)) }
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
    val info = runCatching { Tasks.await(manager.requestReviewFlow()) }.getOrNull()
    if (info == null) {
      openStoreWriteReview()
      return
    }
    val launched =
      runCatching {
        Tasks.await(manager.launchReviewFlow(activity, info))
        true
      }.getOrDefault(false)
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
      isDownloaded -> UpdateAvailabilityState.Downloaded(versionName)
      isAvailable -> UpdateAvailabilityState.Available(versionName, "market://details?id=${context.packageName}", true)
      info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ->
        UpdateAvailabilityState.Available(versionName, "market://details?id=${context.packageName}", false)
      else -> UpdateAvailabilityState.Unavailable
    }
  }

  override suspend fun startFlexibleUpdate(): Boolean {
    val activity = activityProvider() ?: return false
    val info = runCatching { Tasks.await(appUpdateManager.appUpdateInfo) }.getOrNull() ?: return false
    if (!info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) return false
    return runCatching {
      appUpdateManager.startUpdateFlowForResult(
        info,
        activity,
        AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
        ANDROID_FLEXIBLE_UPDATE_REQUEST_CODE,
      )
      true
    }.getOrDefault(false)
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
