package com.gcaguilar.biciradar.wear

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.StationsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceSnapshotRepository
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.core.remainingSeconds
import com.gcaguilar.biciradar.core.surfaceStatusLevel
import com.gcaguilar.biciradar.core.surfaceStatusTextShort
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val WearPrimary = Color(0xFF1D74BD)
private val WearSecondary = Color(0xFF64C23A)
private val WearNeutral = Color(0xFF64779D)
private val WearError = Color(0xFFCF6679)
private val WearSurface = Color(0xFF1A1A2E)
private val WearOnSurface = Color(0xFFE8EDF4)

private enum class WearTab { Cercanas, Favoritas }

@Composable
internal fun WearRoot(
  platformBindings: PlatformBindings,
  refreshKey: Int,
  launchStationId: String?,
  launchStationNonce: Int,
) {
  val context = LocalContext.current.applicationContext
  val graph = remember(platformBindings) {
    SharedGraph.Companion.create(platformBindings)
  }
  val stationsRepository = remember(graph) { graph.stationsRepository }
  val favoritesRepository = remember(graph) { graph.favoritesRepository }
  val surfaceSnapshotRepository = remember(graph) { graph.surfaceSnapshotRepository }
  val surfaceMonitoringRepository = remember(graph) { graph.surfaceMonitoringRepository }
  val startStationMonitoring = remember(graph) { graph.startStationMonitoring }
  val stopStationMonitoring = remember(graph) { graph.stopStationMonitoring }
  val stationsState by stationsRepository.state.collectAsState()
  val favoriteIds by favoritesRepository.favoriteIds.collectAsState()
  val homeStationId by favoritesRepository.homeStationId.collectAsState()
  val workStationId by favoritesRepository.workStationId.collectAsState()
  val surfaceBundle by surfaceSnapshotRepository.bundle.collectAsState()
  val scope = rememberCoroutineScope()
  var selectedStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var currentTab by rememberSaveable { mutableStateOf(WearTab.Cercanas) }
  val activeMonitoring = surfaceBundle?.monitoringSession?.takeIf { it.isActive }

  LaunchedEffect(graph, refreshKey) {
    favoritesRepository.syncFromPeer()
    surfaceSnapshotRepository.bootstrap()
    surfaceMonitoringRepository.bootstrap()
    refreshWearSurface(
      context = context,
      stationsRepository = stationsRepository,
      favoritesRepository = favoritesRepository,
      surfaceSnapshotRepository = surfaceSnapshotRepository,
    )
  }

  LaunchedEffect(graph) {
    while (true) {
      delay(30_000)
      refreshWearSurface(
        context = context,
        stationsRepository = stationsRepository,
        favoritesRepository = favoritesRepository,
        surfaceSnapshotRepository = surfaceSnapshotRepository,
      )
    }
  }

  LaunchedEffect(launchStationNonce) {
    selectedStationId = launchStationId
  }

  MaterialTheme {
    Box(
      modifier = Modifier.fillMaxSize().background(Color.Black),
      contentAlignment = Alignment.Center,
    ) {
      val selectedStation = selectedStationId?.let { stationsRepository.stationById(it) }

      when {
        selectedStation != null -> WearStationDetail(
          station = selectedStation,
          isFavorite = selectedStation.id in favoriteIds,
          savedPlaceLabel = wearSavedPlaceLabel(selectedStation.id, homeStationId, workStationId),
          currentMonitoring = activeMonitoring,
          onBack = { selectedStationId = null },
          onToggleFavorite = {
            scope.launch {
              favoritesRepository.toggle(selectedStation.id)
              refreshWearSurface(
                context = context,
                stationsRepository = stationsRepository,
                favoritesRepository = favoritesRepository,
                surfaceSnapshotRepository = surfaceSnapshotRepository,
              )
            }
          },
          onToggleMonitoring = {
            scope.launch {
              if (activeMonitoring?.stationId == selectedStation.id) {
                stopStationMonitoring.execute(clear = true)
              } else {
                startStationMonitoring.execute(stationId = selectedStation.id)
              }
              FavoriteStationTileService.requestUpdate(context)
            }
          },
          onRoute = { graph.routeLauncher.launch(selectedStation) },
        )

        stationsState.isLoading -> CircularProgressIndicator(
          colors = ProgressIndicatorDefaults.colors(indicatorColor = WearPrimary),
        )

        stationsState.errorMessage != null -> WearErrorScreen(
          message = stationsState.errorMessage!!,
          onRetry = { scope.launch { stationsRepository.loadIfNeeded() } },
        )

        else -> WearDashboard(
          stations = stationsState.stations,
          favoriteIds = favoriteIds,
          homeStationId = homeStationId,
          workStationId = workStationId,
          surfaceSnapshot = surfaceBundle,
          activeMonitoring = activeMonitoring,
          currentTab = currentTab,
          onTabSelected = { currentTab = it },
          onStationSelected = { selectedStationId = it.id },
          onOpenSurfaceStation = { stationId -> selectedStationId = stationId },
          onRefresh = {
            scope.launch {
              refreshWearSurface(
                context = context,
                stationsRepository = stationsRepository,
                favoritesRepository = favoritesRepository,
                surfaceSnapshotRepository = surfaceSnapshotRepository,
                forceRefresh = true,
              )
            }
          },
          onStartMonitoringFavorite = { stationId ->
            scope.launch {
              startStationMonitoring.execute(stationId = stationId)
              FavoriteStationTileService.requestUpdate(context)
            }
          },
          onRouteToSavedPlace = { stationId ->
            stationsRepository.stationById(stationId)?.let(graph.routeLauncher::launch)
          },
          onStopMonitoring = {
            scope.launch {
              stopStationMonitoring.execute(clear = true)
              FavoriteStationTileService.requestUpdate(context)
            }
          },
        )
      }
    }
  }
}

