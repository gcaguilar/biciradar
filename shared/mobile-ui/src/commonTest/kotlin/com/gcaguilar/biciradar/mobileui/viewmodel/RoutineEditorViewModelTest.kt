package com.gcaguilar.biciradar.mobileui.viewmodel

import com.gcaguilar.biciradar.core.FavoriteCategory
import com.gcaguilar.biciradar.core.FavoritesCategorizationCapability
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.RoutineRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.auth.NoOpAuthProvider
import com.gcaguilar.biciradar.core.backend.BackendConfig
import com.gcaguilar.biciradar.mobileui.usecases.FavoritesManagementUseCase
import com.gcaguilar.biciradar.mobileui.usecases.RoutineManagementUseCase
import com.gcaguilar.biciradar.testutils.FakeFavoritesRepository
import com.gcaguilar.biciradar.testutils.FakeSettingsRepository
import com.gcaguilar.biciradar.testutils.FakeStationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RoutineEditorViewModelTest {
  private val dispatcher = StandardTestDispatcher()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(dispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun buildViewModel(
    routineId: String? = null,
    prefillStationId: String? = null,
    prefillStationName: String? = null,
    prefillHour: Int? = null,
    prefillMinute: Int? = null,
  ): Triple<RoutineEditorViewModel, FakeRoutineRepository, FakeFavoriteRepository> {
    val stationsRepo = FakeStationsRepository()
    val favoritesRepo = FakeFavoriteRepository()
    val routineRepo = FakeRoutineRepository()
    val viewModel =
      RoutineEditorViewModel(
        routineManagementUseCase =
          RoutineManagementUseCase(
            routineRepo,
            stationsRepo,
            FakeSettingsRepository(),
            NoOpAuthProvider,
            BackendConfig(),
          ),
        favoritesManagementUseCase =
          FavoritesManagementUseCase(
            favoritesRepository = favoritesRepo,
            favoritesCategories = favoritesRepo,
            stationsRepository = stationsRepo,
            settingsRepository = FakeSettingsRepository(),
          ),
        routineId = routineId,
        prefillStationId = prefillStationId,
        prefillStationName = prefillStationName,
        prefillHour = prefillHour,
        prefillMinute = prefillMinute,
      )
    return Triple(viewModel, routineRepo, favoritesRepo)
  }

  @Test
  fun `can save only after days and at least one station are set`() =
    runTest(dispatcher) {
      val (viewModel, _, favoritesRepo) = buildViewModel()
      favoritesRepo.favoriteIds.value = setOf("station-1")
      advanceUntilIdle()

      assertTrue(!viewModel.uiState.value.canSave)
      viewModel.onDayToggle(RoutineDay.MONDAY)
      advanceUntilIdle()
      assertTrue(!viewModel.uiState.value.canSave)

      val station =
        viewModel.uiState.value.favoriteStations
          .single()
      viewModel.onAddStep(station)
      advanceUntilIdle()
      assertTrue(viewModel.uiState.value.canSave)
    }

  @Test
  fun `favorites appear in picker and add step uses bike kind by default`() =
    runTest(dispatcher) {
      val (viewModel, _, favoritesRepo) = buildViewModel()
      favoritesRepo.favoriteIds.value = setOf("station-1")
      advanceUntilIdle()

      assertEquals(
        "station-1",
        viewModel.uiState.value.favoriteStations
          .single()
          .id,
      )

      viewModel.onAddStep(
        viewModel.uiState.value.favoriteStations
          .single(),
      )
      advanceUntilIdle()
      val step =
        viewModel.uiState.value.steps
          .single()
      assertEquals("station-1", step.stationId)
      assertEquals(SurfaceMonitoringKind.Bikes, step.kind)
    }

  @Test
  fun `saving emits event and persists routine with schedule and steps`() =
    runTest(dispatcher) {
      val (viewModel, routineRepo, favoritesRepo) = buildViewModel()
      favoritesRepo.favoriteIds.value = setOf("station-1")
      advanceUntilIdle()

      viewModel.onNameChange("Gimnasio")
      viewModel.onDayToggle(RoutineDay.MONDAY)
      viewModel.onDayToggle(RoutineDay.WEDNESDAY)
      viewModel.onTimeChanged(7, 5)
      viewModel.onAddStep(
        viewModel.uiState.value.favoriteStations
          .single(),
      )
      advanceUntilIdle()
      val stepId =
        viewModel.uiState.value.steps
          .single()
          .id
      viewModel.onStepKindSelected(stepId, SurfaceMonitoringKind.Docks)
      viewModel.onRemoveStep("missing-step")

      var saved = false
      val collector =
        launch(dispatcher) {
          viewModel.events.collect { event ->
            if (event is RoutineEditorEvent.Saved) saved = true
          }
        }

      viewModel.onSave()
      advanceUntilIdle()
      collector.cancel()

      assertTrue(saved)
      val persisted = assertNotNull(routineRepo.lastSaved)
      assertEquals("Gimnasio", persisted.name)
      assertEquals(setOf(RoutineDay.MONDAY, RoutineDay.WEDNESDAY), persisted.daysOfWeek)
      assertEquals(7, persisted.hour)
      assertEquals(5, persisted.minute)
      assertEquals(1, persisted.steps.size)
      assertEquals(SurfaceMonitoringKind.Docks, persisted.steps.single().kind)
    }

  @Test
  fun `prefills an arrival step from a trip`() =
    runTest(dispatcher) {
      val (viewModel, _, _) =
        buildViewModel(
          prefillStationId = "station-1",
          prefillStationName = "Plaza Espana",
          prefillHour = 9,
          prefillMinute = 15,
        )
      advanceUntilIdle()

      val state = viewModel.uiState.value
      assertEquals(9, state.hour)
      assertEquals(15, state.minute)
      val step = state.steps.single()
      assertEquals("station-1", step.stationId)
      assertEquals("Plaza Espana", step.stationName)
      assertEquals(SurfaceMonitoringKind.Docks, step.kind)
    }
}

private class FakeRoutineRepository : RoutineRepository {
  private val mutable = MutableStateFlow<List<Routine>>(emptyList())
  override val state: StateFlow<List<Routine>> = mutable
  var lastSaved: Routine? = null

  override suspend fun bootstrap() = Unit

  override fun routineById(id: String): Routine? = mutable.value.firstOrNull { it.id == id }

  override suspend fun upsertRoutine(routine: Routine) {
    lastSaved = routine
    mutable.value = (mutable.value.filterNot { it.id == routine.id } + routine)
  }

  override suspend fun deleteRoutine(id: String) {
    mutable.value = mutable.value.filterNot { it.id == id }
  }

  override suspend fun setEnabled(
    id: String,
    enabled: Boolean,
  ) {
    mutable.value = mutable.value.map { if (it.id == id) it.copy(isEnabled = enabled) else it }
  }
}

private class FakeFavoriteRepository :
  FakeFavoritesRepository(),
  FavoritesCategorizationCapability {
  override val categories: StateFlow<List<FavoriteCategory>> = MutableStateFlow(emptyList())
  override val stationCategory: StateFlow<Map<String, String>> = MutableStateFlow(emptyMap())

  override suspend fun upsertCategory(
    id: String,
    label: String,
    isSystem: Boolean,
  ) = Unit

  override suspend fun removeCategory(categoryId: String) = Unit

  override suspend fun assignStationToCategory(
    stationId: String,
    categoryId: String?,
  ) = Unit
}
