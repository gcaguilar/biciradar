package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.distanceBetween
import com.gcaguilar.biciradar.testutils.FakeSettingsRepository
import com.gcaguilar.biciradar.testutils.FakeStationsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull

class FindNearestStationFromTest {
  @Test
  fun `any requirement returns the nearest station inside the radius`() =
    runTest {
      val near = station(id = "near", latitudeDelta = NEAR_LATITUDE_DELTA)
      val middle = station(id = "middle", latitudeDelta = MIDDLE_LATITUDE_DELTA)
      val far = station(id = "far", latitudeDelta = FAR_LATITUDE_DELTA)
      val (useCase, stationsRepository) = useCase(listOf(far, near, middle))

      val result = useCase.execute(ORIGIN)

      assertEquals("near", result?.id)
      assertEquals(1, stationsRepository.loadIfNeededCount)
    }

  @Test
  fun `falls back to the absolute nearest station when nothing is inside the radius`() =
    runTest {
      val far = station(id = "far", latitudeDelta = FAR_LATITUDE_DELTA)
      val veryFar = station(id = "very-far", latitudeDelta = VERY_FAR_LATITUDE_DELTA)
      val (useCase, _) = useCase(listOf(veryFar, far), radiusMeters = 500)

      val result = useCase.execute(ORIGIN)

      assertEquals("far", result?.id)
    }

  @Test
  fun `bikes requirement skips a nearer station without bikes`() =
    runTest {
      val emptyNear = station(id = "near", latitudeDelta = NEAR_LATITUDE_DELTA, bikesAvailable = 0)
      val stockedMiddle = station(id = "middle", latitudeDelta = MIDDLE_LATITUDE_DELTA, bikesAvailable = 3)
      val (useCase, _) = useCase(listOf(emptyNear, stockedMiddle))

      val result = useCase.execute(ORIGIN, StationRequirement.BIKES)

      assertEquals("middle", result?.id)
    }

  @Test
  fun `ebikes requirement only accepts stations with ebikes`() =
    runTest {
      val regularBikesOnly =
        station(id = "near", latitudeDelta = NEAR_LATITUDE_DELTA, bikesAvailable = 9, ebikesAvailable = 0)
      val withEbike =
        station(id = "middle", latitudeDelta = MIDDLE_LATITUDE_DELTA, bikesAvailable = 2, ebikesAvailable = 1)
      val (useCase, _) = useCase(listOf(regularBikesOnly, withEbike))

      val result = useCase.execute(ORIGIN, StationRequirement.EBIKES)

      assertEquals("middle", result?.id)
    }

  @Test
  fun `slots requirement only accepts stations with free docks`() =
    runTest {
      val fullNear = station(id = "near", latitudeDelta = NEAR_LATITUDE_DELTA, slotsFree = 0)
      val freeMiddle = station(id = "middle", latitudeDelta = MIDDLE_LATITUDE_DELTA, slotsFree = 1)
      val (useCase, _) = useCase(listOf(fullNear, freeMiddle))

      val result = useCase.execute(ORIGIN, StationRequirement.SLOTS)

      assertEquals("middle", result?.id)
    }

  @Test
  fun `distances are recomputed from the supplied origin instead of stored values`() =
    runTest {
      val storedNearButActuallyFar =
        station(id = "stored-near", latitudeDelta = FAR_LATITUDE_DELTA, distanceMeters = 10)
      val storedFarButActuallyNear =
        station(id = "stored-far", latitudeDelta = NEAR_LATITUDE_DELTA, distanceMeters = 900)
      val (useCase, _) = useCase(listOf(storedNearButActuallyFar, storedFarButActuallyNear))

      val result = useCase.execute(ORIGIN)

      assertEquals("stored-far", result?.id)
      assertEquals(distanceBetween(ORIGIN, storedFarButActuallyNear.location), result?.distanceMeters)
      assertNotEquals(900, result?.distanceMeters)
    }

