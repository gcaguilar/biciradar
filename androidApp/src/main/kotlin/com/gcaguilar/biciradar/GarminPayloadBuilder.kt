package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsState
import java.util.ArrayList
import java.util.HashMap

internal class GarminPayloadBuilder {
  private val maxNameLength = 20

  fun build(state: StationsState): HashMap<String, Any?> = build(state.stations, state.lastUpdatedEpoch)

  fun build(
    stations: List<Station>,
    lastUpdatedEpochMs: Long? = null,
  ): HashMap<String, Any?> {
    val payload = HashMap<String, Any?>()
    payload["nearest"] = stations.firstOrNull()?.toPayload()
    payload["backup"] = ArrayList(stations.drop(1).take(4).map { it.toPayload() })
    payload["timestamp"] = ((lastUpdatedEpochMs ?: System.currentTimeMillis()) / 1000L).toInt()
    return payload
  }

  private fun Station.toPayload(): HashMap<String, Any> {
    val payload = HashMap<String, Any>()
    payload["id"] = id
    payload["name"] = truncateName(name)
    payload["bikes"] = bikesAvailable
    payload["distance"] = distanceMeters
    payload["ebikes"] = ebikesAvailable
    return payload
  }

  private fun truncateName(name: String): String =
    if (name.length > maxNameLength) {
      name.take(maxNameLength - 3) + "..."
    } else {
      name
    }
}
