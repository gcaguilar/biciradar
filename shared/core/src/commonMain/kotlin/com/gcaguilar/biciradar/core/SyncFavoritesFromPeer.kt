package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

/**
 * Synchronizes favorites from a paired device (e.g. phone ↔ watch).
 * No-op on platforms where watch sync is not available.
 *
 * Before the favorites, it adopts the city published by the peer (see
 * [WatchSyncBridge.latestSelectedCityId]): the city is only configured on the
 * phone, and favorite ids only make sense within their city. Only the watch
 * bridge returns a city, so on the phone this step is a no-op.
 *
 * Uses [FavoritesPeerSyncCapability] to express the minimum required contract.
 */
@SingleIn(AppScope::class)
@Inject
class SyncFavoritesFromPeer(
  private val favoritesPeerSync: FavoritesPeerSyncCapability,
  private val watchSyncBridge: WatchSyncBridge,
  private val settingsRepository: SettingsRepository,
  private val changeCityUseCase: ChangeCityUseCase,
) {
  suspend fun execute() {
    syncSelectedCityFromPeer()
    favoritesPeerSync.syncFromPeer()
  }

  private suspend fun syncSelectedCityFromPeer() {
    // Las implementaciones del puente ya capturan sus errores y devuelven null.
    val peerCity = watchSyncBridge.latestSelectedCityId()?.let(City::fromId) ?: return
    if (peerCity.id == settingsRepository.currentSelectedCity().id) return
    // Las favoritas llegan del teléfono justo después: no se borran aquí.
    changeCityUseCase.execute(peerCity, clearFavorites = false)
  }
}