  @Test
  fun `sorted stations recompute distance and break ties by name then id`() {
    val westTwin = station(id = "west", name = "Beta", longitudeDelta = -LONGITUDE_TWIN_DELTA)
    val eastTwin = station(id = "east", name = "Alpha", longitudeDelta = LONGITUDE_TWIN_DELTA)
    val other = station(id = "other", latitudeDelta = FAR_LATITUDE_DELTA)
    val sameNameLowerId = station(id = "a-1", name = "Same", longitudeDelta = LONGITUDE_TWIN_DELTA)
    val sameNameHigherId = station(id = "b-1", name = "Same", longitudeDelta = -LONGITUDE_TWIN_DELTA)

    val firstPass =
      stationsSortedByDistanceFrom(
        stations = listOf(westTwin, other, sameNameHigherId, eastTwin, sameNameLowerId),
        origin = ORIGIN,
      )
    val secondPass =
      stationsSortedByDistanceFrom(
        stations = listOf(sameNameLowerId, eastTwin, sameNameHigherId, other, westTwin),
        origin = ORIGIN,
      )

    assertEquals(listOf("Alpha", "Beta", "Same", "Same", "other"), firstPass.map { it.name })
    assertEquals(listOf("a-1", "b-1"), firstPass.filter { it.name == "Same" }.map { it.id })
    assertEquals(firstPass.map { it.id }, secondPass.map { it.id })
    assertEquals(distanceBetween(ORIGIN, other.location), firstPass.last().distanceMeters)
  }

  @Test
  fun `sorting does not mutate the input list or its stations`() {
    val near = station(id = "near", latitudeDelta = NEAR_LATITUDE_DELTA, distanceMeters = 777)
    val far = station(id = "far", latitudeDelta = FAR_LATITUDE_DELTA, distanceMeters = 5)
    val stations = mutableListOf(far, near)
    val snapshot = stations.toList()

    val sorted = stationsSortedByDistanceFrom(stations, ORIGIN)

    assertEquals(snapshot, stations)
    assertEquals(snapshot, listOf(far, near))
    assertEquals(777, near.distanceMeters)
    assertEquals(5, far.distanceMeters)
    assertEquals(listOf("near", "far"), sorted.map { it.id })
    assertNotSame(near, sorted.first())
    assertEquals(distanceBetween(ORIGIN, near.location), sorted.first().distanceMeters)
  }

  @Test
  fun `returns null when the repository has no stations`() =
    runTest {
      val (useCase, _) = useCase(emptyList())

      assertNull(useCase.execute(ORIGIN))
    }
}

private val ORIGIN = GeoPoint(41.65, -0.88)

/** Roughly 100 m north of [ORIGIN]. */
private const val NEAR_LATITUDE_DELTA = 0.0009

/** Roughly 300 m north of [ORIGIN], still inside the default 500 m radius. */
private const val MIDDLE_LATITUDE_DELTA = 0.0027

/** Roughly 980 m north of [ORIGIN], outside the default 500 m radius. */
private const val FAR_LATITUDE_DELTA = 0.0088

/** Roughly 2 km north of [ORIGIN]. */
private const val VERY_FAR_LATITUDE_DELTA = 0.0180

/** Symmetric east/west offset producing identical distances from [ORIGIN]. */
private const val LONGITUDE_TWIN_DELTA = 0.001

private fun useCase(
  stations: List<Station>,
  radiusMeters: Int = DEFAULT_SEARCH_RADIUS_METERS,
): Pair<FindNearestStationFrom, FakeStationsRepository> {
  val stationsRepository =
    FakeStationsRepository().apply {
      state.value = StationsState(stations = stations)
    }
  val settingsRepository =
    FakeSettingsRepository().apply {
      searchRadiusMeters.value = radiusMeters
    }
  return FindNearestStationFrom(
    stationsRepository = stationsRepository,
    settingsRepository = settingsRepository,
  ) to stationsRepository
}

private fun station(
  id: String,
  name: String = id,
  latitudeDelta: Double = 0.0,
  longitudeDelta: Double = 0.0,
  bikesAvailable: Int = 5,
  ebikesAvailable: Int = 0,
  slotsFree: Int = 5,
  distanceMeters: Int = 0,
): Station =
  Station(
    id = id,
    name = name,
    address = "address-$id",
    location =
      GeoPoint(
        latitude = ORIGIN.latitude + latitudeDelta,
        longitude = ORIGIN.longitude + longitudeDelta,
      ),
    bikesAvailable = bikesAvailable,
    slotsFree = slotsFree,
    distanceMeters = distanceMeters,
    ebikesAvailable = ebikesAvailable,
  )
