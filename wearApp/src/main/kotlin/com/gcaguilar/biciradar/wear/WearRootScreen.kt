package com.gcaguilar.biciradar.wear

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceSnapshotBundle
import com.gcaguilar.biciradar.core.SurfaceState
import com.gcaguilar.biciradar.core.SurfaceStationSnapshot
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.di.CoreGraph
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.core.remainingSeconds
import com.gcaguilar.biciradar.core.surfaceStatusLevel
import com.gcaguilar.biciradar.core.surfaceStatusTextShort

private val WearPrimary = Color(0xFF1D74BD)
private val WearSecondary = Color(0xFF64C23A)
private val WearNeutral = Color(0xFF64779D)
private val WearError = Color(0xFFCF6679)
private val WearSurface = Color(0xFF1A1A2E)
private val WearOnSurface = Color(0xFFE8EDF4)
private const val WearLargeFontScale = 1.2f

@Composable
internal fun WearRoot(
  platformBindings: PlatformBindings,
  graph: SharedGraph? = null,
  refreshKey: Int,
  launchStationId: String?,
  launchStationNonce: Int,
  screenshotSurface: WearScreenshotSurface?,
) {
  val context = LocalContext.current.applicationContext
  val resolvedGraph =
    remember(platformBindings, graph) {
      graph ?: CoreGraph.Companion.create(platformBindings)
    }
  val factory = remember(resolvedGraph, context) { WearViewModelFactory(appContext = context, graph = resolvedGraph) }
  val viewModel: WearViewModel = viewModel(key = "wear-root") { factory.create() }
  val uiState by viewModel.uiState.collectAsState()
  val activeMonitoring = uiState.activeMonitoring

  LaunchedEffect(refreshKey) {
    viewModel.onPermissionRefresh(refreshKey)
  }

  LaunchedEffect(launchStationNonce) {
    viewModel.onLaunchStationRequested(launchStationId)
  }

  MaterialTheme {
    Box(
      modifier = Modifier.fillMaxSize().background(Color.Black),
      contentAlignment = Alignment.Center,
    ) {
      val selectedStation =
        uiState.selectedStationId?.let { stationId ->
          uiState.stations.firstOrNull { it.id == stationId }
        }
      val errorMessage = uiState.errorMessage

      when {
        screenshotSurface != null -> WearScreenshotRoot(screenshotSurface = screenshotSurface)
        selectedStation != null ->
          WearStationDetail(
            station = selectedStation,
            isFavorite = selectedStation.id in uiState.favoriteIds,
            savedPlaceLabel = wearSavedPlaceLabel(selectedStation.id, uiState.homeStationId, uiState.workStationId),
            currentMonitoring = activeMonitoring,
            onBack = viewModel::onBackFromStationDetail,
            onToggleFavorite = { viewModel.onToggleFavorite(selectedStation.id) },
            onToggleMonitoring = { viewModel.onToggleMonitoring(selectedStation.id) },
            onRoute = { viewModel.onRoute(selectedStation.id) },
            onRouteInPhone = { viewModel.onRouteInPhone(selectedStation.id) },
          )

        uiState.isLoading ->
          CircularProgressIndicator(
            colors = ProgressIndicatorDefaults.colors(indicatorColor = WearPrimary),
          )

        errorMessage != null ->
          WearErrorScreen(
            message = errorMessage,
            onRetry = viewModel::onRetry,
          )

        else ->
          WearDashboard(
            stations = uiState.stations,
            favoriteIds = uiState.favoriteIds,
            homeStationId = uiState.homeStationId,
            workStationId = uiState.workStationId,
            surfaceSnapshot = uiState.surfaceBundle,
            activeMonitoring = activeMonitoring,
            currentTab = uiState.currentTab,
            onTabSelected = viewModel::onTabSelected,
            onStationSelected = { viewModel.onStationSelected(it.id) },
            onOpenSurfaceStation = viewModel::onStationSelected,
            onRefresh = viewModel::onRefresh,
            onStartMonitoringFavorite = viewModel::onStartMonitoringFavorite,
            onRouteToSavedPlace = viewModel::onRoute,
            onStopMonitoring = viewModel::onStopMonitoring,
          )
      }
    }
  }
}

