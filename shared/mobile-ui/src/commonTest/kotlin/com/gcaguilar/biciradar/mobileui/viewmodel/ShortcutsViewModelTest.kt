package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.AssistantIntentResolver
import com.gcaguilar.biciradar.core.AssistantResolution
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsState
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
class ShortcutsViewModelTest {
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
  fun `resolves assistant action into latest answer`() = runTest(dispatcher) {
    val viewModel = ShortcutsViewModel(
      assistantIntentResolver = RecordingAssistantIntentResolver(),
    )

    viewModel.resolveInitialAction(
      action = AssistantAction.NearestStation,
      stations = listOf(
        Station(
          id = "station-1",
          name = "Plaza Espana",
          address = "Centro",
          location = GeoPoint(41.65, -0.88),
          bikesAvailable = 7,
          slotsFree = 5,
          distanceMeters = 120,
        ),
      ),
      favoriteIds = setOf("station-1"),
      searchRadiusMeters = 900,
    )

    advanceUntilIdle()

    assertEquals("resolved:1:1:900", viewModel.uiState.value.latestAnswer)
  }
}

private class RecordingAssistantIntentResolver : AssistantIntentResolver {
  override suspend fun resolve(
    action: AssistantAction,
    stationsState: StationsState,
    favoriteIds: Set<String>,
    searchRadiusMeters: Int,
  ): AssistantResolution = AssistantResolution(
    spokenResponse = "resolved:${stationsState.stations.size}:${favoriteIds.size}:$searchRadiusMeters",
  )
}
