package com.gcaguilar.biciradar.mobileui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobileui.components.map.EnvironmentalLayerCard
import com.gcaguilar.biciradar.mobileui.components.map.MapControls
import com.gcaguilar.biciradar.mobileui.components.map.MapFiltersPanel
import com.gcaguilar.biciradar.mobileui.components.map.MapSearchBar
import com.gcaguilar.biciradar.mobileui.components.map.StationDetailBottomSheet
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.close
import com.gcaguilar.biciradar.mobile_ui.generated.resources.details
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendGood
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendHigh
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendLow
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendMedium
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendModerate
import com.gcaguilar.biciradar.mobile_ui.generated.resources.environmentalLegendPoor
import com.gcaguilar.biciradar.mobile_ui.generated.resources.loadStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapClearEnvironmentalLayer
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapEnvironmentalLayerHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapFilterAirQuality
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapFilterPollen
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapLocationFallbackDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapMyLocation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNearestFallbackSummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNearestStationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStationsOnScreen
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStationsWithinRadius
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapSearchStationOrAddress
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapSelectedStationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapStationDistanceSummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapTryAnotherQuery
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapUpdateFailed
import com.gcaguilar.biciradar.mobile_ui.generated.resources.retry
import com.gcaguilar.biciradar.mobile_ui.generated.resources.route
import com.gcaguilar.biciradar.mobile_ui.generated.resources.save
import com.gcaguilar.biciradar.mobile_ui.generated.resources.saved
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MapScreen(
  mobilePlatform: MobileUiPlatform,
  stations: List<Station>,
  favoriteIds: Set<String>,
  loading: Boolean,
  errorMessage: String?,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  onRefreshStations: () -> Unit,
  nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
  searchQuery: String,
  searchRadiusMeters: Int,
  userLocation: GeoPoint?,
  isMapReady: Boolean,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onRetry: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  activeFilters: Set<MapFilter>,
  onToggleFilter: (MapFilter, Set<MapFilter>) -> Unit,
  onAvailableFiltersChange: (Set<MapFilter>) -> Unit,
  selectedMapStationId: String?,
  hasExplicitMapSelection: Boolean,
  isCardDismissed: Boolean,
  showEnvironmentalSheet: Boolean,
  recenterRequestToken: Int,
  onStationSelectedOnMap: (String) -> Unit,
  onStationCardDismissed: () -> Unit,
  onRecenterRequested: () -> Unit,
  onEnvironmentalSheetShown: () -> Unit,
  onEnvironmentalSheetDismissed: () -> Unit,
  onClearEnvironmentalFilters: () -> Unit,
  onReconcileSelection: (List<Station>, com.gcaguilar.biciradar.core.NearbyStationSelection, String) -> Unit,
  environmentalSnapshots: List<MapEnvironmentalZoneSnapshot>,
  onEnvironmentalLayerChanged: (MapEnvironmentalLayer?) -> Unit,
  paddingValues: PaddingValues,
) {
  val nearestStation = nearestSelection.highlightedStation
  val availableFilters = remember(stations) {
    availableMapFilters(stations)
  }
  val activeEnvironmentalLayer = remember(activeFilters) {
    activeEnvironmentalLayerForFilters(activeFilters)
  }

  val mapStations = remember(stations, activeFilters) {
    applyMapFilters(stations, activeFilters)
  }
  val estimatedEnvironmentalSnapshots = remember(stations, activeEnvironmentalLayer) {
    if (activeEnvironmentalLayer == null) {
      emptyList()
    } else {
      buildMapEnvironmentalZoneSnapshots(stations)
    }
  }
  LaunchedEffect(activeEnvironmentalLayer) {
    if (activeEnvironmentalLayer == null) {
      onEnvironmentalSheetDismissed()
    }
    onEnvironmentalLayerChanged(activeEnvironmentalLayer)
  }
  LaunchedEffect(availableFilters) {
    onAvailableFiltersChange(availableFilters)
  }

  LaunchedEffect(mapStations, nearestStation?.id, searchQuery) {
    onReconcileSelection(mapStations, nearestSelection, searchQuery)
  }

  val selectedMapStation = remember(selectedMapStationId, mapStations, nearestStation, searchQuery) {
    selectedMapStationId?.let { id -> mapStations.firstOrNull { station -> station.id == id } }
      ?: when {
        mapStations.isEmpty() -> null
        searchQuery.isNotBlank() -> mapStations.firstOrNull()
        else -> nearestStation?.takeIf { nearest -> mapStations.any { it.id == nearest.id } } ?: mapStations.firstOrNull()
      }
  }
  val mapIsShowingNearestSelection = !hasExplicitMapSelection && selectedMapStation?.id == nearestStation?.id
  val mapIsShowingNearestFallback = selectedMapStation?.id == nearestStation?.id && nearestSelection.usesFallback

  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues)
      .background(pageBackgroundColor(mobilePlatform)),
  ) {
    val platformEnvironmentalOverlay = activeEnvironmentalLayer?.let { layer ->
      EnvironmentalOverlayData(
        layer = when (layer) {
          MapEnvironmentalLayer.AirQuality -> EnvironmentalOverlayLayer.AirQuality
          MapEnvironmentalLayer.Pollen -> EnvironmentalOverlayLayer.Pollen
        },
        zones = environmentalSnapshots
          .ifEmpty { estimatedEnvironmentalSnapshots }
          .mapNotNull { zone ->
            val value = when (layer) {
              MapEnvironmentalLayer.AirQuality -> zone.airQualityScore
              MapEnvironmentalLayer.Pollen -> zone.pollenScore
            } ?: return@mapNotNull null
            EnvironmentalOverlayZone(
              center = GeoPoint(zone.centerLatitude, zone.centerLongitude),
              value = value,
            )
          },
      )
    }

    PlatformStationMap(
      modifier = Modifier.fillMaxSize(),
      stations = mapStations,
      userLocation = userLocation,
      highlightedStationId = selectedMapStation?.id,
      isMapReady = isMapReady,
      onStationSelected = { station ->
        onStationSelectedOnMap(station.id)
      },
      recenterRequestToken = recenterRequestToken,
      environmentalOverlay = platformEnvironmentalOverlay,
    )

    // Panel superior con búsqueda y filtros
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MapSearchBar(
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        mobilePlatform = mobilePlatform,
      )
      if (mobilePlatform != MobileUiPlatform.Desktop) {
        MapFiltersPanel(
          activeFilters = activeFilters,
          availableFilters = availableFilters,
          onToggleFilter = { filter ->
            onToggleFilter(filter, availableFilters)
          },
        )
      }
      DataFreshnessBanner(
        freshness = dataFreshness,
        lastUpdatedEpoch = lastUpdatedEpoch,
        loading = loading,
        onRefresh = onRefreshStations,
        modifier = Modifier.padding(bottom = 8.dp),
      )
    }

    // Mensajes de error o estado vacío
    AnimatedVisibility(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 16.dp),
      visible = errorMessage != null || (!loading && mapStations.isEmpty()),
      enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
      label = "map-centered-feedback",
    ) {
      if (errorMessage != null) {
        EmptyStatePlaceholder(
          title = stringResource(Res.string.mapUpdateFailed),
          description = errorMessage,
          primaryAction = stringResource(Res.string.retry),
          onPrimaryAction = onRetry,
        )
      } else {
        EmptyStatePlaceholder(
          title = if (searchQuery.isBlank()) {
            stringResource(Res.string.mapNoStationsOnScreen)
          } else {
            stringResource(Res.string.mapNoStations)
          },
          description = if (searchQuery.isBlank()) {
            stringResource(Res.string.mapLocationFallbackDescription)
          } else {
            stringResource(Res.string.mapTryAnotherQuery)
          },
          primaryAction = stringResource(Res.string.loadStations),
          onPrimaryAction = onRetry,
        )
      }
    }

    // Panel inferior con tarjeta de estación y controles
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
      ) {
        AnimatedVisibility(
          visible = selectedMapStation != null && !isCardDismissed,
          modifier = Modifier.weight(1f),
          enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
          exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
          label = "map-selected-station-overlay",
        ) {
          selectedMapStation?.let { station ->
            StationDetailBottomSheet(
              modifier = Modifier.fillMaxWidth(),
              station = station,
              isFavorite = station.id in favoriteIds,
              isShowingNearestSelection = mapIsShowingNearestSelection,
              isFallbackSelection = mapIsShowingNearestFallback,
              searchRadiusMeters = searchRadiusMeters,
              mobilePlatform = mobilePlatform,
              onFavoriteToggle = { onFavoriteToggle(station) },
              onOpenStationDetails = { onStationSelected(station) },
              onQuickRoute = { onQuickRoute(station) },
              onDismiss = onStationCardDismissed,
            )
          }
        }
        if (mobilePlatform != MobileUiPlatform.Desktop) {
          MapControls(
            enabled = userLocation != null || mapStations.isNotEmpty(),
            showEnvironmentalButton = activeEnvironmentalLayer != null,
            onRecenterClick = onRecenterRequested,
            onEnvironmentalClick = onEnvironmentalSheetShown,
          )
        }
      }
    }

    // Bottom sheet de capa ambiental
    if (activeEnvironmentalLayer != null && showEnvironmentalSheet) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = onEnvironmentalSheetDismissed,
        sheetState = sheetState,
        containerColor = LocalBiziColors.current.surface,
      ) {
        EnvironmentalLayerCard(
          layer = activeEnvironmentalLayer,
          zones = if (environmentalSnapshots.isNotEmpty()) environmentalSnapshots else estimatedEnvironmentalSnapshots,
          onClear = onClearEnvironmentalFilters,
        )
      }
    }
  }
}