@Composable
private fun WearScreenshotRoot(screenshotSurface: WearScreenshotSurface) {
  when (screenshotSurface) {
    WearScreenshotSurface.Dashboard -> WearScreenshotDashboard()

    WearScreenshotSurface.StationDetail ->
      WearStationDetail(
        station = sampleWearStation,
        isFavorite = true,
        savedPlaceLabel = "Casa",
        currentMonitoring = null,
        onBack = {},
        onToggleFavorite = {},
        onToggleMonitoring = {},
        onRoute = {},
        onRouteInPhone = {},
      )

    WearScreenshotSurface.Monitoring -> WearScreenshotMonitoring()
  }
}

@Composable
private fun WearScreenshotDashboard() {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 28.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
  ) {
    Text(
      text = "BiciRadar",
      style = MaterialTheme.typography.titleSmall,
      color = WearPrimary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    WearFavoriteSurfaceCard(
      state = wearFavoriteSurfaceState(sampleWearSurfaceBundle),
      onClick = {},
    )
    WearSavedPlaceSurfaceCard(
      state = sampleWearSavedPlaceSurface,
      hasLargeFont = false,
      onClick = {},
    )
  }
}

@Composable
private fun WearScreenshotMonitoring() {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 28.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
  ) {
    Text(
      text = "Monitorización",
      style = MaterialTheme.typography.titleSmall,
      color = WearPrimary,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    WearMonitoringSurfaceCard(
      state = wearMonitoringSurfaceState(sampleWearMonitoringSession, sampleWearMonitoringSession.remainingSeconds()),
      onClick = {},
    )
    Button(
      modifier = Modifier.fillMaxWidth(),
      onClick = {},
      colors = ButtonDefaults.buttonColors(containerColor = WearSurface),
    ) {
      WearButtonLabel(text = "Abrir alternativa", color = WearNeutral)
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
  val hasLargeFont = LocalDensity.current.fontScale >= WearLargeFontScale
  val nearbyStations = stations.take(8)
  val favoriteStations =
    sortWearFavoriteStations(
      stations = stations.filter { it.id in favoriteIds },
      homeStationId = homeStationId,
      workStationId = workStationId,
    )
  val listState = rememberScalingLazyListState()
  val favoriteSurface = wearFavoriteSurfaceState(surfaceSnapshot)
  val monitoringSurface = activeMonitoring?.let { wearMonitoringSurfaceState(it, it.remainingSeconds()) }
  val savedPlaceSurfaces =
    wearSavedPlaceSurfaceStates(
      stations = stations,
      homeStationId = homeStationId,
      workStationId = workStationId,
    )

  ScreenScaffold(scrollState = listState) {
    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
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
                WearButtonLabel(text = "Abrir alternativa", color = WearNeutral)
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
            WearButtonLabel(text = "Detener monitorización", color = WearNeutral)
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
            WearButtonLabel(text = "Monitorizar favorita", color = Color.White)
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
            hasLargeFont = hasLargeFont,
            onClick = { onRouteToSavedPlace(savedPlace.stationId) },
          )
        }
      }

      item {
        FlowRow(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          WearTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            Button(
              modifier = if (hasLargeFont) Modifier.fillMaxWidth() else Modifier.weight(1f),
              onClick = { onTabSelected(tab) },
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = if (isSelected) WearPrimary else WearSurface,
                ),
            ) {
              WearButtonLabel(
                text = tab.name,
                color = WearOnSurface,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              )
            }
          }
        }
      }

      val displayedStations =
        when (currentTab) {
          WearTab.Cercanas -> nearbyStations
          WearTab.Favoritas -> favoriteStations
        }

      if (displayedStations.isEmpty()) {
        item {
          Text(
            text =
              when (currentTab) {
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
            hasLargeFont = hasLargeFont,
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
          WearButtonLabel(text = "↻  Actualizar", color = WearNeutral)
        }
      }
    }
  }
}

@Composable
private fun WearAvailabilityDot(available: Int) {
  val color =
    when {
      available == 0 -> WearError
      available <= 2 -> Color(0xFFF28000)
      else -> WearSecondary
    }
  Box(
    modifier =
      Modifier
        .size(8.dp)
        .clip(CircleShape)
        .background(color),
  )
}

@Composable
private fun WearStationRow(
  station: Station,
  hasLargeFont: Boolean,
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
        maxLines = if (hasLargeFont) 3 else 2,
        overflow = TextOverflow.Ellipsis,
        color = WearOnSurface,
      )
      Spacer(Modifier.height(4.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearChip(label = formatDistance(station.distanceMeters), color = WearNeutral)
        WearChip(label = "🚲 ${station.bikesAvailable}", color = WearPrimary)
        WearChip(label = "🅿 ${station.slotsFree}", color = WearSecondary)
      }
    }
  }
}