@Composable
private fun WearDashboard(
  stations: List<Station>,
  favoriteIds: Set<String>,
  homeStationId: String?,
  workStationId: String?,
  surfaceSnapshot: SurfaceSnapshotBundle?,
  activeMonitoring: SurfaceMonitoringSession?,
  currentTab: WearTab,
  onTabSelected: (WearTab) -> Unit,
  onStationSelected: (Station) -> Unit,
  onOpenSurfaceStation: (String) -> Unit,
  onRefresh: () -> Unit,
  onStartMonitoringFavorite: (String) -> Unit,
  onRouteToSavedPlace: (String) -> Unit,
  onStopMonitoring: () -> Unit,
) {
  val nearbyStations = stations.take(8)
  val favoriteStations = sortWearFavoriteStations(
    stations = stations.filter { it.id in favoriteIds },
    homeStationId = homeStationId,
    workStationId = workStationId,
  )
  val listState = rememberScalingLazyListState()
  val favoriteSurface = wearFavoriteSurfaceState(surfaceSnapshot)
  val monitoringSurface = activeMonitoring?.let { wearMonitoringSurfaceState(it, it.remainingSeconds()) }
  val savedPlaceSurfaces = wearSavedPlaceSurfaceStates(
    stations = stations,
    homeStationId = homeStationId,
    workStationId = workStationId,
  )

  ScalingLazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = listState,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    monitoringSurface?.let { monitoring ->
      item {
        WearMonitoringSurfaceCard(
          state = monitoring,
          onClick = { onOpenSurfaceStation(monitoring.stationId) },
        )
      }
      monitoring.alternativeText?.let {
        activeMonitoring.alternativeStationId?.let { alternativeStationId ->
          item {
            Button(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
              onClick = { onOpenSurfaceStation(alternativeStationId) },
              colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
            ) {
              Text("Abrir alternativa", style = MaterialTheme.typography.labelSmall, color = WearNeutral)
            }
          }
        }
      }
      item {
        Button(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onStopMonitoring,
          colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
        ) {
          Text("Detener monitorización", style = MaterialTheme.typography.labelSmall, color = WearNeutral)
        }
      }
    }

    item {
      WearFavoriteSurfaceCard(
        state = favoriteSurface,
        onClick = { favoriteSurface.stationId?.let(onOpenSurfaceStation) },
      )
    }

    if (favoriteSurface.kind == WearFavoriteSurfaceKind.Favorite &&
      activeMonitoring == null &&
      favoriteSurface.stationId != null
    ) {
      item {
        Button(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = { onStartMonitoringFavorite(favoriteSurface.stationId) },
          colors = ButtonDefaults.buttonColors(containerColor = WearPrimary),
        ) {
          Text("Monitorizar favorita", style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
      }
    }

    if (savedPlaceSurfaces.isNotEmpty()) {
      item {
        Text(
          text = "Trayectos",
          style = MaterialTheme.typography.labelSmall,
          color = WearPrimary,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(top = 4.dp),
        )
      }
      items(savedPlaceSurfaces, key = { it.stationId }) { savedPlace ->
        WearSavedPlaceSurfaceCard(
          state = savedPlace,
          onClick = { onRouteToSavedPlace(savedPlace.stationId) },
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearTab.entries.forEach { tab ->
          val isSelected = currentTab == tab
          Button(
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(tab) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isSelected) WearPrimary else WearSurface,
            ),
          ) {
            Text(
              text = tab.name,
              style = MaterialTheme.typography.labelSmall,
              color = WearOnSurface,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
          }
        }
      }
    }

    val displayedStations = when (currentTab) {
      WearTab.Cercanas -> nearbyStations
      WearTab.Favoritas -> favoriteStations
    }

    if (displayedStations.isEmpty()) {
      item {
        Text(
          text = when (currentTab) {
            WearTab.Cercanas -> "No hay estaciones cercanas."
            WearTab.Favoritas -> "No tienes favoritas aún."
          },
          color = WearNeutral,
          textAlign = TextAlign.Center,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(16.dp),
        )
      }
    } else {
      items(displayedStations, key = { it.id }) { station ->
        WearStationRow(
          station = station,
          isFavorite = station.id in favoriteIds,
          savedPlaceLabel = wearSavedPlaceLabel(station.id, homeStationId, workStationId),
          onClick = { onStationSelected(station) },
        )
      }
    }

    item {
      Spacer(Modifier.height(4.dp))
      Button(
        onClick = onRefresh,
        colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
      ) {
        Text("↻  Actualizar", style = MaterialTheme.typography.labelSmall, color = WearNeutral)
      }
    }
  }
}

@Composable
private fun WearAvailabilityDot(available: Int) {
  val color = when {
    available == 0 -> WearError
    available <= 2 -> Color(0xFFF28000)
    else -> WearSecondary
  }
  Box(
    modifier = Modifier
      .size(8.dp)
      .clip(CircleShape)
      .background(color),
  )
}

@Composable
private fun WearStationRow(
  station: Station,
  isFavorite: Boolean,
  savedPlaceLabel: String?,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = if (isFavorite) "★ ${station.name}" else station.name,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = WearOnSurface,
      )
      Spacer(Modifier.height(4.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        WearAvailabilityDot(available = station.bikesAvailable)
        Text(
          text = station.surfaceStatusTextShort(),
          style = MaterialTheme.typography.labelSmall,
          color = wearStatusColor(station.surfaceStatusLevel()),
          fontWeight = FontWeight.SemiBold,
        )
        savedPlaceLabel?.let { label ->
          WearChip(label = label, color = WearNeutral)
        }
      }
      Spacer(Modifier.height(4.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        WearChip(label = formatDistance(station.distanceMeters), color = WearNeutral)
        WearChip(label = "🚲 ${station.bikesAvailable}", color = WearPrimary)
        WearChip(label = "🅿 ${station.slotsFree}", color = WearSecondary)
      }
    }
  }
}

