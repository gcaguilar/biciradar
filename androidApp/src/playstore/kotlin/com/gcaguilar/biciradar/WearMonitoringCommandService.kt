package com.gcaguilar.biciradar

import com.gcaguilar.biciradar.core.DEFAULT_SURFACE_MONITORING_DURATION_SECONDS
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.TripMonitoringFailure
import com.gcaguilar.biciradar.core.WatchMonitoringCommandPayload
import com.gcaguilar.biciradar.core.WatchMonitoringCommandResult
import com.gcaguilar.biciradar.core.WatchMonitoringProtocol
import com.gcaguilar.biciradar.core.auth.AuthState
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Ejecuta en el teléfono los comandos de monitorización que envía el reloj.
 *
 * El reloj no tiene sesión Auth0 ni token push, así que la ventana de backend
 * (la que hace llegar los avisos) solo puede abrirla el teléfono.
 */
class WearMonitoringCommandService : WearableListenerService() {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  override fun onMessageReceived(messageEvent: MessageEvent) {
    super.onMessageReceived(messageEvent)
    if (messageEvent.path != WatchMonitoringProtocol.COMMAND_PATH) return
    val command = WatchMonitoringProtocol.decodeCommand(messageEvent.data) ?: return
    // En el flujo normal BiciRadarApplication.onCreate ya inicializó el grafo; el
    // servicio puede arrancar el proceso por su cuenta, así que se cubre ese caso.
    if (!BiziAppGraph.isInitialized()) {
      BiziAppGraph.initialize(application)
    }
    val graph = BiziAppGraph.graph
    scope.launch {
      val result =
        runCatching { execute(graph, command) }
          .getOrElse {
            WatchMonitoringCommandResult(
              stationId = command.stationId,
              success = false,
              failure = TripMonitoringFailure.BackendUnavailable,
            )
          }
      BiziAppGraph.platformBindings.watchMonitoringBridge.sendResult(result)
    }
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }

  private suspend fun execute(
    graph: SharedGraph,
    command: WatchMonitoringCommandPayload,
  ): WatchMonitoringCommandResult =
    when (command.action) {
      WatchMonitoringProtocol.ACTION_START -> {
        startMonitoring(graph, command)
      }

      WatchMonitoringProtocol.ACTION_STOP -> {
        graph.stopStationMonitoring.execute(stationId = command.stationId)
        WatchMonitoringCommandResult(stationId = command.stationId, success = true)
      }

      else -> {
        WatchMonitoringCommandResult(stationId = command.stationId, success = false)
      }
    }

  private suspend fun startMonitoring(
    graph: SharedGraph,
    command: WatchMonitoringCommandPayload,
  ): WatchMonitoringCommandResult {
    val stationId =
      command.stationId
        ?: return WatchMonitoringCommandResult(stationId = null, success = false)
    // Este proceso puede ser nuevo: restaura las credenciales antes de mirar el estado.
    runCatching { graph.authProvider.bootstrap() }
    if (graph.authProvider.credentialsState.value != AuthState.Authenticated) {
      return WatchMonitoringCommandResult(
        stationId = stationId,
        success = false,
        failure = TripMonitoringFailure.SignInRequired,
      )
    }
    val kind =
      command.kind
        ?.let { name -> runCatching { SurfaceMonitoringKind.valueOf(name) }.getOrNull() }
        ?: SurfaceMonitoringKind.Docks
    val duration =
      command.durationSeconds.takeIf { it > 0 }
        ?: DEFAULT_SURFACE_MONITORING_DURATION_SECONDS
    val started = graph.startStationMonitoring.execute(stationId, duration, kind)
    if (!started) {
      return WatchMonitoringCommandResult(
        stationId = stationId,
        success = false,
        failure = TripMonitoringFailure.BackendUnavailable,
      )
    }
    // La notificación persistente es opcional (puede estar restringida en
    // segundo plano); el aviso real llega igualmente por push.
    runCatching { TripMonitorService.start(this) }
    return WatchMonitoringCommandResult(stationId = stationId, success = true)
  }
}
