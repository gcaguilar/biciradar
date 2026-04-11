package com.gcaguilar.biciradar.core.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.ExternalLinks
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.requestAppUpdateInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.tasks.await

internal class AndroidExternalLinks(
  private val context: Context,
  private val appConfiguration: AppConfiguration,
) : ExternalLinks {
  override fun openFeedbackForm() {
    openUri(appConfiguration.feedbackFormUrl)
  }

  fun openPrivacyPolicy() {
    openUri(appConfiguration.privacyPolicyUrl)
  }

  private fun openUri(uriString: String) {
    val intent =
      Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
    context.startActivity(intent)
  }
}

internal class AndroidPermissionPrompter(
  private val context: Context,
) : PermissionPrompter {
  var locationPermissionRequester: (suspend () -> Boolean)? = null

  override suspend fun hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
      context,
      android.Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
      ) == PackageManager.PERMISSION_GRANTED

  override suspend fun requestLocationPermission(): Boolean {
    locationPermissionRequester?.let { return it() }
    return hasLocationPermission()
  }
}

internal class AndroidReviewPrompter(
  private val context: Context,
  private val activityProvider: () -> Activity?,
) : ReviewPrompter {
  override suspend fun requestInAppReview() {
    val activity = activityProvider() ?: return
    val manager = ReviewManagerFactory.create(context)
    val info = runCatching { manager.requestReviewFlow().await() }.getOrNull() ?: return
    runCatching { manager.launchReviewFlow(activity, info).await() }
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
    val info = runCatching { manager.requestReviewFlow().await() }.getOrNull()
    if (info == null) {
      openStoreWriteReview()
      return
    }
    val launched =
      runCatching {
        manager.launchReviewFlow(activity, info).await()
        true
      }.getOrDefault(false)
    if (!launched) {
      openStoreWriteReview()
    }
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

internal class AndroidAppUpdatePrompter(
  private val context: Context,
  private val activityProvider: () -> Activity?,
) : AppUpdatePrompter {
  private val appUpdateManager: AppUpdateManager by lazy(LazyThreadSafetyMode.NONE) {
    AppUpdateManagerFactory.create(context)
  }

  override suspend fun checkForUpdate(): UpdateAvailabilityState {
    val info =
      runCatching { appUpdateManager.requestAppUpdateInfo() }.getOrNull()
        ?: return UpdateAvailabilityState.Unknown
    val isDownloaded = info.installStatus() == InstallStatus.DOWNLOADED
    val isAvailable =
      info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
        info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
    val versionName = info.availableVersionCode().toString()
    return when {
      isDownloaded -> UpdateAvailabilityState.Downloaded(versionName = versionName)
      isAvailable ->
        UpdateAvailabilityState.Available(
          versionName = versionName,
          storeUrl = "market://details?id=${context.packageName}",
          isFlexibleAllowed = true,
        )
      info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE ->
        UpdateAvailabilityState.Available(
          versionName = versionName,
          storeUrl = "market://details?id=${context.packageName}",
          isFlexibleAllowed = false,
        )
      else -> UpdateAvailabilityState.Unavailable
    }
  }

  override suspend fun startFlexibleUpdate(): Boolean {
    val activity = activityProvider() ?: return false
    val info = runCatching { appUpdateManager.requestAppUpdateInfo() }.getOrNull() ?: return false
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
    val info = runCatching { appUpdateManager.requestAppUpdateInfo() }.getOrNull() ?: return false
    if (info.installStatus() != InstallStatus.DOWNLOADED) return false
    return runCatching {
      appUpdateManager.completeUpdate().await()
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

  companion object {
    const val ANDROID_FLEXIBLE_UPDATE_REQUEST_CODE = 1207
  }
}
