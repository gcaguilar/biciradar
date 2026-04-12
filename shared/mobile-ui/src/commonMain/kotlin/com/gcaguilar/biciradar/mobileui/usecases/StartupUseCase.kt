package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Result of the startup initialization process.
 */
internal data class StartupResult(
  val settingsBootstrapped: Boolean = false,
  val favoritesBootstrapped: Boolean = false,
)

/**
 * Use case that handles application startup initialization.
 * Groups settings, favorites, and stations repository operations.
 */
@Inject
internal class StartupUseCase(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val stationsRepository: StationsRepository,
) {
  // Expose repository states as flows for observation
  val stationsState: StateFlow<StationsState> = stationsRepository.state
  val favoriteIds: StateFlow<Set<String>> = favoritesRepository.favoriteIds
  val homeStationId: StateFlow<String?> = favoritesRepository.homeStationId
  val workStationId: StateFlow<String?> = favoritesRepository.workStationId
  val hasCompletedOnboarding: StateFlow<Boolean> = settingsRepository.hasCompletedOnboarding
  val onboardingChecklist: StateFlow<OnboardingChecklistSnapshot> = settingsRepository.onboardingChecklist

  /**
   * Bootstraps settings repository.
   */
  suspend fun bootstrapSettings(): Boolean {
    runCatching { settingsRepository.bootstrap() }
    return true
  }

  /**
   * Bootstraps favorites repository by syncing from peer.
   */
  suspend fun bootstrapFavorites(): Boolean {
    runCatching { favoritesRepository.syncFromPeer() }
    return true
  }

  /**
   * Checks if onboarding is completed based on settings.
   */
  fun isOnboardingCompleted(): Boolean =
    settingsRepository.hasCompletedOnboarding.value ||
      settingsRepository.onboardingChecklist.value.isCompleted()

  /**
   * Gets the current onboarding checklist state.
   */
  fun currentOnboardingChecklist(): OnboardingChecklistSnapshot = settingsRepository.onboardingChecklist.value

  /**
   * Updates the onboarding checklist with the given transform.
   */
  suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    settingsRepository.updateOnboardingChecklist(transform)
  }

  /**
   * Forces a refresh of stations.
   */
  suspend fun refreshStations() {
    stationsRepository.forceRefresh()
  }

  /**
   * Loads stations if needed.
   */
  suspend fun loadStationsIfNeeded() {
    stationsRepository.loadIfNeeded()
  }

  /**
   * Syncs favorites from peer.
   */
  suspend fun syncFavoritesFromPeer() {
    runCatching { favoritesRepository.syncFromPeer() }
  }
}
