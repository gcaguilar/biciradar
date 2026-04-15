package com.gcaguilar.biciradar.wear

import android.content.Context
import android.util.Log
import com.gcaguilar.biciradar.core.Station
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

private const val TAG = "GarminBleManager"

data class GarminStationData(
  val id: String,
  val name: String,
  val bikes: Int,
  val distance: Int,
  val ebikes: Int = 0,
)

data class GarminStationsPayload(
  val nearest: GarminStationData?,
  val backup: List<GarminStationData>,
  val timestamp: Long,
)

class GarminPayloadBuilder {
  private val maxNameLength = 20

  fun build(stations: List<Station>): GarminStationsPayload {
    val nearest = stations.firstOrNull()?.toGarminData()
    val backup = stations.drop(1).take(4).map { it.toGarminData() }

    return GarminStationsPayload(
      nearest = nearest,
      backup = backup,
      timestamp = System.currentTimeMillis() / 1000,
    )
  }

  private fun Station.toGarminData() =
    GarminStationData(
      id = id,
      name = truncateName(name),
      bikes = bikesAvailable,
      distance = distanceMeters,
      ebikes = ebikesAvailable,
    )

  private fun truncateName(name: String): String =
    if (name.length > maxNameLength) {
      name.take(maxNameLength - 1) + "…"
    } else {
      name
    }

  fun serializeToJson(payload: GarminStationsPayload): String =
    buildString {
      append("{")
      append("\"nearest\":")
      if (payload.nearest != null) {
        append("{\"id\":\"${payload.nearest.id}\",")
        append("\"name\":\"${payload.nearest.name}\",")
        append("\"bikes\":${payload.nearest.bikes},")
        append("\"distance\":${payload.nearest.distance},")
        append("\"ebikes\":${payload.nearest.ebikes}}")
      } else {
        append("null")
      }
      append(",\"backup\":[")
      append(
        payload.backup.joinToString(",") { station ->
          "{\"id\":\"${station.id}\",\"name\":\"${station.name}\",\"bikes\":${station.bikes},\"distance\":${station.distance},\"ebikes\":${station.ebikes}}"
        },
      )
      append("],\"timestamp\":${payload.timestamp}}")
    }
}

class GarminBleManager(
  private val context: Context,
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val payloadBuilder = GarminPayloadBuilder()
  private var isInitialized = false

  interface Listener {
    fun onGarminDevicesFound(devices: List<Any>)

    fun onGarminMessageReceived(message: Any?)

    fun onGarminError(error: String)
  }

  private var listener: Listener? = null

  fun setListener(listener: Listener?) {
    this.listener = listener
  }

  fun initialize() {
    isInitialized = true
    Log.d(TAG, "Garmin BLE Manager initialized")
  }

  fun sendStationsToGarmin(stations: List<Station>) {
    if (!isInitialized) {
      Log.w(TAG, "Cannot send stations - not initialized")
      return
    }

    scope.launch {
      try {
        val devices = getConnectedDevices()
        listener?.onGarminDevicesFound(devices)

        val payload = payloadBuilder.build(stations)
        val jsonPayload = payloadBuilder.serializeToJson(payload)
        Log.d(TAG, "Sending payload to ${devices.size} devices: $jsonPayload")

        devices.forEach { device ->
          sendMessageToDevice(device, jsonPayload)
        }
      } catch (e: Exception) {
        Log.e(TAG, "Error sending stations to Garmin", e)
        listener?.onGarminError("Error enviando datos: ${e.message}")
      }
    }
  }

  private fun getConnectedDevices(): List<Any> =
    try {
      val connectIQClass = Class.forName("com.garmin.connectiq.ConnectIQ")
      val getInstanceMethod =
        connectIQClass.getMethod(
          "getInstance",
          Context::class.java,
          Class.forName("com.garmin.connectiq.ConnectIQ\$IQConnectType"),
        )
      val connectTypeClass = Class.forName("com.garmin.connectiq.ConnectIQ\$IQConnectType")
      val wirelessEnum = connectTypeClass.enumConstants!!.first { it.toString() == "WIRELESS" }
      val connectIQ = getInstanceMethod.invoke(null, context, wirelessEnum)

      val devices = connectIQ.javaClass.getMethod("getConnectedDevices").invoke(connectIQ)
      @Suppress("UNCHECKED_CAST")
      (devices as? List<Any>) ?: emptyList()
    } catch (e: ClassNotFoundException) {
      Log.d(TAG, "Garmin ConnectIQ SDK not available: ${e.message}")
      emptyList()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to get devices: ${e.message}")
      emptyList()
    }

  private fun sendMessageToDevice(
    device: Any,
    message: String,
  ) {
    try {
      val connectIQClass = Class.forName("com.garmin.connectiq.ConnectIQ")
      val getInstanceMethod =
        connectIQClass.getMethod(
          "getInstance",
          Context::class.java,
          Class.forName("com.garmin.connectiq.ConnectIQ\$IQConnectType"),
        )
      val connectTypeClass = Class.forName("com.garmin.connectiq.ConnectIQ\$IQConnectType")
      val wirelessEnum = connectTypeClass.enumConstants!!.first { it.toString() == "WIRELESS" }
      val connectIQ = getInstanceMethod.invoke(null, context, wirelessEnum)

      val sendMessageMethod =
        connectIQ.javaClass.getMethod(
          "sendMessage",
          device.javaClass,
          java.util.UUID::class.java,
          String::class.java,
          Class.forName("com.garmin.connectiq.ConnectIQ\$IQMessageListener"),
        )
      sendMessageMethod.invoke(connectIQ, device, java.util.UUID.fromString(APP_UUID), message, null)
      listener?.onGarminMessageReceived(null)
    } catch (e: ClassNotFoundException) {
      Log.d(TAG, "Garmin SDK not available: ${e.message}")
      listener?.onGarminError("SDK no disponible")
    } catch (e: Exception) {
      Log.w(TAG, "Failed to send message: ${e.message}")
      listener?.onGarminError("Error: ${e.message}")
    }
  }

  fun shutdown() {
    try {
      val connectIQClass = Class.forName("com.garmin.connectiq.ConnectIQ")
      val getInstanceMethod =
        connectIQClass.getMethod(
          "getInstance",
          Context::class.java,
          Class.forName("com.garmin.connectiq.ConnectIQ\$IQConnectType"),
        )
      val connectTypeClass = Class.forName("com.garmin.connectiq.ConnectIQ\$IQConnectType")
      val wirelessEnum = connectTypeClass.enumConstants!!.first { it.toString() == "WIRELESS" }
      val connectIQ = getInstanceMethod.invoke(null, context, wirelessEnum)
      connectIQ.javaClass.getMethod("shutdown").invoke(connectIQ)
      Log.d(TAG, "Garmin ConnectIQ shutdown")
    } catch (e: Exception) {
      Log.d(TAG, "Shutdown skipped: ${e.message}")
    }
    isInitialized = false
  }

  companion object {
    const val APP_UUID = "9b02e1cf-d60a-42d8-ad70-8c8ef1c4bdfa"
  }
}
