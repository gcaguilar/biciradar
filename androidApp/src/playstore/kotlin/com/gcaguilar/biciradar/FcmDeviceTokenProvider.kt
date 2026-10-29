package com.gcaguilar.biciradar

import android.content.Context
import com.gcaguilar.biciradar.core.push.DeviceTokenProvider
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** FCM-backed [DeviceTokenProvider] for the Play Store flavor. */
class FcmDeviceTokenProvider(
  private val context: Context,
) : DeviceTokenProvider {
  override val platform: String = "android"
  override val appId: String? = context.packageName
  override val locale: String? =
    context.resources.configuration.locales[0]
      .toLanguageTag()
  override val pushEnvironment: String = if (BuildConfig.DEBUG) "sandbox" else "production"

  override suspend fun currentToken(): String? =
    withContext(Dispatchers.IO) {
      runCatching { Tasks.await(FirebaseMessaging.getInstance().token) }.getOrNull()
    }
}
