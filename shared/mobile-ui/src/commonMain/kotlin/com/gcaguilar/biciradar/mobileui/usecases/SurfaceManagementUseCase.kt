package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import dev.zacsweers.metro.Inject

@Inject
internal class SurfaceManagementUseCase(
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
) {
  suspend fun bootstrap() {
    surfaceSnapshotRepository.bootstrap()
    surfaceMonitoringRepository.bootstrap()
    runCatching { savedPlaceAlertsRepository.bootstrap() }
  }

  suspend fun refreshSnapshot() {
    surfaceSnapshotRepository.refreshSnapshot()
  }
}
