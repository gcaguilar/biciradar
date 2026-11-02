package com.gcaguilar.biciradar.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Comandos de monitorización que el reloj envía al teléfono.
 *
 * El reloj no tiene sesión Auth0 ni token push propios, así que la vigilancia
 * real (ventana de backend + avisos) siempre la abre el teléfono.
 */
@Serializable
data class WatchMonitoringCommandPayload(
  val action: String,
  val stationId: String? = null,
  val durationSeconds: Int = 0,
  val kind: String? = null,
)

/** Respuesta del teléfono a un comando de monitorización. */
@Serializable
data class WatchMonitoringResultPayload(
  val stationId: String? = null,
  val success: Boolean,
  val failure: String? = null,
)

/** Sesiones activas del teléfono, para espejarlas en el reloj. */
@Serializable
data class WatchMonitoringSessionsPayload(
  val updatedAt: Long,
  val sessions: List<SurfaceMonitoringSession>,
)

/** Serialización y rutas de los mensajes Wearable entre reloj y teléfono. */
object WatchMonitoringProtocol {
  const val COMMAND_PATH = "/bici/monitoring-command"
  const val RESULT_PATH = "/bici/monitoring-result"
  const val SESSIONS_PATH = "/bizi/monitoring-sessions"

  const val ACTION_START = "start"
  const val ACTION_STOP = "stop"

  private val json = Json { ignoreUnknownKeys = true }

  fun encodeCommand(payload: WatchMonitoringCommandPayload): ByteArray =
    json.encodeToString(WatchMonitoringCommandPayload.serializer(), payload).encodeToByteArray()

  fun decodeCommand(bytes: ByteArray): WatchMonitoringCommandPayload? =
    runCatching {
      json.decodeFromString(WatchMonitoringCommandPayload.serializer(), bytes.decodeToString())
    }.getOrNull()

  fun encodeResult(payload: WatchMonitoringResultPayload): ByteArray =
    json.encodeToString(WatchMonitoringResultPayload.serializer(), payload).encodeToByteArray()

  fun decodeResult(bytes: ByteArray): WatchMonitoringResultPayload? =
    runCatching {
      json.decodeFromString(WatchMonitoringResultPayload.serializer(), bytes.decodeToString())
    }.getOrNull()

  fun encodeSessions(payload: WatchMonitoringSessionsPayload): ByteArray =
    json.encodeToString(WatchMonitoringSessionsPayload.serializer(), payload).encodeToByteArray()

  fun decodeSessions(bytes: ByteArray): WatchMonitoringSessionsPayload? =
    runCatching {
      json.decodeFromString(WatchMonitoringSessionsPayload.serializer(), bytes.decodeToString())
    }.getOrNull()
}