@Composable
private fun WearChip(
  label: String,
  color: Color,
) {
  Box(
    modifier =
      Modifier
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
private fun WearStatBadge(
  label: String,
  value: Int,
  positiveColor: Color,
) {
  val valueColor = if (value > 0) positiveColor else WearError
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier =
      Modifier
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
  val hasLargeFont = LocalDensity.current.fontScale >= WearLargeFontScale
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
        maxLines = if (hasLargeFont) 3 else 2,
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
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
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
  val hasLargeFont = LocalDensity.current.fontScale >= WearLargeFontScale
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
        maxLines = if (hasLargeFont) 3 else 2,
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
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
  hasLargeFont: Boolean,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
        maxLines = if (hasLargeFont) 3 else 2,
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
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
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
  onRouteInPhone: () -> Unit,
) {
  val hasLargeFont = LocalDensity.current.fontScale >= WearLargeFontScale
  val listState = rememberScalingLazyListState()
  val isMonitoringThisStation = currentMonitoring?.stationId == station.id && currentMonitoring.isActive
  val monitoringActionLabel =
    when {
      isMonitoringThisStation -> "Detener monitorización"
      currentMonitoring?.isActive == true -> "Monitorizar aquí"
      else -> "Monitorizar huecos"
    }
  ScreenScaffold(scrollState = listState) {
    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      item {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color = WearOnSurface,
          maxLines = if (hasLargeFont) 4 else 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
      }
      item {
        Text(
          text = station.address,
          style = MaterialTheme.typography.bodySmall,
          color = WearNeutral,
          textAlign = TextAlign.Center,
          maxLines = if (hasLargeFont) 4 else 3,
          overflow = TextOverflow.Ellipsis,
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
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
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
          WearButtonLabel(text = "Abrir ruta", color = Color.White)
        }
      }
      item {
        Button(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onRouteInPhone,
          colors = ButtonDefaults.buttonColors(containerColor = WearSecondary),
        ) {
          WearButtonLabel(text = "Ruta en teléfono", color = Color.White)
        }
      }
      item {
        Button(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onToggleMonitoring,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = if (isMonitoringThisStation) WearSurface else WearPrimary,
            ),
        ) {
          WearButtonLabel(
            text = monitoringActionLabel,
            color = if (isMonitoringThisStation) WearNeutral else Color.White,
          )
        }
      }
      item {
        Button(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onToggleFavorite,
          colors =
            ButtonDefaults.buttonColors(
              containerColor = if (isFavorite) WearSurface else WearSecondary.copy(alpha = 0.25f),
            ),
        ) {
          WearButtonLabel(
            text = if (isFavorite) "★ Quitar favorita" else "☆ Añadir favorita",
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
          WearButtonLabel(text = "← Volver", color = WearNeutral)
        }
      }
    }
  }
}

@Composable
private fun WearButtonLabel(
  text: String,
  color: Color,
  fontWeight: FontWeight = FontWeight.Medium,
) {
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
    color = color,
    fontWeight = fontWeight,
    textAlign = TextAlign.Center,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.fillMaxWidth(),
  )
}

private fun wearStatusColor(level: SurfaceStatusLevel): Color =
  when (level) {
    SurfaceStatusLevel.Good -> WearSecondary
    SurfaceStatusLevel.Low -> Color(0xFFF28000)
    SurfaceStatusLevel.Empty,
    SurfaceStatusLevel.Full,
    SurfaceStatusLevel.Unavailable,
    -> WearError
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
      WearButtonLabel(text = "Reintentar", color = Color.White)
    }
  }
}

private val sampleWearStation =
  Station(
    id = "wear-screenshot-home",
    name = "19-Asalto. Servet",
    address = "C/ Asalto - C/ Miguel Servet",
    location = GeoPoint(latitude = 41.6488, longitude = -0.8891),
    bikesAvailable = 10,
    slotsFree = 12,
    distanceMeters = 48,
  )

private val sampleWearWorkStation =
  Station(
    id = "wear-screenshot-work",
    name = "96-Pomarón",
    address = "Av. Cesáreo Alierta - Camino Cabaldós",
    location = GeoPoint(latitude = 41.6401, longitude = -0.8725),
    bikesAvailable = 6,
    slotsFree = 9,
    distanceMeters = 211,
  )

private val sampleWearNearbyStation =
  Station(
    id = "wear-screenshot-nearby",
    name = "18-Plaza España",
    address = "Plaza España",
    location = GeoPoint(latitude = 41.6514, longitude = -0.8808),
    bikesAvailable = 4,
    slotsFree = 15,
    distanceMeters = 132,
  )

