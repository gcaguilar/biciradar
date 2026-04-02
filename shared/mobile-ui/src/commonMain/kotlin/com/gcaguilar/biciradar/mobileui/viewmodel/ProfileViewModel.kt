package com.gcaguilar.biciradar.mobileui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.ChangeCityUseCase
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.PermissionPrompter
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SettingsRepository
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

class ProfileViewModel(
  private val settingsRepository: SettingsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val changeCityUseCase: ChangeCityUseCase,
  private val permissionPrompter: PermissionPrompter,
  private val localNotifier: LocalNotifier,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUiState())
  val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

  private var latestChecklist = settingsRepository.onboardingChecklist.value
  private var latestFavoriteIds: Set<String> = favoritesRepository.favoriteIds.value
  private var latestHomeStationId: String? = favoritesRepository.homeStationId.value
  private var latestWorkStationId: String? = favoritesRepository.workStationId.value
  private var latestHasLocationPermission: Boolean? = null
  private var latestHasNotificationPermission: Boolean? = null

  init {
    viewModelScope.launch {
      settingsRepository.searchRadiusMeters.collect { radius ->
        publishUiState(_uiState.value.copy(searchRadiusMeters = radius))
      }
    }

    viewModelScope.launch {
      settingsRepository.preferredMapApp.collect { app ->
        publishUiState(_uiState.value.copy(preferredMapApp = app))
      }
    }

    viewModelScope.launch {
      settingsRepository.themePreference.collect { theme ->
        publishUiState(_uiState.value.copy(themePreference = theme))
      }
    }

    viewModelScope.launch {
      settingsRepository.selectedCity.collect { city ->
        publishUiState(_uiState.value.copy(selectedCity = city))
      }
    }

    viewModelScope.launch {
      settingsRepository.onboardingChecklist.collect { checklist ->
        latestChecklist = checklist
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { favoriteIds ->
        latestFavoriteIds = favoriteIds
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.homeStationId.collect { homeStationId ->
        latestHomeStationId = homeStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.workStationId.collect { workStationId ->
        latestWorkStationId = workStationId
        publishUiState()
      }
    }

    refreshSetupRequirements()
  }

  private fun publishUiState(baseState: ProfileUiState = _uiState.value) {
    val hasLocationPermission = latestHasLocationPermission
    val hasNotificationPermission = latestHasNotificationPermission
    val showProfileSetupCard = hasLocationPermission != null &&
      hasNotificationPermission != null &&
      latestChecklist.needsProfileSetupCard(
        hasLocationPermission = hasLocationPermission,
        hasNotificationPermission = hasNotificationPermission,
        hasFavoriteStations = latestFavoriteIds.isNotEmpty(),
        hasHomeStation = latestHomeStationId != null,
        hasWorkStation = latestWorkStationId != null,
      )
    _uiState.value = baseState.copy(showProfileSetupCard = showProfileSetupCard)
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
      changeCityUseCase.execute(city = city)
      refreshSetupRequirements()
    }
  }

  fun updateLatestAnswer(answer: String) {
    publishUiState(_uiState.value.copy(latestAnswer = answer))
  }

  fun setAssistantSuggestions(suggestions: List<AssistantAction>) {
    publishUiState(_uiState.value.copy(assistantSuggestions = suggestions))
  }

  fun setShortcutGuides(guides: List<ShortcutGuide>) {
    publishUiState(_uiState.value.copy(shortcutGuides = guides))
  }

  private fun refreshSetupRequirements() {
    viewModelScope.launch {
      latestHasLocationPermission = permissionPrompter.hasLocationPermission()
      latestHasNotificationPermission = localNotifier.hasPermission()
      publishUiState()
    }
  }
}
