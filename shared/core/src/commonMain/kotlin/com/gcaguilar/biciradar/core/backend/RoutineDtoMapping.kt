package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.RoutineStep
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind

/**
 * Bidirectional mapping between the app routine model and the backend DTOs.
 *
 * `Bikes` (needs bikes) maps to `DEPARTURE`; `Docks` (needs docks) maps to
 * `ARRIVAL`.
 */
fun SurfaceMonitoringKind.toWireKind(): String =
  when (this) {
    SurfaceMonitoringKind.Bikes -> "DEPARTURE"
    SurfaceMonitoringKind.Docks -> "ARRIVAL"
  }

fun wireKindToSurfaceMonitoringKind(kind: String): SurfaceMonitoringKind? =
  when (kind.uppercase()) {
    "DEPARTURE" -> SurfaceMonitoringKind.Bikes
    "ARRIVAL" -> SurfaceMonitoringKind.Docks
    else -> null
  }

fun RoutineStep.toRoutineStepDto(): RoutineStepDto =
  RoutineStepDto(
    id = id,
    stationId = stationId,
    cityId = cityId,
    kind = kind.toWireKind(),
    durationSeconds = durationSeconds,
    threshold = threshold,
    fallbackStationId = fallbackStationId,
    alternativeRadiusMeters = alternativeRadiusMeters,
  )

fun Routine.toRoutineDto(): RoutineDto =
  RoutineDto(
    id = id,
    name = name,
    daysOfWeek = daysOfWeek.map { it.isoValue }.sorted(),
    hour = hour,
    minute = minute,
    enabled = isEnabled,
    steps = steps.map { it.toRoutineStepDto() },
  )

/**
 * Maps the wire routine back to the app model. [stationNames] lets callers keep
 * a human-readable station name that is not part of the backend contract.
 */
fun RoutineDto.toRoutine(stationNames: Map<String, String> = emptyMap()): Routine =
  Routine(
    id = id,
    name = name,
    daysOfWeek = daysOfWeek.mapNotNull(RoutineDay::fromIso).toSet(),
    hour = hour,
    minute = minute,
    isEnabled = enabled,
    steps =
      steps.map { step ->
        RoutineStep(
          id = step.id,
          stationId = step.stationId,
          stationName = stationNames[step.stationId].orEmpty(),
          cityId = step.cityId,
          durationSeconds = step.durationSeconds,
          kind = wireKindToSurfaceMonitoringKind(step.kind) ?: SurfaceMonitoringKind.Bikes,
          threshold = step.threshold,
          fallbackStationId = step.fallbackStationId,
          alternativeRadiusMeters = step.alternativeRadiusMeters,
        )
      },
  )
