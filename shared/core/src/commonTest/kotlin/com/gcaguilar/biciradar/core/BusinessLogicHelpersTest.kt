package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals

class BusinessLogicHelpersTest {
  @Test
  fun `saved place alert helper finds rule by target identity`() {
    val target = SavedPlaceAlertTarget.Home("station-1", City.ZARAGOZA.id, "Plaza Espana")
    val rules =
      listOf(
        SavedPlaceAlertRule(
          id = target.identityKey(),
          target = target,
          condition = SavedPlaceAlertCondition.BikesAtLeast(2),
        ),
      )

    assertEquals(target.identityKey(), findSavedPlaceAlertRule(rules, target)?.id)
  }

  @Test
  fun `nearby helper variants delegate to bike and slot predicates`() {
    val stations =
      listOf(
        Station(
          id = "no-bikes",
          name = "Sin bicis",
          address = "Centro",
          location = GeoPoint(41.65, -0.88),
          bikesAvailable = 0,
          slotsFree = 5,
          distanceMeters = 100,
        ),
        Station(
          id = "with-bikes",
          name = "Con bicis",
          address = "Centro",
          location = GeoPoint(41.66, -0.89),
          bikesAvailable = 4,
          slotsFree = 0,
          distanceMeters = 150,
        ),
      )

    assertEquals("with-bikes", selectNearbyStationWithBikes(stations, 500).highlightedStation?.id)
    assertEquals("no-bikes", selectNearbyStationWithSlots(stations, 500).highlightedStation?.id)
  }
}
