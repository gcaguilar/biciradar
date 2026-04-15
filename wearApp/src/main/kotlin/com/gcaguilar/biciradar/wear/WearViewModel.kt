package com.gcaguilar.biciradar.wear

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.BootstrapSession
import com.gcaguilar.biciradar.core.FindStationById
import com.gcaguilar.biciradar.core.ObserveFavorites
import com.gcaguilar.biciradar.core.ObserveStationsState
import com.gcaguilar.biciradar.core.ObserveSurfaceMonitoring
import com.gcaguilar.biciradar.core.ObserveSurfaceSnapshot
import com.gcaguilar.biciradar.core.RefreshStationAvailability
import com.gcaguilar.biciradar.core.RefreshStationDataIfNeeded
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.StartStationMonitoring
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StopStationMonitoring
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SyncFavoritesFromPeer
import com.gcaguilar.biciradar.core.ToggleFavoriteStation
import com.gcaguilar.biciradar.wear.ongoing.MonitoringOngoingActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal enum class WearTab { Cercanas, Favoritas }

internal data class WearRootUiState(
  val stations: List<Station> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val favoriteIds: Set<String> = emptySet(),
  val homeStationId: String? = null,
  val workStationId: String? = null,
  val surfaceBundle: SurfaceSnapshotBundle? = null,
  val activeMonitoring: SurfaceMonitoringSession? = null,
  val selectedStationId: String? = null,
  val currentTab: WearTab = WearTab.Cercanas,
)

