package com.gcaguilar.biciradar.mobileui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.loadStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapLocationFallbackDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStationsOnScreen
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapTryAnotherQuery
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapUpdateFailed
import com.gcaguilar.biciradar.mobile_ui.generated.resources.retry
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.components.map.EnvironmentalLayerCard
import com.gcaguilar.biciradar.mobileui.components.map.MapControls
import com.gcaguilar.biciradar.mobileui.components.map.MapFiltersPanel
import com.gcaguilar.biciradar.mobileui.components.map.MapSearchBar
import com.gcaguilar.biciradar.mobileui.components.map.StationDetailBottomSheet
import com.gcaguilar.biciradar.mobileui.viewmodel.MapEnvironmentalUiState
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun MapScreen(
  state: MapEnvironmentalUiState,
  mobilePlatform: MobileUiPlatform,
  onRefreshStations: () -> Unit,
  isMapReady: Boolean,
  onSearchQueryChange: (String) -> Unit,
  onStationSelected: (Station) -> Unit,
  onRetry: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onStationSelectedOnMap: (String) -> Unit,
  onStationCardDismissed: () -> Unit,
  onRecenterRequested: () -> Unit,
  onEnvironmentalSheetShown: () -> Unit,
  onEnvironmentalSheetDismissed: () -> Unit,
  onClearEnvironmentalFilters: () -> Unit,
  onToggleFilter: (MapFilter, Set<MapFilter>) -> Unit,
  paddingValues: PaddingValues,
) {
  val nearestStation = state.nearestSelection.highlightedStation
  val estimatedEnvironmentalSnapshots =
    if (state.activeEnvironmentalLayer == null) {
      emptyList()
    } else {
      buildMapEnvironmentalZoneSnapshots(state.stations)
    }

  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(pageBackgroundColor(mobilePlatform)),
  ) {
    val platformEnvironmentalOverlay =
      state.activeEnvironmentalLayer?.let { layer ->
        EnvironmentalOverlayData(
          layer =
            when (layer) {
              MapEnvironmentalLayer.AirQuality -> EnvironmentalOverlayLayer.AirQuality
              MapEnvironmentalLayer.Pollen -> EnvironmentalOverlayLayer.Pollen
            },
          zones =
            state.zones
              .ifEmpty { estimatedEnvironmentalSnapshots }
              .mapNotNull { zone ->
                val value =
                  when (layer) {
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
      stations = state.stations,
      userLocation = state.userLocation,
      highlightedStationId = state.selectedMapStation?.id,
      isMapReady = isMapReady,
      onStationSelected = { station ->
        onStationSelectedOnMap(station.id)
      },
      recenterRequestToken = state.recenterRequestToken,
      environmentalOverlay = platformEnvironmentalOverlay,
    )

    // Panel superior con búsqueda y filtros
    Column(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      MapSearchBar(
        searchQuery = state.searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        mobilePlatform = mobilePlatform,
      )
      if (mobilePlatform != MobileUiPlatform.Desktop) {
        MapFiltersPanel(
          activeFilters = state.persistedActiveFilters,
          availableFilters = state.availableFilters,
          onToggleFilter = { filter -> onToggleFilter(filter, state.availableFilters) },
        )
      }
      DataFreshnessBanner(
        freshness = state.dataFreshness,
        lastUpdatedEpoch = state.lastUpdatedEpoch,
        loading = state.loading,
        onRefresh = onRefreshStations,
        modifier = Modifier.padding(bottom = 8.dp),
      )
    }

    // Mensajes de error o estado vacío
    AnimatedVisibility(
      modifier =
        Modifier
          .align(Alignment.Center)
          .padding(horizontal = 16.dp),
      visible = state.errorMessage != null || (!state.loading && state.stations.isEmpty()),
      enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
      label = "map-centered-feedback",
    ) {
      if (state.errorMessage != null) {
        EmptyStatePlaceholder(
          title = stringResource(Res.string.mapUpdateFailed),
          description = state.errorMessage,
          primaryAction = stringResource(Res.string.retry),
          onPrimaryAction = onRetry,
        )
      } else {
        EmptyStatePlaceholder(
          title =
            if (state.searchQuery.isBlank()) {
              stringResource(Res.string.mapNoStationsOnScreen)
            } else {
              stringResource(Res.string.mapNoStations)
            },
          description =
            if (state.searchQuery.isBlank()) {
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
      modifier =
        Modifier
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
          visible = state.selectedMapStation != null && !state.isCardDismissed,
          modifier = Modifier.weight(1f),
          enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
          exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
          label = "map-selected-station-overlay",
        ) {
          state.selectedMapStation?.let { station ->
            StationDetailBottomSheet(
              modifier = Modifier.fillMaxWidth(),
              station = station,
              isFavorite = station.id in state.favoriteIds,
              isShowingNearestSelection = state.isShowingNearestSelection,
              isFallbackSelection = state.isShowingNearestFallback,
              searchRadiusMeters = state.searchRadiusMeters,
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
            enabled = state.userLocation != null || state.stations.isNotEmpty(),
            showEnvironmentalButton = state.activeEnvironmentalLayer != null,
            onRecenterClick = onRecenterRequested,
            onEnvironmentalClick = onEnvironmentalSheetShown,
          )
        }
      }
    }

    // Bottom sheet de capa ambiental
    if (state.activeEnvironmentalLayer != null && state.showEnvironmentalSheet) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = onEnvironmentalSheetDismissed,
        sheetState = sheetState,
        containerColor = LocalBiziColors.current.surface,
      ) {
        EnvironmentalLayerCard(
          layer = state.activeEnvironmentalLayer,
          zones = if (state.zones.isNotEmpty()) state.zones else estimatedEnvironmentalSnapshots,
          onClear = onClearEnvironmentalFilters,
        )
      }
    }
  }
}
