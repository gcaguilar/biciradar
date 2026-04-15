package com.gcaguilar.biciradar.wear

import android.content.Context
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class WearPhoneRouteRequester(
  context: Context,
) {
  private val appContext = context.applicationContext
  private val capabilityClient by lazy(LazyThreadSafetyMode.NONE) { Wearable.getCapabilityClient(appContext) }
  private val messageClient by lazy(LazyThreadSafetyMode.NONE) { Wearable.getMessageClient(appContext) }

  suspend fun requestRoute(stationId: String): Boolean {
    val capabilityInfo =
      runCatching {
        capabilityClient
          .getCapability(PHONE_ROUTE_CAPABILITY, CapabilityClient.FILTER_REACHABLE)
          .await()
      }.getOrNull() ?: return false

    val nodeId = capabilityInfo.nodes.firstOrNull()?.id ?: return false
    return runCatching {
      messageClient.sendMessage(nodeId, ROUTE_TO_STATION_PATH, stationId.encodeToByteArray()).await()
      true
    }.getOrDefault(false)
  }

  companion object {
    const val PHONE_ROUTE_CAPABILITY = "biciradar_phone_route"
    const val ROUTE_TO_STATION_PATH = "/bici/route-to-station"
  }
}
