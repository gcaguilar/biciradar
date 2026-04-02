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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
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
  environmentalSnapshots: List<MapEnvironmentalZoneSnapshot>,
  onEnvironmentalLayerChanged: (MapEnvironmentalLayer?) -> Unit,
  paddingValues: PaddingValues,
) {
  val nearestStation = nearestSelection.highlightedStation
  var selectedMapStationId by rememberSaveable { mutableStateOf<String?>(null) }
  var hasExplicitMapSelection by rememberSaveable { mutableStateOf(false) }
  var isCardDismissed by rememberSaveable { mutableStateOf(false) }
  var showEnvironmentalSheet by rememberSaveable { mutableStateOf(false) }
  var activeFilters by rememberSaveable(stateSaver = MapFilterSetSaver) { mutableStateOf(emptySet<MapFilter>()) }
  var recenterRequestToken by rememberSaveable { mutableStateOf(0) }
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
      showEnvironmentalSheet = false
    }
    onEnvironmentalLayerChanged(activeEnvironmentalLayer)
  }

  LaunchedEffect(selectedMapStationId) {
    isCardDismissed = false
  }

  LaunchedEffect(mapStations, nearestStation?.id, searchQuery) {
    val hasSelectedStation = selectedMapStationId != null && mapStations.any { station -> station.id == selectedMapStationId }
    if (!hasSelectedStation) {
      selectedMapStationId = if (searchQuery.isNotBlank()) {
        mapStations.firstOrNull()?.id
      } else {
        nearestStation?.takeIf { nearest -> mapStations.any { it.id == nearest.id } }?.id ?: mapStations.firstOrNull()?.id
      }
      hasExplicitMapSelection = false
    }
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

  LaunchedEffect(searchQuery, mapStations) {
    if (searchQuery.isBlank()) return@LaunchedEffect
    val firstMatch = mapStations.firstOrNull() ?: return@LaunchedEffect
    selectedMapStationId = firstMatch.id
    hasExplicitMapSelection = false
  }

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
        hasExplicitMapSelection = true
        selectedMapStationId = station.id
      },
      recenterRequestToken = recenterRequestToken,
      environmentalOverlay = platformEnvironmentalOverlay,
    )

    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      StationSearchField(
        mobilePlatform = mobilePlatform,
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = stringResource(Res.string.mapSearchStationOrAddress),
      )
      if (mobilePlatform != MobileUiPlatform.Desktop) {
        MapFilterChipRow(
          activeFilters = activeFilters,
          onToggleFilter = { filter ->
            val next = toggleMapFilterSelection(activeFilters, filter)
            activeFilters = next
            if (isEnvironmentalMapFilter(filter)) {
              showEnvironmentalSheet = filter in next
            }
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
        EmptyStateCard(
          title = stringResource(Res.string.mapUpdateFailed),
          description = errorMessage,
          primaryAction = stringResource(Res.string.retry),
          onPrimaryAction = onRetry,
        )
      } else {
        EmptyStateCard(
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
            MapSelectedStationCard(
              modifier = Modifier.fillMaxWidth(),
              mobilePlatform = mobilePlatform,
              station = station,
              isFavorite = station.id in favoriteIds,
              isShowingNearestSelection = mapIsShowingNearestSelection,
              isFallbackSelection = mapIsShowingNearestFallback,
              searchRadiusMeters = searchRadiusMeters,
              onFavoriteToggle = { onFavoriteToggle(station) },
              onOpenStationDetails = { onStationSelected(station) },
              onQuickRoute = { onQuickRoute(station) },
              onDismiss = { isCardDismissed = true },
            )
          }
        }
        if (mobilePlatform != MobileUiPlatform.Desktop) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            activeEnvironmentalLayer?.let {
              MapEnvironmentalSheetButton(
                onClick = { showEnvironmentalSheet = true },
              )
            }
            MapRecenterButton(
              enabled = userLocation != null || mapStations.isNotEmpty(),
              onClick = {
                recenterRequestToken += 1
                isCardDismissed = false
              },
            )
          }
        }
      }
    }

    if (activeEnvironmentalLayer != null && showEnvironmentalSheet) {
      val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
      ModalBottomSheet(
        onDismissRequest = { showEnvironmentalSheet = false },
        sheetState = sheetState,
        containerColor = LocalBiziColors.current.surface,
      ) {
        EnvironmentalLayerCard(
          layer = activeEnvironmentalLayer,
          zones = if (environmentalSnapshots.isNotEmpty()) environmentalSnapshots else estimatedEnvironmentalSnapshots,
          onClear = {
            showEnvironmentalSheet = false
            activeFilters = clearEnvironmentalMapFilters(activeFilters)
          },
        )
      }
    }
  }
}

