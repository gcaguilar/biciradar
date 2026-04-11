package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SavedPlaceAlertsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedPlaceAlertsUiState(
  val rules: List<SavedPlaceAlertRule> = emptyList(),
)

class SavedPlaceAlertsViewModel(
  private val savedPlaceAlertsRepository: SavedPlaceAlertsRepository,
) : ViewModel() {
  val uiState: StateFlow<SavedPlaceAlertsUiState> =
    savedPlaceAlertsRepository.rules
      .map { rules -> SavedPlaceAlertsUiState(rules = rules) }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SavedPlaceAlertsUiState(rules = savedPlaceAlertsRepository.rules.value),
      )

  init {
    viewModelScope.launch {
      savedPlaceAlertsRepository.bootstrap()
    }
  }

  fun onSetEnabled(
    ruleId: String,
    enabled: Boolean,
  ) {
    viewModelScope.launch {
      savedPlaceAlertsRepository.setRuleEnabled(ruleId, enabled)
    }
  }

  fun onUpsert(
    target: SavedPlaceAlertTarget,
    condition: SavedPlaceAlertCondition,
  ) {
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
