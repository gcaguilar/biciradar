package com.gcaguilar.biciradar.mobileui


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardVoice
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.PreferredMapApp
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.filterStationsByQuery
import com.gcaguilar.biciradar.core.findSavedPlaceAlertRule
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.isGoogleMapsReady
import com.gcaguilar.biciradar.core.selectNearbyStation
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobileui.components.BiziNavigationShell
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.components.OverlayManager
import com.gcaguilar.biciradar.mobileui.components.station.OutlineActionPill
import com.gcaguilar.biciradar.mobileui.components.station.RoutePill
import com.gcaguilar.biciradar.mobileui.components.station.StationMetricPill
import com.gcaguilar.biciradar.mobileui.components.station.StationRow
import com.gcaguilar.biciradar.mobileui.experience.GuidedOnboardingCallbacks
import com.gcaguilar.biciradar.mobileui.experience.GuidedOnboardingFlow
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.NavigationHost
import com.gcaguilar.biciradar.mobileui.navigation.NavigationHostConfig
import com.gcaguilar.biciradar.mobileui.navigation.Screen
import com.gcaguilar.biciradar.mobileui.navigation.ViewModelFactories
import com.gcaguilar.biciradar.mobileui.theme.ThemeProvider
import com.gcaguilar.biciradar.mobileui.theme.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.theme.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.AppRootViewModelFactory
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@androidx.compose.runtime.Stable
internal class AppState {
  var searchQuery by mutableStateOf("")
  var pendingAssistantAction by mutableStateOf<AssistantAction?>(null)
  var pendingLaunchRequest by mutableStateOf<MobileLaunchRequest?>(null)
  var pendingAssistantLaunchRequest by mutableStateOf<AssistantLaunchRequest?>(null)
}

@Composable
private fun rememberAppState(): AppState = remember { AppState() }

internal fun shouldNavigateToFavoritesAfterOnboarding(
  hasPendingFavoritesNavigation: Boolean,
  shouldShowGuidedOnboarding: Boolean,
): Boolean = hasPendingFavoritesNavigation && !shouldShowGuidedOnboarding

/**
 * Creates and remembers all ViewModel factories needed for navigation.
 */
@Composable
private fun rememberViewModelFactories(
  graph: com.gcaguilar.biciradar.core.SharedGraph,
  platformBindings: PlatformBindings,
): ViewModelFactories {
  val trip = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory(
      tripRepository = graph.tripRepository,
      surfaceMonitoringRepository = graph.surfaceMonitoringRepository,
      geoSearchUseCase = graph.geoSearchUseCase,
      reverseGeocodeUseCase = graph.reverseGeocodeUseCase,
      settingsRepository = graph.settingsRepository,
    )
  }
  val nearby = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory(
      stationsRepository = graph.stationsRepository,
      favoritesRepository = graph.favoritesRepository,
      routeLauncher = graph.routeLauncher,
      settingsRepository = graph.settingsRepository,
    )
  }
  val mapEnvironmental = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalViewModelFactory(
      environmentalRepository = graph.environmentalRepository,
      settingsRepository = graph.settingsRepository,
    )
  }
  val shortcuts = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModelFactory(
      assistantIntentResolver = graph.assistantIntentResolver,
      stationsRepository = graph.stationsRepository,
      favoritesRepository = graph.favoritesRepository,
      settingsRepository = graph.settingsRepository,
    )
  }
  val favorites = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory(
      favoritesRepository = graph.favoritesRepository,
      stationsRepository = graph.stationsRepository,
      settingsRepository = graph.settingsRepository,
      savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
      routeLauncher = graph.routeLauncher,
    )
  }
  val profile = remember(graph, platformBindings) {
    com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory(
      settingsRepository = graph.settingsRepository,
      stationsRepository = graph.stationsRepository,
      favoritesRepository = graph.favoritesRepository,
      savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
      canSelectGoogleMapsInIos = platformBindings.mapSupport.currentStatus().googleMapsAppInstalled,
    )
  }
  val savedPlaceAlerts = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModelFactory(
      savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
    )
  }
  val stationDetail = remember(graph) {
    com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModelFactory(
      favoritesRepository = graph.favoritesRepository,
      settingsRepository = graph.settingsRepository,
      savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
      datosBiziApi = graph.datosBiziApi,
      routeLauncher = graph.routeLauncher,
    )
  }
  return ViewModelFactories(
    trip = trip,
    nearby = nearby,
    mapEnvironmental = mapEnvironmental,
    shortcuts = shortcuts,
    favorites = favorites,
    profile = profile,
    savedPlaceAlerts = savedPlaceAlerts,
    stationDetail = stationDetail,
  )
}

/**
 * Creates and remembers the navigation configuration.
 */
