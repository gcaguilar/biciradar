package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.mobileui.usecases.RoutineManagementUseCase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RoutineListUiState(
  val routines: List<Routine> = emptyList(),
  val loading: Boolean = true,
  val authState: AuthState = AuthState.Anonymous,
  val isBackendConfigured: Boolean = false,
)

@Inject
@ViewModelKey
@ContributesIntoMap(AppScope::class)
class RoutineListViewModel(
  private val routineManagementUseCase: RoutineManagementUseCase,
) : ViewModel() {
  private val transient = MutableStateFlow(RoutineListUiState(loading = true))

  val uiState: StateFlow<RoutineListUiState> =
    combine(
      transient,
      routineManagementUseCase.routines,
      routineManagementUseCase.authState,
    ) { state, routines, authState ->
      state.copy(
        routines = routines.sortedBy { it.displayName.lowercase() },
        loading = false,
        authState = authState,
        isBackendConfigured = routineManagementUseCase.isBackendConfigured,
      )
    }.stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      transient.value.copy(
        routines = routineManagementUseCase.routines.value,
        isBackendConfigured = routineManagementUseCase.isBackendConfigured,
      ),
    )

  init {
    viewModelScope.launch {
      routineManagementUseCase.bootstrap()
      transient.value = transient.value.copy(loading = false)
    }
  }

  fun onLogin() {
    viewModelScope.launch { routineManagementUseCase.login() }
  }

  fun onLogout() {
    viewModelScope.launch { routineManagementUseCase.logout() }
  }

  fun onToggleEnabled(
    routineId: String,
    enabled: Boolean,
  ) {
    viewModelScope.launch {
      routineManagementUseCase.setEnabled(routineId, enabled)
    }
  }

  fun onDelete(routineId: String) {
    viewModelScope.launch {
      routineManagementUseCase.delete(routineId)
    }
  }

  fun onRetry() {
    viewModelScope.launch {
      routineManagementUseCase.bootstrap()
      transient.value = transient.value.copy(loading = false)
    }
  }
}
