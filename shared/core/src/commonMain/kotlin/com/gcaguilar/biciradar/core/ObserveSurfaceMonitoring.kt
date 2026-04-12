package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

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
  val state: StateFlow<SurfaceMonitoringSession?> get() = surfaceMonitoringRepository.state
}
