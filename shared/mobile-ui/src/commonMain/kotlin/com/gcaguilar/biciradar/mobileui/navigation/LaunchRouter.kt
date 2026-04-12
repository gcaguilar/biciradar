package com.gcaguilar.biciradar.mobileui.navigation

import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.mobileui.LaunchCoordinator
import com.gcaguilar.biciradar.mobileui.LaunchResolution
import com.gcaguilar.biciradar.mobileui.state.AppState

/**
 * Sealed interface representing launch requests from mobile intents.
 */
sealed interface MobileLaunchRequest {
  data object Home : MobileLaunchRequest

  data object Map : MobileLaunchRequest

  data object Favorites : MobileLaunchRequest

  data object SavedPlaceAlerts : MobileLaunchRequest

  data object NearestStation : MobileLaunchRequest

  data object NearestStationWithBikes : MobileLaunchRequest

  data object NearestStationWithSlots : MobileLaunchRequest

  data object OpenAssistant : MobileLaunchRequest

  data object StationStatus : MobileLaunchRequest

  data class MonitorStation(
    val stationId: String,
  ) : MobileLaunchRequest

  data class SelectCity(
    val cityId: String,
  ) : MobileLaunchRequest

  data class RouteToStation(
    val stationId: String? = null,
  ) : MobileLaunchRequest

  data class ShowStation(
    val stationId: String,
  ) : MobileLaunchRequest
}

/**
 * Sealed interface representing launch requests from assistant intents.
 */
sealed interface AssistantLaunchRequest {
  data class SearchStation(
    val stationQuery: String,
  ) : AssistantLaunchRequest

  data class StationStatus(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest

  data class StationBikeCount(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest

  data class StationSlotCount(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest

  data class RouteToStation(
    val stationId: String? = null,
    val stationQuery: String? = null,
  ) : AssistantLaunchRequest
}

/**
 * Router for handling launch requests and navigation.
 * Encapsulates the logic for resolving and applying launch resolutions.
 */
internal class LaunchRouter(
  private val navController: NavHostController,
  private val appState: AppState,
  private val launchCoordinator: LaunchCoordinator,
) {
  /**
   * Applies a launch resolution by updating app state and navigating as needed.
   */
  fun applyResolution(resolution: LaunchResolution) {
    resolution.searchQuery?.let { appState.pendingMapSearchQuery = it }
    resolution.assistantAction?.let { appState.pendingAssistantAction = it }
    resolution.screen?.let { screen ->
      navController.navigate(screen) { launchSingleTop = true }
    }
  }
}
