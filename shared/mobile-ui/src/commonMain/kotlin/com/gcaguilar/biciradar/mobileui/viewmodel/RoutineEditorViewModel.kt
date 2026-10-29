package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.RoutineStep
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.filterStationsByQuery
import com.gcaguilar.biciradar.mobileui.usecases.FavoritesManagementUseCase
import com.gcaguilar.biciradar.mobileui.usecases.RoutineManagementUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactoryKey
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class RoutineStationPickerMode {
  Favorites,
  Search,
  Map,
}

data class RoutineEditorUiState(
  val routineId: String? = null,
  val name: String = "",
  val selectedDays: Set<RoutineDay> = emptySet(),
  val hour: Int = 8,
  val minute: Int = 0,
  val steps: List<RoutineStep> = emptyList(),
  val canSave: Boolean = false,
  val isSaving: Boolean = false,
  val saveError: String? = null,
  val pickerMode: RoutineStationPickerMode? = null,
  val pickerQuery: String = "",
  val filteredStations: List<Station> = emptyList(),
  val loadingSearch: Boolean = false,
  val allStations: List<Station> = emptyList(),
  val favoriteStations: List<Station> = emptyList(),
  val homeStationId: String? = null,
  val workStationId: String? = null,
  val userLocation: GeoPoint? = null,
  val stationsLoading: Boolean = false,
)

sealed class RoutineEditorEvent {
  data object Saved : RoutineEditorEvent()
}

private data class RoutineEditorTransient(
  val name: String = "",
  val selectedDays: Set<RoutineDay> = emptySet(),
  val hour: Int = 8,
  val minute: Int = 0,
  val steps: List<RoutineStep> = emptyList(),
  val isSaving: Boolean = false,
  val saveError: String? = null,
  val pickerMode: RoutineStationPickerMode? = null,
  val pickerQuery: String = "",
  val filteredStations: List<Station> = emptyList(),
  val loadingSearch: Boolean = false,
)

