package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.local.BiciRadarDatabase

/** A trip monitoring window persisted so an orphaned one can be cancelled on boot. */
data class PersistedTripWindow(
  val windowId: String,
  val stationId: String,
  val startedAt: Long,
)

/**
 * Persists the currently open ephemeral trip window. Trips live only in memory,
 * so on a fresh process any persisted window is orphaned and must be cancelled
 * (the backend expiry is the base guarantee; this is the safety net).
 */
class TripWindowStore(
  private val database: BiciRadarDatabase?,
) {
  fun current(): PersistedTripWindow? =
    database
      ?.biciradarQueries
      ?.getTripMonitoringWindow()
      ?.executeAsOneOrNull()
      ?.let { PersistedTripWindow(windowId = it.window_id, stationId = it.station_id, startedAt = it.started_at) }

  fun save(
    windowId: String,
    stationId: String,
    startedAt: Long,
  ) {
    database?.biciradarQueries?.upsertTripMonitoringWindow(
      windowId = windowId,
      stationId = stationId,
      startedAt = startedAt,
    )
  }

  fun clear() {
    database?.biciradarQueries?.clearTripMonitoringWindow()
  }
}