@Composable
private fun WearChip(label: String, color: Color) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(color.copy(alpha = 0.18f))
      .padding(horizontal = 6.dp, vertical = 2.dp),
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = color,
      fontWeight = FontWeight.Medium,
    )
  }
}

@Composable
private fun WearStatBadge(label: String, value: Int, positiveColor: Color) {
  val valueColor = if (value > 0) positiveColor else WearError
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(10.dp))
      .background(valueColor.copy(alpha = 0.15f))
      .padding(horizontal = 10.dp, vertical = 6.dp),
  ) {
    Text(
      text = "$value",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = valueColor,
    )
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = WearNeutral,
    )
  }
}

@Composable
private fun WearFavoriteSurfaceCard(
  state: WearFavoriteSurfaceState,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = "Favorita",
        style = MaterialTheme.typography.labelSmall,
        color = WearPrimary,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = WearOnSurface,
      )
      Spacer(Modifier.height(4.dp))
      when (state.kind) {
        WearFavoriteSurfaceKind.Favorite -> {
          state.statusText?.let { statusText ->
            Text(
              text = statusText,
              style = MaterialTheme.typography.labelSmall,
              color = wearStatusColor(state.statusLevel ?: SurfaceStatusLevel.Unavailable),
              fontWeight = FontWeight.SemiBold,
            )
          }
          state.updatedText?.let { updatedText ->
            Text(
              text = updatedText,
              style = MaterialTheme.typography.labelSmall,
              color = WearNeutral,
            )
          }
          Spacer(Modifier.height(4.dp))
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            state.bikesLabel?.let { WearChip(label = it, color = WearPrimary) }
            state.docksLabel?.let { WearChip(label = it, color = WearSecondary) }
          }
        }

        else -> {
          Text(
            text = state.supportingText,
            style = MaterialTheme.typography.bodySmall,
            color = WearNeutral,
          )
        }
      }
    }
  }
}