private val sampleWearStations =
  listOf(
    sampleWearStation,
    sampleWearNearbyStation,
    sampleWearWorkStation,
  )

private val sampleWearSavedPlaceSurface =
  WearSavedPlaceSurfaceState(
    stationId = sampleWearWorkStation.id,
    label = "Trabajo",
    title = sampleWearWorkStation.name,
    statusText = "Disponible",
    statusLevel = SurfaceStatusLevel.Good,
    bikesLabel = "🚲 ${sampleWearWorkStation.bikesAvailable}",
    docksLabel = "🅿 ${sampleWearWorkStation.slotsFree}",
  )

private val sampleWearSurfaceBundle =
  SurfaceSnapshotBundle(
    generatedAtEpoch = System.currentTimeMillis(),
    favoriteStation =
      SurfaceStationSnapshot(
        id = sampleWearStation.id,
        nameShort = "19-Asalto. Servet",
        nameFull = sampleWearStation.name,
        cityId = "zaragoza",
        latitude = sampleWearStation.location.latitude,
        longitude = sampleWearStation.location.longitude,
        bikesAvailable = sampleWearStation.bikesAvailable,
        docksAvailable = sampleWearStation.slotsFree,
        statusTextShort = "Disponible",
        statusLevel = SurfaceStatusLevel.Good,
        lastUpdatedEpoch = System.currentTimeMillis(),
        distanceMeters = sampleWearStation.distanceMeters,
        isFavorite = true,
        alternativeStationId = sampleWearWorkStation.id,
        alternativeStationName = sampleWearWorkStation.name,
        alternativeDistanceMeters = sampleWearWorkStation.distanceMeters,
      ),
    homeStation =
      SurfaceStationSnapshot(
        id = sampleWearStation.id,
        nameShort = "19-Asalto. Servet",
        nameFull = sampleWearStation.name,
        cityId = "zaragoza",
        latitude = sampleWearStation.location.latitude,
        longitude = sampleWearStation.location.longitude,
        bikesAvailable = sampleWearStation.bikesAvailable,
        docksAvailable = sampleWearStation.slotsFree,
        statusTextShort = "Disponible",
        statusLevel = SurfaceStatusLevel.Good,
        lastUpdatedEpoch = System.currentTimeMillis(),
        distanceMeters = sampleWearStation.distanceMeters,
      ),
    workStation =
      SurfaceStationSnapshot(
        id = sampleWearWorkStation.id,
        nameShort = "96-Pomarón",
        nameFull = sampleWearWorkStation.name,
        cityId = "zaragoza",
        latitude = sampleWearWorkStation.location.latitude,
        longitude = sampleWearWorkStation.location.longitude,
        bikesAvailable = sampleWearWorkStation.bikesAvailable,
        docksAvailable = sampleWearWorkStation.slotsFree,
        statusTextShort = "Disponible",
        statusLevel = SurfaceStatusLevel.Good,
        lastUpdatedEpoch = System.currentTimeMillis(),
        distanceMeters = sampleWearWorkStation.distanceMeters,
      ),
    nearbyStations = emptyList(),
    monitoringSession = null,
    state =
      SurfaceState(
        hasLocationPermission = true,
        hasNotificationPermission = true,
        hasFavoriteStation = true,
        isDataFresh = true,
        lastSyncEpoch = System.currentTimeMillis(),
        cityId = "zaragoza",
        cityName = "Zaragoza",
        userLatitude = 41.6488,
        userLongitude = -0.8891,
      ),
  )

private val sampleWearMonitoringSession =
  SurfaceMonitoringSession(
    stationId = sampleWearStation.id,
    stationName = sampleWearStation.name,
    cityId = "zaragoza",
    kind = SurfaceMonitoringKind.Bikes,
    status = SurfaceMonitoringStatus.AlternativeAvailable,
    bikesAvailable = sampleWearStation.bikesAvailable,
    docksAvailable = sampleWearStation.slotsFree,
    statusLevel = SurfaceStatusLevel.Good,
    startedAtEpoch = System.currentTimeMillis() - 2 * 60 * 1000L,
    expiresAtEpoch = System.currentTimeMillis() + 8 * 60 * 1000L,
    lastUpdatedEpoch = System.currentTimeMillis(),
    isActive = true,
    alternativeStationId = sampleWearWorkStation.id,
    alternativeStationName = sampleWearWorkStation.name,
    alternativeDistanceMeters = sampleWearWorkStation.distanceMeters,
  )
