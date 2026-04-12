package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes [SurfaceSnapshotRepository.bundle] without leaking the repository.
 *
 * Used by Android ViewModels that need to reactively collect the surface bundle.
 */
@SingleIn(AppScope::class)
@Inject
class ObserveSurfaceSnapshot(
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
  val bundle: StateFlow<SurfaceSnapshotBundle?> get() = surfaceSnapshotRepository.bundle
}
