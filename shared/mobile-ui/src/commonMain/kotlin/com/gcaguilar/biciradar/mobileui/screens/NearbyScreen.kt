package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.TripMode
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.loadStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapLocationFallbackDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStationsOnScreen
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyCardActionsHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersNoResultsAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersNoResultsDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersNoResultsTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyLocationPermissionAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyLocationPermissionBody
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyLocationPermissionTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNearYou
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNearestWithBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNearestWithSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNoBikesNearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNoSlotsNearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyQuickActionsDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyRadiusFallbackHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationsSortedByBikesDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationsSortedByNameDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationsSortedByPredictedDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationsSortedBySlotsDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationsSortedDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyUpdatingStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.retry
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.FeedbackBottomSheet
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.NearbyBikeType
import com.gcaguilar.biciradar.mobileui.NearbyMaxDistance
import com.gcaguilar.biciradar.mobileui.NearbySort
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.components.NearbyFiltersButton
import com.gcaguilar.biciradar.mobileui.components.NearbyFiltersSheet
import com.gcaguilar.biciradar.mobileui.components.TripModeSelector
import com.gcaguilar.biciradar.mobileui.components.cards.QuickRouteActionCard
import com.gcaguilar.biciradar.mobileui.components.station.StationRow
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.routeIcon
import com.gcaguilar.biciradar.mobileui.viewmodel.NearbyUiState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun NearbyScreen(
  state: NearbyUiState,
  mobilePlatform: MobileUiPlatform,
  onStationSelected: (Station) -> Unit,
  onRetry: () -> Unit,
  onRefresh: () -> Unit,
  onFavoriteToggle: (Station) -> Unit,
  onQuickRoute: (Station) -> Unit,
  onRequestLocationPermission: () -> Unit,
  onTripModeSelected: (TripMode) -> Unit,
  onBikeTypeSelected: (NearbyBikeType) -> Unit,
  onMaxDistanceSelected: (NearbyMaxDistance) -> Unit,
  onFavoritesOnlyToggled: () -> Unit,
  onSortSelected: (NearbySort) -> Unit,
  onClearFilters: () -> Unit,
  refreshControl: @Composable () -> Unit,
  showFeedbackBottomSheet: Boolean,
  onFeedbackDismiss: () -> Unit,
  onOpenFeedbackForm: () -> Unit,
  paddingValues: PaddingValues,
) {
  var showFiltersSheet by remember { mutableStateOf(false) }
  Box(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(pageBackgroundColor(mobilePlatform)),
    contentAlignment = Alignment.TopCenter,
  ) {
    LazyColumn(
      modifier = Modifier.responsivePageWidth(),
      contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item("header") {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
          if (mobilePlatform == MobileUiPlatform.IOS) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Text(
                  text = stringResource(Res.string.nearby),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = stringResource(Res.string.nearbyQuickActionsDescription),
                  style = MaterialTheme.typography.bodyMedium,
                  color = LocalBiziColors.current.muted,
                )
              }
              refreshControl()
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top,
            ) {
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Text(
                  text = stringResource(Res.string.nearbyNearYou),
                  style = MaterialTheme.typography.headlineMedium,
                  fontWeight = FontWeight.Bold,
                  color = LocalBiziColors.current.red,
                )
                Text(
                  text = stringResource(state.filters.sort.descriptionResource()),
                  style = MaterialTheme.typography.bodyMedium,
                  color = LocalBiziColors.current.muted,
                )
              }
              refreshControl()
            }
          }
          TripModeSelector(
            selectedMode = state.tripMode,
            onModeSelected = onTripModeSelected,
            mobilePlatform = mobilePlatform,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(
            modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            QuickRouteActionCard(
              modifier = Modifier.weight(1f),
              title = stringResource(Res.string.nearbyNearestWithBikes),
              emptyTitle = stringResource(Res.string.nearbyNoBikesNearby),
              selection = state.nearestWithBikesSelection,
              icon = Icons.AutoMirrored.Filled.DirectionsBike,
              tint = LocalBiziColors.current.red,
              mobilePlatform = mobilePlatform,
              onRoute = onQuickRoute,
            )
            QuickRouteActionCard(
              modifier = Modifier.weight(1f),
              title = stringResource(Res.string.nearbyNearestWithSlots),
              emptyTitle = stringResource(Res.string.nearbyNoSlotsNearby),
              selection = state.nearestWithSlotsSelection,
              icon = Icons.Filled.LocalParking,
              tint = LocalBiziColors.current.blue,
              mobilePlatform = mobilePlatform,
              onRoute = onQuickRoute,
            )
          }
        }
      }

      item("freshness") {
        DataFreshnessBanner(
          freshness = state.dataFreshness,
          lastUpdatedEpoch = state.lastUpdatedEpoch,
          loading = state.isLoading,
          onRefresh = onRefresh,
        )
      }

      if (!state.locationPermissionGranted) {
        item("location-permission") {
          EmptyStatePlaceholder(
            title = stringResource(Res.string.nearbyLocationPermissionTitle),
            description = stringResource(Res.string.nearbyLocationPermissionBody),
            primaryAction = stringResource(Res.string.nearbyLocationPermissionAction),
            onPrimaryAction = onRequestLocationPermission,
          )
        }
      }

      item("stations-header") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text =
                if (state.isLoading) {
                  stringResource(
                    Res.string.nearbyUpdatingStations,
                  )
                } else {
                  stringResource(Res.string.nearbyStations)
                },
              modifier = Modifier.weight(1f),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.width(12.dp))
            NearbyFiltersButton(
              activeCount = state.filters.activeCount,
              onClick = { showFiltersSheet = true },
            )
          }
          Text(
            text =
              if (state.nearestSelection.usesFallback) {
                stringResource(Res.string.nearbyRadiusFallbackHint)
              } else {
                stringResource(Res.string.nearbyCardActionsHint)
              },
            style = MaterialTheme.typography.bodySmall,
            color = LocalBiziColors.current.muted,
          )
          AnimatedVisibility(
            visible = state.errorMessage != null,
            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
            label = "nearby-error",
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(state.errorMessage.orEmpty(), color = LocalBiziColors.current.red)
              OutlinedButton(onClick = onRetry) {
                Icon(Icons.Filled.Sync, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.retry))
              }
            }
          }
        }
      }

      item("stations-empty") {
        AnimatedVisibility(
          visible = !state.isLoading && state.stations.isEmpty(),
          enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
          exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
          label = "nearby-empty",
        ) {
          if (state.filters.isDefault) {
            EmptyStatePlaceholder(
              title = stringResource(Res.string.mapNoStationsOnScreen),
              description = stringResource(Res.string.mapLocationFallbackDescription),
              primaryAction = stringResource(Res.string.loadStations),
              onPrimaryAction = onRetry,
            )
          } else {
            EmptyStatePlaceholder(
              title = stringResource(Res.string.nearbyFiltersNoResultsTitle),
              description = stringResource(Res.string.nearbyFiltersNoResultsDescription),
              primaryAction = stringResource(Res.string.nearbyFiltersNoResultsAction),
              onPrimaryAction = onClearFilters,
            )
          }
        }
      }

      items(state.stations.take(12), key = { it.id }) { station ->
        StationRow(
          mobilePlatform = mobilePlatform,
          station = station,
          isFavorite = station.id in state.favoriteIds,
          onClick = { onStationSelected(station) },
          onFavoriteToggle = { onFavoriteToggle(station) },
          onQuickRoute = { onQuickRoute(station) },
          routeIcon = state.tripMode.routeIcon(),
        )
      }
    }
  }

  if (showFiltersSheet) {
    NearbyFiltersSheet(
      filters = state.filters,
      showBikeTypeFilter = state.supportsBikeTypeFilter,
      onBikeTypeSelected = onBikeTypeSelected,
      onMaxDistanceSelected = onMaxDistanceSelected,
      onFavoritesOnlyToggled = onFavoritesOnlyToggled,
      onSortSelected = onSortSelected,
      onReset = onClearFilters,
      onDismiss = { showFiltersSheet = false },
    )
  }

  if (showFeedbackBottomSheet) {
    FeedbackBottomSheet(
      onDismiss = onFeedbackDismiss,
      onOpenFeedbackForm = onOpenFeedbackForm,
    )
  }
}

private fun NearbySort.descriptionResource(): StringResource =
  when (this) {
    NearbySort.NEAREST -> Res.string.nearbyStationsSortedDescription
    NearbySort.MOST_BIKES -> Res.string.nearbyStationsSortedByBikesDescription
    NearbySort.MOST_SLOTS -> Res.string.nearbyStationsSortedBySlotsDescription
    NearbySort.NAME -> Res.string.nearbyStationsSortedByNameDescription
    NearbySort.BEST_PREDICTED_AVAILABILITY -> Res.string.nearbyStationsSortedByPredictedDescription
  }
