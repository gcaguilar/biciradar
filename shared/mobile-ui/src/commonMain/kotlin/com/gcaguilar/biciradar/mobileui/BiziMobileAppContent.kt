package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.screens.FavoritesScreen
import com.gcaguilar.biciradar.mobileui.screens.FavoritesSearchScreen
import com.gcaguilar.biciradar.mobileui.screens.NearbyScreen
import com.gcaguilar.biciradar.mobileui.screens.ProfileScreen
import com.gcaguilar.biciradar.mobileui.screens.ShortcutsScreen
import com.gcaguilar.biciradar.mobileui.screens.StationDetailScreen
import com.gcaguilar.biciradar.mobileui.screens.TripDestinationSearchScreen
import com.gcaguilar.biciradar.mobileui.screens.TripMapPickerScreen
import com.gcaguilar.biciradar.mobileui.screens.TripScreen
import com.gcaguilar.biciradar.mobileui.viewmodel.TripMapPickerMode
import kotlinx.coroutines.launch

internal object BiziMobileAppContent {
  @Composable
  fun TripScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel,
    mobilePlatform: MobileUiPlatform,
    onOpenDestinationPicker: () -> Unit,
    onOpenStationPicker: () -> Unit,
    paddingValues: PaddingValues,
  ) {
    val state by viewModel.uiState.collectAsState()
    TripScreen(
      state = state,
      mobilePlatform = mobilePlatform,
      onDismissAlert = viewModel::onDismissAlert,
      onClearTrip = viewModel::onClearTrip,
      onStopMonitoring = viewModel::onStopMonitoring,
      onDurationSelected = viewModel::onDurationSelected,
      onStartMonitoring = viewModel::onStartMonitoringRequested,
      onRefreshStations = viewModel::onRefresh,
      onOpenDestinationPicker = onOpenDestinationPicker,
      onOpenStationPicker = onOpenStationPicker,
      onLaunchBikeRoute = viewModel::onLaunchBikeRoute,
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun TripMapPickerScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel,
    mobilePlatform: MobileUiPlatform,
    pickerMode: TripMapPickerMode,
    isMapReady: Boolean,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
  ) {
    val state by viewModel.uiState.collectAsState()
    TripMapPickerScreen(
      state = state,
      mobilePlatform = mobilePlatform,
      pickerMode = pickerMode,
      isMapReady = isMapReady,
      paddingValues = paddingValues,
      onBack = onBack,
      onCancelMapPicker = viewModel::onCancelMapPicker,
      onEnterMapPicker = viewModel::onEnterMapPicker,
      onStationPickedFromMap = viewModel::onStationPickedFromMap,
      onLocationPicked = viewModel::onLocationPicked,
      onQueryChange = viewModel::onQueryChange,
      onSuggestionSelected = viewModel::onSuggestionSelected,
      onConfirmMapSelection = viewModel::onConfirmMapSelection,
    )
  }

  @Composable
  fun TripDestinationSearchScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
  ) {
    val state by viewModel.uiState.collectAsState()
    TripDestinationSearchScreen(
      state = state,
      mobilePlatform = mobilePlatform,
      paddingValues = paddingValues,
      onBack = onBack,
      onQueryChange = viewModel::onQueryChange,
      onSuggestionSelected = viewModel::onSuggestionSelected,
    )
  }

  @Composable
  fun NearbyScreenContent(
    state: com.gcaguilar.biciradar.mobileui.viewmodel.NearbyUiState,
    mobilePlatform: MobileUiPlatform,
    onStationSelected: (Station) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    onRequestLocationPermission: () -> Unit,
    paddingValues: PaddingValues,
  ) = NearbyScreen(
    state = state,
    mobilePlatform = mobilePlatform,
    onStationSelected = onStationSelected,
    onRetry = onRetry,
    onRefresh = onRefresh,
    onFavoriteToggle = onFavoriteToggle,
    onQuickRoute = onQuickRoute,
    onRequestLocationPermission = onRequestLocationPermission,
    paddingValues = paddingValues,
  )

  @Composable
  fun NearbyScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModel,
    mobilePlatform: MobileUiPlatform,
    onStationSelected: (Station) -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    NearbyScreen(
      state = uiState,
      mobilePlatform = mobilePlatform,
      onStationSelected = onStationSelected,
      onRetry = viewModel::onRetry,
      onRefresh = viewModel::onRefresh,
      onFavoriteToggle = viewModel::onFavoriteToggle,
      onQuickRoute = viewModel::onQuickRoute,
      onRequestLocationPermission = viewModel::onRequestLocationPermission,
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun FavoritesScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel,
    mobilePlatform: MobileUiPlatform,
    onOpenAssistant: () -> Unit,
    onStationSelected: (Station) -> Unit,
    onOpenSavedPlaceAlerts: () -> Unit,
    onOpenSearch: () -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    FavoritesScreen(
      state = uiState,
      mobilePlatform = mobilePlatform,
      onOpenAssistant = onOpenAssistant,
      onSearchQueryChange = viewModel::onSearchQueryChange,
      onStationSelected = onStationSelected,
      onAssignHomeStation = viewModel::onAssignHomeStation,
      onAssignWorkStation = viewModel::onAssignWorkStation,
      onClearHomeStation = viewModel::onClearHomeStation,
      onClearWorkStation = viewModel::onClearWorkStation,
      onRemoveFavorite = viewModel::onRemoveFavorite,
      onQuickRoute = viewModel::onQuickRoute,
      onRefreshStations = viewModel::onRefresh,
      onOpenSavedPlaceAlerts = onOpenSavedPlaceAlerts,
      onOpenSearch = onOpenSearch,
      paddingValues = paddingValues,
      onCreateCustomCategory = viewModel::onCreateCustomCategory,
      onNewCategoryNameChange = viewModel::onNewCategoryNameChange,
      onAssignCandidateToCategory = viewModel::onAssignCandidateToCategory,
      onRemoveCustomCategory = viewModel::onRemoveCustomCategory,
      onClearCategoryAssignment = viewModel::onClearCategoryAssignment,
      onUpsertSavedPlaceAlert = viewModel::onUpsertSavedPlaceAlert,
      onRemoveSavedPlaceAlertForTarget = viewModel::onRemoveSavedPlaceAlertForTarget,
    )
  }

  @Composable
  fun FavoritesSearchScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel,
    mobilePlatform: MobileUiPlatform,
    onBack: () -> Unit,
    onStationSelected: (Station) -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    FavoritesSearchScreen(
      mobilePlatform = mobilePlatform,
      allStations = uiState.allStations,
      favoriteStationIds = uiState.favoriteStations.mapTo(mutableSetOf()) { it.id },
      homeStationId = uiState.homeStation?.id,
      workStationId = uiState.workStation?.id,
      categories = uiState.categories,
      stationCategory = uiState.stationCategory,
      searchQuery = uiState.searchQuery,
      newCategoryName = uiState.newCategoryName,
      onSearchQueryChange = viewModel::onSearchQueryChange,
      onNewCategoryNameChange = viewModel::onNewCategoryNameChange,
      onBack = onBack,
      onOpenStationDetails = onStationSelected,
      onToggleFavorite = viewModel::onToggleFavorite,
      onAssignHome = { station ->
        viewModel.onAssignHomeStation(station)
        onBack()
      },
      onAssignWork = { station ->
        viewModel.onAssignWorkStation(station)
        onBack()
      },
      onAssignStationToCategory = { station, categoryId ->
        viewModel.onAssignStationToCategory(station, categoryId)
        onBack()
      },
      onCreateCustomCategory = {
        val label = uiState.newCategoryName.trim()
        if (label.isNotBlank()) {
          viewModel.onCreateCustomCategory(label)
          viewModel.onNewCategoryNameChange("")
        }
      },
    )
  }

  @Composable
  fun ProfileScreenContent(
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    searchRadiusMeters: Int,
    preferredMapApp: PreferredMapApp,
    themePreference: ThemePreference,
    selectedCity: City,
    onSearchRadiusSelected: (Int) -> Unit,
    onPreferredMapAppSelected: (PreferredMapApp) -> Unit,
    onThemePreferenceSelected: (ThemePreference) -> Unit,
    onCitySelected: (City) -> Unit,
    canSelectGoogleMapsInIos: Boolean = true,
    showProfileSetupCard: Boolean = false,
    onShowChangelog: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onOpenShortcuts: () -> Unit = {},
    onOpenGarminPairing: () -> Unit = {},
    onRateApp: () -> Unit = {},
  ) = ProfileScreen(
    state =
      com.gcaguilar.biciradar.mobileui.viewmodel.ProfileUiState(
        searchRadiusMeters = searchRadiusMeters,
        preferredMapApp = preferredMapApp,
        canSelectGoogleMapsInIos = canSelectGoogleMapsInIos,
        themePreference = themePreference,
        selectedCity = selectedCity,
        showProfileSetupCard = showProfileSetupCard,
        filteredCities = City.entries.sortedBy { it.displayName },
      ),
    mobilePlatform = mobilePlatform,
    paddingValues = paddingValues,
    onSearchRadiusSelected = onSearchRadiusSelected,
    onPreferredMapAppSelected = onPreferredMapAppSelected,
    onThemePreferenceSelected = onThemePreferenceSelected,
    onCitySelected = onCitySelected,
    onCitySearchQueryChange = {},
    onClearCitySearchQuery = {},
    onShowChangelog = onShowChangelog,
    onOpenOnboarding = onOpenOnboarding,
    onOpenShortcuts = onOpenShortcuts,
    onOpenFeedback = onOpenFeedback,
    onOpenGarminPairing = onOpenGarminPairing,
    onRateApp = onRateApp,
  )

  @Composable
  fun ProfileScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onOpenOnboarding: () -> Unit,
    onOpenShortcuts: () -> Unit,
    platformBindings: PlatformBindings,
    onShowChangelogManual: () -> Unit,
  ) {
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreen(
      state = uiState,
      mobilePlatform = mobilePlatform,
      paddingValues = paddingValues,
      onSearchRadiusSelected = viewModel::onSearchRadiusSelected,
      onPreferredMapAppSelected = viewModel::onPreferredMapAppSelected,
      onThemePreferenceSelected = viewModel::onThemePreferenceSelected,
      onCitySelected = viewModel::onCitySelected,
      onCitySearchQueryChange = viewModel::onCitySearchQueryChange,
      onClearCitySearchQuery = viewModel::clearCitySearchQuery,
      onShowChangelog = onShowChangelogManual,
      onOpenOnboarding = onOpenOnboarding,
      onOpenShortcuts = onOpenShortcuts,
      onOpenFeedback = { platformBindings.externalLinks.openFeedbackForm() },
      onOpenGarminPairing = { platformBindings.externalLinks.openGarminDevicePairing() },
      onRateApp = {
        if (mobilePlatform == MobileUiPlatform.Android) {
          scope.launch {
            platformBindings.reviewPrompter.requestInAppReviewOrStoreFallback()
          }
        } else {
          platformBindings.reviewPrompter.openStoreWriteReview()
        }
      },
    )
  }

  @Composable
  fun SavedPlaceAlertsScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    SavedPlaceAlertsListScreen(
      mobilePlatform = mobilePlatform,
      rules = uiState.rules,
      paddingValues = paddingValues,
      onBack = onBack,
      onSetEnabled = viewModel::onSetEnabled,
      onUpsert = viewModel::onUpsert,
      onRemoveRule = viewModel::onRemoveRule,
    )
  }

  @Composable
  fun ShortcutsScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    initialAction: AssistantAction?,
    onInitialActionConsumed: () -> Unit,
    onBack: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel, initialAction) {
      val action = initialAction ?: return@LaunchedEffect
      viewModel.resolveInitialAction(action)
      onInitialActionConsumed()
    }
    ShortcutsScreen(
      mobilePlatform = mobilePlatform,
      paddingValues = paddingValues,
      searchRadiusMeters = uiState.searchRadiusMeters,
      latestAnswer = uiState.latestAnswer,
      onBack = onBack,
    )
  }

  @Composable
  fun MapScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalViewModel,
    mobilePlatform: MobileUiPlatform,
    isMapReady: Boolean,
    onStationSelected: (Station) -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    MapScreen(
      state = uiState,
      mobilePlatform = mobilePlatform,
      onRefreshStations = viewModel::onRefresh,
      isMapReady = isMapReady,
      onSearchQueryChange = viewModel::onSearchQueryChange,
      onStationSelected = onStationSelected,
      onRetry = viewModel::onRetry,
      onFavoriteToggle = viewModel::onFavoriteToggle,
      onQuickRoute = viewModel::onQuickRoute,
      onStationSelectedOnMap = viewModel::onStationSelected,
      onStationCardDismissed = viewModel::onStationCardDismissed,
      onRecenterRequested = viewModel::onRecenterRequested,
      onEnvironmentalSheetShown = viewModel::onEnvironmentalSheetShown,
      onEnvironmentalSheetDismissed = viewModel::onEnvironmentalSheetDismissed,
      onClearEnvironmentalFilters = viewModel::onClearEnvironmentalFilters,
      onToggleFilter = viewModel::onToggleFilter,
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun StationDetailScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModel,
    mobilePlatform: MobileUiPlatform,
    isMapReady: Boolean,
    onBack: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    StationDetailScreen(
      state = uiState,
      mobilePlatform = mobilePlatform,
      isMapReady = isMapReady,
      onRefreshStations = viewModel::onRefresh,
      onBack = onBack,
      onToggleFavorite = viewModel::onToggleFavorite,
      onToggleHome = viewModel::onToggleHome,
      onToggleWork = viewModel::onToggleWork,
      onRoute = { uiState.station?.let(viewModel::onRoute) },
      onUpsertSavedPlaceAlert = viewModel::onUpsertSavedPlaceAlert,
      onRemoveSavedPlaceAlertForTarget = viewModel::onRemoveSavedPlaceAlertForTarget,
    )
  }
}
