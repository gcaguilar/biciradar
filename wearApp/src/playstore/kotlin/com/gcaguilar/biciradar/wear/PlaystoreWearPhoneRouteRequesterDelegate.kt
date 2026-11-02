package com.gcaguilar.biciradar.wear

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.Wearable
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor

/**
 * Sends a "route to station" request to the phone.
 *
 * Availability is cached and kept up to date by a [CapabilityClient] listener, so
 * [isRouteAvailable] is a plain in-memory read and is safe to call from the main thread.
 *
 * The route itself is launched with [RemoteActivityHelper]: Play Services starts the
 * phone activity on our behalf, so it is exempt from the Android 10+ background
 * activity launch restrictions and the watch shows the standard "Opening on phone"
 * progress UI.
 */
class PlaystoreWearPhoneRouteRequesterDelegate(
  context: Context,
) {
  private val appContext = context.applicationContext
  private val capabilityClient by lazy(LazyThreadSafetyMode.NONE) { Wearable.getCapabilityClient(appContext) }
  private val remoteActivityHelper by lazy(LazyThreadSafetyMode.NONE) { RemoteActivityHelper(appContext) }

  @Volatile
  private var routeAvailable: Boolean = false

  @Volatile
  private var phoneNodeId: String? = null

  @Volatile
  private var onAvailabilityChanged: ((Boolean) -> Unit)? = null

  private val capabilityListener =
    CapabilityClient.OnCapabilityChangedListener { capabilityInfo ->
      applyCapabilityInfo(capabilityInfo)
    }

  init {
    runCatching {
      capabilityClient.addListener(capabilityListener, PHONE_ROUTE_CAPABILITY)
    }
    refreshAvailability()
  }

  fun isRouteAvailable(): Boolean = routeAvailable

  fun setOnAvailabilityChanged(listener: ((Boolean) -> Unit)?) {
    onAvailabilityChanged = listener
    listener?.invoke(routeAvailable)
  }

  fun clear() {
    runCatching { capabilityClient.removeListener(capabilityListener) }
    onAvailabilityChanged = null
  }

  /**
   * Fire-and-forget contract kept for the reflective bridge. Returns whether the
   * remote intent was handed off; use [requestRouteAsync] when the delivery result matters.
   */
  fun requestRoute(stationId: String): Boolean {
    val future = runCatching { dispatchRoute(stationId) }.getOrNull()
    return future != null
  }

  /**
   * Dispatches the remote intent and reports the delivery result through [onResult]
   * without ever blocking the caller.
   */
  fun requestRouteAsync(
    stationId: String,
    onResult: (Boolean) -> Unit,
  ) {
    val future = runCatching { dispatchRoute(stationId) }.getOrNull()
    if (future == null) {
      onResult(false)
      return
    }
    future.addListener(
      {
        val delivered = runCatching { future.get() }.isSuccess
        if (!delivered) {
          applyAvailability(available = false)
        }
        onResult(delivered)
      },
      DIRECT_EXECUTOR,
    )
  }

  private fun dispatchRoute(stationId: String): ListenableFuture<Void>? {
    if (!routeAvailable) return null
    val uri =
      Uri
        .Builder()
        .scheme(DEEP_LINK_SCHEME)
        .authority(STATION_HOST)
        .appendPath(stationId)
        .appendQueryParameter(ACTION_QUERY_PARAMETER, ROUTE_TO_STATION_ACTION)
        .build()
    val intent =
      Intent(Intent.ACTION_VIEW, uri).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
      }
    return remoteActivityHelper.startRemoteActivity(intent, phoneNodeId)
  }

  private fun refreshAvailability() {
    runCatching {
      capabilityClient
        .getCapability(PHONE_ROUTE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
        .addOnSuccessListener { capabilityInfo -> applyCapabilityInfo(capabilityInfo) }
        .addOnFailureListener { /* Leave the cached value untouched. */ }
    }
  }

  private fun applyCapabilityInfo(capabilityInfo: CapabilityInfo) {
    if (capabilityInfo.name != PHONE_ROUTE_CAPABILITY) return
    phoneNodeId = capabilityInfo.nodes.firstOrNull()?.id
    applyAvailability(available = phoneNodeId != null)
  }

  private fun applyAvailability(available: Boolean) {
    if (routeAvailable == available) return
    routeAvailable = available
    onAvailabilityChanged?.invoke(available)
  }

  companion object {
    const val PHONE_ROUTE_CAPABILITY = "biciradar_phone_route"

    private const val DEEP_LINK_SCHEME = "biciradar"
    private const val STATION_HOST = "station"
    private const val ACTION_QUERY_PARAMETER = "action"
    private const val ROUTE_TO_STATION_ACTION = "route_to_station"
    private val DIRECT_EXECUTOR = Executor { runnable -> runnable.run() }
  }
}