@Composable
private fun rememberNavigationConfig(
  navController: NavHostController,
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  stationsState: com.gcaguilar.biciradar.core.StationsState,
  nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
  searchRadiusMeters: Int,
  isMapReady: Boolean,
  appState: AppState,
  scope: kotlinx.coroutines.CoroutineScope,
  stationsRepository: com.gcaguilar.biciradar.core.StationsRepository,
  favoritesRepository: com.gcaguilar.biciradar.core.FavoritesRepository,
  graph: com.gcaguilar.biciradar.core.SharedGraph,
  platformBindings: PlatformBindings,
  onOpenOnboarding: () -> Unit,
  onShowChangelogManual: () -> Unit,
): NavigationHostConfig {
  val onRefreshStations = remember(scope, stationsRepository) {
    {
      scope.launch { stationsRepository.forceRefresh() }
      Unit
    }
  }
  val onRetry = remember(scope, stationsRepository) {
    { scope.launch { stationsRepository.loadIfNeeded() }; Unit }
  }
  val onFavoriteToggle = remember(scope, favoritesRepository) {
    { station: Station -> scope.launch { favoritesRepository.toggle(station.id) }; Unit }
  }
  val onQuickRoute = remember(graph, scope) {
    { station: Station ->
      scope.launch {
        graph.engagementRepository.markRouteOpened()
        graph.routeLauncher.launch(station)
      }
      Unit
    }
  }
  val onOpenAssistant = remember(navController) {
    { navController.navigate(Screen.Shortcuts) { launchSingleTop = true } }
  }
  val onSearchQueryChange = remember(appState) { { query: String -> appState.searchQuery = query } }
  val onInitialActionConsumed = remember(appState) { { appState.pendingAssistantAction = null } }

  return NavigationHostConfig(
    navController = navController,
    mobilePlatform = mobilePlatform,
    stations = stations,
    favoriteIds = favoriteIds,
    loading = stationsState.isLoading,
    errorMessage = stationsState.errorMessage,
    stationsFreshness = stationsState.freshness,
    stationsLastUpdatedEpoch = stationsState.lastUpdatedEpoch,
    onRefreshStations = onRefreshStations,
    nearestSelection = nearestSelection,
    userLocation = stationsState.userLocation,
    searchQuery = appState.searchQuery,
    searchRadiusMeters = searchRadiusMeters,
    isMapReady = isMapReady,
    onSearchQueryChange = onSearchQueryChange,
    onRetry = onRetry,
    onFavoriteToggle = onFavoriteToggle,
    onQuickRoute = onQuickRoute,
    onOpenAssistant = onOpenAssistant,
    localNotifier = platformBindings.localNotifier,
    routeLauncher = graph.routeLauncher,
    platformBindings = platformBindings,
    initialAssistantAction = appState.pendingAssistantAction,
    onInitialActionConsumed = onInitialActionConsumed,
    onOpenOnboarding = onOpenOnboarding,
    onShowChangelogManual = onShowChangelogManual,
    paddingValues = PaddingValues(),
  )
}

