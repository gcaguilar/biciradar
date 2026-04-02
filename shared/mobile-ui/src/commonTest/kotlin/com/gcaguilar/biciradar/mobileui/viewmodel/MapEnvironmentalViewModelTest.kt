package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.EnvironmentalReading
import com.gcaguilar.biciradar.core.EnvironmentalRepository
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.MapEnvironmentalLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MapEnvironmentalViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `loads environmental snapshots from repository when a layer becomes active`() = runTest(dispatcher) {
    val viewModel = MapEnvironmentalViewModel(
      environmentalRepository = FakeEnvironmentalRepository(),
    )

    viewModel.onStationsChanged(
      listOf(
        station("ne", 41.7, -0.8),
        station("nw", 41.7, -0.9),
        station("se", 41.6, -0.8),
        station("sw", 41.6, -0.9),
      ),
    )
    viewModel.onEnvironmentalLayerChanged(MapEnvironmentalLayer.AirQuality)

    advanceUntilIdle()

    assertEquals(4, viewModel.uiState.value.zones.size)
    assertEquals(listOf(10, 20, 30, 40), viewModel.uiState.value.zones.map { it.airQualityScore })
  }

  @Test
  fun `clears snapshots when environmental layer is removed`() = runTest(dispatcher) {
    val viewModel = MapEnvironmentalViewModel(
      environmentalRepository = FakeEnvironmentalRepository(),
    )

    viewModel.onStationsChanged(listOf(station("only", 41.65, -0.88)))
    viewModel.onEnvironmentalLayerChanged(MapEnvironmentalLayer.Pollen)
    advanceUntilIdle()
    assertEquals(1, viewModel.uiState.value.zones.size)

    viewModel.onEnvironmentalLayerChanged(null)
    advanceUntilIdle()

    assertEquals(emptyList(), viewModel.uiState.value.zones)
  }
}

private class FakeEnvironmentalRepository : EnvironmentalRepository {
  override suspend fun readingAt(latitude: Double, longitude: Double): EnvironmentalReading? {
    val key = "${latitude},${longitude}"
    return when (key) {
      "41.7,-0.8" -> EnvironmentalReading(airQualityIndex = 10, pollenIndex = 1)
      "41.7,-0.9" -> EnvironmentalReading(airQualityIndex = 20, pollenIndex = 2)
      "41.6,-0.8" -> EnvironmentalReading(airQualityIndex = 30, pollenIndex = 3)
      "41.6,-0.9" -> EnvironmentalReading(airQualityIndex = 40, pollenIndex = 4)
      "41.65,-0.88" -> EnvironmentalReading(airQualityIndex = 50, pollenIndex = 5)
      else -> null
    }
  }
}

private fun station(id: String, latitude: Double, longitude: Double) = Station(
  id = id,
  name = id,
  address = "Centro",
  location = GeoPoint(latitude, longitude),
  bikesAvailable = 5,
  slotsFree = 5,
  distanceMeters = 100,
)
