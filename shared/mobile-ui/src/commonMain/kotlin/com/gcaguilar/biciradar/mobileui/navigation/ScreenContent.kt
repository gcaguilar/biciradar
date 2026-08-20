package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.mobileui.BiziMobileAppContent
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModel
import com.gcaguilar.biciradar.mobileui.viewmodel.TripMapPickerMode
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.FlowPreview

/**
 * Flat renderer for a single detail [Screen], with no navigation state of its own.
 * This is the Kotlin half of the "single screen renderer" the Liquid Glass migration
 * needs: when native iOS chrome (SwiftUI `NavigationStack`) owns the back stack, each
 * pushed destination gets its own `ComposeUIViewController` hosting just this function,
 * instead of a full [BiziNavHost] (see `ScreenViewController` in `BiziMobileViewController.kt`).
 *
 * Top-level tabs (Nearby/Map/Favorites/Trip/Profile) are NOT rendered here — those keep
 * running through their own per-tab [BiziNavHost] instance, which is what preserves the
 * "search reuses the tab's ViewModel" behavior for [Screen.FavoritesSearch] and the trip
 * picker screens *within* a tab's own back stack.
 *
 * Known trade-off: because each pushed screen below lives in its own Compose UIViewController
 * (its own ViewModelStore), [Screen.FavoritesSearch], [Screen.TripDestinationSearch] and
 * [Screen.TripMapPicker] get a *new* `metroViewModel` instance (same key, same underlying
 * repositories via the shared [com.gcaguilar.biciradar.core.SharedGraph]) rather than literally
 * reusing the parent tab's ViewModel object the way the single shared [BiziNavHost] does today.
 * Functionally equivalent for now; revisit if a screen starts relying on exact instance identity.
 */
@FlowPreview
@Composable
internal fun ScreenContent(
  route: Screen,
  mobilePlatform: MobileUiPlatform,
  platformBindings: PlatformBindings,
  isMapReady: Boolean,
  onBack: () -> Unit,
  onNavigate: (Screen) -> Unit,
) {
  when (route) {
    is Screen.StationDetail -> {
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
        onBack = onBack,
      )
    }

    is Screen.FavoritesSearch -> {
      val viewModel = metroViewModel<FavoritesViewModel>(key = "favorites")
      BiziMobileAppContent.FavoritesSearchScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        onBack = onBack,
        onStationSelected = { station -> onNavigate(Screen.StationDetail(station.id)) },
      )
    }

    is Screen.SavedPlaceAlerts -> {
      val viewModel = metroViewModel<SavedPlaceAlertsViewModel>(key = "saved-place-alerts")
      BiziMobileAppContent.SavedPlaceAlertsScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        onBack = onBack,
      )
    }

    is Screen.TripDestinationSearch -> {
      val viewModel = metroViewModel<TripViewModel>(key = "trip")
      BiziMobileAppContent.TripDestinationSearchScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        onBack = onBack,
      )
    }

    is Screen.TripMapPicker -> {
      val viewModel = metroViewModel<TripViewModel>(key = "trip")
      val mode = TripMapPickerMode.entries.firstOrNull { it.name == route.mode } ?: TripMapPickerMode.Station
      BiziMobileAppContent.TripMapPickerScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        pickerMode = mode,
        isMapReady = isMapReady,
        paddingValues = PaddingValues(),
        onBack = onBack,
      )
    }

    is Screen.Shortcuts -> {
      val viewModel = metroViewModel<ShortcutsViewModel>(key = "shortcuts")
      BiziMobileAppContent.ShortcutsScreenContent(
        viewModel = viewModel,
        mobilePlatform = mobilePlatform,
        paddingValues = PaddingValues(),
        initialAction = null,
        onInitialActionConsumed = {},
        onBack = onBack,
      )
    }

    // Top-level tabs render through their own per-tab BiziNavHost, not through this
    // single-screen dispatcher. CitySelection isn't reachable from BiziNavHost today.
    else -> {
      Unit
    }
  }
}
