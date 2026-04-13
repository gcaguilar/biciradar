package com.gcaguilar.biciradar.mobileui.initialization

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceManagementUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay

/**
 * Handles application bootstrap and initialization.
 * Coordinates settings, favorites, stations, and other repository initializations.
 *
 * **Error handling**: [bootstrapSettings] propaga excepciones porque un fallo de
 * settings es crítico y debe ser observable (e.g. para mostrar una pantalla de error).
 * [bootstrapFavorites] atrapa su excepción con [runCatching] porque la sincronización
 * con el reloj es no-crítica — la app puede arrancar sin ella.
 */
@Inject
internal class AppInitializer(
  private val startupUseCase: StartupUseCase,
  private val appLifecycleUseCase: AppLifecycleUseCase,
  private val surfaceManagementUseCase: SurfaceManagementUseCase,
) {
  private val clock: () -> Long = ::epochMillisForUi

  /**
   * Initializes settings repository.
   * Throws if settings cannot be bootstrapped.
   */
  suspend fun initializeSettings() {
    startupUseCase.bootstrapSettings()
  }

  /**
   * Initializes favorites repository.
   * Peer sync failures are swallowed here (non-critical) but logged.
   */
  suspend fun initializeFavorites() {
    runCatching { startupUseCase.bootstrapFavorites() }
      .onFailure { /* Non-critical: app can start without watch sync */ }
  }

  /**
   * Refreshes stations data.
   */
  suspend fun refreshStations() {
    runCatching { startupUseCase.syncFavoritesFromPeer() }
      .onFailure { /* Peer sync non-critical */ }
    startupUseCase.refreshStations()
  }

  /**
   * Performs the complete bootstrap sequence.
   * This includes surface repositories, settings, favorites, and changelog setup.
   *
   * @param onSettingsBootstrapped Callback when settings are initialized
   * @param onFavoritesBootstrapped Callback when favorites are initialized
   * @param updatePendingChangelog Callback to update pending changelog
   */
  suspend fun bootstrap(
    onSettingsBootstrapped: () -> Unit,
    onFavoritesBootstrapped: () -> Unit,
    updatePendingChangelog: () -> Unit,
  ) {
    // Initialize surface repositories
    surfaceManagementUseCase.bootstrap()

    // Initialize settings first (throws on failure)
    initializeSettings()
    onSettingsBootstrapped()

    // Update changelog after settings are ready
    updatePendingChangelog()

    // Mark session started
    appLifecycleUseCase.markSessionStarted(clock())
    appLifecycleUseCase.markUsefulSession(clock())

    // Initialize favorites (non-critical, swallows peer-sync errors)
    initializeFavorites()
    onFavoritesBootstrapped()
  }

  /**
   * Starts the minimum splash timer.
   *
   * @param onMinimumSplashElapsed Callback when minimum splash time has elapsed
   */
  suspend fun startMinimumSplashTimer(onMinimumSplashElapsed: () -> Unit) {
    delay(700)
    onMinimumSplashElapsed()
  }

  /**
   * Loads stations if needed (for empty state retry).
   */
  suspend fun loadStationsIfNeeded() {
    startupUseCase.loadStationsIfNeeded()
  }

  /**
   * Syncs favorites from peer (non-critical; swallows errors).
   */
  suspend fun syncFavoritesFromPeer() {
    runCatching { startupUseCase.syncFavoritesFromPeer() }
      .onFailure { /* Peer sync non-critical */ }
  }

  /**
   * Refreshes the surface snapshot.
   */
  suspend fun refreshSurfaceSnapshot() {
    surfaceManagementUseCase.refreshSnapshot()
  }

  /**
   * Checks if onboarding is completed.
   */
  fun isOnboardingCompleted(): Boolean = startupUseCase.isOnboardingCompleted()

  /**
   * Gets the current onboarding checklist.
   */
  fun currentOnboardingChecklist(): OnboardingChecklistSnapshot = startupUseCase.currentOnboardingChecklist()

  /**
   * Updates the onboarding checklist.
   */
  suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    startupUseCase.updateOnboardingChecklist(transform)
  }
}