@Composable
private fun EnvironmentalLayerCard(
  layer: MapEnvironmentalLayer,
  zones: List<MapEnvironmentalZoneSnapshot>,
  onClear: () -> Unit,
) {
  val c = LocalBiziColors.current
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = c.surface),
  ) {
    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Text(
        text = when (layer) {
          MapEnvironmentalLayer.AirQuality -> stringResource(Res.string.mapFilterAirQuality)
          MapEnvironmentalLayer.Pollen -> stringResource(Res.string.mapFilterPollen)
        },
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        text = stringResource(Res.string.mapEnvironmentalLayerHint),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      EnvironmentalLegendRow(layer = layer)
      zones.take(4).forEach { zone ->
        val score = when (layer) {
          MapEnvironmentalLayer.AirQuality -> zone.airQualityScore
          MapEnvironmentalLayer.Pollen -> zone.pollenScore
        }
        val tone = environmentalToneForLayer(layer = layer, score = score, muted = c.muted)
        val valueText = when {
          score == null -> "--"
          layer == MapEnvironmentalLayer.AirQuality -> "AQI $score"
          else -> "$score gr/m3"
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(zone.zoneLabel, style = MaterialTheme.typography.bodySmall, color = c.ink)
          Text(
            valueText,
            style = MaterialTheme.typography.bodySmall,
            color = tone,
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
      TextButton(onClick = onClear, contentPadding = PaddingValues(0.dp)) {
        Text(stringResource(Res.string.mapClearEnvironmentalLayer))
      }
    }
  }
}

@Composable
private fun EnvironmentalLegendRow(layer: MapEnvironmentalLayer) {
  val c = LocalBiziColors.current
  val labels = when (layer) {
    MapEnvironmentalLayer.AirQuality -> listOf(
      stringResource(Res.string.environmentalLegendGood),
      stringResource(Res.string.environmentalLegendModerate),
      stringResource(Res.string.environmentalLegendPoor),
    )
    MapEnvironmentalLayer.Pollen -> listOf(
      stringResource(Res.string.environmentalLegendLow),
      stringResource(Res.string.environmentalLegendMedium),
      stringResource(Res.string.environmentalLegendHigh),
    )
  }
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    listOf(c.green, c.orange, c.red).forEachIndexed { index, color ->
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        MapColorDot(color = color)
        Text(labels[index], style = MaterialTheme.typography.labelSmall, color = c.muted)
      }
    }
  }
}

private fun environmentalToneForLayer(layer: MapEnvironmentalLayer, score: Int?, muted: Color): Color = when {
  score == null -> muted
  layer == MapEnvironmentalLayer.AirQuality && score <= 50 -> Color(0xFF26A69A)
  layer == MapEnvironmentalLayer.AirQuality && score <= 100 -> Color(0xFFFFB300)
  layer == MapEnvironmentalLayer.AirQuality -> Color(0xFFD84315)
  layer == MapEnvironmentalLayer.Pollen && score <= 10 -> Color(0xFF8BC34A)
  layer == MapEnvironmentalLayer.Pollen && score <= 30 -> Color(0xFFFF9800)
  else -> Color(0xFFC2185B)
}

@Composable
private fun MapFilterChipRow(
  activeFilters: Set<MapFilter>,
  onToggleFilter: (MapFilter) -> Unit,
) {
  Row(
    modifier = Modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    MapFilter.entries.forEach { filter ->
      MapFilterChip(
        filter = filter,
        label = stringResource(filter.labelKey),
        selected = filter in activeFilters,
        onClick = { onToggleFilter(filter) },
      )
    }
  }
}

@Composable
private fun MapColorDot(
  color: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier
      .size(10.dp)
      .clip(CircleShape)
      .background(color),
  )
}

