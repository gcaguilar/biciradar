package com.gcaguilar.biciradar

import android.content.Intent
import android.net.Uri
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WearRouteMessageService : WearableListenerService() {
  override fun onMessageReceived(messageEvent: MessageEvent) {
    super.onMessageReceived(messageEvent)
    if (messageEvent.path != ROUTE_TO_STATION_PATH) return

    val stationId = messageEvent.data.decodeToString().trim()
    if (stationId.isEmpty()) return

    val launchIntent =
      Intent(this, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse("biciradar://station/$stationId?action=route_to_station&station_id=$stationId")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    startActivity(launchIntent)
  }

  companion object {
    private const val ROUTE_TO_STATION_PATH = "/bici/route-to-station"
  }
}
