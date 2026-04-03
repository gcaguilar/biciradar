package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory

/**
 * Configuration data class for NavigationHost dependencies.
 * Groups all required dependencies to simplify parameter passing.
 */
internal data class NavigationHostConfig(
  val navController: NavHostController,
  val mobilePlatform: MobileUiPlatform,
  val stations: List<Station>,
  val favoriteIds: Set<String>,
  val loading: Boolean,
  val errorMessage: String?,
  val stationsFreshness: DataFreshness,
  val stationsLastUpdatedEpoch: Long?,
  val onRefreshStations: () -> Unit,
  val nearestSelection: NearbyStationSelection,
  val userLocation: GeoPoint?,
  val searchQuery: String,
  val searchRadiusMeters: Int,
  val isMapReady: Boolean,
  val onSearchQueryChange: (String) -> Unit,
  val onRetry: () -> Unit,
  val onFavoriteToggle: (Station) -> Unit,
  val onQuickRoute: (Station) -> Unit,
  val onOpenAssistant: () -> Unit,
  val localNotifier: LocalNotifier,
  val routeLauncher: RouteLauncher,
  val platformBindings: PlatformBindings,
  val initialAssistantAction: AssistantAction?,
  val onInitialActionConsumed: () -> Unit,
  val onOpenOnboarding: () -> Unit,
  val onShowChangelogManual: () -> Unit,
  val paddingValues: PaddingValues,
)

/**
 * ViewModel factory container for all screens.
 */
internal data class ViewModelFactories(
  val trip: TripViewModelFactory,
  val nearby: NearbyViewModelFactory,
  val mapEnvironmental: MapEnvironmentalViewModelFactory,
  val shortcuts: ShortcutsViewModelFactory,
  val favorites: FavoritesViewModelFactory,
  val profile: ProfileViewModelFactory,
  val savedPlaceAlerts: SavedPlaceAlertsViewModelFactory,
  val stationDetail: StationDetailViewModelFactory,
)

/**
 * Main navigation host composable that wraps BiziNavHost.
 * Simplifies the call site by accepting configuration objects.
 */
@Composable
internal fun NavigationHost(
  config: NavigationHostConfig,
  factories: ViewModelFactories,
) {
  BiziNavHost(
    navController = config.navController,
    mobilePlatform = config.mobilePlatform,
    tripViewModelFactory = factories.trip,
    nearbyViewModelFactory = factories.nearby,
    mapEnvironmentalViewModelFactory = factories.mapEnvironmental,
    shortcutsViewModelFactory = factories.shortcuts,
    favoritesViewModelFactory = factories.favorites,
    profileViewModelFactory = factories.profile,
    savedPlaceAlertsViewModelFactory = factories.savedPlaceAlerts,
    stationDetailViewModelFactory = factories.stationDetail,
    stations = config.stations,
    favoriteIds = config.favoriteIds,
    loading = config.loading,
    errorMessage = config.errorMessage,
    stationsFreshness = config.stationsFreshness,
    stationsLastUpdatedEpoch = config.stationsLastUpdatedEpoch,
    onRefreshStations = config.onRefreshStations,
    nearestSelection = config.nearestSelection,
    userLocation = config.userLocation,
    searchQuery = config.searchQuery,
    searchRadiusMeters = config.searchRadiusMeters,
    isMapReady = config.isMapReady,
    onSearchQueryChange = config.onSearchQueryChange,
    onRetry = config.onRetry,
    onFavoriteToggle = config.onFavoriteToggle,
    onQuickRoute = config.onQuickRoute,
    onOpenAssistant = config.onOpenAssistant,
    localNotifier = config.localNotifier,
    routeLauncher = config.routeLauncher,
    platformBindings = config.platformBindings,
    initialAssistantAction = config.initialAssistantAction,
    onInitialActionConsumed = config.onInitialActionConsumed,
    onOpenOnboarding = config.onOpenOnboarding,
    onShowChangelogManual = config.onShowChangelogManual,
    paddingValues = config.paddingValues,
  )
}
