package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.usecases.SettingsAggregationUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
  val searchRadiusMeters: Int = 500,
  val preferredMapApp: PreferredMapApp = PreferredMapApp.GoogleMaps,
  val canSelectGoogleMapsInIos: Boolean = true,
  val themePreference: ThemePreference = ThemePreference.System,
  val selectedCity: City = City.ZARAGOZA,
  val latestAnswer: String = "Ask about stations, favorites, or routes",
  val assistantSuggestions: List<AssistantAction> = emptyList(),
  val shortcutGuides: List<ShortcutGuide> = emptyList(),
  val showProfileSetupCard: Boolean = false,
)

data class ShortcutGuide(
  val title: String,
  val description: String,
  val icon: String,
)

internal class ProfileViewModel(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val changeCityUseCase: ChangeCityUseCase,
  private val settingsAggregationUseCase: SettingsAggregationUseCase,
  private val canSelectGoogleMapsInIos: Boolean,
) : ViewModel() {
  private val latestAnswer = MutableStateFlow("Ask about stations, favorites, or routes")
  private val assistantSuggestions = MutableStateFlow<List<AssistantAction>>(emptyList())
  private val shortcutGuides = MutableStateFlow<List<ShortcutGuide>>(emptyList())

  private val repositoryState = combine(
    settingsRepository.searchRadiusMeters,
    settingsRepository.preferredMapApp,
    settingsRepository.themePreference,
    settingsRepository.selectedCity,
    settingsRepository.onboardingChecklist,
  ) { searchRadius, preferredMapApp, themePreference, selectedCity, checklist ->
    ProfileUiState(
      searchRadiusMeters = searchRadius,
      preferredMapApp = preferredMapApp,
      canSelectGoogleMapsInIos = canSelectGoogleMapsInIos,
      themePreference = themePreference,
      selectedCity = selectedCity,
      showProfileSetupCard = settingsAggregationUseCase.shouldShowProfileSetupSection(checklist),
    )
  }

  val uiState: StateFlow<ProfileUiState> = combine(
    repositoryState,
    latestAnswer,
    assistantSuggestions,
    shortcutGuides,
  ) { base, answer, suggestions, guides ->
    val preferredMapApp = base.preferredMapApp
    val normalizedMapApp = if (!canSelectGoogleMapsInIos && preferredMapApp == PreferredMapApp.GoogleMaps) {
      PreferredMapApp.AppleMaps
    } else {
      preferredMapApp
    }
    base.copy(
      preferredMapApp = normalizedMapApp,
      latestAnswer = answer,
      assistantSuggestions = suggestions,
      shortcutGuides = guides,
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.Eagerly,
    initialValue = ProfileUiState(canSelectGoogleMapsInIos = canSelectGoogleMapsInIos),
  )

  init {
    settingsRepository.preferredMapApp
      .onEach { app ->
        if (!canSelectGoogleMapsInIos && app == PreferredMapApp.GoogleMaps) {
          settingsRepository.setPreferredMapApp(PreferredMapApp.AppleMaps)
        }
      }
      .launchIn(viewModelScope)
  }

  fun onSearchRadiusSelected(radius: Int) {
    viewModelScope.launch {
      settingsRepository.setSearchRadiusMeters(radius)
    }
  }

  fun onPreferredMapAppSelected(app: PreferredMapApp) {
    viewModelScope.launch {
      if (app == PreferredMapApp.GoogleMaps && !canSelectGoogleMapsInIos) {
        return@launch
      }
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
      changeCityUseCase.execute(city = city)
    }
  }

  fun updateLatestAnswer(answer: String) {
    latestAnswer.update { answer }
  }

  fun setAssistantSuggestions(suggestions: List<AssistantAction>) {
    assistantSuggestions.update { suggestions }
  }

  fun setShortcutGuides(guides: List<ShortcutGuide>) {
    shortcutGuides.update { guides }
  }
}
