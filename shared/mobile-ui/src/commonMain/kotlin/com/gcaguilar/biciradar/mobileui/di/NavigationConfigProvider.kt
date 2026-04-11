package com.gcaguilar.biciradar.mobileui.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.navigation.NavigationHostConfig
import com.gcaguilar.biciradar.mobileui.state.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Crea y memoriza la configuración de navegación.
 *
 * @param navController Controlador de navegación
 * @param mobilePlatform Plataforma móvil actual
 * @param stations Lista de estaciones filtradas
 * @param favoriteIds IDs de estaciones favoritas
 * @param stationsState Estado de carga de estaciones
 * @param nearestSelection Selección de estación más cercana
 * @param searchRadiusMeters Radio de búsqueda en metros
 * @param isMapReady Indica si el mapa está listo
 * @param appState Estado global de la aplicación
 * @param scope CoroutineScope para operaciones asíncronas
 * @param stationsRepository Repositorio de estaciones
 * @param favoritesRepository Repositorio de favoritos
 * @param graph Grafo de dependencias compartidas
 * @param platformBindings Bindings de plataforma
 * @param onOpenOnboarding Callback para abrir onboarding
 * @param onShowChangelogManual Callback para mostrar changelog
 * @return NavigationHostConfig configurada
 */
@Composable
internal fun rememberNavigationConfig(
  navController: NavHostController,
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  stationsState: com.gcaguilar.biciradar.core.StationsState,
  nearestSelection: NearbyStationSelection,
  searchRadiusMeters: Int,
  isMapReady: Boolean,
  appState: AppState,
  scope: CoroutineScope,
  stationsRepository: StationsRepository,
  favoritesRepository: FavoritesRepository,
  graph: SharedGraph,
  platformBindings: PlatformBindings,
  onOpenOnboarding: () -> Unit,
  onShowChangelogManual: () -> Unit,
): NavigationHostConfig {
  val onRefreshStations =
    remember(scope, stationsRepository) {
      {
        scope.launch { stationsRepository.forceRefresh() }
        Unit
      }
    }

  val onRetry =
    remember(scope, stationsRepository) {
      {
        scope.launch { stationsRepository.loadIfNeeded() }
        Unit
      }
    }

  val onFavoriteToggle =
    remember(scope, favoritesRepository) {
      { station: Station ->
        scope.launch { favoritesRepository.toggle(station.id) }
        Unit
      }
    }

  val onQuickRoute =
    remember(graph, scope) {
      { station: Station ->
        scope.launch {
          graph.engagementRepository.markRouteOpened()
          graph.routeLauncher.launch(station)
        }
        Unit
      }
    }

  val onOpenAssistant =
    remember(navController) {
      {
        navController.navigate(
          com.gcaguilar.biciradar.mobileui.navigation.Screen.Shortcuts,
        ) { launchSingleTop = true }
      }
    }

  val onSearchQueryChange = remember(appState) { { query: String -> appState.updateSearchQuery(query) } }
  val onInitialActionConsumed = remember(appState) { { appState.clearPendingAssistantAction() } }

  return remember(
    navController,
    mobilePlatform,
    stations,
    favoriteIds,
    stationsState,
    nearestSelection,
    searchRadiusMeters,
    isMapReady,
    appState,
    scope,
    stationsRepository,
    favoritesRepository,
    graph,
    platformBindings,
    onOpenOnboarding,
    onShowChangelogManual,
  ) {
    NavigationHostConfig(
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
      searchQuery = appState.searchQuery.value,
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
      initialAssistantAction = appState.pendingAssistantAction.value,
      onInitialActionConsumed = onInitialActionConsumed,
      onOpenOnboarding = onOpenOnboarding,
      onShowChangelogManual = onShowChangelogManual,
      paddingValues =
        androidx.compose.foundation.layout
          .PaddingValues(0.dp),
    )
  }
}
