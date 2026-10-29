package com.gcaguilar.biciradar.core.push

/**
 * Platform source of the current push registration token. Implemented on
 * Android (FCM) and iOS (APNs via FCM); defaults to a no-op on wearable/desktop.
 */
interface DeviceTokenProvider {
  val platform: String
  val appId: String?
  val locale: String?

  /** `sandbox` for development builds, `production` for TestFlight/App Store. */
  val pushEnvironment: String

  suspend fun currentToken(): String?
}

object NoOpDeviceTokenProvider : DeviceTokenProvider {
  override val platform: String = "unknown"
  override val appId: String? = null
  override val locale: String? = null
  override val pushEnvironment: String = "production"

  override suspend fun currentToken(): String? = null
}
