package com.gcaguilar.biciradar.mobileui.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.mobileui.viewmodel.AppRootViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.FavoritesViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ProfileViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.SavedPlaceAlertsViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.ShortcutsViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.StationDetailViewModelFactory
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModelFactory

/**
 * Contenedor de todas las factories de ViewModels.
 * Facilita la inyección de dependencias en la navegación.
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
 * Crea y memoriza todas las factories de ViewModels necesarias para la navegación.
 *
 * @param graph El grafo de dependencias compartidas
 * @param platformBindings Bindings de plataforma específicos
 * @return ViewModelFactories configuradas
 */
@Composable
internal fun rememberViewModelFactories(
  graph: SharedGraph,
  platformBindings: PlatformBindings,
): ViewModelFactories {
  val trip =
    remember(graph) {
      TripViewModelFactory(
        tripRepository = graph.tripRepository,
        surfaceMonitoringRepository = graph.surfaceMonitoringRepository,
        geoSearchUseCase = graph.geoSearchUseCase,
        reverseGeocodeUseCase = graph.reverseGeocodeUseCase,
        settingsRepository = graph.settingsRepository,
        stationsRepository = graph.stationsRepository,
      )
    }

  val nearby =
    remember(graph) {
      NearbyViewModelFactory(
        stationsRepository = graph.stationsRepository,
        favoritesRepository = graph.favoritesRepository,
        routeLauncher = graph.routeLauncher,
        settingsRepository = graph.settingsRepository,
      )
    }

  val mapEnvironmental =
    remember(graph) {
      MapEnvironmentalViewModelFactory(
        environmentalRepository = graph.environmentalRepository,
        settingsRepository = graph.settingsRepository,
        stationsRepository = graph.stationsRepository,
        favoritesRepository = graph.favoritesRepository,
      )
    }

  val shortcuts =
    remember(graph) {
      ShortcutsViewModelFactory(
        assistantIntentResolver = graph.assistantIntentResolver,
        stationsRepository = graph.stationsRepository,
        favoritesRepository = graph.favoritesRepository,
        settingsRepository = graph.settingsRepository,
      )
    }

  val favorites =
    remember(graph) {
      FavoritesViewModelFactory(
        favoritesRepository = graph.favoritesRepository,
        stationsRepository = graph.stationsRepository,
        settingsRepository = graph.settingsRepository,
        savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
        routeLauncher = graph.routeLauncher,
      )
    }

  val profile =
    remember(graph, platformBindings) {
      ProfileViewModelFactory(
        settingsRepository = graph.settingsRepository,
        stationsRepository = graph.stationsRepository,
        favoritesRepository = graph.favoritesRepository,
        savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
        canSelectGoogleMapsInIos = platformBindings.mapSupport.currentStatus().googleMapsAppInstalled,
      )
    }

  val savedPlaceAlerts =
    remember(graph) {
      SavedPlaceAlertsViewModelFactory(
        savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
      )
    }

  val stationDetail =
    remember(graph) {
      StationDetailViewModelFactory(
        favoritesRepository = graph.favoritesRepository,
        settingsRepository = graph.settingsRepository,
        savedPlaceAlertsRepository = graph.savedPlaceAlertsRepository,
        stationsRepository = graph.stationsRepository,
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
 * Crea la factory para AppRootViewModel.
 *
 * @param graph El grafo de dependencias compartidas
 * @param platformBindings Bindings de plataforma específicos
 * @return AppRootViewModelFactory configurada
 */

/**
 * Contenedor de todas las factories de ViewModels incluyendo los especializados.
 */
@Composable
internal fun rememberAppRootViewModelFactory(
  graph: SharedGraph,
  platformBindings: PlatformBindings,
): AppRootViewModelFactory =
  remember(graph, platformBindings) {
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
