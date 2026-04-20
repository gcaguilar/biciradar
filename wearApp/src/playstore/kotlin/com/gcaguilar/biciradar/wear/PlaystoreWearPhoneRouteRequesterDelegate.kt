package com.gcaguilar.biciradar.wear

import android.content.Context
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.Wearable

class PlaystoreWearPhoneRouteRequesterDelegate(
  context: Context,
) {
  private val appContext = context.applicationContext
  private val capabilityClient by lazy(LazyThreadSafetyMode.NONE) { Wearable.getCapabilityClient(appContext) }
  private val messageClient by lazy(LazyThreadSafetyMode.NONE) { Wearable.getMessageClient(appContext) }

  fun isRouteAvailable(): Boolean = reachablePhoneNodeId() != null

  fun requestRoute(stationId: String): Boolean {
    val nodeId = reachablePhoneNodeId() ?: return false
    return runCatching {
      Tasks.await(messageClient.sendMessage(nodeId, ROUTE_TO_STATION_PATH, stationId.encodeToByteArray()))
      true
    }.getOrDefault(false)
  }

  private fun reachablePhoneNodeId(): String? {
    val capabilityInfo =
      runCatching {
        Tasks.await(
          capabilityClient.getCapability(PHONE_ROUTE_CAPABILITY, CapabilityClient.FILTER_REACHABLE),
        )
      }.getOrNull() ?: return null
    return capabilityInfo.nodes.firstOrNull()?.id
  }

  companion object {
    const val PHONE_ROUTE_CAPABILITY = "biciradar_phone_route"
    const val ROUTE_TO_STATION_PATH = "/bici/route-to-station"
  }
}
