package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import kotlin.test.Test
import kotlin.test.assertEquals

class FavoritesVisibilityTest {
  @Test
  fun `visible favorites excludes home and work from the list`() {
    val stations =
      listOf(
        station(id = "home", name = "Casa"),
        station(id = "other-1", name = "Plaza España"),
        station(id = "work", name = "Trabajo"),
        station(id = "other-2", name = "Universidad"),
      )

    val visible =
      visibleFavoriteStations(
        stations = stations,
        favoriteIds = setOf("home", "other-1", "work", "other-2"),
        homeStationId = "home",
        workStationId = "work",
      )

    assertEquals(listOf("other-1", "other-2"), visible.map { it.id })
  }

  @Test
  fun `visible favorites restores saved places when they are cleared`() {
    val stations =
      listOf(
        station(id = "home", name = "Casa"),
        station(id = "other", name = "Plaza España"),
      )

    val visible =
      visibleFavoriteStations(
        stations = stations,
        favoriteIds = setOf("home", "other"),
        homeStationId = null,
        workStationId = "work",
      )

    assertEquals(listOf("home", "other"), visible.map { it.id })
  }

  @Test
  fun `visible favorites deduplicates repeated station ids`() {
    val stations =
      listOf(
        station(id = "other", name = "Plaza España"),
        station(id = "other", name = "Plaza España duplicada"),
        station(id = "home", name = "Casa"),
      )

    val visible =
      visibleFavoriteStations(
        stations = stations,
        favoriteIds = setOf("other", "home"),
        homeStationId = null,
        workStationId = null,
      )

    assertEquals(listOf("other", "home"), visible.map { it.id })
  }
}

private fun station(
  id: String,
  name: String,
) = Station(
  id = id,
  name = name,
  address = "Centro",
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = 4,
  slotsFree = 6,
  distanceMeters = 120,
)
