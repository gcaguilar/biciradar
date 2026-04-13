package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.Inject

@Inject
class RefreshWidgetDataUseCase(
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
) {
  suspend fun execute(): Boolean = runCatching {
    surfaceSnapshotRepository.bootstrap()
    surfaceSnapshotRepository.refreshSnapshot()
  }.isSuccess
}
