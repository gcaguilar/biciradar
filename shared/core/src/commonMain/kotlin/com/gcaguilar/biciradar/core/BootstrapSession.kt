package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Bootstraps all persistent repositories that require an explicit init step.
 *
 * Call once at app startup before any data access. Idempotent — safe to call
 * multiple times (each repository guards against double-bootstrap internally).
 *
 * iOS-only repositories (surface snapshot / monitoring) are bootstrapped here
 * too; on watchOS they are no-ops since the underlying data sources are empty.
 */
@SingleIn(AppScope::class)
@Inject
class BootstrapSession(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
) {
  suspend fun execute() {
    settingsRepository.bootstrap()
    favoritesRepository.bootstrap()
    savedPlaceAlertsRepository.bootstrap()
    surfaceSnapshotRepository.bootstrap()
    surfaceMonitoringRepository.bootstrap()
  }
}
