package com.gcaguilar.biciradar.wear

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.StartStationMonitoring
import com.gcaguilar.biciradar.core.StopStationMonitoring
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
  val selectedStationId: String? = null,
  val currentTab: WearTab = WearTab.Cercanas,
) {
  val activeMonitoring: SurfaceMonitoringSession?
    get() = surfaceBundle?.monitoringSession?.takeIf { it.isActive }
}

internal class WearViewModel(
  private val appContext: Context,
  private val stationsRepository: StationsRepository,
  private val favoritesRepository: FavoritesRepository,
  private val surfaceSnapshotRepository: SurfaceSnapshotRepository,
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository,
  private val startStationMonitoring: StartStationMonitoring,
  private val stopStationMonitoring: StopStationMonitoring,
  private val routeLauncher: RouteLauncher,
) : ViewModel() {

  private val _uiState = MutableStateFlow(WearRootUiState())
  val uiState: StateFlow<WearRootUiState> = _uiState.asStateFlow()
  
  private val ongoingActivity = MonitoringOngoingActivity(appContext)

  private var latestStations: List<Station> = emptyList()
  private var latestIsLoading: Boolean = false
  private var latestErrorMessage: String? = null
  private var latestFavoriteIds: Set<String> = emptySet()
  private var latestHomeStationId: String? = null
  private var latestWorkStationId: String? = null
  private var latestSurfaceBundle: SurfaceSnapshotBundle? = null
  private var selectedStationId: String? = null
  private var currentTab: WearTab = WearTab.Cercanas

  init {
    viewModelScope.launch {
      stationsRepository.state.collect { state ->
        latestStations = state.stations
        latestIsLoading = state.isLoading
        latestErrorMessage = state.errorMessage
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.favoriteIds.collect { ids ->
        latestFavoriteIds = ids
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.homeStationId.collect { homeStationId ->
        latestHomeStationId = homeStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.workStationId.collect { workStationId ->
        latestWorkStationId = workStationId
        publishUiState()
      }
    }

    viewModelScope.launch {
      surfaceSnapshotRepository.bundle.collect { bundle ->
        latestSurfaceBundle = bundle
        publishUiState()
      }
    }

    viewModelScope.launch {
      favoritesRepository.syncFromPeer()
      surfaceSnapshotRepository.bootstrap()
      surfaceMonitoringRepository.bootstrap()
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
      stationsRepository.loadIfNeeded()
    }
  }

  fun onRefresh() {
    viewModelScope.launch {
      refreshWearSurface(appContext, forceRefresh = true)
    }
  }

  fun onToggleFavorite(stationId: String) {
    viewModelScope.launch {
      favoritesRepository.toggle(stationId)
      refreshWearSurface(appContext)
    }
  }

  fun onToggleMonitoring(stationId: String) {
    viewModelScope.launch {
      if (latestSurfaceBundle?.monitoringSession?.takeIf { it.isActive }?.stationId == stationId) {
        stopStationMonitoring.execute(clear = true)
        ongoingActivity.stop()
      } else {
        startStationMonitoring.execute(stationId = stationId)
        // Iniciar ongoing activity
        val station = stationsRepository.stationById(stationId)
        if (station != null) {
          ongoingActivity.start(
            stationId = stationId,
            stationName = station.name,
            remainingSeconds = 300 // 5 minutos por defecto
          )
        }
      }
      FavoriteStationTileService.requestUpdate(appContext)
    }
  }

  fun onStartMonitoringFavorite(stationId: String) {
    viewModelScope.launch {
      startStationMonitoring.execute(stationId = stationId)
      // Iniciar ongoing activity
      val station = stationsRepository.stationById(stationId)
      if (station != null) {
        ongoingActivity.start(
          stationId = stationId,
          stationName = station.name,
          remainingSeconds = 300 // 5 minutos por defecto
        )
      }
      FavoriteStationTileService.requestUpdate(appContext)
    }
  }

  fun onStopMonitoring() {
    viewModelScope.launch {
      stopStationMonitoring.execute(clear = true)
      ongoingActivity.stop()
      FavoriteStationTileService.requestUpdate(appContext)
    }
  }

  fun onRoute(stationId: String) {
    stationsRepository.stationById(stationId)?.let(routeLauncher::launch)
  }

  private fun publishUiState() {
    _uiState.value = WearRootUiState(
      stations = latestStations,
      isLoading = latestIsLoading,
      errorMessage = latestErrorMessage,
      favoriteIds = latestFavoriteIds,
      homeStationId = latestHomeStationId,
      workStationId = latestWorkStationId,
      surfaceBundle = latestSurfaceBundle,
      selectedStationId = selectedStationId,
      currentTab = currentTab,
    )
  }

  private suspend fun refreshWearSurface(
    context: Context,
    forceRefresh: Boolean = false,
  ) {
    favoritesRepository.syncFromPeer()
    if (forceRefresh) {
      stationsRepository.forceRefresh()
    } else {
      stationsRepository.loadIfNeeded()
      val stationIds = latestStations.take(10).map { it.id }
      if (stationIds.isNotEmpty()) {
        stationsRepository.refreshAvailability(stationIds)
      }
    }
    surfaceSnapshotRepository.refreshSnapshot()
    FavoriteStationTileService.requestUpdate(context)
  }
}

internal class WearViewModelFactory(
  private val appContext: Context,
  private val graph: SharedGraph,
) {
  fun create(): WearViewModel = WearViewModel(
    appContext = appContext.applicationContext,
    stationsRepository = graph.stationsRepository,
    favoritesRepository = graph.favoritesRepository,
    surfaceSnapshotRepository = graph.surfaceSnapshotRepository,
    surfaceMonitoringRepository = graph.surfaceMonitoringRepository,
    startStationMonitoring = graph.startStationMonitoring,
    stopStationMonitoring = graph.stopStationMonitoring,
    routeLauncher = graph.routeLauncher,
  )
}
