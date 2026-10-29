package com.gcaguilar.biciradar

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Receives FCM messages for the Play Store flavor.
 *
 * Alert copy is composed by the backend; the data payload is forwarded to the
 * shared [com.gcaguilar.biciradar.core.backend.PushMessageHandler], which
 * publishes it (trip auto-cancel) and refreshes the station cache and widgets.
 */
class BiciRadarMessagingService : FirebaseMessagingService() {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onNewToken(token: String) {
    if (!BiziAppGraph.isInitialized()) return
    // Re-registering flows through SubscriptionSyncService so the device is
    // bound to the signed-in account and interests are reconciled.
    scope.launch {
      runCatching { BiziAppGraph.graph.subscriptionSyncService.syncNow() }
    }
  }

  override fun onMessageReceived(message: RemoteMessage) {
    if (!BiziAppGraph.isInitialized()) return
    scope.launch {
      runCatching { BiziAppGraph.graph.pushMessageHandler.onDataMessage(message.data) }
      runCatching { WidgetRefreshWorker.scheduleImmediate(applicationContext) }
    }
  }
}