@AssistedInject
class RoutineEditorViewModel(
  private val routineManagementUseCase: RoutineManagementUseCase,
  private val favoritesManagementUseCase: FavoritesManagementUseCase,
  @Assisted private val routineId: String?,
  @Assisted private val prefillStationId: String?,
  @Assisted private val prefillStationName: String?,
  @Assisted private val prefillHour: Int?,
  @Assisted private val prefillMinute: Int?,
) : ViewModel() {
  @AssistedFactory
  @ManualViewModelAssistedFactoryKey
  @ContributesIntoMap(AppScope::class)
  interface Factory : ManualViewModelAssistedFactory {
    fun create(
      routineId: String?,
      prefillStationId: String? = null,
      prefillStationName: String? = null,
      prefillHour: Int? = null,
      prefillMinute: Int? = null,
    ): RoutineEditorViewModel
  }

  private val transient = MutableStateFlow(RoutineEditorTransient())
  private val eventsFlow = MutableSharedFlow<RoutineEditorEvent>(extraBufferCapacity = 1)
  val events: SharedFlow<RoutineEditorEvent> = eventsFlow

  private val stationsState: StateFlow<StationsState> = favoritesManagementUseCase.stationsState

  init {
    viewModelScope.launch {
      routineManagementUseCase.bootstrap()
      when {
        routineId != null -> {
          routineManagementUseCase.routineById(routineId)?.let { routine ->
            transient.value =
              transient.value.copy(
                name = routine.name,
                selectedDays = routine.daysOfWeek,
                hour = routine.hour,
                minute = routine.minute,
                steps = routine.steps,
              )
          }
        }

        prefillStationId != null -> {
          transient.value =
            transient.value.copy(
              hour = prefillHour ?: transient.value.hour,
              minute = prefillMinute ?: transient.value.minute,
              steps =
                listOf(
                  RoutineStep(
                    id = "step-${Random.nextLong().toString(36)}",
                    stationId = prefillStationId,
                    stationName = prefillStationName.orEmpty(),
                    cityId = routineManagementUseCase.currentCityId,
                    durationSeconds = MONITORING_DURATION_OPTIONS_SECONDS.firstOrNull() ?: 600,
                    // ARRIVAL: the user is leaving the bike, so watch free docks.
                    kind = SurfaceMonitoringKind.Docks,
                  ),
                ),
            )
        }
      }
    }
  }

  val uiState: StateFlow<RoutineEditorUiState> =
    combine(
      transient,
      stationsState,
      favoritesManagementUseCase.favoriteIds,
      favoritesManagementUseCase.homeStationId,
      favoritesManagementUseCase.workStationId,
    ) { t, stations, favoriteIds, homeId, workId ->
      val favoriteStations =
        stations.stations
          .filter { it.id in favoriteIds || it.id == homeId || it.id == workId }
          .sortedWith(
            compareBy<Station> { it.id != homeId }
              .thenBy { it.id != workId }
              .thenBy { it.name },
          )
      val filtered =
        if (t.pickerQuery.isBlank()) {
          emptyList()
        } else {
          filterStationsByQuery(stations.stations, t.pickerQuery).take(12)
        }
      RoutineEditorUiState(
        routineId = routineId,
        name = t.name,
        selectedDays = t.selectedDays,
        hour = t.hour,
        minute = t.minute,
        steps = t.steps,
        canSave = t.selectedDays.isNotEmpty() && t.steps.isNotEmpty() && !t.isSaving,
        isSaving = t.isSaving,
        saveError = t.saveError,
        pickerMode = t.pickerMode,
        pickerQuery = t.pickerQuery,
        filteredStations = filtered,
        loadingSearch = t.loadingSearch,
        allStations = stations.stations,
        favoriteStations = favoriteStations,
        homeStationId = homeId,
        workStationId = workId,
        userLocation = stations.userLocation,
        stationsLoading = stations.isLoading,
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      RoutineEditorUiState(),
    )

  fun onNameChange(name: String) {
    transient.update { it.copy(name = name) }
  }

  fun onDayToggle(day: RoutineDay) {
    transient.update {
      val days =
        if (day in it.selectedDays) {
          it.selectedDays - day
        } else {
          it.selectedDays + day
        }
      it.copy(selectedDays = days)
    }
  }

  fun onTimeChanged(
    hour: Int,
    minute: Int,
  ) {
    transient.update { it.copy(hour = hour, minute = minute) }
  }

  fun onAddStep(station: Station) {
    val step =
      RoutineStep(
        id = "step-${Random.nextLong().toString(36)}",
        stationId = station.id,
        stationName = station.name,
        cityId = routineManagementUseCase.currentCityId,
        durationSeconds = MONITORING_DURATION_OPTIONS_SECONDS.firstOrNull() ?: 600,
        kind = SurfaceMonitoringKind.Bikes,
      )
    transient.update { it.copy(steps = it.steps + step) }
  }

  fun onRemoveStep(stepId: String) {
    transient.update { it.copy(steps = it.steps.filterNot { s -> s.id == stepId }) }
  }

  fun onStepDurationSelected(
    stepId: String,
    durationSeconds: Int,
  ) {
    transient.update {
      it.copy(
        steps =
          it.steps.map { step -> if (step.id == stepId) step.copy(durationSeconds = durationSeconds) else step },
      )
    }
  }

  fun onStepKindSelected(
    stepId: String,
    kind: SurfaceMonitoringKind,
  ) {
    transient.update {
      it.copy(
        steps =
          it.steps.map { step -> if (step.id == stepId) step.copy(kind = kind) else step },
      )
    }
  }

  fun onEnterPicker(mode: RoutineStationPickerMode) {
    transient.update {
      it.copy(
        pickerMode = mode,
        pickerQuery = "",
        filteredStations = emptyList(),
      )
    }
  }

  fun onExitPicker() {
    transient.update { it.copy(pickerMode = null, pickerQuery = "", filteredStations = emptyList()) }
  }

  fun onPickerQueryChange(query: String) {
    transient.update { it.copy(pickerQuery = query, loadingSearch = query.isNotBlank()) }
  }

  fun onSavedNavigatedAway() {
    onExitPicker()
  }

  fun onSave() {
    val t = transient.value
    if (t.selectedDays.isEmpty() || t.steps.isEmpty() || t.isSaving) return
    transient.update { it.copy(isSaving = true, saveError = null) }
    viewModelScope.launch {
      val routine =
        Routine(
          id = routineId ?: "r-${Random.nextLong().toString(36)}",
          name = t.name.trim(),
          daysOfWeek = t.selectedDays,
          hour = t.hour,
          minute = t.minute,
          isEnabled = true,
          steps = t.steps,
        )
      runCatching { routineManagementUseCase.upsert(routine) }
        .onSuccess { eventsFlow.emit(RoutineEditorEvent.Saved) }
        .onFailure { error ->
          transient.update { state ->
            state.copy(isSaving = false, saveError = error.message ?: "Error al guardar la rutina")
          }
        }
    }
  }
}
