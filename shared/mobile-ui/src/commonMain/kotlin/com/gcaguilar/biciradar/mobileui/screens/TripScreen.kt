package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.geo.GeoResult
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobileui.DataFreshnessBanner
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.core.LocalNotifier
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformStationMap
import com.gcaguilar.biciradar.core.RouteLauncher
import com.gcaguilar.biciradar.mobileui.components.trip.TripMonitoringActiveCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripMonitoringSetupCard
import com.gcaguilar.biciradar.mobileui.components.trip.TripStationCard
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.TripViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.clear
import com.gcaguilar.biciradar.mobile_ui.generated.resources.clearField
import com.gcaguilar.biciradar.mobile_ui.generated.resources.cancelMap
import com.gcaguilar.biciradar.mobile_ui.generated.resources.destination
import com.gcaguilar.biciradar.mobile_ui.generated.resources.destinationPlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.gotIt
import com.gcaguilar.biciradar.mobile_ui.generated.resources.pickOnMap
import com.gcaguilar.biciradar.mobile_ui.generated.resources.searchingNearbyStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.suggestions
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tapMapToPickDestination
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripNoNearbyAlternative
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationFull
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationNoLongerHasSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSubtitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSuggestedAlternative
import com.gcaguilar.biciradar.mobile_ui.generated.resources.whereAreYouGoing
import com.gcaguilar.biciradar.mobile_ui.generated.resources.walkRouteTo

private fun geoSuggestionSecondaryText(result: GeoResult): String? {
  val address = result.address.trim()
  if (address.isBlank()) return null
  if (address.equals(result.name.trim(), ignoreCase = true)) return null
  return address
}