@Composable
private fun MapFilterChip(
  filter: MapFilter,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  val accent = when (filter) {
    MapFilter.BIKES_AND_SLOTS -> c.green
    MapFilter.ONLY_BIKES -> c.blue
    MapFilter.ONLY_SLOTS -> c.red
    MapFilter.ONLY_EBIKES -> c.orange
    MapFilter.ONLY_REGULAR_BIKES -> c.purple
    MapFilter.AIR_QUALITY -> c.green
    MapFilter.POLLEN -> c.orange
  }
  val backgroundColor by animateColorAsState(
    targetValue = c.surface,
    animationSpec = tween(180),
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) accent else c.ink,
    animationSpec = tween(180),
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) accent else c.panel,
    animationSpec = tween(180),
  )
  val selectionScale by animateFloatAsState(
    targetValue = if (selected) 1f else 0.98f,
    animationSpec = spring(dampingRatio = 0.82f, stiffness = 700f),
    label = "map-filter-scale",
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = backgroundColor,
    border = BorderStroke(1.dp, borderColor),
    modifier = Modifier
      .graphicsLayer {
        scaleX = selectionScale
        scaleY = selectionScale
      }
      .clickable(onClick = onClick),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      MapColorDot(color = accent)
      Text(
        text = label,
        color = contentColor,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun MapRecenterButton(
  enabled: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  Surface(
    modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    shape = CircleShape,
    color = c.surface.copy(alpha = if (enabled) 0.96f else 0.88f),
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Icon(
      imageVector = Icons.Filled.MyLocation,
      contentDescription = stringResource(Res.string.mapMyLocation),
      tint = if (enabled) c.green else c.muted,
      modifier = Modifier.padding(14.dp).size(22.dp),
    )
  }
}

@Composable
private fun MapEnvironmentalSheetButton(
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  Surface(
    modifier = Modifier.clickable(onClick = onClick),
    shape = CircleShape,
    color = c.surface.copy(alpha = 0.96f),
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Icon(
      imageVector = Icons.Filled.Tune,
      contentDescription = stringResource(Res.string.details),
      tint = c.blue,
      modifier = Modifier.padding(14.dp).size(22.dp),
    )
  }
}

@Composable
private fun MapSelectedStationCard(
  modifier: Modifier = Modifier,
  mobilePlatform: MobileUiPlatform,
  station: Station,
  isFavorite: Boolean,
  isShowingNearestSelection: Boolean,
  isFallbackSelection: Boolean,
  searchRadiusMeters: Int,
  onFavoriteToggle: () -> Unit,
  onOpenStationDetails: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onDismiss: () -> Unit,
) {
  val c = LocalBiziColors.current
  val overlayTitle = if (mobilePlatform == MobileUiPlatform.IOS) c.ink else c.onAccent
  val overlayBody = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else c.onAccent.copy(alpha = 0.84f)
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(if (mobilePlatform == MobileUiPlatform.IOS) 24.dp else 28.dp),
    border = if (mobilePlatform == MobileUiPlatform.IOS) BorderStroke(1.dp, c.red.copy(alpha = 0.12f)) else null,
    colors = CardDefaults.cardColors(containerColor = if (mobilePlatform == MobileUiPlatform.IOS) c.surface else c.red),
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 14.dp, vertical = 13.dp),
      verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          if (isFallbackSelection) {
            stringResource(Res.string.mapNoStationsWithinRadius, searchRadiusMeters)
          } else if (isShowingNearestSelection) {
            stringResource(Res.string.mapNearestStationLabel)
          } else {
            stringResource(Res.string.mapSelectedStationLabel)
          },
          color = if (mobilePlatform == MobileUiPlatform.IOS) c.red else overlayBody,
        )
        Icon(
          imageVector = Icons.Filled.Close,
          contentDescription = stringResource(Res.string.close),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.muted else overlayBody,
          modifier = Modifier.size(20.dp).clickable(onClick = onDismiss),
        )
      }
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleLarge,
          color = overlayTitle,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = station.address,
          style = MaterialTheme.typography.bodySmall,
          color = overlayBody,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = if (isFallbackSelection) {
          stringResource(
            Res.string.mapNearestFallbackSummary,
            formatDistance(station.distanceMeters),
            station.bikesAvailable,
            station.slotsFree,
          )
        } else {
          stringResource(
            Res.string.mapStationDistanceSummary,
            formatDistance(station.distanceMeters),
            station.bikesAvailable,
            station.slotsFree,
          )
        },
        color = overlayBody,
      )
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        RoutePill(
          label = stringResource(Res.string.route),
          onDarkBackground = mobilePlatform != MobileUiPlatform.IOS,
          onClick = { onQuickRoute(station) },
        )
        if (mobilePlatform == MobileUiPlatform.IOS) {
          FavoritePill(
            active = isFavorite,
            onClick = onFavoriteToggle,
            label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
          )
        } else {
          OutlineActionPill(
            label = if (isFavorite) stringResource(Res.string.saved) else stringResource(Res.string.save),
            tint = c.onAccent,
            borderTint = c.onAccent.copy(alpha = 0.32f),
            onClick = onFavoriteToggle,
          )
        }
        OutlineActionPill(
          label = stringResource(Res.string.details),
          tint = if (mobilePlatform == MobileUiPlatform.IOS) c.red else c.onAccent,
          borderTint = if (mobilePlatform == MobileUiPlatform.IOS) c.red.copy(alpha = 0.16f) else c.onAccent.copy(alpha = 0.32f),
          onClick = { onOpenStationDetails(station) },
        )
      }
    }
  }
}