@Composable
fun BiziMobileApp(
  platformBindings: PlatformBindings,
  modifier: Modifier = Modifier,
  refreshKey: Any? = Unit,
  launchRequest: MobileLaunchRequest? = null,
  assistantLaunchRequest: AssistantLaunchRequest? = null,
  onTripRepositoryReady: ((TripRepository) -> Unit)? = null,
  onSurfaceMonitoringRepositoryReady: ((SurfaceMonitoringRepository, FavoritesRepository) -> Unit)? = null,
  onSurfaceSnapshotRepositoryReady: ((SurfaceSnapshotRepository) -> Unit)? = null,
  onStartupReadyChanged: (Boolean) -> Unit = {},
  useInAppStartupSplash: Boolean = true,
) {
  val mobilePlatform = remember { currentMobileUiPlatform() }
  val graph = remember(platformBindings) {
    SharedGraph.Companion.create(platformBindings)
  }
  val mapSupportStatus = remember(platformBindings) { platformBindings.mapSupport.currentStatus() }
  val stationsRepository = remember(graph) { graph.stationsRepository }
  val favoritesRepository = remember(graph) { graph.favoritesRepository }
  val settingsRepository = remember(graph) { graph.settingsRepository }
  val launchCoordinator = remember(graph, platformBindings) {
    LaunchCoordinator(
      changeCityUseCase = graph.changeCityUseCase,
      favoritesRepository = graph.favoritesRepository,
      localNotifier = platformBindings.localNotifier,
      routeLauncher = graph.routeLauncher,
      stationsRepository = graph.stationsRepository,
      surfaceMonitoringRepository = graph.surfaceMonitoringRepository,
      surfaceSnapshotRepository = graph.surfaceSnapshotRepository,
    )
  }
  val appRootViewModelFactory = remember(graph, platformBindings) {
    AppRootViewModelFactory(
      settingsRepository = graph.settingsRepository,
      favoritesRepository = graph.favoritesRepository,
      stationsRepository = graph.stationsRepository,
      savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
      engagementRepository = graph.engagementRepository,
      surfaceSnapshotRepository = graph.surfaceSnapshotRepository,
      surfaceMonitoringRepository = graph.surfaceMonitoringRepository,
      appUpdatePrompter = platformBindings.appUpdatePrompter,
      reviewPrompter = platformBindings.reviewPrompter,
      appVersion = platformBindings.appVersion,
    )
  }
  val appRootViewModel = viewModel(key = "app-root") { appRootViewModelFactory.create() }
  val appRootUiState by appRootViewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()
  val appState = rememberAppState()
  val navController = rememberNavController()
  val stationsState by stationsRepository.state.collectAsState()
  val favoriteIds by favoritesRepository.favoriteIds.collectAsState()
  val searchRadiusMeters by settingsRepository.searchRadiusMeters.collectAsState()
  val preferredMapApp by settingsRepository.preferredMapApp.collectAsState()
  val themePreference by settingsRepository.themePreference.collectAsState()
  val canSelectGoogleMapsInIos = remember(mobilePlatform, mapSupportStatus) {
    mobilePlatform != MobileUiPlatform.IOS || mapSupportStatus.googleMapsAppInstalled
  }
  val isMapReady = when {
    mobilePlatform == MobileUiPlatform.IOS && preferredMapApp == PreferredMapApp.GoogleMaps ->
      mapSupportStatus.googleMapsSdkLinked && canSelectGoogleMapsInIos
    mobilePlatform == MobileUiPlatform.IOS -> false
    else -> mapSupportStatus.isGoogleMapsReady()
  }
  val onboardingChecklist = appRootUiState.onboardingChecklist
  var showFeedbackDialog by remember { mutableStateOf(false) }
  var pendingOnboardingFavoritesNavigation by remember { mutableStateOf(false) }
  val isCityConfigured = !(appRootUiState.isCitySelectionRequired)
  val shouldShowGuidedOnboarding = appRootUiState.shouldShowGuidedOnboarding

  LaunchedEffect(graph) {
    platformBindings.onGraphCreated(graph)
    onTripRepositoryReady?.invoke(graph.tripRepository)
    onSurfaceMonitoringRepositoryReady?.invoke(graph.surfaceMonitoringRepository, graph.favoritesRepository)
    onSurfaceSnapshotRepositoryReady?.invoke(graph.surfaceSnapshotRepository)
  }

  LaunchedEffect(refreshKey) {
    appRootViewModel.onRefreshSignal()
  }

  LaunchedEffect(shouldShowGuidedOnboarding, pendingOnboardingFavoritesNavigation, navController) {
    if (shouldNavigateToFavoritesAfterOnboarding(
        hasPendingFavoritesNavigation = pendingOnboardingFavoritesNavigation,
        shouldShowGuidedOnboarding = shouldShowGuidedOnboarding,
      )
    ) {
      navController.navigate(Screen.Favorites) { launchSingleTop = true }
      pendingOnboardingFavoritesNavigation = false
    }
  }

  val nearestSelection = remember(stationsState.stations, searchRadiusMeters) {
    selectNearbyStation(stationsState.stations, searchRadiusMeters)
  }

  BiziLaunchEffects(
    startupLaunchReady = appRootUiState.startupLaunchReady,
    onStartupReadyChanged = onStartupReadyChanged,
    appState = appState,
    launchRequest = launchRequest,
    assistantLaunchRequest = assistantLaunchRequest,
    stationsState = stationsState,
    searchRadiusMeters = searchRadiusMeters,
    launchCoordinator = launchCoordinator,
    navController = navController,
  )

  val filteredStations = remember(stationsState.stations, appState.searchQuery) {
    filterStations(stationsState.stations, appState.searchQuery)
  }
  val changelogPresentation = appRootUiState.changelogPresentation
  val showStartupSplash = remember(
    useInAppStartupSplash,
    appRootUiState.startupLaunchReady,
  ) {
    useInAppStartupSplash && !appRootUiState.startupLaunchReady
  }

  ThemeProvider(mobilePlatform, themePreference) {
    val windowLayout = LocalBiziWindowLayout.current
    Surface(
        modifier = modifier.fillMaxSize(),
        color = pageBackgroundColor(mobilePlatform),
      ) {
        when {
          !appRootUiState.settingsBootstrapped -> {
            // Loading state while settings are being initialized
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
            ) {
              CircularProgressIndicator()
            }
          }
          !isCityConfigured -> {
            CitySelectionScreen(
              onCitySelected = { city ->
                scope.launch {
                  graph.changeCityUseCase.execute(city = city)
                }
              },
            )
          }
          shouldShowGuidedOnboarding -> {
            val onboardingCallbacks = remember(
              scope,
              platformBindings,
              settingsRepository,
              navController,
            ) {
              GuidedOnboardingCallbacks(
                onContinueFeatureHighlights = {
                  appRootViewModel.onOnboardingFeatureHighlightsContinued()
                },
                onRequestLocationPermission = {
                  scope.launch {
                    platformBindings.permissionPrompter.requestLocationPermission()
                  }
                  appRootViewModel.onOnboardingLocationDecisionMade()
                },
                onDismissLocationStep = {
                  appRootViewModel.onOnboardingLocationDecisionMade()
                },
                onRequestNotificationsPermission = {
                  scope.launch {
                    platformBindings.localNotifier.requestPermission()
                  }
                  appRootViewModel.onOnboardingNotificationsDecisionMade()
                },
                onDismissNotificationsStep = {
                  appRootViewModel.onOnboardingNotificationsDecisionMade()
                },
                onOpenFavorites = {
                  appRootViewModel.onOnboardingOpenFavoritesRequested()
                  pendingOnboardingFavoritesNavigation = true
                },
                onDismissFirstFavoriteStep = {
                  appRootViewModel.onOnboardingFirstFavoriteDismissed()
                },
                onDismissSavedPlacesStep = {
                  appRootViewModel.onOnboardingSavedPlacesDismissed()
                },
                onCompleteSurfacesStep = {
                  appRootViewModel.onOnboardingSurfacesCompleted()
                },
              )
            }
            GuidedOnboardingFlow(
              checklist = onboardingChecklist,
              callbacks = onboardingCallbacks,
            )
          }
          else -> {
            Box(Modifier.fillMaxSize()) {
              AnimatedContent(
                targetState = showStartupSplash,
                transitionSpec = {
                  fadeIn(animationSpec = tween(220)).togetherWith(fadeOut(animationSpec = tween(140)))
                },
                label = "startup-splash-transition",
              ) { splashVisible ->
                if (splashVisible) {
                  StartupSplashScreen(mobilePlatform = mobilePlatform)
                } else {
                  // Create ViewModel factories
                  val viewModelFactories = rememberViewModelFactories(graph, platformBindings)

                  // Create navigation configuration
                  val navConfig = rememberNavigationConfig(
                    navController = navController,
                    mobilePlatform = mobilePlatform,
                    stations = filteredStations,
                    favoriteIds = favoriteIds,
                    stationsState = stationsState,
                    nearestSelection = nearestSelection,
                    searchRadiusMeters = searchRadiusMeters,
                    isMapReady = isMapReady,
                    appState = appState,
                    scope = scope,
                    stationsRepository = stationsRepository,
                    favoritesRepository = favoritesRepository,
                    graph = graph,
                    platformBindings = platformBindings,
                    onOpenOnboarding = appRootViewModel::onOnboardingOpenedFromSettings,
                    onShowChangelogManual = appRootViewModel::showChangelogHistory,
                  )

                  Box(modifier = Modifier.fillMaxSize()) {
                    BiziNavigationShell(
                      mobilePlatform = mobilePlatform,
                      navController = navController,
                      windowLayout = windowLayout,
                    ) { innerPadding ->
                      NavigationHost(
                        config = navConfig.copy(paddingValues = innerPadding),
                        factories = viewModelFactories,
                      )
                    }
                    OverlayManager(
                      mobilePlatform = mobilePlatform,
                      updateBanner = appRootUiState.topUpdateBanner,
                      showFeedbackNudge = appRootUiState.showFeedbackNudge,
                      showFeedbackDialog = showFeedbackDialog,
                      changelogSections = changelogPresentation?.sections ?: emptyList(),
                      highlightedVersion = changelogPresentation?.highlightedVersion,
                      showChangelog = !showStartupSplash && changelogPresentation != null,
                      onDismissAvailableUpdate = appRootViewModel::dismissAvailableUpdate,
                      onDismissDownloadedUpdate = appRootViewModel::dismissDownloadedUpdate,
                      onStartUpdate = appRootViewModel::onStartUpdateRequested,
                      onRestartToUpdate = appRootViewModel::onRestartToUpdateRequested,
                      onFeedbackSend = {
                        appRootViewModel.onFeedbackOpened()
                        showFeedbackDialog = true
                      },
                      onFeedbackDismiss = appRootViewModel::onFeedbackDismissed,
                      onFeedbackDialogDismiss = { showFeedbackDialog = false },
                      onOpenFeedbackForm = {
                        platformBindings.externalLinks.openFeedbackForm()
                        showFeedbackDialog = false
                      },
                      onChangelogDismiss = appRootViewModel::dismissChangelog,
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

@Composable
internal fun NearbyScreen(
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
) {
  val nearestWithBikesSelection = remember(stations, searchRadiusMeters) {
    selectNearbyStationWithBikes(stations, searchRadiusMeters)
  }
  val nearestWithSlotsSelection = remember(stations, searchRadiusMeters) {
    selectNearbyStationWithSlots(stations, searchRadiusMeters)
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier = Modifier.responsivePageWidth(),
    ) {
      // Header + quick-action cards — always visible, never scroll away
      Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        if (mobilePlatform == MobileUiPlatform.IOS) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Text(
                text = stringResource(Res.string.nearby),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = stringResource(Res.string.nearbyQuickActionsDescription),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }
            RefreshButtonWithCountdown(
              countdown = refreshCountdownSeconds,
              loading = loading,
              onRefresh = onRefresh,
            )
          }
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
          ) {
            Column(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = stringResource(Res.string.nearbyNearYou),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = LocalBiziColors.current.red,
              )
              Text(
                text = stringResource(Res.string.nearbyStationsSortedDescription),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }
            RefreshButtonWithCountdown(
              countdown = refreshCountdownSeconds,
              loading = loading,
              onRefresh = onRefresh,
            )
          }
        }
        Row(
          modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          QuickRouteActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(Res.string.nearbyNearestWithBikes),
            emptyTitle = stringResource(Res.string.nearbyNoBikesNearby),
            selection = nearestWithBikesSelection,
            icon = Icons.AutoMirrored.Filled.DirectionsBike,
            tint = LocalBiziColors.current.red,
            mobilePlatform = mobilePlatform,
            onRoute = onQuickRoute,
          )
          QuickRouteActionCard(
            modifier = Modifier.weight(1f),
            title = stringResource(Res.string.nearbyNearestWithSlots),
            emptyTitle = stringResource(Res.string.nearbyNoSlotsNearby),
            selection = nearestWithSlotsSelection,
            icon = Icons.Filled.LocalParking,
            tint = LocalBiziColors.current.blue,
            mobilePlatform = mobilePlatform,
            onRoute = onQuickRoute,
          )
        }
      }
      DataFreshnessBanner(
        freshness = dataFreshness,
        lastUpdatedEpoch = lastUpdatedEpoch,
        loading = loading,
        onRefresh = onRefresh,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 10.dp),
      )
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = if (loading) stringResource(Res.string.nearbyUpdatingStations) else stringResource(Res.string.nearbyStations),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = if (nearestSelection.usesFallback) {
                stringResource(Res.string.nearbyRadiusFallbackHint)
              } else {
                stringResource(Res.string.nearbyCardActionsHint)
              },
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            AnimatedVisibility(
              visible = errorMessage != null,
              enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
              exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
              label = "nearby-error",
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(errorMessage.orEmpty(), color = LocalBiziColors.current.red)
                OutlinedButton(onClick = onRetry) {
                  Icon(Icons.Filled.Sync, contentDescription = null)
                  Spacer(Modifier.width(8.dp))
                  Text(stringResource(Res.string.retry))
                }
              }
            }
          }
        }
        item {
          AnimatedVisibility(
            visible = !loading && stations.isEmpty(),
            enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
            label = "nearby-empty",
          ) {
            EmptyStatePlaceholder(
              title = stringResource(Res.string.mapNoStationsOnScreen),
              description = stringResource(Res.string.mapLocationFallbackDescription),
              primaryAction = stringResource(Res.string.loadStations),
              onPrimaryAction = onRetry,
            )
          }
        }
        items(stations.take(12), key = { it.id }) { station ->
          StationRow(
            mobilePlatform = mobilePlatform,
            station = station,
            isFavorite = station.id in favoriteIds,
            onClick = { onStationSelected(station) },
            onFavoriteToggle = { onFavoriteToggle(station) },
            onQuickRoute = { onQuickRoute(station) },
          )
        }
      }
    }

  }
}

