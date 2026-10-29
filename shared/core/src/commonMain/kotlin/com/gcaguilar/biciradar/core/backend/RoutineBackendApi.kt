package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.Routine

/**
 * Narrow seam for routine synchronisation. Implemented by [BiciRadarApiClient];
 * tests substitute a fake without any HTTP machinery.
 */
interface RoutineBackendApi {
  val isConfigured: Boolean

  suspend fun listRoutines(): List<RoutineDto>

  suspend fun upsertRoutine(routine: Routine): RoutineDto

  suspend fun deleteRoutine(id: String)
}
