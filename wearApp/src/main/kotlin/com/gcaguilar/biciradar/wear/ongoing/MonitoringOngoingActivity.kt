package com.gcaguilar.biciradar.wear.ongoing

import android.content.Context

/**
 * Helper para gestionar ongoing activities de monitorización.
 *
 * Esta clase simplifica el uso de OngoingActivityManager para el caso
 * específico de monitorización de estaciones.
 */
class MonitoringOngoingActivity(
  private val context: Context,
) {
  private val manager = OngoingActivityManager.getInstance(context)

  /**
   * Inicia la ongoing activity para monitorización.
   */
  fun start(
    stationId: String,
    stationName: String,
    remainingSeconds: Int = 300, // 5 minutos por defecto
  ) {
    manager.startMonitoringActivity(
      stationId = stationId,
      stationName = stationName,
      remainingSeconds = remainingSeconds,
    )
  }

  /**
   * Detiene la ongoing activity.
   */
  fun stop() {
    manager.stopCurrentActivity()
  }
}
