package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.backend.SubscriptionSyncService
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Contrato del bootstrap que hay que ejecutar al arrancar la aplicación.
 *
 * Restaura las credenciales persistidas (Auth0) y arranca los servicios que
 * dependen de la sesión. Existe como interfaz para que el arranque de UI pueda
 * depender de él sin acoplarse a [BootstrapSession] y sustituirlo en tests.
 */
interface SessionBootstrap {
  /** Idempotente: seguro llamarlo en cada arranque. */
  suspend fun execute()
}

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
@ContributesBinding(AppScope::class)
@Inject
class BootstrapSession(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val routineRepository: RoutineRepository,
  private val subscriptionSyncService: SubscriptionSyncService,
  private val authProvider: AuthProvider,
) : SessionBootstrap {
  override suspend fun execute() {
    runCatching { authProvider.bootstrap() }
    settingsRepository.bootstrap()
    favoritesRepository.bootstrap()
    savedPlaceAlertsRepository.bootstrap()
    surfaceSnapshotRepository.bootstrap()
    surfaceMonitoringRepository.bootstrap()
    routineRepository.bootstrap()
    subscriptionSyncService.start()
  }
}