@Composable
private fun RefreshButtonWithCountdown(
  countdown: Int,
  loading: Boolean,
  onRefresh: () -> Unit,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    IconButton(onClick = onRefresh, enabled = !loading) {
      Icon(Icons.Filled.Sync, contentDescription = stringResource(Res.string.refreshStations))
    }
    if (countdown > 0 && !loading) {
      val minutes = countdown / 60
      val seconds = countdown % 60
      Text(
        text = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s",
        style = MaterialTheme.typography.labelSmall,
        color = LocalBiziColors.current.muted,
      )
    }
  }
}

@Composable
internal fun FavoritesScreen(
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
  savedPlaceAlertsCityId: String,
  savedPlaceAlertRules: List<SavedPlaceAlertRule>,
  onUpsertSavedPlaceAlert: ((SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit)?,
  onRemoveSavedPlaceAlertForTarget: ((SavedPlaceAlertTarget) -> Unit)?,
) {
  var alertEditor by remember { mutableStateOf<Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>?>(null) }
  val upsertAlert = onUpsertSavedPlaceAlert
  val removeAlertForTarget = onRemoveSavedPlaceAlertForTarget
  if (upsertAlert != null && removeAlertForTarget != null) {
    alertEditor?.let { (target, rule) ->
      SavedPlaceAlertEditorSheet(
        target = target,
        existingRule = rule,
        onDismiss = { alertEditor = null },
        onSave = { cond ->
          upsertAlert(target, cond)
          alertEditor = null
        },
        onRemove = {
          removeAlertForTarget(target)
          alertEditor = null
        },
      )
    }
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = if (mobilePlatform == MobileUiPlatform.Desktop) Modifier.fillMaxWidth() else Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (mobilePlatform == MobileUiPlatform.IOS) stringResource(Res.string.favorites) else stringResource(Res.string.myStations),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.favoritesSubtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      item {
        StationSearchField(
          mobilePlatform = mobilePlatform,
          value = searchQuery,
          onValueChange = onSearchQueryChange,
          label = stringResource(Res.string.favoritesSearchStation),
        )
      }
      item {
        DataFreshnessBanner(
          freshness = dataFreshness,
          lastUpdatedEpoch = lastUpdatedEpoch,
          loading = stationsLoading,
          onRefresh = onRefreshStations,
        )
      }
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
        ) {
          Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(stringResource(Res.string.savedPlaceAlertsTitle), fontWeight = FontWeight.SemiBold)
            Text(
              stringResource(Res.string.savedPlaceAlertsProfileSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            Text(
              stringResource(Res.string.savedPlaceAlertsStationDetailHint),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
            OutlinedButton(
              modifier = Modifier.fillMaxWidth(),
              onClick = onOpenSavedPlaceAlerts,
            ) {
              Icon(Icons.Filled.Notifications, contentDescription = null)
              Spacer(Modifier.width(8.dp))
              Text(stringResource(Res.string.savedPlaceAlertsProfileAction))
            }
          }
        }
      }
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = stringResource(Res.string.homeAndWork),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = stringResource(Res.string.homeAndWorkDescription),
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
      }
      item {
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.home),
          station = homeStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignHomeStation,
          onClear = onClearHomeStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick = run {
            val s = homeStation
            if (s != null && upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.Home(s.id, savedPlaceAlertsCityId, s.name)
                alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
              }
            } else {
              null
            }
          },
          savedPlaceAlertActive = homeStation?.let { s ->
            findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.Home(s.id, savedPlaceAlertsCityId, s.name),
            ) != null
          } == true,
        )
      }
      item {
        SavedPlaceCard(
          mobilePlatform = mobilePlatform,
          title = stringResource(Res.string.work),
          station = workStation,
          assignmentCandidate = assignmentCandidate,
          onAssignCandidate = onAssignWorkStation,
          onClear = onClearWorkStation,
          onOpenStationDetails = onStationSelected,
          onQuickRoute = onQuickRoute,
          onSavedPlaceAlertClick = run {
            val s = workStation
            if (s != null && upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.Work(s.id, savedPlaceAlertsCityId, s.name)
                alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
              }
            } else {
              null
            }
          },
          savedPlaceAlertActive = workStation?.let { s ->
            findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.Work(s.id, savedPlaceAlertsCityId, s.name),
            ) != null
          } == true,
        )
      }
      item {
        AnimatedVisibility(
          visible = stations.isEmpty() && homeStation == null && workStation == null,
          enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
          exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
          label = "favorites-empty",
        ) {
          EmptyStatePlaceholder(
            title = stringResource(Res.string.favoritesEmptyTitle),
            description = stringResource(Res.string.favoritesEmptyDescription),
          )
        }
      }
      if (stations.isNotEmpty()) {
        items(stations.distinctBy { it.id }, key = { it.id }) { station ->
          DismissibleFavoriteStationRow(
            mobilePlatform = mobilePlatform,
            station = station,
            canAssignHome = homeStation == null,
            canAssignWork = workStation == null,
            onClick = { onStationSelected(station) },
            onAssignHome = { onAssignHomeStation(station) },
            onAssignWork = { onAssignWorkStation(station) },
            onQuickRoute = { onQuickRoute(station) },
            onRemoveFavorite = { onRemoveFavorite(station) },
            onSavedPlaceAlertClick = if (upsertAlert != null) {
              {
                val t = SavedPlaceAlertTarget.FavoriteStation(station.id, savedPlaceAlertsCityId, station.name)
                alertEditor = t to findSavedPlaceAlertRule(savedPlaceAlertRules, t)
              }
            } else {
              null
            },
            savedPlaceAlertActive = findSavedPlaceAlertRule(
              savedPlaceAlertRules,
              SavedPlaceAlertTarget.FavoriteStation(station.id, savedPlaceAlertsCityId, station.name),
            ) != null,
          )
        }
      }
    }
  }
}

