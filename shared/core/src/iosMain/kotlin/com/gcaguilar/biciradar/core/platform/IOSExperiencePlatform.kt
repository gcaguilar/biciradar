package com.gcaguilar.biciradar.core.platform

import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.AppUpdatePrompter
import com.gcaguilar.biciradar.core.ExternalLinks
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.ReviewPrompter
import com.gcaguilar.biciradar.core.UpdateAvailabilityState
import com.gcaguilar.biciradar.core.compareAppVersionStrings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Serializable
private data class ItunesLookupResponse(
  val results: List<ItunesLookupResult> = emptyList(),
)

@Serializable
private data class ItunesLookupResult(
  val version: String? = null,
)

internal class IOSExternalLinksImpl(
  private val appConfiguration: AppConfiguration,
) : ExternalLinks {
  override fun openFeedbackForm() {
    val url = NSURL.URLWithString(appConfiguration.feedbackFormUrl) ?: return
    UIApplication.sharedApplication.openURL(
      url = url,
      options = emptyMap<Any?, Any>(),
      completionHandler = null,
    )
  }
}

internal class IOSPermissionPrompterImpl : PermissionPrompter {
  override suspend fun hasLocationPermission(): Boolean {
    val status = CLLocationManager.authorizationStatus()
    return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
      status == kCLAuthorizationStatusAuthorizedAlways
  }

  override suspend fun requestLocationPermission(): Boolean {
    CLLocationManager().requestWhenInUseAuthorization()
    return hasLocationPermission()
  }
}

internal class IOSReviewPrompterImpl(
  private val appConfiguration: AppConfiguration,
) : ReviewPrompter {
  override suspend fun requestInAppReview() {
    // StoreKit scene-based prompt is invoked from Swift if needed; silent no-op here avoids brittle objc interop.
  }

  override fun openStoreWriteReview() {
    val urlString = appConfiguration.iosAppStoreUrl ?: return
    NSURL.URLWithString(urlString)?.let { url ->
      UIApplication.sharedApplication.openURL(
        url = url,
        options = emptyMap<Any?, Any>(),
        completionHandler = null,
      )
    }
  }
}

internal class IOSAppUpdatePrompterImpl(
  private val appConfiguration: AppConfiguration,
  private val httpClient: HttpClient,
  private val json: Json,
  private val currentAppVersion: String,
) : AppUpdatePrompter {
  override suspend fun checkForUpdate(): UpdateAvailabilityState {
    val bundleId = appConfiguration.iosAppBundleId.trim()
    if (bundleId.isBlank() || appConfiguration.iosAppStoreId.isBlank()) {
      return UpdateAvailabilityState.Unavailable
    }
    val url = "https://itunes.apple.com/lookup?bundleId=$bundleId"
    return runCatching {
      val body = httpClient.get(url).body<String>()
      val response = json.decodeFromString<ItunesLookupResponse>(body)
      val remoteVersion = response.results.firstOrNull()?.version?.trim()?.takeIf { it.isNotBlank() }
        ?: return@runCatching UpdateAvailabilityState.Unavailable
      if (compareAppVersionStrings(remoteVersion, currentAppVersion) > 0) {
        val storeUrl = appConfiguration.iosAppStoreUrl
          ?: "https://apps.apple.com/app/id${appConfiguration.iosAppStoreId}"
        UpdateAvailabilityState.Available(
          versionName = remoteVersion,
          storeUrl = storeUrl,
          isFlexibleAllowed = false,
        )
      } else {
        UpdateAvailabilityState.Unavailable
      }
    }.getOrElse { UpdateAvailabilityState.Unknown }
  }

  override suspend fun startFlexibleUpdate(): Boolean = false

  override suspend fun completeFlexibleUpdateIfReady(): Boolean = false

  override fun openStoreListing() {
    val url = appConfiguration.iosAppStoreUrl
      ?: "https://apps.apple.com/app/id${appConfiguration.iosAppStoreId}"
    NSURL.URLWithString(url)?.let { storeUrl ->
      UIApplication.sharedApplication.openURL(
        url = storeUrl,
        options = emptyMap<Any?, Any>(),
        completionHandler = null,
      )
    }
  }
}
