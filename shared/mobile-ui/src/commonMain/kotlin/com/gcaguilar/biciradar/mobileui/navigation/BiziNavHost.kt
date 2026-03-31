package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.mobileui.BiziMobileAppContent
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.SavedPlaceAlertsListScreen
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory
import kotlinx.coroutines.launch

@Composable
internal fun BiziNavHost(
  navController: NavHostController,
  mobilePlatform: MobileUiPlatform,
  tripViewModelFactory: TripViewModelFactory,
  nearbyViewModelFactory: NearbyViewModelFactory,
  favoritesViewModelFactory: FavoritesViewModelFactory,
  profileViewModelFactory: ProfileViewModelFactory,
  // Shared state (still needed by Map / StationDetail / Trip)
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  stationsFreshness: DataFreshness,
  stationsLastUpdatedEpoch: Long?,
  onRefreshStations: () -> Unit,
  nearestSelection: NearbyStationSelection,
  userLocation: GeoPoint?,
  searchQuery: String,
  searchRadiusMeters: Int,
  isMapReady: Boolean,
  onSearchQueryChange: (String) -> Unit,
  // Map interactions (still needed by MapScreen)
  onRetry: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  // Assistant / misc
  onOpenAssistant: () -> Unit,
  localNotifier: LocalNotifier,
  routeLauncher: RouteLauncher,
  platformBindings: PlatformBindings,
  graph: SharedGraph,
  stationsRepository: StationsRepository,
  initialAssistantAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  onShowChangelogManual: () -> Unit,
  paddingValues: PaddingValues,
  modifier: Modifier = Modifier,
) {
  NavHost(
    navController = navController,
    startDestination = Screen.Nearby,
    modifier = modifier.padding(paddingValues),
  ) {
    composable<Screen.Nearby>(
      deepLinks = listOf(
        navDeepLink<Screen.Nearby>(basePath = "${DeepLinks.BASE_URI}nearby"),
        navDeepLink<Screen.Nearby>(basePath = DeepLinks.HOME_URI),
      ),
    ) {
      val nearbyViewModel = viewModel(key = "nearby") { nearbyViewModelFactory.create() }
      DisposableEffect(nearbyViewModel) {
        nearbyViewModel.setActive(true)
        onDispose { nearbyViewModel.setActive(false) }
      }
      BiziMobileAppContent.NearbyScreenContent(
        viewModel = nearbyViewModel,
        mobilePlatform = mobilePlatform,
        onStationSelected = remember(navController) { { station ->
          navController.navigate(Screen.StationDetail(station.id))
        } },
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.Map>(
      deepLinks = listOf(navDeepLink<Screen.Map>(basePath = DeepLinks.MAP_URI)),
    ) {
      BiziMobileAppContent.MapScreenContent(
        mobilePlatform = mobilePlatform,
        stations = stations,
        favoriteIds = favoriteIds,
        loading = loading,
        errorMessage = errorMessage,
        dataFreshness = stationsFreshness,
        lastUpdatedEpoch = stationsLastUpdatedEpoch,
        onRefreshStations = onRefreshStations,
        nearestSelection = nearestSelection,
        searchQuery = searchQuery,
        searchRadiusMeters = searchRadiusMeters,
        userLocation = userLocation,
        isMapReady = isMapReady,
        onSearchQueryChange = onSearchQueryChange,
        onStationSelected = remember(navController) { { station ->
          navController.navigate(Screen.StationDetail(station.id))
        } },
        onRetry = onRetry,
        onFavoriteToggle = onFavoriteToggle,
        onQuickRoute = onQuickRoute,
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.Favorites>(
      deepLinks = listOf(navDeepLink<Screen.Favorites>(basePath = DeepLinks.FAVORITES_URI)),
    ) {
      val favoritesViewModel = viewModel(key = "favorites") { favoritesViewModelFactory.create() }
      BiziMobileAppContent.FavoritesScreenContent(
        viewModel = favoritesViewModel,
        mobilePlatform = mobilePlatform,
        onOpenAssistant = onOpenAssistant,
        onOpenSavedPlaceAlerts = remember(navController) {
          { navController.navigate(Screen.SavedPlaceAlerts) { launchSingleTop = true } }
        },
        onStationSelected = remember(navController) { { station ->
          navController.navigate(Screen.StationDetail(station.id))
        } },
        dataFreshness = stationsFreshness,
        lastUpdatedEpoch = stationsLastUpdatedEpoch,
        stationsLoading = loading,
        onRefreshStations = onRefreshStations,
        paddingValues = PaddingValues(),
        graph = graph,
      )
    }

    composable<Screen.Trip>(
      deepLinks = listOf(navDeepLink<Screen.Trip>(basePath = "${DeepLinks.BASE_URI}trip")),
    ) { backStackEntry ->
      val route = backStackEntry.toRoute<Screen.Trip>()
      val viewModel = viewModel(key = "trip") { tripViewModelFactory.create() }
      var prefilledApplied by rememberSaveable { mutableStateOf(false) }
      if (route.prefilledQuery != null && !prefilledApplied) {
        LaunchedEffect(Unit) {
          viewModel.onQueryChange(route.prefilledQuery)
          prefilledApplied = true
        }
      }
      BiziMobileAppContent.TripScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        localNotifier = localNotifier,
        routeLauncher = routeLauncher,
        userLocation = userLocation,
        stations = stations,
        isMapReady = isMapReady,
        dataFreshness = stationsFreshness,
        lastUpdatedEpoch = stationsLastUpdatedEpoch,
        stationsLoading = loading,
        onRefreshStations = onRefreshStations,
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.Profile>(
      deepLinks = listOf(navDeepLink<Screen.Profile>(basePath = "${DeepLinks.BASE_URI}profile")),
    ) {
      val profileViewModel = viewModel(key = "profile") { profileViewModelFactory.create() }
      BiziMobileAppContent.ProfileScreenContent(
        viewModel = profileViewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        onOpenShortcuts = remember(navController) { { navController.navigate(Screen.Shortcuts) { launchSingleTop = true } } },
        onOpenSavedPlaceAlerts = remember(navController) {
          { navController.navigate(Screen.SavedPlaceAlerts) { launchSingleTop = true } }
        },
        graph = graph,
        platformBindings = platformBindings,
        favoriteIds = favoriteIds,
        onShowChangelogManual = onShowChangelogManual,
      )
    }

    composable<Screen.SavedPlaceAlerts>(
      deepLinks = listOf(navDeepLink<Screen.SavedPlaceAlerts>(basePath = DeepLinks.SAVED_PLACE_ALERTS_URI)),
    ) {
      val rules by graph.savedPlaceAlertsRepository.rules.collectAsState()
      val scope = rememberCoroutineScope()
      SavedPlaceAlertsListScreen(
        mobilePlatform = mobilePlatform,
        rules = rules,
        paddingValues = PaddingValues(),
        onBack = remember(navController) { { navController.popBackStack() } },
        onSetEnabled = { id, enabled ->
          scope.launch { graph.savedPlaceAlertsRepository.setRuleEnabled(id, enabled) }
        },
        onUpsert = { target, condition ->
          scope.launch { graph.savedPlaceAlertsRepository.upsertRule(target, condition) }
        },
        onRemoveRule = { id ->
          scope.launch { graph.savedPlaceAlertsRepository.removeRule(id) }
        },
      )
    }

    composable<Screen.Shortcuts>(
      deepLinks = listOf(navDeepLink<Screen.Shortcuts>(basePath = "${DeepLinks.BASE_URI}shortcuts")),
    ) {
      BiziMobileAppContent.ShortcutsScreenContent(
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        searchRadiusMeters = searchRadiusMeters,
        graph = graph,
        stationsRepository = stationsRepository,
        favoriteIds = favoriteIds,
        initialAction = initialAssistantAction,
        onInitialActionConsumed = onInitialActionConsumed,
        onBack = remember(navController) {
          {
            if (!navController.popBackStack()) {
              navController.navigate(Screen.Profile) { launchSingleTop = true }
            }
          }
        },
      )
    }

    composable<Screen.StationDetail>(
      deepLinks = listOf(navDeepLink<Screen.StationDetail>(basePath = "${DeepLinks.BASE_URI}station")),
    ) { backStackEntry ->
      val route = backStackEntry.toRoute<Screen.StationDetail>()
      val station = stations.firstOrNull { it.id == route.stationId }
      if (station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
        return@composable
      }
      BiziMobileAppContent.StationDetailScreenContent(
        mobilePlatform = mobilePlatform,
        station = station,
        graph = graph,
        favoriteIds = favoriteIds,
        userLocation = userLocation,
        isMapReady = isMapReady,
        dataFreshness = stationsFreshness,
        lastUpdatedEpoch = stationsLastUpdatedEpoch,
        stationsLoading = loading,
        onRefreshStations = onRefreshStations,
        onBack = remember(navController) { { navController.popBackStack() } },
        stationsRepository = stationsRepository,
      )
    }
  }
}
