package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes the reactive favorite-related state from [FavoritesRepository]
 * without leaking the repository.
 *
 * Used by Android ViewModels that collect these flows to drive UI.
 */
@SingleIn(AppScope::class)
@Inject
class ObserveFavorites(
  private val favoritesRepository: FavoritesRepository,
) {
  val favoriteIds: StateFlow<Set<String>> get() = favoritesRepository.favoriteIds
  val homeStationId: StateFlow<String?> get() = favoritesRepository.homeStationId
  val workStationId: StateFlow<String?> get() = favoritesRepository.workStationId

  /** Synchronous snapshot of the current home station ID (for non-reactive callers). */
  fun currentHomeStationId(): String? = favoritesRepository.currentHomeStationId()

  /** Synchronous snapshot of the current work station ID (for non-reactive callers). */
  fun currentWorkStationId(): String? = favoritesRepository.currentWorkStationId()
}
