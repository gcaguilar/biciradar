package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Synchronizes favorites from a paired device (e.g. phone ↔ watch).
 * No-op on platforms where watch sync is not available.
 */
@SingleIn(AppScope::class)
@Inject
class SyncFavoritesFromPeer(
  private val favoritesRepository: FavoritesRepository,
) {
  suspend fun execute() {
    favoritesRepository.syncFromPeer()
  }
}