internal class WearViewModel(
  private val appContext: Context,
  private val observeStationsState: ObserveStationsState,
  private val observeFavorites: ObserveFavorites,
  private val observeSurfaceMonitoring: ObserveSurfaceMonitoring,
  private val observeSurfaceSnapshot: ObserveSurfaceSnapshot,
  private val bootstrapSession: BootstrapSession,
  private val syncFavoritesFromPeer: SyncFavoritesFromPeer,
  private val toggleFavoriteStation: ToggleFavoriteStation,
  private val refreshStationDataIfNeeded: RefreshStationDataIfNeeded,
  private val refreshStationAvailability: RefreshStationAvailability,
  private val findStationById: FindStationById,
  private val startStationMonitoring: StartStationMonitoring,
  private val stopStationMonitoring: StopStationMonitoring,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {
  private val _uiState = MutableStateFlow(WearRootUiState())
  val uiState: StateFlow<WearRootUiState> = _uiState.asStateFlow()

  private val ongoingActivity = MonitoringOngoingActivity(appContext)
  private val phoneRouteRequester = WearPhoneRouteRequester(appContext)

  private var latestStations: List<Station> = emptyList()
  private var latestIsLoading: Boolean = false
  private var latestErrorMessage: String? = null
  private var latestFavoriteIds: Set<String> = emptySet()
  private var latestHomeStationId: String? = null
  private var latestWorkStationId: String? = null
  private var latestSurfaceBundle: SurfaceSnapshotBundle? = null
  private var latestActiveMonitoring: SurfaceMonitoringSession? = null
  private var selectedStationId: String? = null
  private var currentTab: WearTab = WearTab.Cercanas

  init {
    viewModelScope.launch {
      observeStationsState.state.collect { state ->
        latestStations = state.stations
        latestIsLoading = state.isLoading
        latestErrorMessage = state.errorMessage
        publishUiState()
      }
    }

    viewModelScope.launch {
      observeFavorites.favoriteIds.collect { ids ->
        latestFavoriteIds = ids
        publishUiState()
      }
    }

    viewModelScope.launch {
      observeFavorites.homeStationId.collect { homeStationId ->
        latestHomeStationId = homeStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      observeFavorites.workStationId.collect { workStationId ->
        latestWorkStationId = workStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      observeSurfaceMonitoring.state.collect { session ->
        latestActiveMonitoring = session?.takeIf { it.isActive }
        publishUiState()
      }
    }

    viewModelScope.launch {
      observeSurfaceSnapshot.bundle.collect { bundle ->
        latestSurfaceBundle = bundle
        publishUiState()
      }
    }

    viewModelScope.launch {
      bootstrapSession.execute()
      syncFavoritesFromPeer.execute()
      refreshWearSurface(appContext)
    }

    viewModelScope.launch {
      while (true) {
        delay(30_000)
        refreshWearSurface(appContext)
      }
    }
  }

  fun onPermissionRefresh(refreshKey: Int) {
    if (refreshKey <= 0) return
    viewModelScope.launch {
      refreshWearSurface(appContext)
    }
  }

  fun onLaunchStationRequested(stationId: String?) {
    selectedStationId = stationId
    publishUiState()
  }

  fun onStationSelected(stationId: String) {
    selectedStationId = stationId
    publishUiState()
  }

  fun onBackFromStationDetail() {
    selectedStationId = null
    publishUiState()
  }

  fun onTabSelected(tab: WearTab) {
    currentTab = tab
    publishUiState()
  }

  fun onRetry() {
    viewModelScope.launch {
      refreshStationDataIfNeeded.execute()
    }
  }

  fun onRefresh() {
    viewModelScope.launch {
      refreshWearSurface(appContext, forceRefresh = true)
    }
  }

  fun onToggleFavorite(stationId: String) {
    viewModelScope.launch {
      toggleFavoriteStation.execute(stationId)
      refreshWearSurface(appContext)
    }
  }

  fun onToggleMonitoring(stationId: String) {
    viewModelScope.launch {
      if (latestActiveMonitoring?.stationId == stationId) {
        stopStationMonitoring.execute(clear = true)
        ongoingActivity.stop()
      } else {
        startStationMonitoring.execute(stationId = stationId)
        val station = findStationById.execute(stationId)
        if (station != null) {
          ongoingActivity.start(
            stationId = stationId,
            stationName = station.name,
            remainingSeconds = 300,
          )
        }
      }
      FavoriteStationTileService.requestUpdate(appContext)
      NearbyStationsTileService.requestUpdate(appContext)
    }
  }

  fun onStartMonitoringFavorite(stationId: String) {
    viewModelScope.launch {
      startStationMonitoring.execute(stationId = stationId)
      val station = findStationById.execute(stationId)
      if (station != null) {
        ongoingActivity.start(
          stationId = stationId,
          stationName = station.name,
          remainingSeconds = 300,
        )
      }
      FavoriteStationTileService.requestUpdate(appContext)
      NearbyStationsTileService.requestUpdate(appContext)
    }
  }

  fun onStopMonitoring() {
    viewModelScope.launch {
      stopStationMonitoring.execute(clear = true)
      ongoingActivity.stop()
      FavoriteStationTileService.requestUpdate(appContext)
      NearbyStationsTileService.requestUpdate(appContext)
    }
  }

  fun onRoute(stationId: String) {
    findStationById.execute(stationId)?.let(routeLauncher::launch)
  }

  fun onRouteInPhone(stationId: String) {
    viewModelScope.launch {
      val requested = phoneRouteRequester.requestRoute(stationId)
      if (!requested) {
        latestErrorMessage = "No se pudo abrir la ruta en el teléfono"
        publishUiState()
      }
    }
  }

  private fun publishUiState() {
    _uiState.value =
      WearRootUiState(
        stations = latestStations,
        isLoading = latestIsLoading,
        errorMessage = latestErrorMessage,
        favoriteIds = latestFavoriteIds,
        homeStationId = latestHomeStationId,
        workStationId = latestWorkStationId,
        surfaceBundle = latestSurfaceBundle,
        activeMonitoring = latestActiveMonitoring,
        selectedStationId = selectedStationId,
        currentTab = currentTab,
      )
  }

  private suspend fun refreshWearSurface(
    context: Context,
    forceRefresh: Boolean = false,
  ) {
    syncFavoritesFromPeer.execute()
    if (forceRefresh) {
      refreshStationDataIfNeeded.execute(forceRefresh = true)
    } else {
      refreshStationDataIfNeeded.execute()
      val stationIds = latestStations.take(10).map { it.id }
      if (stationIds.isNotEmpty()) {
        refreshStationAvailability.execute(stationIds)
      }
    }
    FavoriteStationTileService.requestUpdate(context)
    NearbyStationsTileService.requestUpdate(context)
  }
}

internal class WearViewModelFactory(
  private val appContext: Context,
  private val graph: SharedGraph,
) {
  fun create(): WearViewModel =
    WearViewModel(
      appContext = appContext.applicationContext,
      observeStationsState = graph.observeStationsState,
      observeFavorites = graph.observeFavorites,
      observeSurfaceMonitoring = graph.observeSurfaceMonitoring,
      observeSurfaceSnapshot = graph.observeSurfaceSnapshot,
      bootstrapSession = graph.bootstrapSession,
      syncFavoritesFromPeer = graph.syncFavoritesFromPeer,
      toggleFavoriteStation = graph.toggleFavoriteStation,
      refreshStationDataIfNeeded = graph.refreshStationDataIfNeeded,
      refreshStationAvailability = graph.refreshStationAvailability,
      findStationById = graph.findStationById,
      startStationMonitoring = graph.startStationMonitoring,
      stopStationMonitoring = graph.stopStationMonitoring,
      routeLauncher = graph.routeLauncher,
    )
}
