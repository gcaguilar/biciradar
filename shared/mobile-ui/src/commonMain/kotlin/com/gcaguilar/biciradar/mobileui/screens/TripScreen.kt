package com.gcaguilar.biciradar.mobileui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.clear
import com.gcaguilar.biciradar.mobile_ui.generated.resources.gotIt
import com.gcaguilar.biciradar.mobile_ui.generated.resources.searchingNearbyStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.trip
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripBikeRouteAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapDestinationTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapStationAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripNoNearbyAlternative
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationFull
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationNoLongerHasSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSuggestedAlternative
import com.gcaguilar.biciradar.mobile_ui.generated.resources.whereAreYouGoing
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.biziCardBorder
import com.gcaguilar.biciradar.mobileui.biziCardColors
import com.gcaguilar.biciradar.mobileui.biziCardElevation
import com.gcaguilar.biciradar.mobileui.components.cards.BiziSectionCard
import com.gcaguilar.biciradar.mobileui.components.cards.BiziStatusCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripMonitoringActiveCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripMonitoringSetupCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripStationCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.TripUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TripScreen(
  state: TripUiState,
  mobilePlatform: MobileUiPlatform,
  onDismissAlert: () -> Unit,
  onClearTrip: () -> Unit,
  onStopMonitoring: () -> Unit,
  onDurationSelected: (Int) -> Unit,
  onStartMonitoring: () -> Unit,
  onRefreshStations: () -> Unit,
  onOpenDestinationPicker: () -> Unit,
  onOpenStationPicker: () -> Unit,
  onLaunchBikeRoute: (Station) -> Unit,
  paddingValues: PaddingValues,
) {
  val colors = LocalBiziColors.current

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
      contentPadding = PaddingValues(BiziSpacing.screenPadding),
      verticalArrangement = Arrangement.spacedBy(BiziSpacing.screenPadding),
    ) {
      item("header") {
        Column(verticalArrangement = Arrangement.spacedBy(BiziSpacing.small)) {
          Text(
            text = stringResource(Res.string.trip),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = stringResource(Res.string.tripSubtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.muted,
          )
        }
      }

      item("freshness") {
        DataFreshnessBanner(
          freshness = state.dataFreshness,
          lastUpdatedEpoch = state.lastUpdatedEpoch,
          loading = state.stationsLoading,
          onRefresh = onRefreshStations,
        )
      }

      state.alert?.let { alert ->
        item("alert") {
          BiziStatusCard(
            tint = colors.red,
            containerAlpha = 0.09f,
            borderAlpha = 0.22f,
          ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BiziSpacing.medium),
              ) {
                Icon(Icons.Filled.Sync, contentDescription = null, tint = colors.red)
                Text(
                  text = stringResource(Res.string.tripStationFull),
                  style = MaterialTheme.typography.titleMedium,
                  color = colors.red,
                  fontWeight = FontWeight.Bold,
                )
              }
              Text(
                text = stringResource(Res.string.tripStationNoLongerHasSlots, alert.fullStation.name),
                style = MaterialTheme.typography.bodyMedium,
              )
              alert.alternativeStation?.let { alternativeStation ->
                val alternativeDistance = alert.alternativeDistanceMeters?.toString().orEmpty()
                Text(
                  text =
                    stringResource(
                      Res.string.tripSuggestedAlternative,
                      alternativeStation.name,
                      alternativeDistance,
                      alternativeStation.slotsFree,
                    ),
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              } ?: Text(
                text = stringResource(Res.string.tripNoNearbyAlternative),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
              )
              Button(
                onClick = onDismissAlert,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Text(stringResource(Res.string.gotIt))
              }
          }
        }
      }

      item("hero") {
        BiziSectionCard(
          title = stringResource(Res.string.whereAreYouGoing),
          contentSpacing = BiziSpacing.xxLarge,
        ) {
            Text(
              text =
                if (state.destination == null) {
                  stringResource(Res.string.tripSubtitle)
                } else {
                  state.destination.name
                },
              style =
                if (state.destination == null) {
                  MaterialTheme.typography.bodyMedium
                } else {
                  MaterialTheme.typography.titleMedium
                },
              color = if (state.destination == null) colors.muted else colors.ink,
              maxLines = if (state.destination == null) 2 else 3,
              overflow = TextOverflow.Ellipsis,
            )
            Button(
              onClick = onOpenDestinationPicker,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(Res.string.tripMapDestinationTitle))
            }
            OutlinedButton(
              onClick = onOpenStationPicker,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(Res.string.tripMapStationAction))
            }
          }
        }

      state.destination?.let { destination ->
        item("destination-summary") {
          BiziCard {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(BiziSpacing.screenPadding),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge),
            ) {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = colors.red,
                modifier = Modifier.size(22.dp),
              )
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(BiziSpacing.xSmall),
              ) {
                Text(
                  text = stringResource(Res.string.whereAreYouGoing),
                  style = MaterialTheme.typography.labelMedium,
                  color = colors.muted,
                )
                Text(
                  text = destination.name,
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 3,
                  overflow = TextOverflow.Ellipsis,
                )
              }
              OutlinedButton(
                onClick = onClearTrip,
              ) {
                Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(stringResource(Res.string.clear))
              }
            }
          }
        }
      }

      if (state.destination != null && state.isSearchingStation) {
        item("searching") {
          BiziCard {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge, Alignment.CenterHorizontally),
            ) {
              androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = colors.red,
              )
              Text(
                text = stringResource(Res.string.searchingNearbyStation),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.muted,
              )
            }
          }
        }
      }

      if (state.destination != null && state.searchError != null) {
        item("search-error") {
          BiziStatusCard(
            tint = colors.red,
            containerAlpha = 0.07f,
            borderAlpha = BiziAlpha.selectedBorder,
            contentPadding = BiziSpacing.xxLarge,
            contentSpacing = 0.dp,
          ) {
            Text(
              text = state.searchError,
              style = MaterialTheme.typography.bodyMedium,
              color = colors.red,
            )
          }
        }
      }

      val suggestedStation = state.nearestStationWithSlots
      if (suggestedStation != null && !state.isSearchingStation) {
        item("station") {
          Column(verticalArrangement = Arrangement.spacedBy(BiziSpacing.xLarge)) {
            TripStationCard(
              station = suggestedStation,
              distanceMeters = state.distanceToStation,
            )
            Button(
              onClick = { onLaunchBikeRoute(suggestedStation) },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(containerColor = colors.blue),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(8.dp))
              Text(stringResource(Res.string.tripBikeRouteAction))
            }
          }
        }

        if (state.monitoring.isActive) {
          item("monitoring-active") {
            TripMonitoringActiveCard(
              monitoring = state.monitoring,
              onStop = onStopMonitoring,
            )
          }
        } else {
          item("monitoring-setup") {
            TripMonitoringSetupCard(
              selectedDurationSeconds = state.selectedDurationSeconds,
              onDurationSelected = onDurationSelected,
              onStartMonitoring = onStartMonitoring,
            )
          }
        }
      }
    }
  }
}