@Composable
private fun CitySelectionScreen(
  onCitySelected: (City) -> Unit,
) {
  val colors = LocalBiziColors.current
  var searchQuery by remember { mutableStateOf("") }
  val sortedCities = remember { City.entries.sortedBy { it.displayName } }
  val normalizedQuery = remember(searchQuery) { searchQuery.trim().normalizedForSearch() }
  val filteredCities = remember(normalizedQuery, sortedCities) {
    if (normalizedQuery.isBlank()) {
      sortedCities
    } else {
      sortedCities.filter { city ->
        city.displayName.normalizedForSearch().contains(normalizedQuery)
      }
    }
  }
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(colors.background)
      .windowInsetsPadding(WindowInsets.statusBars)
      .padding(24.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = stringResource(Res.string.citySelectionTitle),
      style = MaterialTheme.typography.headlineMedium,
      fontWeight = FontWeight.Bold,
      color = colors.ink,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(Res.string.citySelectionSubtitle),
      style = MaterialTheme.typography.bodyMedium,
      color = colors.muted,
    )
    Spacer(modifier = Modifier.height(20.dp))
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      placeholder = { Text(stringResource(Res.string.citySelectionSearchPlaceholder)) },
      colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surface,
        unfocusedContainerColor = colors.surface,
      ),
    )
    Spacer(modifier = Modifier.height(12.dp))
    LazyColumn(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      if (filteredCities.isEmpty()) {
        item {
          Text(
            text = stringResource(Res.string.citySelectionSearchNoResults),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
            modifier = Modifier.padding(vertical = 8.dp),
          )
        }
      }
      items(filteredCities.size) { index ->
        val city = filteredCities[index]
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onCitySelected(city) },
          colors = CardDefaults.cardColors(containerColor = colors.surface),
          border = BorderStroke(1.dp, colors.panel),
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = city.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
              )
              if (city.supportsEbikes) {
                Text(
                  text = "Bicis eléctricas disponibles",
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.muted,
                )
              }
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
              contentDescription = null,
              tint = colors.red,
            )
          }
        }
      }
    }
    Spacer(modifier = Modifier.height(24.dp))
  }
}

