package com.gcaguilar.biciradar.core.backend

import kotlinx.serialization.Serializable

/** Alternative station embedded in a push data payload. */
@Serializable
data class PushAlternative(
  val stationId: String,
  val bikesAvailable: Int,
  val slotsFree: Int,
  val distanceMeters: Int,
)

/**
 * Data payload delivered by the backend alongside the localised alert push.
 * Mirrors `PushDataPayload` in the backend (the `payload` data field).
 */
@Serializable
data class PushMessage(
  val cityId: String,
  val stationId: String,
  val event: String,
  /** Routine id, or the ephemeral monitoring-window id for trip alerts. */
  val routineId: String? = null,
  val alternative: PushAlternative? = null,
)
