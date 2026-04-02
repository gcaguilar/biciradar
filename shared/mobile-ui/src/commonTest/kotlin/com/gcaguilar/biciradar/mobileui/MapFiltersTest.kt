package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import kotlin.test.Test
import kotlin.test.assertEquals

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
}

private fun station(
  id: String,
  bikes: Int,
  slots: Int,
): Station = Station(
  id = id,
  name = id,
  address = "address-$id",
  location = GeoPoint(41.65, -0.88),
  bikesAvailable = bikes,
  slotsFree = slots,
  distanceMeters = 100,
  ebikesAvailable = bikes,
  regularBikesAvailable = bikes,
)
