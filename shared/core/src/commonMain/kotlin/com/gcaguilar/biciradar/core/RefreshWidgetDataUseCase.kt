package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.Inject

@Inject
class RefreshWidgetDataUseCase(
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val logger: Logger,
) {
  suspend fun execute(): Boolean =
    runCatching {
      surfaceSnapshotRepository.bootstrap()
      surfaceSnapshotRepository.refreshSnapshot()
    }.onFailure { error ->
      logger.error("WidgetRefresh", "Failed to refresh widget data", error)
    }.isSuccess
}
