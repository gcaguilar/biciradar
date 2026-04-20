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

internal class AndroidExternalLinks(
  private val context: Context,
  private val appConfiguration: AppConfiguration,
) : ExternalLinks {
  override fun openFeedbackForm() {
    openUri(appConfiguration.feedbackFormUrl)
  }

  override fun openGarminDevicePairing() = Unit

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
    if (activityProvider() == null) return
    openStoreWriteReview()
  }

  override fun openStoreWriteReview() {
    openStoreUri("market://details?id=${context.packageName}&showAllReviews=true")
  }

  override suspend fun requestInAppReviewOrStoreFallback() {
    openStoreWriteReview()
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
  override suspend fun checkForUpdate(): UpdateAvailabilityState = UpdateAvailabilityState.Unknown

  override suspend fun startFlexibleUpdate(): Boolean = false

  override suspend fun completeFlexibleUpdateIfReady(): Boolean = false

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
}
