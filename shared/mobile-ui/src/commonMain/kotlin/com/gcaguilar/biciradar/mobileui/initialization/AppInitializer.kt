package com.gcaguilar.biciradar.mobileui.initialization

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import com.gcaguilar.biciradar.mobileui.usecases.StartupUseCase
import com.gcaguilar.biciradar.mobileui.usecases.SurfaceManagementUseCase
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.delay

/**
 * Result of the application initialization process.
 */
internal data class InitializationResult(
  val settingsBootstrapped: Boolean = false,
  val favoritesBootstrapped: Boolean = false,
  val initialLoadAttemptFinished: Boolean = false,
  val minimumSplashElapsed: Boolean = false,
)

/**
 * Handles application bootstrap and initialization.
 * Coordinates settings, favorites, stations, and other repository initializations.
 */
@Inject
internal class AppInitializer(
  private val startupUseCase: StartupUseCase,
  private val appLifecycleUseCase: AppLifecycleUseCase,
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
  private val surfaceManagementUseCase: SurfaceManagementUseCase,
) {
  private val clock: () -> Long = ::epochMillisForUi
  /**
   * Initializes settings repository.
   * @return true if initialization completed
   */
  suspend fun initializeSettings(): Boolean {
    startupUseCase.bootstrapSettings()
    return true
  }

  /**
   * Initializes favorites repository.
   * @return true if initialization completed
   */
  suspend fun initializeFavorites(): Boolean {
    startupUseCase.bootstrapFavorites()
    return true
  }

  /**
   * Refreshes stations data.
   */
  suspend fun refreshStations() {
    startupUseCase.syncFavoritesFromPeer()
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

    // Initialize settings first
    initializeSettings()
    onSettingsBootstrapped()

    // Update changelog after settings are ready
    updatePendingChangelog()

    // Mark session started
    appLifecycleUseCase.markSessionStarted(clock())
    appLifecycleUseCase.markUsefulSession(clock())

    // Initialize favorites
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
   * Syncs favorites from peer.
   */
  suspend fun syncFavoritesFromPeer() {
    startupUseCase.syncFavoritesFromPeer()
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
