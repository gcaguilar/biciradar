package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.FavoritesPeerSyncCapability
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Use case that handles application startup initialization.
 * Groups settings, favorites, and stations repository operations.
 *
 * **Error handling**: las funciones de bootstrap propagan sus excepciones directamente.
 * Los fallos de inicialización son críticos y deben ser observables por el llamador;
 * no se usan runCatching silenciosos. El caller (AppInitializer) decide si envolver
 * en un try/catch o dejar que la coroutine cancele el scope con el error.
 */
@Inject
internal class StartupUseCase(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val favoritesPeerSync: FavoritesPeerSyncCapability,
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
   * Throws if the settings cannot be initialised.
   */
  suspend fun bootstrapSettings() {
    settingsRepository.bootstrap()
  }

  /**
   * Bootstraps favorites repository by syncing from peer.
   *
   * Peer sync es no-crítico: si falla se loguea pero no cancela el bootstrap.
   * La app puede funcionar sin sincronización de reloj.
   */
  suspend fun bootstrapFavorites() {
    favoritesPeerSync.syncFromPeer()
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
   * Propagates any exception to the caller.
   */
  suspend fun syncFavoritesFromPeer() {
    favoritesPeerSync.syncFromPeer()
  }
}
