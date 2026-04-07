package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MapFiltersTest {
  @Test
  fun `environmental filters can coexist with bike availability filters`() {
    val filters = toggleMapFilterSelection(
      activeFilters = setOf(MapFilter.ONLY_BIKES),
      toggledFilter = MapFilter.AIR_QUALITY,
    )

    assertEquals(setOf(MapFilter.ONLY_BIKES, MapFilter.AIR_QUALITY), filters)
  }

  @Test
  fun `switching environmental layer preserves the active bike filter`() {
    val filters = toggleMapFilterSelection(
      activeFilters = setOf(MapFilter.ONLY_SLOTS, MapFilter.AIR_QUALITY),
      toggledFilter = MapFilter.POLLEN,
    )

    assertEquals(setOf(MapFilter.ONLY_SLOTS, MapFilter.POLLEN), filters)
  }

  @Test
  fun `clearing environmental filters keeps the bike filter selected`() {
    val filters = clearEnvironmentalMapFilters(
      setOf(MapFilter.ONLY_EBIKES, MapFilter.POLLEN),
    )

    assertEquals(setOf(MapFilter.ONLY_EBIKES), filters)
  }

  @Test
  fun `applying environmental plus bike filters still filters stations by availability`() {
    val stations = listOf(
      station(id = "mixed", bikes = 2, slots = 3),
      station(id = "only-slots", bikes = 0, slots = 5),
      station(id = "empty", bikes = 0, slots = 0),
    )

    val filtered = applyMapFilters(
      stations = stations,
      activeFilters = setOf(MapFilter.BIKES_AND_SLOTS, MapFilter.AIR_QUALITY),
    )

    assertEquals(listOf("mixed"), filtered.map { it.id })
  }

  @Test
  fun `available filters hide regular bikes when city has only ebikes`() {
    val stations = listOf(
      station(
        id = "ebike-only-1",
        bikes = 4,
        slots = 2,
        ebikes = 4,
        regularBikes = 0,
      ),
      station(
        id = "ebike-only-2",
        bikes = 1,
        slots = 6,
        ebikes = 1,
        regularBikes = 0,
      ),
    )

    val available = availableMapFilters(stations)

    assertTrue(MapFilter.ONLY_EBIKES in available)
    assertFalse(MapFilter.ONLY_REGULAR_BIKES in available)
  }

  @Test
  fun `sanitize active filters removes unavailable filters after city change`() {
    val active = setOf(MapFilter.ONLY_REGULAR_BIKES, MapFilter.POLLEN)
    val available = setOf(MapFilter.BIKES_AND_SLOTS, MapFilter.POLLEN)

    val sanitized = sanitizeActiveMapFilters(active, available)

    assertEquals(setOf(MapFilter.POLLEN), sanitized)
  }
}

private fun station(
  id: String,
  bikes: Int,
  slots: Int,
  ebikes: Int = bikes,
  regularBikes: Int = bikes,
): Station = Station(
  id = id,
  name = id,
  address = "address-$id",
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = bikes,
  slotsFree = slots,
  distanceMeters = 100,
  ebikesAvailable = ebikes,
  regularBikesAvailable = regularBikes,
)
