package com.gcaguilar.biciradar

import android.app.Application
import android.util.Log
import com.garmin.android.connectiq.ConnectIQ
import com.garmin.android.connectiq.IQApp
import com.garmin.android.connectiq.IQDevice
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.ArrayList
import java.util.HashMap

private const val GARMIN_TAG = "GarminCompanion"

object GarminCompanionBridge {
  private var manager: GarminCompanionManager? = null

  fun initialize(
    application: Application,
    graph: SharedGraph,
  ) {
    if (manager != null) return
    manager = GarminCompanionManager(application, graph).also { it.start() }
  }
}

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

private class GarminCompanionManager(
  application: Application,
  private val graph: SharedGraph,
) {
  private val appContext = application.applicationContext
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val payloadBuilder = GarminPayloadBuilder()
  private val connectIQ = ConnectIQ.getInstance(appContext, ConnectIQ.IQConnectType.WIRELESS)
  private val registeredAppsByDeviceId = mutableMapOf<Long, IQApp>()
  private val deviceIdsWithEvents = mutableSetOf<Long>()
  private var latestPayload: HashMap<String, Any?>? = null
  private var sdkReady = false

  private val sdkListener =
    object : ConnectIQ.ConnectIQListener {
      override fun onSdkReady() {
        sdkReady = true
        Log.d(GARMIN_TAG, "ConnectIQ SDK ready")
        registerConnectedDevices()
        sendLatestPayloadToRegisteredApps()
      }

      override fun onInitializeError(status: ConnectIQ.IQSdkErrorStatus) {
        Log.w(GARMIN_TAG, "ConnectIQ initialize error: $status")
      }

      override fun onSdkShutDown() {
        sdkReady = false
        registeredAppsByDeviceId.clear()
        deviceIdsWithEvents.clear()
        Log.d(GARMIN_TAG, "ConnectIQ SDK shut down")
      }
    }

  private val deviceEventListener =
    object : ConnectIQ.IQDeviceEventListener {
      override fun onDeviceStatusChanged(
        device: IQDevice,
        status: IQDevice.IQDeviceStatus,
      ) {
        Log.d(GARMIN_TAG, "Device ${device.friendlyName} status=$status")
        when (status) {
          IQDevice.IQDeviceStatus.CONNECTED -> ensureAppRegistration(device)
          else -> unregisterDevice(device)
        }
      }
    }

  private val appEventListener =
    object : ConnectIQ.IQApplicationEventListener {
      override fun onMessageReceived(
        device: IQDevice,
        app: IQApp,
        message: MutableList<Any>?,
        status: ConnectIQ.IQMessageStatus,
      ) {
        if (status != ConnectIQ.IQMessageStatus.SUCCESS) {
          Log.w(GARMIN_TAG, "Ignoring Garmin message with status=$status from ${device.friendlyName}")
          return
        }
        if (!isRefreshRequest(message)) return
        Log.d(GARMIN_TAG, "Refresh requested from Garmin device ${device.friendlyName}")
        scope.launch {
          runCatching {
            graph.bootstrapSession.execute()
            graph.refreshStationDataIfNeeded.execute(forceRefresh = true)
            publishState(graph.observeStationsState.state.value)
          }.onFailure { error ->
            Log.w(GARMIN_TAG, "Failed to refresh after Garmin request", error)
          }
        }
      }
    }

  fun start() {
    scope.launch {
      runCatching {
        graph.bootstrapSession.execute()
        graph.getNearbyStationList.execute(limit = 5)
      }.onFailure { error ->
        Log.w(GARMIN_TAG, "Failed to bootstrap Garmin companion state", error)
      }

      graph.observeStationsState.state.collectLatest { state ->
        publishState(state)
      }
    }

    runCatching {
      connectIQ.initialize(appContext, false, sdkListener)
    }.onFailure { error ->
      Log.w(GARMIN_TAG, "Failed to initialize ConnectIQ", error)
    }
  }

  private fun publishState(state: StationsState) {
    latestPayload = payloadBuilder.build(state)
    if (!sdkReady) return
    registerConnectedDevices()
    sendLatestPayloadToRegisteredApps()
  }

  private fun registerConnectedDevices() {
    val devices =
      runCatching { connectIQ.connectedDevices }
        .onFailure { error -> Log.w(GARMIN_TAG, "Failed to list Garmin devices", error) }
        .getOrDefault(emptyList())

    devices.forEach { device ->
      registerDeviceEvents(device)
      ensureAppRegistration(device)
    }
  }

  private fun registerDeviceEvents(device: IQDevice) {
    val deviceId = device.deviceIdentifier
    if (!deviceIdsWithEvents.add(deviceId)) return
    runCatching {
      connectIQ.registerForDeviceEvents(device, deviceEventListener)
    }.onFailure { error ->
      deviceIdsWithEvents.remove(deviceId)
      Log.w(GARMIN_TAG, "Failed to register device events for ${device.friendlyName}", error)
    }
  }

  private fun ensureAppRegistration(device: IQDevice) {
    if (!sdkReady) return
    runCatching {
      connectIQ.getApplicationInfo(
        GARMIN_WIDGET_APP_ID,
        device,
        object : ConnectIQ.IQApplicationInfoListener {
          override fun onApplicationInfoReceived(app: IQApp) {
            if (app.status != IQApp.IQAppStatus.INSTALLED) {
              Log.d(GARMIN_TAG, "Garmin widget status for ${device.friendlyName}: ${app.status}")
              unregisterDevice(device)
              return
            }
            registerInstalledApp(device, app)
          }

          override fun onApplicationNotInstalled(applicationId: String) {
            Log.d(GARMIN_TAG, "Garmin widget not installed on ${device.friendlyName}")
            unregisterDevice(device)
          }
        },
      )
    }.onFailure { error ->
      Log.w(GARMIN_TAG, "Failed to fetch Garmin app info for ${device.friendlyName}", error)
    }
  }

  private fun registerInstalledApp(
    device: IQDevice,
    app: IQApp,
  ) {
    val deviceId = device.deviceIdentifier
    if (registeredAppsByDeviceId.containsKey(deviceId)) return
    runCatching {
      connectIQ.registerForAppEvents(device, app, appEventListener)
      registeredAppsByDeviceId[deviceId] = app
      Log.d(GARMIN_TAG, "Registered Garmin app events for ${device.friendlyName}")
      sendPayload(device, app)
    }.onFailure { error ->
      Log.w(GARMIN_TAG, "Failed to register Garmin app events for ${device.friendlyName}", error)
    }
  }

  private fun sendLatestPayloadToRegisteredApps() {
    registeredAppsByDeviceId.forEach { (deviceId, app) ->
      val device = currentConnectedDevice(deviceId) ?: return@forEach
      sendPayload(device, app)
    }
  }

  private fun sendPayload(
    device: IQDevice,
    app: IQApp,
  ) {
    val payload = latestPayload ?: return
    runCatching {
      connectIQ.sendMessage(
        device,
        app,
        payload,
        object : ConnectIQ.IQSendMessageListener {
          override fun onMessageStatus(
            device: IQDevice,
            app: IQApp,
            status: ConnectIQ.IQMessageStatus,
          ) {
            if (status != ConnectIQ.IQMessageStatus.SUCCESS) {
              Log.w(GARMIN_TAG, "Garmin send failed for ${device.friendlyName}: $status")
            }
          }
        },
      )
    }.onFailure { error ->
      Log.w(GARMIN_TAG, "Failed to send Garmin payload to ${device.friendlyName}", error)
    }
  }

  private fun currentConnectedDevice(deviceId: Long): IQDevice? =
    runCatching { connectIQ.connectedDevices }
      .getOrDefault(emptyList())
      .firstOrNull { it.deviceIdentifier == deviceId }

  private fun unregisterDevice(device: IQDevice) {
    val deviceId = device.deviceIdentifier
    val app = registeredAppsByDeviceId.remove(deviceId)
    if (app != null) {
      runCatching { connectIQ.unregisterForApplicationEvents(device, app) }
        .onFailure { error -> Log.w(GARMIN_TAG, "Failed to unregister Garmin app events", error) }
    }
  }

  private fun isRefreshRequest(message: List<Any>?): Boolean {
    val payload = message?.firstOrNull() as? Map<*, *> ?: return false
    return payload["type"] == "refresh_request"
  }
}

internal const val GARMIN_WIDGET_APP_ID = "9b02e1cf-d60a-42d8-ad70-8c8ef1c4bdfa"