private fun geoSuggestionSecondaryText(result: GeoResult): String? {
  val address = result.address.trim()
  if (address.isBlank()) return null
  if (address.equals(result.name.trim(), ignoreCase = true)) return null
  return address
}

@Composable
private fun AvailabilityCard(
  modifier: Modifier,
  label: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  mobilePlatform: MobileUiPlatform,
) {
  Card(
    modifier = modifier,
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, tint.copy(alpha = 0.14f)) else null,
    colors = CardDefaults.cardColors(
      containerColor = if (mobilePlatform == MobileUiPlatform.IOS) {
        LocalBiziColors.current.surface
      } else {
        tint.copy(alpha = 0.08f)
      },
    ),
  ) {
    Column(
      modifier = Modifier
        .padding(16.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.86f, stiffness = 500f)),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(label, color = tint)
      Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
    }
  }
}



@Composable
private fun DismissibleFavoriteStationRow(
  mobilePlatform: MobileUiPlatform,
  station: Station,
  canAssignHome: Boolean,
  canAssignWork: Boolean,
  onClick: () -> Unit,
  onAssignHome: () -> Unit,
  onAssignWork: () -> Unit,
  onQuickRoute: () -> Unit,
  onRemoveFavorite: () -> Unit,
  onSavedPlaceAlertClick: (() -> Unit)?,
  savedPlaceAlertActive: Boolean,
) {
  val dismissState = rememberSwipeToDismissBoxState()
  LaunchedEffect(dismissState.currentValue) {
    if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
      onRemoveFavorite()
      dismissState.snapTo(SwipeToDismissBoxValue.Settled)
    }
  }
  SwipeToDismissBox(
    state = dismissState,
    enableDismissFromStartToEnd = false,
    backgroundContent = {
      FavoriteDismissBackground(
        mobilePlatform = mobilePlatform,
        progress = dismissState.progress,
      )
    },
    content = {
      StationRow(
        mobilePlatform = mobilePlatform,
        station = station,
        isFavorite = true,
        onClick = onClick,
        onFavoriteToggle = {},
        onQuickRoute = onQuickRoute,
        savedPlaceAlertSlot = if (onSavedPlaceAlertClick != null) {
          {
            IconButton(
              onClick = onSavedPlaceAlertClick,
              modifier = Modifier.size(40.dp),
            ) {
              Icon(
                imageVector = if (savedPlaceAlertActive) Icons.Filled.Notifications else Icons.Outlined.Notifications,
                contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
                tint = if (savedPlaceAlertActive) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
              )
            }
          }
        } else {
          null
        },
        extraActions = {
          if (canAssignHome) {
            SavedPlaceQuickAction(
              label = stringResource(Res.string.home),
              tint = LocalBiziColors.current.green,
              onClick = onAssignHome,
            )
          }
          if (canAssignWork) {
            SavedPlaceQuickAction(
              label = stringResource(Res.string.work),
              tint = LocalBiziColors.current.blue,
              onClick = onAssignWork,
            )
          }
        },
        showFavoriteCta = false,
      )
    },
  )
}

