package com.gcaguilar.biciradar.core

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Repositorio de rutinas. Persiste el horario compartido y las estaciones
 * ([RoutineStep]) de cada rutina.
 *
 * La monitorización programada no vive aquí: los schedulers de plataforma
 * observan [state] y traducen el horario a sus APIs nativas.
 */
interface RoutineRepository {
  val state: StateFlow<List<Routine>>

  suspend fun bootstrap()

  fun routineById(id: String): Routine?

  suspend fun upsertRoutine(routine: Routine)

  suspend fun deleteRoutine(id: String)

  suspend fun setEnabled(
    id: String,
    enabled: Boolean,
  )
}

/**
 * Device-local persistence seam. [SyncingRoutineRepository] decorates it with
 * backend synchronisation; tests and anonymous mode can depend on the local
 * implementation directly.
 */
interface LocalRoutineRepository : RoutineRepository

/**
 * Implementación de LocalRoutineRepository.
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<LocalRoutineRepository>())
@Inject
class RoutineRepositoryImpl(
  private val scope: CoroutineScope,
  private val database: BiciRadarDatabase? = null,
) : LocalRoutineRepository {
  private val mutableState = MutableStateFlow<List<Routine>>(emptyList())

  @kotlin.concurrent.Volatile private var bootstrapped = false

  override val state: StateFlow<List<Routine>> = mutableState.asStateFlow()

  init {
    database?.let { db ->
      scope.launch {
        combine(
          db.biciradarQueries
            .getAllRoutines()
            .asFlow()
            .mapToList(Dispatchers.Default),
          db.biciradarQueries
            .getAllRoutineSteps()
            .asFlow()
            .mapToList(Dispatchers.Default),
        ) { routineRows, stepRows ->
          routineRows.map { row -> row.toRoutine(stepRows) }.sortedBy { it.id }
        }.collect { routines -> mutableState.value = routines }
      }
    }
  }

  override suspend fun bootstrap() {
    if (bootstrapped) return
    val db = database
    if (db != null) {
      mutableState.value =
        runCatching {
          val stepRows = db.biciradarQueries.getAllRoutineSteps().executeAsList()
          db.biciradarQueries
            .getAllRoutines()
            .executeAsList()
            .map { it.toRoutine(stepRows) }
            .sortedBy { it.id }
        }.getOrDefault(mutableState.value)
    }
    bootstrapped = true
  }

  override fun routineById(id: String): Routine? = mutableState.value.firstOrNull { it.id == id }

  override suspend fun upsertRoutine(routine: Routine) {
    if (!bootstrapped) bootstrap()
    val db = database
    if (db == null) {
      mutableState.value =
        (mutableState.value.filterNot { it.id == routine.id } + routine).sortedBy { it.id }
      return
    }
    runCatching {
      db.transaction {
        db.biciradarQueries.upsertRoutine(
          id = routine.id,
          name = routine.name,
          daysBitmap = routineDaysToBitmap(routine.daysOfWeek),
          hour = routine.hour.toLong(),
          minute = routine.minute.toLong(),
          isEnabled = if (routine.isEnabled) 1L else 0L,
        )
        db.biciradarQueries.deleteRoutineSteps(routine.id)
        routine.steps.forEachIndexed { index, step ->
          db.biciradarQueries.insertRoutineStep(
            id = step.id,
            routineId = routine.id,
            position = index.toLong(),
            stationId = step.stationId,
            stationName = step.stationName,
            cityId = step.cityId,
            durationSeconds = step.durationSeconds.toLong(),
            kind = step.kind.name,
            threshold = step.threshold?.toLong(),
            fallbackStationId = step.fallbackStationId,
            alternativeRadiusMeters = step.alternativeRadiusMeters?.toLong(),
          )
        }
      }
    }
  }

  override suspend fun deleteRoutine(id: String) {
    if (!bootstrapped) bootstrap()
    val db = database
    if (db == null) {
      mutableState.value = mutableState.value.filterNot { it.id == id }
      return
    }
    runCatching {
      db.transaction {
        db.biciradarQueries.deleteRoutineSteps(id)
        db.biciradarQueries.deleteRoutine(id)
      }
    }
  }

  override suspend fun setEnabled(
    id: String,
    enabled: Boolean,
  ) {
    val routine = routineById(id) ?: return
    upsertRoutine(routine.copy(isEnabled = enabled))
  }
}

private fun Routines.toRoutine(stepRows: List<Routine_steps>): Routine =
  Routine(
    id = id,
    name = name,
    daysOfWeek = routineDaysFromBitmap(days_bitmap),
    hour = hour.toInt(),
    minute = minute.toInt(),
    isEnabled = is_enabled != 0L,
    steps =
      stepRows
        .asSequence()
        .filter { it.routine_id == id }
        .sortedBy { it.position }
        .map { step ->
          RoutineStep(
            id = step.id,
            stationId = step.station_id,
            stationName = step.station_name,
            cityId = step.city_id,
            durationSeconds = step.duration_seconds.toInt(),
            kind = runCatching { SurfaceMonitoringKind.valueOf(step.kind) }.getOrDefault(SurfaceMonitoringKind.Bikes),
            threshold = step.threshold?.toInt(),
            fallbackStationId = step.fallback_station_id,
            alternativeRadiusMeters = step.alternative_radius_meters?.toInt(),
          )
        }.toList(),
  )
