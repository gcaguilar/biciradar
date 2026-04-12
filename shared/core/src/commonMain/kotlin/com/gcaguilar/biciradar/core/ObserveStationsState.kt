package com.gcaguilar.biciradar.core

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.StateFlow

/**
 * Exposes [StationsRepository.state] without leaking the repository.
 *
 * Used by Android ViewModels that need to reactively collect station state.
 */
@SingleIn(AppScope::class)
@Inject
class ObserveStationsState(
  private val stationsRepository: StationsRepository,
) {
  val state: StateFlow<StationsState> get() = stationsRepository.state
}
