package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.TripDestination
import com.gcaguilar.biciradar.core.TripMonitoringState
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TripUiStateTest {
  @Test
  fun `planning is offered when there is no destination and no active trip`() {
    assertTrue(TripUiState().showPlanning)
  }

  @Test
  fun `planning disappears once a destination is chosen`() {
    assertFalse(TripUiState(destination = destination()).showPlanning)
  }

  @Test
  fun `planning disappears while a trip is being watched`() {
    assertFalse(TripUiState(monitoring = TripMonitoringState(isActive = true)).showPlanning)
  }

  private fun destination() =
    TripDestination(
      name = "Oficina",
      location = GeoPoint(41.6488, -0.8891),
    )
}
