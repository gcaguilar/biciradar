package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.City
import com.gcaguilar.biciradar.core.DEFAULT_SURFACE_MONITORING_DURATION_SECONDS
import com.gcaguilar.biciradar.core.NearbyStationSelection
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.selectNearbyStation
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import com.gcaguilar.biciradar.mobileui.navigation.Screen

@Composable
internal fun BiziLaunchEffects(
  startupLaunchReady: Boolean,
  onStartupReadyChanged: (Boolean) -> Unit,
  appState: AppState,
  launchRequest: MobileLaunchRequest?,
  assistantLaunchRequest: AssistantLaunchRequest?,
  stationsState: StationsState,
  searchRadiusMeters: Int,
  nearestSelection: NearbyStationSelection,
  graph: SharedGraph,
  stationsRepository: StationsRepository,
  settingsRepository: SettingsRepository,
  navController: NavHostController,
  platformBindings: PlatformBindings,
) {
  LaunchedEffect(launchRequest) {
    appState.pendingLaunchRequest = launchRequest
  }

  LaunchedEffect(assistantLaunchRequest) {
    appState.pendingAssistantLaunchRequest = assistantLaunchRequest
  }

  LaunchedEffect(startupLaunchReady) {
    onStartupReadyChanged(startupLaunchReady)
  }

  LaunchedEffect(startupLaunchReady, appState.pendingLaunchRequest, stationsState.stations, searchRadiusMeters) {
    if (!startupLaunchReady) return@LaunchedEffect
    when (val request = appState.pendingLaunchRequest ?: return@LaunchedEffect) {
      MobileLaunchRequest.Home -> {
        navController.navigate(Screen.Nearby) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.Map -> {
        navController.navigate(Screen.Map) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.Favorites -> {
        navController.navigate(Screen.Favorites) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.SavedPlaceAlerts -> {
        navController.navigate(Screen.SavedPlaceAlerts) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStation -> {
        val station = nearestSelection.highlightedStation ?: return@LaunchedEffect
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithBikes -> {
        val station = selectNearbyStationWithBikes(
          stations = stationsState.stations,
          searchRadiusMeters = searchRadiusMeters,
        ).highlightedStation ?: return@LaunchedEffect
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.NearestStationWithSlots -> {
        val station = selectNearbyStationWithSlots(
          stations = stationsState.stations,
          searchRadiusMeters = searchRadiusMeters,
        ).highlightedStation ?: return@LaunchedEffect
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.OpenAssistant -> {
        navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      MobileLaunchRequest.StationStatus -> {
        val station = stationsState.stations.firstOrNull() ?: return@LaunchedEffect
        appState.pendingAssistantAction = AssistantAction.StationStatus(station.id)
        navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.MonitorStation -> {
        val station = stationsRepository.stationById(request.stationId)
          ?: stationsState.stations.firstOrNull { it.id == request.stationId }
          ?: return@LaunchedEffect
        val notificationsGranted = platformBindings.localNotifier.requestPermission()
        graph.surfaceSnapshotRepository.refreshSnapshot()
        if (notificationsGranted) {
          graph.surfaceMonitoringRepository.startMonitoring(
            stationId = station.id,
            durationSeconds = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
            kind = SurfaceMonitoringKind.Docks,
          )
        }
        navController.navigate(Screen.StationDetail(station.id))
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.SelectCity -> {
        val city = City.fromId(request.cityId) ?: return@LaunchedEffect
        settingsRepository.setSelectedCity(city)
        stationsRepository.forceRefresh()
        navController.navigate(Screen.Nearby) { launchSingleTop = true }
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.RouteToStation -> {
        val station = request.stationId?.let(stationsRepository::stationById)
          ?: stationsState.stations.firstOrNull()
          ?: return@LaunchedEffect
        graph.routeLauncher.launch(station)
        appState.pendingLaunchRequest = null
      }
      is MobileLaunchRequest.ShowStation -> {
        if (stationsRepository.stationById(request.stationId) == null) return@LaunchedEffect
        navController.navigate(Screen.StationDetail(request.stationId))
        appState.pendingLaunchRequest = null
      }
    }
  }

  LaunchedEffect(startupLaunchReady, appState.pendingAssistantLaunchRequest, stationsState.stations) {
    if (!startupLaunchReady) return@LaunchedEffect
    val request = appState.pendingAssistantLaunchRequest ?: return@LaunchedEffect
    val station = resolveLaunchStation(
      stations = stationsState.stations,
      graph = graph,
      stationId = when (request) {
        is AssistantLaunchRequest.RouteToStation -> request.stationId
        is AssistantLaunchRequest.SearchStation -> null
        is AssistantLaunchRequest.StationBikeCount -> request.stationId
        is AssistantLaunchRequest.StationSlotCount -> request.stationId
        is AssistantLaunchRequest.StationStatus -> request.stationId
      },
      stationQuery = when (request) {
        is AssistantLaunchRequest.RouteToStation -> request.stationQuery
        is AssistantLaunchRequest.SearchStation -> request.stationQuery
        is AssistantLaunchRequest.StationBikeCount -> request.stationQuery
        is AssistantLaunchRequest.StationSlotCount -> request.stationQuery
        is AssistantLaunchRequest.StationStatus -> request.stationQuery
      },
    )

    when (request) {
      is AssistantLaunchRequest.SearchStation -> {
        appState.searchQuery = request.stationQuery
        if (station != null) {
          navController.navigate(Screen.StationDetail(station.id))
        } else {
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.StationStatus -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationStatus(station.id)
          navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.StationBikeCount -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationBikeCount(station.id)
          navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.StationSlotCount -> {
        if (station != null) {
          appState.pendingAssistantAction = AssistantAction.StationSlotCount(station.id)
          navController.navigate(Screen.Shortcuts) { launchSingleTop = true }
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
      is AssistantLaunchRequest.RouteToStation -> {
        if (station != null) {
          graph.routeLauncher.launch(station)
        } else {
          appState.searchQuery = request.stationQuery.orEmpty()
          navController.navigate(Screen.Map) { launchSingleTop = true }
        }
      }
    }

    appState.pendingAssistantLaunchRequest = null
  }
}
