package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.screens.ProfileScreen
import com.gcaguilar.biciradar.mobileui.screens.ShortcutsScreen
import com.gcaguilar.biciradar.mobileui.screens.StationDetailScreen
import com.gcaguilar.biciradar.mobileui.screens.TripScreen

internal object BiziMobileAppContent {
  @Composable
  fun TripScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel,
    mobilePlatform: MobileUiPlatform,
    localNotifier: LocalNotifier,
    routeLauncher: RouteLauncher,
    userLocation: GeoPoint?,
    stations: List<Station>,
    isMapReady: Boolean,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    paddingValues: PaddingValues,
  ) = TripScreen(
    viewModel = viewModel,
    mobilePlatform = mobilePlatform,
    localNotifier = localNotifier,
    routeLauncher = routeLauncher,
    userLocation = userLocation,
    stations = stations,
    isMapReady = isMapReady,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    stationsLoading = stationsLoading,
    onRefreshStations = onRefreshStations,
    paddingValues = paddingValues,
  )

  @Composable
  fun NearbyScreenContent(
    mobilePlatform: MobileUiPlatform,
    stations: List<Station>,
    favoriteIds: Set<String>,
    loading: Boolean,
    errorMessage: String?,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
    searchRadiusMeters: Int,
    onStationSelected: (Station) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    refreshCountdownSeconds: Int,
    paddingValues: PaddingValues,
  ) = NearbyScreen(
    mobilePlatform = mobilePlatform,
    stations = stations,
    favoriteIds = favoriteIds,
    loading = loading,
    errorMessage = errorMessage,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    nearestSelection = nearestSelection,
    searchRadiusMeters = searchRadiusMeters,
    onStationSelected = onStationSelected,
    onRetry = onRetry,
    onRefresh = onRefresh,
    onFavoriteToggle = onFavoriteToggle,
    onQuickRoute = onQuickRoute,
    refreshCountdownSeconds = refreshCountdownSeconds,
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
      mobilePlatform = mobilePlatform,
      stations = uiState.stations,
      favoriteIds = uiState.favoriteIds,
      loading = uiState.isLoading,
      errorMessage = uiState.errorMessage,
      dataFreshness = uiState.dataFreshness,
      lastUpdatedEpoch = uiState.lastUpdatedEpoch,
      nearestSelection = uiState.nearestSelection,
      searchRadiusMeters = uiState.searchRadiusMeters,
      onStationSelected = onStationSelected,
      onRetry = viewModel::onRetry,
      onRefresh = viewModel::onRefresh,
      onFavoriteToggle = viewModel::onFavoriteToggle,
      onQuickRoute = viewModel::onQuickRoute,
      refreshCountdownSeconds = uiState.refreshCountdownSeconds,
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun FavoritesScreenContent(
    mobilePlatform: MobileUiPlatform,
    onOpenAssistant: () -> Unit,
    allStations: List<Station>,
    stations: List<Station>,
    homeStation: Station?,
    workStation: Station?,
    searchQuery: String,
    assignmentCandidate: Station?,
    onSearchQueryChange: (String) -> Unit,
    onStationSelected: (Station) -> Unit,
    onAssignHomeStation: (Station) -> Unit,
    onAssignWorkStation: (Station) -> Unit,
    onClearHomeStation: () -> Unit,
    onClearWorkStation: () -> Unit,
    onRemoveFavorite: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    onOpenSavedPlaceAlerts: () -> Unit,
    paddingValues: PaddingValues,
    savedPlaceAlertsCityId: String = City.ZARAGOZA.id,
    savedPlaceAlertRules: List<SavedPlaceAlertRule> = emptyList(),
    onUpsertSavedPlaceAlert: ((SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit)? = null,
    onRemoveSavedPlaceAlertForTarget: ((SavedPlaceAlertTarget) -> Unit)? = null,
  ) = FavoritesScreen(
    mobilePlatform = mobilePlatform,
    onOpenAssistant = onOpenAssistant,
    allStations = allStations,
    stations = stations,
    homeStation = homeStation,
    workStation = workStation,
    searchQuery = searchQuery,
    assignmentCandidate = assignmentCandidate,
    onSearchQueryChange = onSearchQueryChange,
    onStationSelected = onStationSelected,
    onAssignHomeStation = onAssignHomeStation,
    onAssignWorkStation = onAssignWorkStation,
    onClearHomeStation = onClearHomeStation,
    onClearWorkStation = onClearWorkStation,
    onRemoveFavorite = onRemoveFavorite,
    onQuickRoute = onQuickRoute,
    dataFreshness = dataFreshness,
    lastUpdatedEpoch = lastUpdatedEpoch,
    stationsLoading = stationsLoading,
    onRefreshStations = onRefreshStations,
    onOpenSavedPlaceAlerts = onOpenSavedPlaceAlerts,
    paddingValues = paddingValues,
    savedPlaceAlertsCityId = savedPlaceAlertsCityId,
    savedPlaceAlertRules = savedPlaceAlertRules,
    onUpsertSavedPlaceAlert = onUpsertSavedPlaceAlert,
    onRemoveSavedPlaceAlertForTarget = onRemoveSavedPlaceAlertForTarget,
  )

  @Composable
  fun FavoritesScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel,
    mobilePlatform: MobileUiPlatform,
    onOpenAssistant: () -> Unit,
    onStationSelected: (Station) -> Unit,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    onOpenSavedPlaceAlerts: () -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    FavoritesScreen(
      mobilePlatform = mobilePlatform,
      onOpenAssistant = onOpenAssistant,
      allStations = uiState.allStations,
      stations = uiState.favoriteStations,
      homeStation = uiState.homeStation,
      workStation = uiState.workStation,
      searchQuery = uiState.searchQuery,
      assignmentCandidate = uiState.assignmentCandidate,
      onSearchQueryChange = viewModel::onSearchQueryChange,
      onStationSelected = onStationSelected,
      onAssignHomeStation = viewModel::onAssignHomeStation,
      onAssignWorkStation = viewModel::onAssignWorkStation,
      onClearHomeStation = viewModel::onClearHomeStation,
      onClearWorkStation = viewModel::onClearWorkStation,
      onRemoveFavorite = viewModel::onRemoveFavorite,
      onQuickRoute = viewModel::onQuickRoute,
      dataFreshness = dataFreshness,
      lastUpdatedEpoch = lastUpdatedEpoch,
      stationsLoading = stationsLoading,
      onRefreshStations = onRefreshStations,
      onOpenSavedPlaceAlerts = onOpenSavedPlaceAlerts,
      paddingValues = paddingValues,
      savedPlaceAlertsCityId = uiState.savedPlaceAlertsCityId,
      savedPlaceAlertRules = uiState.savedPlaceAlertRules,
      onUpsertSavedPlaceAlert = viewModel::onUpsertSavedPlaceAlert,
      onRemoveSavedPlaceAlertForTarget = viewModel::onRemoveSavedPlaceAlertForTarget,
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
    showProfileSetupCard: Boolean = false,
    onShowChangelog: () -> Unit = {},
    onOpenFeedback: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    onRateApp: () -> Unit = {},
  ) = ProfileScreen(
    mobilePlatform = mobilePlatform,
    paddingValues = paddingValues,
    searchRadiusMeters = searchRadiusMeters,
    preferredMapApp = preferredMapApp,
    themePreference = themePreference,
    selectedCity = selectedCity,
    onSearchRadiusSelected = onSearchRadiusSelected,
    onPreferredMapAppSelected = onPreferredMapAppSelected,
    onThemePreferenceSelected = onThemePreferenceSelected,
    onCitySelected = onCitySelected,
    showProfileSetupCard = showProfileSetupCard,
    onShowChangelog = onShowChangelog,
    onOpenOnboarding = onOpenOnboarding,
    onOpenFeedback = onOpenFeedback,
    onRateApp = onRateApp,
  )

  @Composable
  fun ProfileScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModel,
    mobilePlatform: MobileUiPlatform,
    paddingValues: PaddingValues,
    onOpenOnboarding: () -> Unit,
    platformBindings: PlatformBindings,
    onShowChangelogManual: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    ProfileScreen(
      mobilePlatform = mobilePlatform,
      paddingValues = paddingValues,
      searchRadiusMeters = uiState.searchRadiusMeters,
      preferredMapApp = uiState.preferredMapApp,
      themePreference = uiState.themePreference,
      selectedCity = uiState.selectedCity,
      onSearchRadiusSelected = viewModel::onSearchRadiusSelected,
      onPreferredMapAppSelected = viewModel::onPreferredMapAppSelected,
      onThemePreferenceSelected = viewModel::onThemePreferenceSelected,
      onCitySelected = viewModel::onCitySelected,
      showProfileSetupCard = uiState.showProfileSetupCard,
      onShowChangelog = onShowChangelogManual,
      onOpenOnboarding = onOpenOnboarding,
      onOpenFeedback = { platformBindings.externalLinks.openFeedbackForm() },
      onRateApp = {
        platformBindings.reviewPrompter.openStoreWriteReview()
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
    stations: List<Station>,
    favoriteIds: Set<String>,
    loading: Boolean,
    errorMessage: String?,
    nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
    searchQuery: String,
    searchRadiusMeters: Int,
    userLocation: GeoPoint?,
    isMapReady: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onStationSelected: (Station) -> Unit,
    onRetry: () -> Unit,
    onFavoriteToggle: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    onRefreshStations: () -> Unit,
    paddingValues: PaddingValues,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(viewModel, stations) {
      viewModel.onStationsChanged(stations)
    }
    MapScreen(
      mobilePlatform = mobilePlatform,
      stations = stations,
      favoriteIds = favoriteIds,
      loading = loading,
      errorMessage = errorMessage,
      dataFreshness = dataFreshness,
      lastUpdatedEpoch = lastUpdatedEpoch,
      onRefreshStations = onRefreshStations,
      nearestSelection = nearestSelection,
      searchQuery = searchQuery,
      searchRadiusMeters = searchRadiusMeters,
      userLocation = userLocation,
      isMapReady = isMapReady,
      onSearchQueryChange = onSearchQueryChange,
      onStationSelected = onStationSelected,
      onRetry = onRetry,
      onFavoriteToggle = onFavoriteToggle,
      onQuickRoute = onQuickRoute,
      environmentalSnapshots = uiState.zones,
      onEnvironmentalLayerChanged = viewModel::onEnvironmentalLayerChanged,
      paddingValues = paddingValues,
    )
  }

  @Composable
  fun StationDetailScreenContent(
    viewModel: com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModel,
    mobilePlatform: MobileUiPlatform,
    station: Station,
    userLocation: GeoPoint?,
    isMapReady: Boolean,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    stationsLoading: Boolean,
    onRefreshStations: () -> Unit,
    onBack: () -> Unit,
  ) {
    val uiState by viewModel.uiState.collectAsState()
    StationDetailScreen(
      mobilePlatform = mobilePlatform,
      station = station,
      isFavorite = uiState.isFavorite,
      isHomeStation = uiState.isHomeStation,
      isWorkStation = uiState.isWorkStation,
      userLocation = userLocation,
      isMapReady = isMapReady,
      supportsUsagePatterns = uiState.supportsUsagePatterns,
      dataFreshness = dataFreshness,
      lastUpdatedEpoch = lastUpdatedEpoch,
      stationsLoading = stationsLoading,
      onRefreshStations = onRefreshStations,
      onBack = onBack,
      onToggleFavorite = viewModel::onToggleFavorite,
      onToggleHome = viewModel::onToggleHome,
      onToggleWork = viewModel::onToggleWork,
      onRoute = { viewModel.onRoute(station) },
      savedPlaceAlertsCityId = uiState.savedPlaceAlertsCityId,
      savedPlaceAlertRules = uiState.savedPlaceAlertRules,
      onUpsertSavedPlaceAlert = viewModel::onUpsertSavedPlaceAlert,
      onRemoveSavedPlaceAlertForTarget = viewModel::onRemoveSavedPlaceAlertForTarget,
      patterns = uiState.patterns,
      patternsLoading = uiState.patternsLoading,
      patternsError = uiState.patternsError,
    )
  }
}
