package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Synchronizes favorites from a paired device (e.g. phone ↔ watch).
 * No-op on platforms where watch sync is not available.
 *
 * Uses [FavoritesPeerSyncCapability] to express the minimum required contract.
 */
@SingleIn(AppScope::class)
@Inject
class SyncFavoritesFromPeer(
  private val favoritesPeerSync: FavoritesPeerSyncCapability,
) {
  suspend fun execute() {
    favoritesPeerSync.syncFromPeer()
  }
}
