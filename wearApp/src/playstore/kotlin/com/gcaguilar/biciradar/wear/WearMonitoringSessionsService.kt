package com.gcaguilar.biciradar.wear

import com.gcaguilar.biciradar.core.WatchMonitoringProtocol
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Espeja en el reloj las sesiones que publica el teléfono.
 *
 * El teléfono es la fuente de verdad: si una ventana termina (por ejemplo con
 * un aviso push), el reloj retira su cuenta atrás en vez de seguir contando.
 */
class WearMonitoringSessionsService : WearableListenerService() {
  override fun onDataChanged(dataEvents: DataEventBuffer) {
    super.onDataChanged(dataEvents)
    val hasSessionsChange =
      dataEvents.any { event ->
        event.type == DataEvent.TYPE_CHANGED &&
          event.dataItem.uri.path == WatchMonitoringProtocol.SESSIONS_PATH
      }
    if (!hasSessionsChange) return

    // El servicio puede arrancar el proceso por su cuenta.
    if (!WearAppGraph.isInitialized()) {
      WearAppGraph.initialize(application)
    }
    val graph = WearAppGraph.graph
    val bridge = WearAppGraph.platformBindings.watchMonitoringBridge
    CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
      val sessions = bridge.latestSessions() ?: return@launch
      graph.surfaceMonitoringRepository.applyRemoteSessions(sessions)
    }
  }
}
