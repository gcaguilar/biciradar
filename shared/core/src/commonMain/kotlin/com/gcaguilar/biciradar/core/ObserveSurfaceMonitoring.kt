package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Exposes [SurfaceMonitoringRepository.state] without leaking the repository.
 *
 * Used by Android Services and ViewModels that need to react to monitoring
 * session changes without direct repository access.
 */
@SingleIn(AppScope::class)
@Inject
class ObserveSurfaceMonitoring(
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
) {
  val state: StateFlow<Map<String, SurfaceMonitoringSession>>
    get() = surfaceMonitoringRepository.state

  /** Sesiones activas en este momento (puede haber varias en paralelo). */
  val activeSessions: Flow<List<SurfaceMonitoringSession>>
    get() = surfaceMonitoringRepository.state.map { sessions -> sessions.values.filter { it.isActive } }
}