@Composable
private fun WearMonitoringSurfaceCard(
  state: WearMonitoringSurfaceState,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = "Monitorización",
        style = MaterialTheme.typography.labelSmall,
        color = WearPrimary,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = WearOnSurface,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.statusText,
        style = MaterialTheme.typography.labelSmall,
        color = wearStatusColor(state.statusLevel),
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = state.countdownText,
        style = MaterialTheme.typography.labelSmall,
        color = WearNeutral,
      )
      Spacer(Modifier.height(4.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        WearChip(label = state.bikesLabel, color = WearPrimary)
        WearChip(label = state.docksLabel, color = WearSecondary)
      }
      state.alternativeText?.let { alternativeText ->
        Spacer(Modifier.height(4.dp))
        Text(
          text = alternativeText,
          style = MaterialTheme.typography.labelSmall,
          color = WearNeutral,
        )
      }
    }
  }
}

@Composable
private fun WearSavedPlaceSurfaceCard(
  state: WearSavedPlaceSurfaceState,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        WearChip(label = state.label, color = WearNeutral)
        Text(
          text = "Ruta rápida",
          style = MaterialTheme.typography.labelSmall,
          color = WearPrimary,
          fontWeight = FontWeight.SemiBold,
        )
      }
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = WearOnSurface,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.statusText,
        style = MaterialTheme.typography.labelSmall,
        color = wearStatusColor(state.statusLevel),
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(4.dp))
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        WearChip(label = state.bikesLabel, color = WearPrimary)
        WearChip(label = state.docksLabel, color = WearSecondary)
      }
    }
  }
}

