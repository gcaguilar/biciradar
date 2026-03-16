package com.gcaguilar.bizizaragoza.mobileui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.gcaguilar.bizizaragoza.core.AssistantAction
import com.gcaguilar.bizizaragoza.core.GeoPoint
import com.gcaguilar.bizizaragoza.core.MapSupportStatus
import com.gcaguilar.bizizaragoza.core.NearbyStationSelection
import com.gcaguilar.bizizaragoza.core.RouteLauncher
import com.gcaguilar.bizizaragoza.core.SharedGraph
import com.gcaguilar.bizizaragoza.core.Station
import com.gcaguilar.bizizaragoza.core.StationsRepository
import com.gcaguilar.bizizaragoza.core.LocalNotifier
import com.gcaguilar.bizizaragoza.mobileui.BiziMobileAppContent
import com.gcaguilar.bizizaragoza.mobileui.MobileUiPlatform
import com.gcaguilar.bizizaragoza.mobileui.viewmodel.FavoritesViewModelFactory
import com.gcaguilar.bizizaragoza.mobileui.viewmodel.NearbyViewModelFactory
import com.gcaguilar.bizizaragoza.mobileui.viewmodel.ProfileViewModelFactory
import com.gcaguilar.bizizaragoza.mobileui.viewmodel.TripViewModelFactory

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
  mapSupportStatus: MapSupportStatus,
  graph: SharedGraph,
  stationsRepository: StationsRepository,
  initialAssistantAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  paddingValues: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val nearbyViewModel = remember(nearbyViewModelFactory) { nearbyViewModelFactory.create() }
  val favoritesViewModel = remember(favoritesViewModelFactory) { favoritesViewModelFactory.create() }
  val profileViewModel = remember(profileViewModelFactory) { profileViewModelFactory.create() }

  NavHost(
    navController = navController,
    startDestination = Screen.Nearby,
    modifier = modifier.padding(paddingValues),
  ) {
    composable<Screen.Nearby>(
      deepLinks = listOf(navDeepLink { uriPattern = "${DeepLinks.BASE_URI}nearby" }),
    ) {
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
      deepLinks = listOf(navDeepLink { uriPattern = "${DeepLinks.BASE_URI}map" }),
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
      deepLinks = listOf(navDeepLink { uriPattern = "${DeepLinks.BASE_URI}favorites" }),
    ) {
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
      deepLinks = listOf(navDeepLink { uriPattern = "${DeepLinks.BASE_URI}trip" }),
    ) { backStackEntry ->
      val route = backStackEntry.toRoute<Screen.Trip>()
      val viewModel = remember(tripViewModelFactory) { tripViewModelFactory.create() }
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
      deepLinks = listOf(navDeepLink { uriPattern = "${DeepLinks.BASE_URI}profile" }),
    ) {
      BiziMobileAppContent.ProfileScreenContent(
        viewModel = profileViewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        mapSupportStatus = mapSupportStatus,
        userLocation = userLocation,
        stations = stations,
        graph = graph,
        stationsRepository = stationsRepository,
        favoriteIds = favoriteIds,
        initialAction = initialAssistantAction,
        onInitialActionConsumed = onInitialActionConsumed,
      )
    }

    composable<Screen.StationDetail>(
      deepLinks = listOf(navDeepLink { uriPattern = "${DeepLinks.BASE_URI}station/{stationId}" }),
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
