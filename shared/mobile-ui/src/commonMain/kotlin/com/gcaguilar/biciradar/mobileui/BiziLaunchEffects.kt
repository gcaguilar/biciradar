package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.core.StationsState

@Composable
internal fun BiziLaunchEffects(
  startupLaunchReady: Boolean,
  onStartupReadyChanged: (Boolean) -> Unit,
  appState: AppState,
  launchRequest: MobileLaunchRequest?,
  assistantLaunchRequest: AssistantLaunchRequest?,
  stationsState: StationsState,
  searchRadiusMeters: Int,
  launchCoordinator: LaunchCoordinator,
  navController: NavHostController,
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
    val request = appState.pendingLaunchRequest ?: return@LaunchedEffect
    val resolution = launchCoordinator.resolveMobileLaunch(
      request = request,
      stations = stationsState.stations,
      searchRadiusMeters = searchRadiusMeters,
    ) ?: return@LaunchedEffect
    applyLaunchResolution(navController, appState, resolution)
    appState.pendingLaunchRequest = null
  }

  LaunchedEffect(startupLaunchReady, appState.pendingAssistantLaunchRequest, stationsState.stations) {
    if (!startupLaunchReady) return@LaunchedEffect
    val request = appState.pendingAssistantLaunchRequest ?: return@LaunchedEffect
    val resolution = launchCoordinator.resolveAssistantLaunch(request, stationsState.stations)
    applyLaunchResolution(navController, appState, resolution)
    appState.pendingAssistantLaunchRequest = null
  }
}

private fun applyLaunchResolution(
  navController: NavHostController,
  appState: AppState,
  resolution: LaunchResolution,
) {
  resolution.searchQuery?.let { appState.searchQuery = it }
  resolution.assistantAction?.let { appState.pendingAssistantAction = it }
  resolution.screen?.let { screen ->
    navController.navigate(screen) { launchSingleTop = true }
  }
}
