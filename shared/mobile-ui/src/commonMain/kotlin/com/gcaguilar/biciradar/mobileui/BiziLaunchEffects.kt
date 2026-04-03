package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.core.StationsState
import com.gcaguilar.biciradar.mobileui.navigation.AssistantLaunchRequest
import com.gcaguilar.biciradar.mobileui.navigation.LaunchRouter
import com.gcaguilar.biciradar.mobileui.navigation.MobileLaunchRequest

@Composable
private fun rememberLaunchRouter(
  navController: NavHostController,
  appState: AppState,
  launchCoordinator: LaunchCoordinator,
): LaunchRouter = remember(navController, appState, launchCoordinator) {
  LaunchRouter(navController, appState, launchCoordinator)
}

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
  val launchRouter = rememberLaunchRouter(navController, appState, launchCoordinator)

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
    launchRouter.applyResolution(resolution)
    appState.pendingLaunchRequest = null
  }

  LaunchedEffect(startupLaunchReady, appState.pendingAssistantLaunchRequest, stationsState.stations) {
    if (!startupLaunchReady) return@LaunchedEffect
    val request = appState.pendingAssistantLaunchRequest ?: return@LaunchedEffect
    val resolution = launchCoordinator.resolveAssistantLaunch(request, stationsState.stations)
    launchRouter.applyResolution(resolution)
    appState.pendingAssistantLaunchRequest = null
  }
}
