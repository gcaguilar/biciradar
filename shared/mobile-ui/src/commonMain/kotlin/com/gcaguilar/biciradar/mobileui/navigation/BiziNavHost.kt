package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.mobileui.BiziMobileAppContent
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.TripMapPickerMode
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.FlowPreview

@FlowPreview
@Composable
internal fun BiziNavHost(
  navController: NavHostController,
  mobilePlatform: MobileUiPlatform,
  canSelectGoogleMapsInIos: Boolean,
  isMapReady: Boolean,
  onOpenAssistant: () -> Unit,
  platformBindings: PlatformBindings,
  initialAssistantAction: AssistantAction?,
  onInitialActionConsumed: () -> Unit,
  initialMapSearchQuery: String?,
  onInitialMapSearchQueryConsumed: () -> Unit,
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
      deepLinks =
        listOf(
          navDeepLink<Screen.Nearby>(basePath = "${DeepLinks.BASE_URI}nearby"),
          navDeepLink<Screen.Nearby>(basePath = DeepLinks.HOME_URI),
        ),
    ) {
      val nearbyViewModel = metroViewModel<NearbyViewModel>(key = "nearby")
      DisposableEffect(nearbyViewModel) {
        nearbyViewModel.setActive(true)
        onDispose { nearbyViewModel.setActive(false) }
      }
      BiziMobileAppContent.NearbyScreenContent(
        viewModel = nearbyViewModel,
        mobilePlatform = mobilePlatform,
        onStationSelected =
          remember(navController) {
            { station ->
              navController.navigate(Screen.StationDetail(station.id))
            }
          },
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.Map>(
      deepLinks = listOf(navDeepLink<Screen.Map>(basePath = DeepLinks.MAP_URI)),
    ) {
      val mapEnvironmentalViewModel = metroViewModel<MapEnvironmentalViewModel>(key = "map-environment")
      LaunchedEffect(initialMapSearchQuery) {
        val query = initialMapSearchQuery ?: return@LaunchedEffect
        mapEnvironmentalViewModel.onSearchQueryChange(query)
        onInitialMapSearchQueryConsumed()
      }
      BiziMobileAppContent.MapScreenContent(
        viewModel = mapEnvironmentalViewModel,
        mobilePlatform = mobilePlatform,
        isMapReady = isMapReady,
        onStationSelected =
          remember(navController) {
            { station ->
              navController.navigate(Screen.StationDetail(station.id))
            }
          },
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.Favorites>(
      deepLinks = listOf(navDeepLink<Screen.Favorites>(basePath = DeepLinks.FAVORITES_URI)),
    ) {
      val favoritesViewModel = metroViewModel<FavoritesViewModel>(key = "favorites")
      BiziMobileAppContent.FavoritesScreenContent(
        viewModel = favoritesViewModel,
        mobilePlatform = mobilePlatform,
        onOpenAssistant = onOpenAssistant,
        onOpenSavedPlaceAlerts =
          remember(navController) {
            { navController.navigate(Screen.SavedPlaceAlerts) { launchSingleTop = true } }
          },
        onOpenSearch =
          remember(navController) {
            { navController.navigate(Screen.FavoritesSearch) { launchSingleTop = true } }
          },
        onStationSelected =
          remember(navController) {
            { station ->
              navController.navigate(Screen.StationDetail(station.id))
            }
          },
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.FavoritesSearch> { backStackEntry ->
      val favoritesStoreOwner =
        remember(backStackEntry, navController) {
          val favoritesRoute = checkNotNull(Screen.Favorites::class.qualifiedName)
          runCatching { navController.getBackStackEntry(favoritesRoute) }.getOrDefault(backStackEntry)
        }
      val metroFactory = LocalMetroViewModelFactory.current
      val favoritesViewModel =
        viewModel<FavoritesViewModel>(
          viewModelStoreOwner = favoritesStoreOwner,
          key = "favorites",
          factory = metroFactory,
        )
      BiziMobileAppContent.FavoritesSearchScreenContent(
        viewModel = favoritesViewModel,
        mobilePlatform = mobilePlatform,
        onBack = remember(navController) { { navController.popBackStack() } },
        onStationSelected =
          remember(navController) {
            { station ->
              navController.navigate(Screen.StationDetail(station.id))
            }
          },
      )
    }

    composable<Screen.Trip>(
      deepLinks = listOf(navDeepLink<Screen.Trip>(basePath = "${DeepLinks.BASE_URI}trip")),
    ) { backStackEntry ->
      val route = backStackEntry.decodeTripRoute(platformBindings.logger)
      val viewModel = metroViewModel<TripViewModel>(key = "trip")
      var lastAppliedPrefilledQuery by remember { mutableStateOf<String?>(null) }
      if (route.prefilledQuery != null && route.prefilledQuery != lastAppliedPrefilledQuery) {
        LaunchedEffect(route.prefilledQuery) {
          viewModel.onQueryChange(route.prefilledQuery)
          lastAppliedPrefilledQuery = route.prefilledQuery
        }
      }
      BiziMobileAppContent.TripScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        onOpenDestinationPicker =
          remember(navController) {
            {
              navController.navigate(Screen.TripDestinationSearch)
            }
          },
        onOpenStationPicker =
          remember(navController) {
            {
              navController.navigate(Screen.TripMapPicker(TripMapPickerMode.Station.name))
            }
          },
        paddingValues = PaddingValues(),
      )
    }

    composable<Screen.TripDestinationSearch> { backStackEntry ->
      val tripStoreOwner =
        remember(backStackEntry, navController) {
          navController.previousBackStackEntry ?: backStackEntry
        }
      val metroFactory = LocalMetroViewModelFactory.current
      val viewModel =
        viewModel<TripViewModel>(
          viewModelStoreOwner = tripStoreOwner,
          key = "trip",
          factory = metroFactory,
        )
      BiziMobileAppContent.TripDestinationSearchScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        onBack = remember(navController) { { navController.popBackStack() } },
      )
    }

    composable<Screen.TripMapPicker> { backStackEntry ->
      val tripStoreOwner =
        remember(backStackEntry, navController) {
          navController.previousBackStackEntry ?: backStackEntry
        }
      val metroFactory = LocalMetroViewModelFactory.current
      val viewModel =
        viewModel<TripViewModel>(
          viewModelStoreOwner = tripStoreOwner,
          key = "trip",
          factory = metroFactory,
        )
      BiziMobileAppContent.TripMapPickerScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        pickerMode = backStackEntry.decodeTripMapPickerMode(platformBindings.logger),
        isMapReady = isMapReady,
        paddingValues = PaddingValues(),
        onBack = remember(navController) { { navController.popBackStack() } },
      )
    }

    composable<Screen.Profile>(
      deepLinks = listOf(navDeepLink<Screen.Profile>(basePath = "${DeepLinks.BASE_URI}profile")),
    ) {
      val profileViewModel =
        assistedMetroViewModel<ProfileViewModel, ProfileViewModel.Factory>(key = "profile") {
          create(canSelectGoogleMapsInIos)
        }
      BiziMobileAppContent.ProfileScreenContent(
        viewModel = profileViewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        onOpenOnboarding = onOpenOnboarding,
        onOpenShortcuts =
          remember(navController) {
            { navController.navigate(Screen.Shortcuts) { launchSingleTop = true } }
          },
        platformBindings = platformBindings,
        onShowChangelogManual = onShowChangelogManual,
      )
    }

    composable<Screen.SavedPlaceAlerts>(
      deepLinks = listOf(navDeepLink<Screen.SavedPlaceAlerts>(basePath = DeepLinks.SAVED_PLACE_ALERTS_URI)),
    ) {
      val viewModel = metroViewModel<SavedPlaceAlertsViewModel>(key = "saved-place-alerts")
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
      val shortcutsViewModel = metroViewModel<ShortcutsViewModel>(key = "shortcuts")
      BiziMobileAppContent.ShortcutsScreenContent(
        viewModel = shortcutsViewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        initialAction = initialAssistantAction,
        onInitialActionConsumed = onInitialActionConsumed,
        onBack =
          remember(navController) {
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
      val viewModel =
        assistedMetroViewModel<StationDetailViewModel, StationDetailViewModel.Factory>(
          key = "station-detail-${route.stationId}",
        ) {
          create(route.stationId)
        }
      BiziMobileAppContent.StationDetailScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        isMapReady = isMapReady,
        onBack = remember(navController) { { navController.popBackStack() } },
      )
    }
  }
}

private fun NavBackStackEntry.decodeTripRoute(logger: com.gcaguilar.biciradar.core.Logger): Screen.Trip =
  runCatching { toRoute<Screen.Trip>() }
    .getOrElse { error ->
      logger.warn(
        "BiziNavHost",
        "Falling back to default trip route after decode failure: ${error.message}",
        error,
      )
      Screen.Trip()
    }

private fun NavBackStackEntry.decodeTripMapPickerMode(logger: com.gcaguilar.biciradar.core.Logger): TripMapPickerMode =
  runCatching { toRoute<Screen.TripMapPicker>().mode }
    .mapCatching { rawMode ->
      TripMapPickerMode.entries.firstOrNull { it.name == rawMode }
        ?: error("Unknown trip map picker mode: $rawMode")
    }.getOrElse { error ->
      logger.warn(
        "BiziNavHost",
        "Falling back to station picker mode after decode failure: ${error.message}",
        error,
      )
      TripMapPickerMode.Station
    }