@Composable
private fun FavoriteDismissBackground(
  mobilePlatform: MobileUiPlatform,
  progress: Float,
) {
  val clampedProgress = progress.coerceIn(0f, 1f)
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp))
      .background(LocalBiziColors.current.red.copy(alpha = 0.10f + (0.10f * clampedProgress)))
      .padding(horizontal = 20.dp, vertical = 12.dp),
    contentAlignment = Alignment.CenterEnd,
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.graphicsLayer {
        alpha = 0.55f + (0.45f * clampedProgress)
        scaleX = 0.92f + (0.08f * clampedProgress)
        scaleY = 0.92f + (0.08f * clampedProgress)
      },
    ) {
      Icon(
        Icons.Filled.Delete,
        contentDescription = null,
        tint = LocalBiziColors.current.red,
      )
      Text(
        text = if (mobilePlatform == MobileUiPlatform.IOS) stringResource(Res.string.removeFavorite) else stringResource(Res.string.deleteFavorite),
        color = LocalBiziColors.current.red,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun SavedPlaceCard(
  mobilePlatform: MobileUiPlatform,
  title: String,
  station: Station?,
  assignmentCandidate: Station?,
  onAssignCandidate: (Station) -> Unit,
  onClear: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onSavedPlaceAlertClick: (() -> Unit)? = null,
  savedPlaceAlertActive: Boolean = false,
) {
  val assignableCandidate = assignmentCandidate?.takeIf { it.id != station?.id }
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 500f)),
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 22.dp else 24.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, LocalBiziColors.current.panel) else null,
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      if (station != null) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            text = station.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = station.address,
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.bikes),
            value = station.bikesAvailable.toString(),
            tint = LocalBiziColors.current.red,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.slots),
            value = station.slotsFree.toString(),
            tint = LocalBiziColors.current.blue,
          )
          StationMetricPill(
            modifier = Modifier.weight(1f),
            label = stringResource(Res.string.distance),
            value = formatDistance(station.distanceMeters),
            tint = LocalBiziColors.current.green,
          )
        }
      } else {
        Text(
          text = stringResource(Res.string.savedPlaceNotSet, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        if (station != null) {
          RoutePill(
            label = stringResource(Res.string.route),
            onClick = { onQuickRoute(station) },
          )
          OutlineActionPill(
            label = stringResource(Res.string.details),
            tint = LocalBiziColors.current.red,
            borderTint = LocalBiziColors.current.red.copy(alpha = 0.16f),
            onClick = { onOpenStationDetails(station) },
          )
        }
        if (assignableCandidate != null) {
          OutlineActionPill(
            label = stringResource(Res.string.assignSearchResult),
            tint = LocalBiziColors.current.blue,
            borderTint = LocalBiziColors.current.blue.copy(alpha = 0.16f),
            onClick = { onAssignCandidate(assignableCandidate) },
          )
        }
        if (station != null) {
          OutlineActionPill(
            label = stringResource(Res.string.remove),
            tint = LocalBiziColors.current.muted,
            borderTint = LocalBiziColors.current.panel,
            onClick = onClear,
          )
        }
        if (station != null && onSavedPlaceAlertClick != null) {
          IconButton(
            onClick = onSavedPlaceAlertClick,
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = if (savedPlaceAlertActive) Icons.Filled.Notifications else Icons.Outlined.Notifications,
              contentDescription = stringResource(Res.string.savedPlaceAlertsBell),
              tint = if (savedPlaceAlertActive) LocalBiziColors.current.blue else LocalBiziColors.current.muted,
            )
          }
        }
      }
      if (assignableCandidate != null) {
        Text(
          text = stringResource(Res.string.currentSearchAssignmentHint, assignableCandidate.name, title),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else if (station == null) {
        Text(
          text = stringResource(Res.string.useSearchToAssignStation),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
    }
  }
}

@Composable
private fun SavedPlaceQuickAction(
  label: String,
  tint: Color,
  onClick: () -> Unit,
) {
  OutlineActionPill(
    label = label,
    tint = tint,
    borderTint = tint.copy(alpha = 0.16f),
    onClick = onClick,
  )
}

@Composable
private fun MobilePageHeader(
  title: String,
  subtitle: String,
  onOpenAssistant: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Row(
      modifier = Modifier.weight(1f),
      horizontalArrangement = Arrangement.Start,
      verticalAlignment = Alignment.Top,
    ) {
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = LocalBiziColors.current.muted,
        )
      }
    }
    Spacer(Modifier.width(12.dp))
    Surface(
      shape = RoundedCornerShape(18.dp),
      color = LocalBiziColors.current.red.copy(alpha = 0.10f),
      border = BorderStroke(1.dp, LocalBiziColors.current.red.copy(alpha = 0.12f)),
      modifier = Modifier.clickable(onClick = onOpenAssistant),
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          Icons.Filled.KeyboardVoice,
          contentDescription = null,
          tint = LocalBiziColors.current.red,
        )
        Text(stringResource(Res.string.shortcuts), color = LocalBiziColors.current.red, fontWeight = FontWeight.SemiBold)
      }
    }
  }
}

