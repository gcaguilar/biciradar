package com.gcaguilar.biciradar.wear

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.lazy.AutoCenteringParams
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.Card
import androidx.wear.compose.material3.ChildButton
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.OutlinedButton
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

private const val WEAR_LARGE_FONT_SCALE = 1.2f

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
      // Ver comentario homólogo en BiziMobileApp: `graph = null` construye un grafo nuevo
      // y duplica los @SingleIn. En producción SIEMPRE debe llegar `WearAppGraph.graph`
      // desde `WearActivity`; sólo previews aisladas deberían caer aquí.
      graph ?: run {
        platformBindings.logger.warn(
          "WearRoot",
          "WearRoot invocado con graph=null. Se está creando un nuevo CoreGraph — duplica " +
            "los @SingleIn(AppScope::class). Debe llegar WearAppGraph.graph desde WearActivity.",
        )
        CoreGraph.Companion.create(platformBindings)
      }
    }
  val factory =
    remember(resolvedGraph, context) {
      WearViewModelFactory(
        appContext = context,
        graph = resolvedGraph,
        watchMonitoringBridge = WearAppGraph.platformBindings.watchMonitoringBridge,
      )
    }
  val wearText = rememberWearText()
  val viewModel: WearViewModel = viewModel(key = "wear-root") { factory.create() }
  val uiState by viewModel.uiState.collectAsState()
  val activeMonitoring = uiState.activeMonitoring

  LaunchedEffect(refreshKey) {
    viewModel.onPermissionRefresh(refreshKey)
  }

  LaunchedEffect(launchStationNonce) {
    viewModel.onLaunchStationRequested(launchStationId)
  }

  LaunchedEffect(uiState.feedbackNonce) {
    val message = uiState.feedbackMessage ?: return@LaunchedEffect
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    viewModel.onFeedbackConsumed()
  }

  // Refresco periódico sólo con la pantalla en primer plano: evita red y CPU con la
  // pantalla apagada (batería en Wear). Al pasar a STOPPED `repeatOnLifecycle` cancela
  // el bucle y lo relanza al volver a RESUMED.
  val lifecycleOwner = LocalLifecycleOwner.current
  LaunchedEffect(lifecycleOwner, viewModel) {
    lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
      viewModel.runPeriodicRefresh()
    }
  }

  // Swipe-to-dismiss / system back from the station detail returns to the list instead of
  // closing the app.
  BackHandler(enabled = screenshotSurface == null && uiState.selectedStationId != null) {
    viewModel.onBackFromStationDetail()
  }

  BiciRadarWearTheme {
    Box(
      modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
      contentAlignment = Alignment.Center,
    ) {
      val selectedStation =
        uiState.selectedStationId?.let { stationId ->
          uiState.stations.firstOrNull { it.id == stationId }
        }
      val errorMessage = uiState.errorMessage

      when {
        screenshotSurface != null -> {
          WearScreenshotRoot(screenshotSurface = screenshotSurface)
        }

        selectedStation != null -> {
          WearStationDetail(
            station = selectedStation,
            isFavorite = selectedStation.id in uiState.favoriteIds,
            savedPlaceLabel =
              wearSavedPlaceLabel(wearText, selectedStation.id, uiState.homeStationId, uiState.workStationId),
            currentMonitoring = activeMonitoring,
            onBack = viewModel::onBackFromStationDetail,
            onToggleFavorite = { viewModel.onToggleFavorite(selectedStation.id) },
            onToggleMonitoring = { viewModel.onToggleMonitoring(selectedStation.id) },
            onRoute = uiState.canRouteOnWatch.takeIf { it }?.let { { viewModel.onRoute(selectedStation.id) } },
            onRouteInPhone =
              uiState.canRouteInPhone.takeIf { it }?.let {
                {
                  viewModel.onRouteInPhone(
                    selectedStation.id,
                  )
                }
              },
          )
        }

        uiState.isLoading -> {
          CircularProgressIndicator()
        }

        errorMessage != null -> {
          WearErrorScreen(
            message = errorMessage,
            onRetry = viewModel::onRetry,
          )
        }

        else -> {
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
}

@Composable
private fun WearScreenshotRoot(screenshotSurface: WearScreenshotSurface) {
  when (screenshotSurface) {
    WearScreenshotSurface.Dashboard -> {
      WearScreenshotDashboard()
    }

    WearScreenshotSurface.StationDetail -> {
      WearStationDetail(
        station = sampleWearStation,
        isFavorite = true,
        savedPlaceLabel = stringResource(R.string.wear_place_home),
        currentMonitoring = null,
        onBack = {},
        onToggleFavorite = {},
        onToggleMonitoring = {},
        onRoute = {},
        onRouteInPhone = {},
      )
    }

    WearScreenshotSurface.Monitoring -> {
      WearScreenshotMonitoring()
    }
  }
}

@Composable
private fun WearScreenshotDashboard() {
  val wearText = rememberWearText()
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
      color = WearPrimaryText,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    WearFavoriteSurfaceCard(
      state = wearFavoriteSurfaceState(wearText, sampleWearSurfaceBundle),
      onClick = {},
    )
    WearSavedPlaceSurfaceCard(
      state = sampleWearSavedPlaceSurface(wearText),
      hasLargeFont = false,
      onClick = {},
    )
  }
}

@Composable
private fun WearScreenshotMonitoring() {
  val wearText = rememberWearText()
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(horizontal = 20.dp, vertical = 28.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
  ) {
    Text(
      text = stringResource(R.string.wear_label_monitoring),
      style = MaterialTheme.typography.titleSmall,
      color = WearPrimaryText,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.align(Alignment.CenterHorizontally),
    )
    WearMonitoringSurfaceCard(
      state =
        wearMonitoringSurfaceState(
          wearText,
          sampleWearMonitoringSession,
          sampleWearMonitoringSession.remainingSeconds(),
        ),
      onClick = {},
    )
    FilledTonalButton(
      modifier = Modifier.fillMaxWidth(),
      onClick = {},
    ) {
      WearButtonLabel(text = stringResource(R.string.wear_action_open_alternative))
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
  val hasLargeFont = LocalDensity.current.fontScale >= WEAR_LARGE_FONT_SCALE
  val wearText = rememberWearText()
  val nearbyStations = stations.take(8)
  val favoriteStations =
    sortWearFavoriteStations(
      stations = stations.filter { it.id in favoriteIds },
      homeStationId = homeStationId,
      workStationId = workStationId,
    )
  val listState = rememberScalingLazyListState()
  val favoriteSurface = wearFavoriteSurfaceState(wearText, surfaceSnapshot)
  val monitoringSurface = activeMonitoring?.let { wearMonitoringSurfaceState(wearText, it, it.remainingSeconds()) }
  val savedPlaceSurfaces =
    wearSavedPlaceSurfaceStates(
      text = wearText,
      stations = stations,
      homeStationId = homeStationId,
      workStationId = workStationId,
    )

  ScreenScaffold(
    scrollState = listState,
    edgeButton = {
      EdgeButton(
        onClick = onRefresh,
        colors = ButtonDefaults.filledTonalButtonColors(),
      ) {
        WearButtonLabel(text = stringResource(R.string.wear_action_refresh))
      }
    },
  ) { scaffoldPadding ->
    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      // autoCentering = null is required for the spacing above the edge button.
      autoCentering = null,
      contentPadding =
        PaddingValues(
          start = 16.dp,
          end = 16.dp,
          top = 32.dp,
          bottom = scaffoldPadding.calculateBottomPadding(),
        ),
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
              FilledTonalButton(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                onClick = { onOpenSurfaceStation(alternativeStationId) },
              ) {
                WearButtonLabel(text = stringResource(R.string.wear_action_open_alternative))
              }
            }
          }
        }
        item {
          FilledTonalButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            onClick = onStopMonitoring,
          ) {
            WearButtonLabel(text = stringResource(R.string.wear_action_stop_monitoring))
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
          ) {
            WearButtonLabel(text = stringResource(R.string.wear_action_monitor_favorite))
          }
        }
      }

      if (savedPlaceSurfaces.isNotEmpty()) {
        item {
          ListHeader {
            Text(text = stringResource(R.string.wear_section_trips))
          }
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
            val tabModifier =
              (if (hasLargeFont) Modifier.fillMaxWidth() else Modifier.weight(1f))
                .semantics { selected = isSelected }
            val tabLabel: @Composable RowScope.() -> Unit = {
              WearButtonLabel(
                text = stringResource(tab.labelRes),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              )
            }
            if (isSelected) {
              Button(modifier = tabModifier, onClick = { onTabSelected(tab) }, content = tabLabel)
            } else {
              FilledTonalButton(modifier = tabModifier, onClick = { onTabSelected(tab) }, content = tabLabel)
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
                WearTab.Cercanas -> stringResource(R.string.wear_empty_nearby)
                WearTab.Favoritas -> stringResource(R.string.wear_empty_favorites)
              },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            savedPlaceLabel = wearSavedPlaceLabel(wearText, station.id, homeStationId, workStationId),
            onClick = { onStationSelected(station) },
          )
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
      available <= 2 -> WearWarning
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
  val wearText = rememberWearText()
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
        color = MaterialTheme.colorScheme.onSurface,
      )
      Spacer(Modifier.height(4.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearAvailabilityDot(available = station.bikesAvailable)
        Text(
          text = wearText.statusLabel(station),
          style = MaterialTheme.typography.labelSmall,
          color = wearStatusColor(station.surfaceStatusLevel()),
          fontWeight = FontWeight.SemiBold,
        )
        savedPlaceLabel?.let { label ->
          WearChip(label = label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
      Spacer(Modifier.height(4.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearChip(label = formatDistance(station.distanceMeters), color = MaterialTheme.colorScheme.onSurfaceVariant)
        WearBikesChip(count = station.bikesAvailable)
        WearDocksChip(count = station.slotsFree)
      }
    }
  }
}

@Composable
private fun WearChip(
  label: String,
  color: Color,
  @DrawableRes iconRes: Int? = null,
  iconDescription: String? = null,
) {
  // Brand colors on their own 18% tint are only ~3.5:1; lighten the text, not the tint.
  val contentColor = lerp(color, Color.White, 0.35f)
  Row(
    modifier =
      Modifier
        .clip(RoundedCornerShape(8.dp))
        .background(color.copy(alpha = 0.18f))
        .padding(horizontal = 6.dp, vertical = 2.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(3.dp),
  ) {
    iconRes?.let {
      Icon(
        painter = painterResource(it),
        contentDescription = iconDescription,
        tint = contentColor,
        modifier = Modifier.size(12.dp),
      )
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = contentColor,
      fontWeight = FontWeight.Medium,
    )
  }
}

/** Bike count chip: vector icon instead of an emoji, which renders differently per watch. */
@Composable
private fun WearBikesChip(count: Int) {
  WearChip(
    label = "$count",
    color = WearPrimary,
    iconRes = R.drawable.ic_bike,
    iconDescription = stringResource(R.string.wear_stat_bikes),
  )
}

@Composable
private fun WearDocksChip(count: Int) {
  WearChip(
    label = "$count",
    color = WearSecondary,
    iconRes = R.drawable.ic_parking,
    iconDescription = stringResource(R.string.wear_stat_docks),
  )
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
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

@Composable
private fun WearFavoriteSurfaceCard(
  state: WearFavoriteSurfaceState,
  onClick: () -> Unit,
) {
  val hasLargeFont = LocalDensity.current.fontScale >= WEAR_LARGE_FONT_SCALE
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = stringResource(R.string.wear_label_favorite),
        style = MaterialTheme.typography.labelSmall,
        color = WearPrimaryText,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = if (hasLargeFont) 3 else 2,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface,
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
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(Modifier.height(4.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            WearBikesChip(count = state.bikesAvailable)
            WearDocksChip(count = state.docksAvailable)
          }
        }

        else -> {
          Text(
            text = state.supportingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
  val hasLargeFont = LocalDensity.current.fontScale >= WEAR_LARGE_FONT_SCALE
  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
    onClick = onClick,
  ) {
    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
      Text(
        text = stringResource(R.string.wear_label_monitoring),
        style = MaterialTheme.typography.labelSmall,
        color = WearPrimaryText,
        fontWeight = FontWeight.SemiBold,
      )
      Spacer(Modifier.height(4.dp))
      Text(
        text = state.title,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = if (hasLargeFont) 3 else 2,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface,
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
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.height(4.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        WearBikesChip(count = state.bikesAvailable)
        WearDocksChip(count = state.docksAvailable)
      }
      state.alternativeText?.let { alternativeText ->
        Spacer(Modifier.height(4.dp))
        Text(
          text = alternativeText,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        WearChip(label = state.label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
          text = stringResource(R.string.wear_quick_route),
          style = MaterialTheme.typography.labelSmall,
          color = WearPrimaryText,
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
        color = MaterialTheme.colorScheme.onSurface,
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
        WearBikesChip(count = state.bikesAvailable)
        WearDocksChip(count = state.docksAvailable)
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
  onRoute: (() -> Unit)?,
  onRouteInPhone: (() -> Unit)?,
) {
  val hasLargeFont = LocalDensity.current.fontScale >= WEAR_LARGE_FONT_SCALE
  val wearText = rememberWearText()
  val listState = rememberScalingLazyListState()
  val isMonitoringThisStation = currentMonitoring?.stationId == station.id && currentMonitoring.isActive
  val monitoringActionLabel =
    when {
      isMonitoringThisStation -> stringResource(R.string.wear_action_stop_monitoring)
      currentMonitoring?.isActive == true -> stringResource(R.string.wear_action_monitor_here)
      else -> stringResource(R.string.wear_action_monitor_docks)
    }
  // One primary action per screen goes in the edge button: route on the watch if it can,
  // otherwise route on the phone.
  val primaryAction: (() -> Unit)? = onRoute ?: onRouteInPhone
  val primaryLabelRes =
    if (onRoute != null) R.string.wear_action_open_route else R.string.wear_action_route_on_phone
  val phoneAction: (() -> Unit)? = if (onRoute != null) onRouteInPhone else null
  val detailContent: @Composable BoxScope.(PaddingValues) -> Unit = { scaffoldPadding ->
    ScalingLazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = listState,
      autoCentering = if (primaryAction != null) null else AutoCenteringParams(),
      contentPadding =
        PaddingValues(
          start = 16.dp,
          end = 16.dp,
          top = 32.dp,
          bottom = if (primaryAction != null) scaffoldPadding.calculateBottomPadding() else 32.dp,
        ),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      item {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = if (hasLargeFont) 4 else 3,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        )
      }
      item {
        Text(
          text = station.address,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      item {
        Text(
          text = wearText.statusLabel(station),
          style = MaterialTheme.typography.bodySmall,
          color = wearStatusColor(station.surfaceStatusLevel()),
        )
      }
      item {
        Text(
          text = formatDistance(station.distanceMeters),
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      item {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.padding(vertical = 4.dp),
        ) {
          WearStatBadge(
            label = stringResource(R.string.wear_stat_bikes),
            value = station.bikesAvailable,
            positiveColor = WearPrimary,
          )
          WearStatBadge(
            label = stringResource(R.string.wear_stat_docks),
            value = station.slotsFree,
            positiveColor = WearSecondary,
          )
        }
      }
      phoneAction?.let { routeInPhoneAction ->
        item {
          FilledTonalButton(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            onClick = routeInPhoneAction,
          ) {
            WearButtonLabel(text = stringResource(R.string.wear_action_route_on_phone))
          }
        }
      }
      item {
        FilledTonalButton(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onToggleMonitoring,
        ) {
          WearButtonLabel(text = monitoringActionLabel)
        }
      }
      item {
        OutlinedButton(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onToggleFavorite,
        ) {
          WearButtonLabel(
            text =
              stringResource(
                if (isFavorite) R.string.wear_action_remove_favorite else R.string.wear_action_add_favorite,
              ),
          )
        }
      }
      item {
        ChildButton(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
          onClick = onBack,
        ) {
          WearButtonLabel(text = stringResource(R.string.wear_action_back))
        }
      }
    }
  }
  if (primaryAction != null) {
    ScreenScaffold(
      scrollState = listState,
      edgeButton = {
        EdgeButton(onClick = primaryAction) {
          WearButtonLabel(text = stringResource(primaryLabelRes))
        }
      },
      content = detailContent,
    )
  } else {
    ScreenScaffold(scrollState = listState, content = detailContent)
  }
}

@Composable
private fun WearButtonLabel(
  text: String,
  fontWeight: FontWeight = FontWeight.Medium,
) {
  // No explicit color: the enclosing button provides the right content color.
  Text(
    text = text,
    style = MaterialTheme.typography.labelSmall,
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

    SurfaceStatusLevel.Low -> WearWarning

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
      color = MaterialTheme.colorScheme.error,
      textAlign = TextAlign.Center,
      style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(8.dp))
    Button(onClick = onRetry) {
      WearButtonLabel(text = stringResource(R.string.wear_action_retry))
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

private fun sampleWearSavedPlaceSurface(text: WearText) =
  WearSavedPlaceSurfaceState(
    stationId = sampleWearWorkStation.id,
    label = text.get(R.string.wear_place_work),
    title = sampleWearWorkStation.name,
    statusText = text.statusLabel(SurfaceStatusLevel.Good),
    statusLevel = SurfaceStatusLevel.Good,
    bikesAvailable = sampleWearWorkStation.bikesAvailable,
    docksAvailable = sampleWearWorkStation.slotsFree,
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

@Composable
internal fun rememberWearText(): WearText {
  val context = LocalContext.current
  return remember(context) { ContextWearText(context) }
}
