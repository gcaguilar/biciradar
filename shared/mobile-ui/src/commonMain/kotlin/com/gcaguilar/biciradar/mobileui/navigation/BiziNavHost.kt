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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.mobileui.BiziMobileAppContent
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.TripMapPickerMode
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory

@Composable
internal fun BiziNavHost(
  navController: NavHostController,
  mobilePlatform: MobileUiPlatform,
  tripViewModelFactory: TripViewModelFactory,
  nearbyViewModelFactory: NearbyViewModelFactory,
  mapEnvironmentalViewModelFactory: MapEnvironmentalViewModelFactory,
  shortcutsViewModelFactory: ShortcutsViewModelFactory,
  favoritesViewModelFactory: FavoritesViewModelFactory,
  profileViewModelFactory: ProfileViewModelFactory,
  savedPlaceAlertsViewModelFactory: SavedPlaceAlertsViewModelFactory,
  stationDetailViewModelFactory: StationDetailViewModelFactory,
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
  initialAssistantAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  onOpenOnboarding: () -> Unit,
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
      val mapEnvironmentalViewModel = viewModel(key = "map-environment") { mapEnvironmentalViewModelFactory.create() }
      BiziMobileAppContent.MapScreenContent(
        viewModel = mapEnvironmentalViewModel,
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
        onOpenSearch = remember(navController) {
          { navController.navigate(Screen.FavoritesSearch) { launchSingleTop = true } }
        },
        onStationSelected = remember(navController) { { station ->
          navController.navigate(Screen.StationDetail(station.id))
        } },
        dataFreshness = stationsFreshness,
        lastUpdatedEpoch = stationsLastUpdatedEpoch,
        stationsLoading = loading,
        onRefreshStations = onRefreshStations,
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.FavoritesSearch> { backStackEntry ->
      val favoritesStoreOwner = remember(backStackEntry, navController) {
        val favoritesRoute = checkNotNull(Screen.Favorites::class.qualifiedName)
        runCatching { navController.getBackStackEntry(favoritesRoute) }.getOrDefault(backStackEntry)
      }
      val favoritesViewModel = viewModel(
        viewModelStoreOwner = favoritesStoreOwner,
        key = "favorites",
      ) { favoritesViewModelFactory.create() }
      BiziMobileAppContent.FavoritesSearchScreenContent(
        viewModel = favoritesViewModel,
        mobilePlatform = mobilePlatform,
        onBack = remember(navController) { { navController.popBackStack() } },
        onStationSelected = remember(navController) { { station ->
          navController.navigate(Screen.StationDetail(station.id))
        } },
      )
    }

    composable<Screen.Trip>(
      deepLinks = listOf(navDeepLink<Screen.Trip>(basePath = "${DeepLinks.BASE_URI}trip")),
    ) { backStackEntry ->
      val route = backStackEntry.toRoute<Screen.Trip>()
      val viewModel = viewModel(key = "trip") { tripViewModelFactory.create() }
      var lastAppliedPrefilledQuery by rememberSaveable { mutableStateOf<String?>(null) }
      if (route.prefilledQuery != null && route.prefilledQuery != lastAppliedPrefilledQuery) {
        LaunchedEffect(route.prefilledQuery) {
          viewModel.onQueryChange(route.prefilledQuery)
          lastAppliedPrefilledQuery = route.prefilledQuery
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
        onOpenDestinationPicker = remember(navController) {
          {
            navController.navigate(Screen.TripMapPicker(TripMapPickerMode.Destination))
          }
        },
        onOpenStationPicker = remember(navController) {
          {
            navController.navigate(Screen.TripMapPicker(TripMapPickerMode.Station))
          }
        },
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.TripMapPicker> { backStackEntry ->
      val route = backStackEntry.toRoute<Screen.TripMapPicker>()
      val tripStoreOwner = remember(backStackEntry, navController) {
        navController.previousBackStackEntry ?: backStackEntry
      }
      val viewModel = viewModel(
        viewModelStoreOwner = tripStoreOwner,
        key = "trip",
      ) { tripViewModelFactory.create() }
      BiziMobileAppContent.TripMapPickerScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        pickerMode = route.mode,
        userLocation = userLocation,
        stations = stations,
        isMapReady = isMapReady,
        paddingValues = PaddingValues(),
        onBack = remember(navController) { { navController.popBackStack() } },
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
        onOpenOnboarding = onOpenOnboarding,
        platformBindings = platformBindings,
        onShowChangelogManual = onShowChangelogManual,
      )
    }

    composable<Screen.SavedPlaceAlerts>(
      deepLinks = listOf(navDeepLink<Screen.SavedPlaceAlerts>(basePath = DeepLinks.SAVED_PLACE_ALERTS_URI)),
    ) {
      val viewModel = viewModel(key = "saved-place-alerts") { savedPlaceAlertsViewModelFactory.create() }
      BiziMobileAppContent.SavedPlaceAlertsScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        onBack = remember(navController) { { navController.popBackStack() } },
      )
    }

    composable<Screen.Shortcuts>(
      deepLinks = listOf(navDeepLink<Screen.Shortcuts>(basePath = "${DeepLinks.BASE_URI}shortcuts")),
    ) {
      val shortcutsViewModel = viewModel(key = "shortcuts") { shortcutsViewModelFactory.create() }
      BiziMobileAppContent.ShortcutsScreenContent(
        viewModel = shortcutsViewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
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
      val viewModel = viewModel(key = "station-detail-${route.stationId}") {
        stationDetailViewModelFactory.create(route.stationId)
      }
      val station = stations.firstOrNull { it.id == route.stationId }
      if (station == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
        return@composable
      }
      BiziMobileAppContent.StationDetailScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        station = station,
        userLocation = userLocation,
        isMapReady = isMapReady,
        dataFreshness = stationsFreshness,
        lastUpdatedEpoch = stationsLastUpdatedEpoch,
        stationsLoading = loading,
        onRefreshStations = onRefreshStations,
        onBack = remember(navController) { { navController.popBackStack() } },
      )
    }
  }
}
