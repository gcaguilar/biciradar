package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SavedPlaceAlertsUiState(
  val rules: List<SavedPlaceAlertRule> = emptyList(),
)

class SavedPlaceAlertsViewModel(
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(SavedPlaceAlertsUiState())
  val uiState: StateFlow<SavedPlaceAlertsUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      savedPlaceAlertsRepository.bootstrap()
      savedPlaceAlertsRepository.rules.collect { rules ->
        _uiState.value = SavedPlaceAlertsUiState(rules = rules)
      }
    }
  }

  fun onSetEnabled(ruleId: String, enabled: Boolean) {
    viewModelScope.launch {
      savedPlaceAlertsRepository.setRuleEnabled(ruleId, enabled)
    }
  }

  fun onUpsert(target: SavedPlaceAlertTarget, condition: SavedPlaceAlertCondition) {
    viewModelScope.launch {
      savedPlaceAlertsRepository.upsertRule(target, condition)
    }
  }

  fun onRemoveRule(ruleId: String) {
    viewModelScope.launch {
      savedPlaceAlertsRepository.removeRule(ruleId)
    }
  }
}