@Composable
internal fun TripScreen(
  viewModel: TripViewModel,
  mobilePlatform: MobileUiPlatform,
  localNotifier: LocalNotifier,
  routeLauncher: RouteLauncher,
  userLocation: GeoPoint?,
  stations: List<Station>,
  isMapReady: Boolean,
  dataFreshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  stationsLoading: Boolean,
  onRefreshStations: () -> Unit,
  paddingValues: PaddingValues,
) {
  val c = LocalBiziColors.current
  val scope = rememberCoroutineScope()
  val uiState by viewModel.uiState.collectAsState()
  val tripState by viewModel.tripState.collectAsState()

  // ---------- layout ----------
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
      // Map picker — hoisted out of LazyColumn so the native map view is never
      // disposed / recreated by lazy-item recycling on scroll.
      AnimatedVisibility(
        visible = tripState.destination == null && uiState.mapPickerActive,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(animationSpec = tween(140)),
      ) {
        Card(
          colors = CardDefaults.cardColors(containerColor = c.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .height(300.dp),
        ) {
          Box(modifier = Modifier.fillMaxSize()) {
            PlatformStationMap(
              modifier = Modifier.fillMaxSize(),
              stations = stations,
              userLocation = userLocation,
              highlightedStationId = null,
              isMapReady = isMapReady,
              onStationSelected = { station ->
                viewModel.onStationPickedFromMap(station)
              },
              onMapClick = { tappedLocation ->
                viewModel.onLocationPicked(tappedLocation)
              },
              pinLocation = uiState.pickedLocation,
            )
            if (uiState.isReverseGeocoding) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(c.background.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center,
              ) {
                CircularProgressIndicator(color = c.red, modifier = Modifier.size(32.dp))
              }
            } else {
              Surface(
                modifier = Modifier
                  .align(Alignment.TopCenter)
                  .padding(top = 10.dp),
                shape = RoundedCornerShape(20.dp),
                color = c.surface.copy(alpha = 0.92f),
              ) {
                Text(
                  stringResource(Res.string.tapMapToPickDestination),
                  style = MaterialTheme.typography.labelMedium,
                  color = c.muted,
                  modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                )
              }
            }
          }
        }
      }
      LazyColumn(
        modifier = Modifier
          .weight(1f),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item(key = "data-freshness") {
          DataFreshnessBanner(
            freshness = dataFreshness,
            lastUpdatedEpoch = lastUpdatedEpoch,
            loading = stationsLoading,
            onRefresh = onRefreshStations,
          )
        }
    // ---------- ALERT card (State 7) — shown above everything when active ----------
    if (tripState.alert != null) {
      val alert = tripState.alert!!
      item(key = "alert") {
        Card(
          colors = CardDefaults.cardColors(containerColor = c.red.copy(alpha = 0.09f)),
          border = BorderStroke(1.dp, c.red.copy(alpha = 0.22f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(Icons.Filled.Sync, contentDescription = null, tint = c.red)
              Text(
                stringResource(Res.string.tripStationFull),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = c.red,
              )
            }
            Text(
              stringResource(Res.string.tripStationNoLongerHasSlots, alert.fullStation.name),
              style = MaterialTheme.typography.bodyMedium,
            )
            val altStation = alert.alternativeStation
            if (altStation != null) {
              val dist = alert.alternativeDistanceMeters
              val distText = if (dist != null) " (${dist} m)" else ""
              Text(
                stringResource(Res.string.tripSuggestedAlternative, altStation.name, distText, altStation.slotsFree),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            } else {
              Text(
                stringResource(Res.string.tripNoNearbyAlternative),
                style = MaterialTheme.typography.bodySmall,
                color = c.muted,
              )
            }
            Button(
              onClick = { viewModel.onDismissAlert() },
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(stringResource(Res.string.gotIt))
            }
          }
        }
      }
    }

    // ---------- DESTINATION INPUT (State 1) — shown when no destination yet ----------
    if (tripState.destination == null) {
      item(key = "destination-input") {
        Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              stringResource(Res.string.whereAreYouGoing),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              stringResource(Res.string.tripSubtitle),
              style = MaterialTheme.typography.bodySmall,
              color = c.muted,
            )
            OutlinedTextField(
              value = uiState.query,
              onValueChange = { viewModel.onQueryChange(it) },
              modifier = Modifier.fillMaxWidth(),
              label = { Text(stringResource(Res.string.destination)) },
              placeholder = { Text(stringResource(Res.string.destinationPlaceholder)) },
              singleLine = true,
              leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
              },
              trailingIcon = {
                if (uiState.query.isNotEmpty()) {
                  IconButton(onClick = { viewModel.onClearQuery() }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(Res.string.clearField))
                  }
                }
              },
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = c.red,
                unfocusedBorderColor = c.panel,
                cursorColor = c.red,
                focusedTextColor = c.ink,
                unfocusedTextColor = c.ink,
                focusedLabelColor = c.ink,
                unfocusedLabelColor = c.muted,
                focusedPlaceholderColor = c.muted,
                unfocusedPlaceholderColor = c.muted,
                focusedLeadingIconColor = c.muted,
                unfocusedLeadingIconColor = c.muted,
                focusedTrailingIconColor = c.muted,
                unfocusedTrailingIconColor = c.muted,
              ),
            )
            OutlinedButton(
              onClick = { viewModel.onMapPickerToggle() },
              modifier = Modifier.fillMaxWidth(),
              border = BorderStroke(1.dp, c.blue.copy(alpha = if (uiState.mapPickerActive) 0.30f else 0.22f)),
              colors = ButtonDefaults.outlinedButtonColors(
                containerColor = if (uiState.mapPickerActive) c.blue.copy(alpha = 0.08f) else Color.Transparent,
              ),
            ) {
              Icon(
                if (uiState.mapPickerActive) Icons.Filled.Close else Icons.Filled.Map,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = c.blue,
              )
              Spacer(Modifier.width(6.dp))
              Text(
                if (uiState.mapPickerActive) {
                  stringResource(Res.string.cancelMap)
                } else {
                  stringResource(Res.string.pickOnMap)
                },
                color = c.blue,
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      // Autocomplete suggestions
      if (uiState.suggestions.isNotEmpty()) {
        item(key = "suggestions-header") {
          Text(
            stringResource(Res.string.suggestions),
            style = MaterialTheme.typography.labelMedium,
            color = c.muted,
          )
        }
        items(uiState.suggestions, key = { it.id }) { prediction ->
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = c.surface,
            border = BorderStroke(1.dp, c.panel),
            modifier = Modifier
              .fillMaxWidth()
              .clickable { viewModel.onSuggestionSelected(prediction) },
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = c.muted,
                modifier = Modifier.size(18.dp),
              )
              Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
              ) {
                Text(
                  prediction.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                geoSuggestionSecondaryText(prediction)?.let { secondaryText ->
                  Text(
                    secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              if (prediction.address.isNotBlank()) {
                Surface(
                  shape = RoundedCornerShape(999.dp),
                  color = c.panel,
                ) {
                  Text(
                    text = stringResource(Res.string.destination),
                    style = MaterialTheme.typography.labelSmall,
                    color = c.muted,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }
        }
      } else if (uiState.isLoadingSuggestions) {
        item(key = "suggestions-loading") {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(24.dp),
              strokeWidth = 2.dp,
              color = c.red,
            )
          }
        }
      } else if (uiState.suggestionsError != null) {
        item(key = "suggestions-error") {
          Text(
            uiState.suggestionsError ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = c.red,
          )
        }
      }
    }

    // ---------- DESTINATION SELECTED section ----------
    if (tripState.destination != null) {
      // Destination header with clear button
      item(key = "destination-header") {
        Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              Icons.Filled.Navigation,
              contentDescription = null,
              tint = c.red,
              modifier = Modifier.size(22.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
              Text(
                stringResource(Res.string.destination),
                style = MaterialTheme.typography.labelSmall,
                color = c.muted,
              )
              Text(
                tripState.destination!!.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
              )
            }
            OutlinedButton(
              onClick = { viewModel.onClearTrip() },
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
              Icon(
                Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(Modifier.width(4.dp))
              Text(stringResource(Res.string.clear), style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }

      // Searching spinner (State 3)
      if (tripState.isSearchingStation) {
        item(key = "searching") {
          Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
              horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = c.red,
              )
              Text(
                stringResource(Res.string.searchingNearbyStation),
                style = MaterialTheme.typography.bodyMedium,
                color = c.muted,
              )
            }
          }
        }
      }

      // Search error
      if (tripState.searchError != null) {
        item(key = "search-error") {
          Card(
            colors = CardDefaults.cardColors(containerColor = c.red.copy(alpha = 0.07f)),
            border = BorderStroke(1.dp, c.red.copy(alpha = 0.18f)),
          ) {
            Text(
              tripState.searchError!!,
              modifier = Modifier.padding(14.dp),
              style = MaterialTheme.typography.bodyMedium,
              color = c.red,
            )
          }
        }
      }
    }

    // Station found card (State 4)
    val station = tripState.nearestStationWithSlots
      if (station != null && !tripState.isSearchingStation) {
        item(key = "station-card") {
          TripStationCard(station = station, distanceMeters = tripState.distanceToStation)
        }

        // Walking route to destination button
        val destination = tripState.destination
        if (destination != null) {
          item(key = "walk-to-destination") {
            OutlinedButton(
              onClick = { routeLauncher.launchWalkToLocation(destination.location) },
              modifier = Modifier.fillMaxWidth(),
              border = BorderStroke(1.dp, c.red.copy(alpha = 0.5f)),
            ) {
              Icon(
                Icons.Filled.Directions,
                contentDescription = null,
                tint = c.red,
                modifier = Modifier.size(18.dp),
              )
              Spacer(Modifier.width(6.dp))
              Text(
                stringResource(Res.string.walkRouteTo, destination.name),
                color = c.red,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Monitoring active (State 6)
        if (tripState.monitoring.isActive) {
          item(key = "monitoring-active") {
            TripMonitoringActiveCard(
              monitoring = tripState.monitoring,
              onStop = { viewModel.onStopMonitoring() },
            )
          }
        } else {
          // Monitoring setup (State 5)
          item(key = "monitoring-setup") {
            TripMonitoringSetupCard(
              selectedDurationSeconds = uiState.selectedDurationSeconds,
              onDurationSelected = { viewModel.onDurationSelected(it) },
              onStartMonitoring = {
                scope.launch {
                  if (localNotifier.requestPermission()) {
                    viewModel.onStartMonitoring()
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
}