@Composable
private fun WearStationDetail(
  station: Station,
  isFavorite: Boolean,
  savedPlaceLabel: String?,
  currentMonitoring: SurfaceMonitoringSession?,
  onBack: () -> Unit,
  onToggleFavorite: () -> Unit,
  onToggleMonitoring: () -> Unit,
  onRoute: () -> Unit,
) {
  val listState = rememberScalingLazyListState()
  val isMonitoringThisStation = currentMonitoring?.stationId == station.id && currentMonitoring.isActive
  val monitoringActionLabel = when {
    isMonitoringThisStation -> "Detener monitorización"
    currentMonitoring?.isActive == true -> "Monitorizar aquí"
    else -> "Monitorizar huecos"
  }
  ScalingLazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = listState,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    item {
      Text(
        text = station.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = WearOnSurface,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      )
    }
    item {
      Text(
        text = station.address,
        style = MaterialTheme.typography.bodySmall,
        color = WearNeutral,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
      )
    }
    savedPlaceLabel?.let { label ->
      item {
        Text(
          text = label,
          style = MaterialTheme.typography.labelSmall,
          color = WearNeutral,
        )
      }
    }
    item {
      Text(
        text = station.surfaceStatusTextShort(),
        style = MaterialTheme.typography.bodySmall,
        color = wearStatusColor(station.surfaceStatusLevel()),
      )
    }
    item {
      Text(
        text = formatDistance(station.distanceMeters),
        style = MaterialTheme.typography.bodySmall,
        color = WearNeutral,
      )
    }
    item {
      Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 4.dp),
      ) {
        WearStatBadge(
          label = "Bicis",
          value = station.bikesAvailable,
          positiveColor = WearPrimary,
        )
        WearStatBadge(
          label = "Huecos",
          value = station.slotsFree,
          positiveColor = WearSecondary,
        )
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onRoute,
        colors = ButtonDefaults.buttonColors(containerColor = WearPrimary),
      ) {
        Text("Abrir ruta", style = MaterialTheme.typography.labelSmall, color = Color.White)
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onToggleMonitoring,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isMonitoringThisStation) WearSurface else WearPrimary,
        ),
      ) {
        Text(
          text = monitoringActionLabel,
          style = MaterialTheme.typography.labelSmall,
          color = if (isMonitoringThisStation) WearNeutral else Color.White,
        )
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onToggleFavorite,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isFavorite) WearSurface else WearSecondary.copy(alpha = 0.25f),
        ),
      ) {
        Text(
          text = if (isFavorite) "★ Quitar favorita" else "☆ Añadir favorita",
          style = MaterialTheme.typography.labelSmall,
          color = if (isFavorite) WearNeutral else WearSecondary,
        )
      }
    }
    item {
      Button(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        onClick = onBack,
        colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
      ) {
        Text("← Volver", style = MaterialTheme.typography.labelSmall, color = WearNeutral)
      }
    }
  }
}

private fun wearStatusColor(level: SurfaceStatusLevel): Color = when (level) {
  SurfaceStatusLevel.Good -> WearSecondary
  SurfaceStatusLevel.Low -> Color(0xFFF28000)
  SurfaceStatusLevel.Empty,
  SurfaceStatusLevel.Full,
  SurfaceStatusLevel.Unavailable -> WearError
}

@Composable
private fun WearErrorScreen(
  message: String,
  onRetry: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
      text = message,
      color = WearError,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))
    Button(
      onClick = onRetry,
      colors = ButtonDefaults.buttonColors(containerColor = WearPrimary),
    ) {
      Text("Reintentar", color = Color.White)
    }
  }
}

private suspend fun refreshWearSurface(
  context: Context,
  stationsRepository: StationsRepository,
  favoritesRepository: FavoritesRepository,
  surfaceSnapshotRepository: SurfaceSnapshotRepository,
  forceRefresh: Boolean = false,
) {
  favoritesRepository.syncFromPeer()
  if (forceRefresh) {
    stationsRepository.forceRefresh()
  } else {
    stationsRepository.loadIfNeeded()
    val stationIds = stationsRepository.state.value.stations.take(10).map { it.id }
    if (stationIds.isNotEmpty()) {
      stationsRepository.refreshAvailability(stationIds)
    }
  }
  surfaceSnapshotRepository.refreshSnapshot()
  FavoriteStationTileService.requestUpdate(context)
}
