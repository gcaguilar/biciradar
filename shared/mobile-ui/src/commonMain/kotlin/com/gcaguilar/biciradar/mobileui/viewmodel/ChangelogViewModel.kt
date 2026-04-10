package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.mobileui.usecases.AppLifecycleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado UI para changelog.
 */
internal data class ChangelogUiState(
  val changelogPresentation: ChangelogPresentation? = null,
)

/**
 * ViewModel especializado en gestionar el changelog:
 * - Mostrar changelog pendiente
 * - Mostrar historial de changelog
 * - Marcar versiones como vistas
 *
 * SRP: Solo se encarga del changelog.
 */
internal class ChangelogViewModel(
  private val appLifecycleUseCase: AppLifecycleUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ChangelogUiState())
  val uiState: StateFlow<ChangelogUiState> = _uiState.asStateFlow()

  /**
   * Verifica y muestra el changelog pendiente si es necesario.
   * @param isSettingsBootstrapped Si los settings ya están inicializados
   * @param isOnboardingComplete Si el onboarding está completo (para suprimir changelog)
   */
  fun checkPendingChangelog(
    isSettingsBootstrapped: Boolean,
    isOnboardingComplete: Boolean,
  ) {
    if (!isSettingsBootstrapped) return

    // Verificar si el changelog debe suprimirse debido a onboarding pendiente
    val suppression = appLifecycleUseCase.checkChangelogSuppression()
    if (suppression.suppressed && suppression.shouldMarkCurrentVersionSeen && suppression.currentVersionToMark != null) {
      viewModelScope.launch {
        appLifecycleUseCase.markChangelogSeen(suppression.currentVersionToMark)
      }
      return
    }

    val presentation = appLifecycleUseCase.getPendingChangelog()
    if (presentation != null) {
      _uiState.update { it.copy(changelogPresentation = presentation) }
    }
  }

  fun showChangelogHistory() {
    val presentation = appLifecycleUseCase.getChangelogHistory() ?: return
    _uiState.update { it.copy(changelogPresentation = presentation) }
  }

  fun dismissChangelog() {
    val presentation = _uiState.value.changelogPresentation
    _uiState.update { it.copy(changelogPresentation = null) }
    val persistSeenVersion = presentation?.persistSeenVersion ?: return
    viewModelScope.launch {
      appLifecycleUseCase.markChangelogSeen(persistSeenVersion)
    }
  }
}
