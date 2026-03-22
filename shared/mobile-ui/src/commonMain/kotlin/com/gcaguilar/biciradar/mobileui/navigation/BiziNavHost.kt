package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.mobileui.BiziMobileAppContent
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory

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
  graph: SharedGraph,
  stationsRepository: StationsRepository,
  initialAssistantAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  paddingValues: PaddingValues,
  modifier: Modifier = Modifier,
) {
  NavHost(
    navController = navController,
    startDestination = Screen.Nearby,
    modifier = modifier.padding(paddingValues),
  ) {
    composable<Screen.Nearby>(
      deepLinks = listOf(navDeepLink<Screen.Nearby>(basePath = "${DeepLinks.BASE_URI}nearby")),
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
      deepLinks = listOf(navDeepLink<Screen.Map>(basePath = "${DeepLinks.BASE_URI}map")),
    ) {
      BiziMobileAppContent.MapScreenContent(
        mobilePlatform = mobilePlatform,
        stations = stations,
        favoriteIds = favoriteIds,
        loading = loading,
        errorMessage = errorMessage,
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
      deepLinks = listOf(navDeepLink<Screen.Favorites>(basePath = "${DeepLinks.BASE_URI}favorites")),
    ) {
      val favoritesViewModel = viewModel(key = "favorites") { favoritesViewModelFactory.create() }
      BiziMobileAppContent.FavoritesScreenContent(
        viewModel = favoritesViewModel,
        mobilePlatform = mobilePlatform,
        onOpenAssistant = onOpenAssistant,
        onStationSelected = remember(navController) { { station ->
          navController.navigate(Screen.StationDetail(station.id))
        } },
        paddingValues = PaddingValues(),
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
        onBack = remember(navController) { { navController.popBackStack() } },
        stationsRepository = stationsRepository,
      )
    }
  }
}
