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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.selectNearbyStationWithBikes
import com.gcaguilar.biciradar.core.selectNearbyStationWithSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.distance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.loadStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapLocationFallbackDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapNoStationsOnScreen
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyCardActionsHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNearestWithBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNearestWithSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNearYou
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNoBikesNearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyNoSlotsNearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyQuickActionsDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyRadiusFallbackHint
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyStationsSortedDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyUpdatingStations
import com.gcaguilar.biciradar.mobile_ui.generated.resources.retry
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.components.buttons.RefreshButtonWithCountdown
import com.gcaguilar.biciradar.mobileui.components.cards.QuickRouteActionCard
import com.gcaguilar.biciradar.mobileui.components.station.StationRow
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import org.jetbrains.compose.resources.stringResource

/**
 * Pantalla de estaciones cercanas.
 * Muestra estaciones ordenadas por distancia y acciones rápidas de ruta.
 */
@Composable
internal fun NearbyScreen(
    mobilePlatform: MobileUiPlatform,
    stations: List<Station>,
    favoriteIds: Set<String>,
    loading: Boolean,
    errorMessage: String?,
    dataFreshness: DataFreshness,
    lastUpdatedEpoch: Long?,
    nearestSelection: com.gcaguilar.biciradar.core.NearbyStationSelection,
    searchRadiusMeters: Int,
    onStationSelected: (Station) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onFavoriteToggle: (Station) -> Unit,
    onQuickRoute: (Station) -> Unit,
    refreshCountdownSeconds: Int,
    paddingValues: PaddingValues,
) {
    val nearestWithBikesSelection = remember(stations, searchRadiusMeters) {
        selectNearbyStationWithBikes(stations, searchRadiusMeters)
    }
    val nearestWithSlotsSelection = remember(stations, searchRadiusMeters) {
        selectNearbyStationWithSlots(stations, searchRadiusMeters)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(pageBackgroundColor(mobilePlatform)),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier.responsivePageWidth(),
        ) {
            // Header + quick-action cards — always visible, never scroll away
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
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
                        RefreshButtonWithCountdown(
                            countdown = refreshCountdownSeconds,
                            loading = loading,
                            onRefresh = onRefresh,
                        )
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
                                text = stringResource(Res.string.nearbyStationsSortedDescription),
                                style = MaterialTheme.typography.bodyMedium,
                                color = LocalBiziColors.current.muted,
                            )
                        }
                        RefreshButtonWithCountdown(
                            countdown = refreshCountdownSeconds,
                            loading = loading,
                            onRefresh = onRefresh,
                        )
                    }
                }
                Row(
                    modifier = Modifier.animateContentSize(animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f)),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    QuickRouteActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.nearbyNearestWithBikes),
                        emptyTitle = stringResource(Res.string.nearbyNoBikesNearby),
                        selection = nearestWithBikesSelection,
                        icon = Icons.AutoMirrored.Filled.DirectionsBike,
                        tint = LocalBiziColors.current.red,
                        mobilePlatform = mobilePlatform,
                        onRoute = onQuickRoute,
                    )
                    QuickRouteActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.nearbyNearestWithSlots),
                        emptyTitle = stringResource(Res.string.nearbyNoSlotsNearby),
                        selection = nearestWithSlotsSelection,
                        icon = Icons.Filled.LocalParking,
                        tint = LocalBiziColors.current.blue,
                        mobilePlatform = mobilePlatform,
                        onRoute = onQuickRoute,
                    )
                }
            }
            DataFreshnessBanner(
                freshness = dataFreshness,
                lastUpdatedEpoch = lastUpdatedEpoch,
                loading = loading,
                onRefresh = onRefresh,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 10.dp),
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (loading) stringResource(Res.string.nearbyUpdatingStations) else stringResource(Res.string.nearbyStations),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (nearestSelection.usesFallback) {
                                stringResource(Res.string.nearbyRadiusFallbackHint)
                            } else {
                                stringResource(Res.string.nearbyCardActionsHint)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = LocalBiziColors.current.muted,
                        )
                        AnimatedVisibility(
                            visible = errorMessage != null,
                            enter = fadeIn(animationSpec = tween(180)) + expandVertically(animationSpec = tween(180)),
                            exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
                            label = "nearby-error",
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(errorMessage.orEmpty(), color = LocalBiziColors.current.red)
                                OutlinedButton(onClick = onRetry) {
                                    Icon(Icons.Filled.Sync, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(Res.string.retry))
                                }
                            }
                        }
                    }
                }
                item {
                    AnimatedVisibility(
                        visible = !loading && stations.isEmpty(),
                        enter = fadeIn(animationSpec = tween(200)) + expandVertically(animationSpec = tween(200)),
                        exit = fadeOut(animationSpec = tween(120)) + shrinkVertically(animationSpec = tween(120)),
                        label = "nearby-empty",
                    ) {
                        EmptyStatePlaceholder(
                            title = stringResource(Res.string.mapNoStationsOnScreen),
                            description = stringResource(Res.string.mapLocationFallbackDescription),
                            primaryAction = stringResource(Res.string.loadStations),
                            onPrimaryAction = onRetry,
                        )
                    }
                }
                items(stations.take(12), key = { it.id }) { station ->
                    StationRow(
                        mobilePlatform = mobilePlatform,
                        station = station,
                        isFavorite = station.id in favoriteIds,
                        onClick = { onStationSelected(station) },
                        onFavoriteToggle = { onFavoriteToggle(station) },
                        onQuickRoute = { onQuickRoute(station) },
                    )
                }
            }
        }

    }
}
