package com.gcaguilar.biciradar.wear

import android.content.Context
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

  fun setOnAvailabilityChanged(listener: (Boolean) -> Unit) {
    runCatching {
      val target = delegate ?: return
      val method = target.javaClass.getMethod("setOnAvailabilityChanged", Function1::class.java)
      method.invoke(target, listener)
    }
  }

  fun clear() {
    runCatching {
      val target = delegate ?: return
      target.javaClass.getMethod("clear").invoke(target)
    }
  }

  suspend fun requestRoute(stationId: String): Boolean {
    val target = delegate ?: return false
    val callbackMethod =
      runCatching {
        target.javaClass.getMethod("requestRouteAsync", String::class.java, Function1::class.java)
      }.getOrNull()
    if (callbackMethod == null) {
      return runCatching {
        val method = target.javaClass.getMethod("requestRoute", String::class.java)
        method.invoke(target, stationId) as? Boolean ?: false
      }.getOrDefault(false)
    }
    return runCatching {
      suspendCancellableCoroutine { continuation ->
        val callback: (Boolean) -> Unit = { delivered ->
          if (continuation.isActive) continuation.resume(delivered)
        }
        callbackMethod.invoke(target, stationId, callback)
      }
    }.getOrDefault(false)
  }
}