@Composable
internal fun StationSearchField(
  mobilePlatform: MobileUiPlatform,
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
) {
  val c = LocalBiziColors.current
  OutlinedTextField(
    modifier = Modifier.fillMaxWidth(),
    value = value,
    onValueChange = onValueChange,
    singleLine = true,
    shape = RoundedCornerShape(20.dp),
    leadingIcon = {
      Icon(Icons.Filled.Search, contentDescription = null, tint = c.muted)
    },
    trailingIcon = if (value.isNotEmpty()) {
      {
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = stringResource(Res.string.clearSearch),
          tint = c.muted,
          modifier = Modifier.clickable { onValueChange("") },
        )
      }
    } else {
      null
    },
    label = if (mobilePlatform == MobileUiPlatform.Android) {
      { Text(label) }
    } else {
      null
    },
    placeholder = { Text(label, color = c.muted) },
    colors = OutlinedTextFieldDefaults.colors(
      focusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.fieldSurfaceIos else c.fieldSurfaceAndroid,
      unfocusedContainerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.fieldSurfaceIos else c.fieldSurfaceAndroid,
      focusedBorderColor = c.red.copy(alpha = if (mobilePlatform == MobileUiPlatform.IOS) 0.18f else 0.30f),
      unfocusedBorderColor = if (mobilePlatform == MobileUiPlatform.IOS) c.panel else c.muted.copy(alpha = 0.18f),
      focusedTextColor = c.ink,
      unfocusedTextColor = c.ink,
      focusedLabelColor = c.ink,
      unfocusedLabelColor = c.muted,
      focusedPlaceholderColor = c.muted,
      unfocusedPlaceholderColor = c.muted,
      focusedLeadingIconColor = c.muted,
      unfocusedLeadingIconColor = c.muted,
      focusedTrailingIconColor = c.muted,
      unfocusedTrailingIconColor = c.muted,
    ),
  )
}

@Composable
private fun QuickRouteActionCard(
  modifier: Modifier,
  title: String,
  emptyTitle: String,
  selection: com.gcaguilar.biciradar.core.NearbyStationSelection,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color,
  mobilePlatform: MobileUiPlatform,
  onRoute: (Station) -> Unit,
) {
  val station = selection.highlightedStation
  Card(
    modifier = modifier
      .clickable(enabled = station != null) {
        station?.let(onRoute)
      },
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, LocalBiziColors.current.panel) else null,
    colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
  ) {
    Column(
      modifier = Modifier
        .padding(14.dp)
        .animateContentSize(animationSpec = spring(dampingRatio = 0.88f, stiffness = 520f)),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
      Text(title, style = MaterialTheme.typography.labelSmall, color = LocalBiziColors.current.muted)
      if (station == null) {
        Text(
          emptyTitle,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = tint,
        )
        Text(
          stringResource(Res.string.refreshStationsToOpenRoute),
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      } else {
        Text(
          station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = tint,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          if (selection.usesFallback) {
            stringResource(
              Res.string.quickRouteFallbackSummary,
              formatDistance(selection.radiusMeters),
              formatDistance(station.distanceMeters),
            )
          } else {
            stringResource(
              Res.string.quickRouteDistanceSummary,
              formatDistance(station.distanceMeters),
              station.bikesAvailable,
              station.slotsFree,
            )
          },
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
        Text(
          stringResource(Res.string.openRoute),
          style = MaterialTheme.typography.labelMedium,
          color = tint,
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
internal fun RadiusSelectionButton(
  modifier: Modifier,
  selected: Boolean,
  label: String,
  onClick: () -> Unit,
) {
  val containerColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red.copy(alpha = 0.10f) else LocalBiziColors.current.surface,
    animationSpec = tween(180),
    label = "radius-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red.copy(alpha = 0.25f) else LocalBiziColors.current.panel,
    animationSpec = tween(180),
    label = "radius-border",
  )
  val textColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red else LocalBiziColors.current.ink,
    animationSpec = tween(180),
    label = "radius-text",
  )
  val selectionScale by animateFloatAsState(
    targetValue = if (selected) 1f else 0.98f,
    animationSpec = spring(dampingRatio = 0.82f, stiffness = 700f),
    label = "radius-scale",
  )
  Surface(
    modifier = modifier
      .graphicsLayer {
        scaleX = selectionScale
        scaleY = selectionScale
      }
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = label,
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 12.dp)
        .animateContentSize(animationSpec = tween(180)),
      color = textColor,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}

private fun filterStations(
  stations: List<Station>,
  searchQuery: String,
): List<Station> = filterStationsByQuery(stations, searchQuery)
