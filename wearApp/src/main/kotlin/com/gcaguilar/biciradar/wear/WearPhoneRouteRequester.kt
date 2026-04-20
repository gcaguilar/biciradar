package com.gcaguilar.biciradar.wear

import android.content.Context

class WearPhoneRouteRequester(
  context: Context,
) {
  private val delegate =
    runCatching {
      val delegateClass =
        Class.forName("com.gcaguilar.biciradar.wear.PlaystoreWearPhoneRouteRequesterDelegate")
      delegateClass.getConstructor(Context::class.java).newInstance(context.applicationContext)
    }.getOrNull()

  fun isRouteAvailable(): Boolean =
    runCatching {
      val method = delegate?.javaClass?.getMethod("isRouteAvailable") ?: return false
      method.invoke(delegate) as? Boolean ?: false
    }.getOrDefault(false)

  suspend fun requestRoute(stationId: String): Boolean =
    runCatching {
      val method = delegate?.javaClass?.getMethod("requestRoute", String::class.java) ?: return false
      method.invoke(delegate, stationId) as? Boolean ?: false
    }.getOrDefault(false)
}
