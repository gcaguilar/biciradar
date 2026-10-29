package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineRepository
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.backend.BackendConfig
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.StateFlow

/**
 * Use case de rutinas: CRUD + acceso al estado de estaciones para el editor
 * (favoritos, búsqueda y mapa).
 */
@Inject
class RoutineManagementUseCase(
  private val routineRepository: RoutineRepository,
  private val stationsRepository: StationsRepository,
  private val settingsRepository: SettingsRepository,
  private val authProvider: AuthProvider,
  private val backendConfig: BackendConfig,
) {
  val routines: StateFlow<List<Routine>> = routineRepository.state
  val stationsState: StateFlow<StationsState> = stationsRepository.state

  /** Estado de sesión para la UI (sincronizado vs solo en este dispositivo). */
  val authState: StateFlow<AuthState> = authProvider.credentialsState

  val isBackendConfigured: Boolean get() = backendConfig.isConfigured

  /** Ciudad seleccionada actualmente; los pasos nuevos la heredan. */
  val currentCityId: String get() = settingsRepository.currentSelectedCity().id

  suspend fun bootstrap() {
    routineRepository.bootstrap()
  }

  fun routineById(id: String): Routine? = routineRepository.routineById(id)

  suspend fun upsert(routine: Routine) {
    routineRepository.upsertRoutine(routine)
  }

  suspend fun delete(routineId: String) {
    routineRepository.deleteRoutine(routineId)
  }

  suspend fun setEnabled(
    routineId: String,
    enabled: Boolean,
  ) {
    routineRepository.setEnabled(routineId, enabled)
  }

  suspend fun login(): Boolean = authProvider.login()

  suspend fun logout() {
    authProvider.logout()
  }
}
