package com.gcaguilar.biciradar.mobileui

import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.core.TripMode
import kotlin.test.Test
import kotlin.test.assertEquals

class NearbyFiltersTest {
  @Test
  fun `distance filter keeps only stations within the limit`() {
    val stations =
      listOf(
        station(id = "near", distanceMeters = 400),
        station(id = "mid", distanceMeters = 900),
        station(id = "far", distanceMeters = 1_800),
      )

    val filtered =
      filterNearbyStations(
        stations = stations,
        filters = NearbyFilters(maxDistance = NearbyMaxDistance.METERS_500),
        favoriteIds = emptySet(),
      )

    assertEquals(listOf("near"), filtered.map(Station::id))
  }

  @Test
  fun `favorites filter keeps only saved stations`() {
    val stations = listOf(station(id = "a"), station(id = "b"))

    val filtered =
      filterNearbyStations(
        stations = stations,
        filters = NearbyFilters(favoritesOnly = true),
        favoriteIds = setOf("b"),
      )

    assertEquals(listOf("b"), filtered.map(Station::id))
  }

  @Test
  fun `bike type filter separates electric and mechanical`() {
    val stations =
      listOf(
        station(id = "electric", bikes = 3, ebikes = 3, regularBikes = 0),
        station(id = "mechanical", bikes = 4, ebikes = 0, regularBikes = 4),
      )

    val electric =
      filterNearbyStations(
        stations = stations,
        filters = NearbyFilters(bikeType = NearbyBikeType.ELECTRIC),
        favoriteIds = emptySet(),
      )
    val mechanical =
      filterNearbyStations(
        stations = stations,
        filters = NearbyFilters(bikeType = NearbyBikeType.MECHANICAL),
        favoriteIds = emptySet(),
      )

    assertEquals(listOf("electric"), electric.map(Station::id))
    assertEquals(listOf("mechanical"), mechanical.map(Station::id))
  }

  @Test
  fun `mechanical filter falls back to total bikes when type breakdown is missing`() {
    val stations =
      listOf(
        station(id = "unknown-type", bikes = 5, ebikes = 0, regularBikes = 0),
        station(id = "electric", bikes = 3, ebikes = 3, regularBikes = 0),
      )

    val mechanical =
      filterNearbyStations(
        stations = stations,
        filters = NearbyFilters(bikeType = NearbyBikeType.MECHANICAL),
        favoriteIds = emptySet(),
      )

    assertEquals(listOf("unknown-type"), mechanical.map(Station::id))
  }

  @Test
  fun `most bikes sort prioritizes availability and breaks ties by distance`() {
    val stations =
      listOf(
        station(id = "few-far", distanceMeters = 200, bikes = 1),
        station(id = "many-near", distanceMeters = 800, bikes = 5),
        station(id = "many-far", distanceMeters = 1_200, bikes = 5),
      )

    val sorted =
      sortNearbyStations(
        stations = stations,
        sort = NearbySort.MOST_BIKES,
        tripMode = TripMode.Pedestrian,
      )

    assertEquals(listOf("many-near", "many-far", "few-far"), sorted.map(Station::id))
  }

  @Test
  fun `most slots sort prioritizes free docks`() {
    val stations =
      listOf(
        station(id = "slot-rich", slots = 9),
        station(id = "slot-poor", slots = 1),
      )

    val sorted =
      sortNearbyStations(
        stations = stations,
        sort = NearbySort.MOST_SLOTS,
        tripMode = TripMode.Cyclist,
      )

    assertEquals(listOf("slot-rich", "slot-poor"), sorted.map(Station::id))
  }

  @Test
  fun `name sort is alphabetical`() {
    val stations =
      listOf(
        station(id = "b", name = "Bravo"),
        station(id = "a", name = "Alfa"),
        station(id = "c", name = "Charlie"),
      )

    val sorted =
      sortNearbyStations(
        stations = stations,
        sort = NearbySort.NAME,
        tripMode = TripMode.Pedestrian,
      )

    assertEquals(listOf("a", "b", "c"), sorted.map(Station::id))
  }

  @Test
  fun `predicted availability uses the pattern for the current hour`() {
    val stations =
      listOf(
        station(id = "pattern-strong", bikes = 1, slots = 5),
        station(id = "now-strong", bikes = 6, slots = 4),
      )
    val patterns =
      mapOf(
        "pattern-strong" to
          listOf(
            pattern(stationId = "pattern-strong", hour = 8, bikesAvg = 12.0, anchorsAvg = 0.5),
          ),
      )

    val sorted =
      sortNearbyStations(
        stations = stations,
        sort = NearbySort.BEST_PREDICTED_AVAILABILITY,
        tripMode = TripMode.Pedestrian,
        patterns = patterns,
        hour = 8,
        isWeekend = false,
      )

    assertEquals(listOf("pattern-strong", "now-strong"), sorted.map(Station::id))
  }

  @Test
  fun `predicted availability falls back to current count without patterns`() {
    val stations =
      listOf(
        station(id = "small", bikes = 1, slots = 5),
        station(id = "big", bikes = 7, slots = 2),
      )

    val sorted =
      sortNearbyStations(
        stations = stations,
        sort = NearbySort.BEST_PREDICTED_AVAILABILITY,
        tripMode = TripMode.Pedestrian,
      )

    assertEquals(listOf("big", "small"), sorted.map(Station::id))
  }

  @Test
  fun `predicted availability ignores other day types`() {
    val station = station(id = "s", bikes = 2, slots = 2)
    val weekendOnly =
      mapOf(
        "s" to
          listOf(
            pattern(stationId = "s", hour = 8, bikesAvg = 20.0, anchorsAvg = 1.0, dayType = "WEEKEND"),
          ),
      )

    val score =
      predictedAvailability(
        station = station,
        tripMode = TripMode.Pedestrian,
        patterns = weekendOnly,
        hour = 8,
        isWeekend = false,
      )

    assertEquals(2.0, score)
  }

  private fun pattern(
    stationId: String,
    hour: Int,
    bikesAvg: Double,
    anchorsAvg: Double,
    dayType: String = "WEEKDAY",
  ) = StationHourlyPattern(
    stationId = stationId,
    dayType = dayType,
    hour = hour,
    bikesAvg = bikesAvg,
    anchorsAvg = anchorsAvg,
    occupancyAvg = 0.5,
    sampleCount = 10,
  )

  private fun station(
    id: String,
    name: String = "Station $id",
    distanceMeters: Int = 300,
    bikes: Int = 4,
    slots: Int = 4,
    ebikes: Int = 0,
    regularBikes: Int = 0,
  ) = Station(
    id = id,
    name = name,
    address = "Centro",
    location = GeoPoint(41.65, -0.88),
    bikesAvailable = bikes,
    slotsFree = slots,
    distanceMeters = distanceMeters,
    ebikesAvailable = ebikes,
    regularBikesAvailable = regularBikes,
  )
}
