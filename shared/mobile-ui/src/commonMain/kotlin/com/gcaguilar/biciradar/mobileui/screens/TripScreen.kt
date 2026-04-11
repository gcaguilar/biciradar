package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.clear
import com.gcaguilar.biciradar.mobile_ui.generated.resources.gotIt
import com.gcaguilar.biciradar.mobile_ui.generated.resources.searchingNearbyStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.trip
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripBikeRouteAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapDestinationTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapStationAction
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripNoNearbyAlternative
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripNoStationWithFreeSlotsNearby
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationFull
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationNoLongerHasSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSuggestedAlternative
import com.gcaguilar.biciradar.mobile_ui.generated.resources.whereAreYouGoing
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.components.trip.TripMonitoringActiveCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripMonitoringSetupCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripStationCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.TripUiState
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TripScreen(
  state: TripUiState,
  mobilePlatform: MobileUiPlatform,
  localNotifier: LocalNotifier,
  routeLauncher: RouteLauncher,
  onDismissAlert: () -> Unit,
  onClearTrip: () -> Unit,
  onStopMonitoring: () -> Unit,
  onDurationSelected: (Int) -> Unit,
  onStartMonitoring: () -> Unit,
  onRefreshStations: () -> Unit,
  onOpenDestinationPicker: () -> Unit,
  onOpenStationPicker: () -> Unit,
  paddingValues: PaddingValues,
) {
  val colors = LocalBiziColors.current
  val scope = rememberCoroutineScope()

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
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item("header") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
          Card(
            colors = CardDefaults.cardColors(containerColor = colors.red.copy(alpha = 0.09f)),
            border = BorderStroke(1.dp, colors.red.copy(alpha = 0.22f)),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
      }

      item("hero") {
        Card(
          colors = CardDefaults.cardColors(containerColor = colors.surface),
        ) {
          Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = stringResource(Res.string.whereAreYouGoing),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text =
                if (state.destination == null) {
                  stringResource(Res.string.tripSubtitle)
                } else {
                  state.destination?.name.orEmpty()
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
      }

      state.destination?.let { destination ->
        item("destination-summary") {
          Card(
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.dp, colors.panel),
          ) {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = colors.red,
                modifier = Modifier.size(22.dp),
              )
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
          Card(colors = CardDefaults.cardColors(containerColor = colors.surface)) {
            Row(
              modifier =
                Modifier
                  .fillMaxWidth()
                  .padding(20.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
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
          Card(
            colors = CardDefaults.cardColors(containerColor = colors.red.copy(alpha = 0.07f)),
            border = BorderStroke(1.dp, colors.red.copy(alpha = 0.18f)),
          ) {
            Text(
              text = state.searchError ?: stringResource(Res.string.tripNoStationWithFreeSlotsNearby),
              modifier = Modifier.padding(14.dp),
              style = MaterialTheme.typography.bodyMedium,
              color = colors.red,
            )
          }
        }
      }

      val suggestedStation = state.nearestStationWithSlots
      if (suggestedStation != null && !state.isSearchingStation) {
        item("station") {
          Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            TripStationCard(
              station = suggestedStation,
              distanceMeters = state.distanceToStation,
            )
            Button(
              onClick = { routeLauncher.launchBikeToLocation(suggestedStation.location) },
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
              onStartMonitoring = {
                scope.launch {
                  if (localNotifier.requestPermission()) {
                    onStartMonitoring()
                  }
                }
              },
            )
          }
        }
      }
    }
  }
}
