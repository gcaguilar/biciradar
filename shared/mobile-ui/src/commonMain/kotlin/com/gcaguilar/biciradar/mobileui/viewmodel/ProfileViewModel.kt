package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DefaultAssistantIntentResolver
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.MR
import com.gcaguilar.biciradar.core.StationsRepository
import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import com.gcaguilar.biciradar.core.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
  val searchRadiusMeters: Int = 500,
  val preferredMapApp: PreferredMapApp = PreferredMapApp.GoogleMaps,
  val themePreference: ThemePreference = ThemePreference.System,
  val selectedCity: City = City.ZARAGOZA,
  val latestAnswer: StringDesc = StringDesc.Resource(MR.strings.askAboutStationsFavoritesOrRoutes),
  val assistantSuggestions: List<AssistantAction> = emptyList(),
  val shortcutGuides: List<ShortcutGuide> = emptyList(),
)

data class ShortcutGuide(
  val title: String,
  val description: String,
  val icon: String,
)

class ProfileViewModel(
  private val settingsRepository: SettingsRepository,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val searchRadiusMeters: Int,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUiState())
  val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      settingsRepository.searchRadiusMeters.collect { radius ->
        _uiState.value = _uiState.value.copy(searchRadiusMeters = radius)
      }
    }

    viewModelScope.launch {
      settingsRepository.preferredMapApp.collect { app ->
        _uiState.value = _uiState.value.copy(preferredMapApp = app)
      }
    }

    viewModelScope.launch {
      settingsRepository.themePreference.collect { theme ->
        _uiState.value = _uiState.value.copy(themePreference = theme)
      }
    }

    viewModelScope.launch {
      settingsRepository.selectedCity.collect { city ->
        _uiState.value = _uiState.value.copy(selectedCity = city)
      }
    }
  }

  fun onSearchRadiusSelected(radius: Int) {
    viewModelScope.launch {
      settingsRepository.setSearchRadiusMeters(radius)
    }
  }

  fun onPreferredMapAppSelected(app: PreferredMapApp) {
    viewModelScope.launch {
      settingsRepository.setPreferredMapApp(app)
    }
  }

  fun onThemePreferenceSelected(theme: ThemePreference) {
    viewModelScope.launch {
      settingsRepository.setThemePreference(theme)
    }
  }

  fun onCitySelected(city: City) {
    viewModelScope.launch {
      settingsRepository.setSelectedCity(city)
      favoritesRepository.clearAll()
      stationsRepository.forceRefresh()
    }
  }

  fun updateLatestAnswer(answer: StringDesc) {
    _uiState.value = _uiState.value.copy(latestAnswer = answer)
  }

  fun setAssistantSuggestions(suggestions: List<AssistantAction>) {
    _uiState.value = _uiState.value.copy(assistantSuggestions = suggestions)
  }

  fun setShortcutGuides(guides: List<ShortcutGuide>) {
    _uiState.value = _uiState.value.copy(shortcutGuides = guides)
  }
}
