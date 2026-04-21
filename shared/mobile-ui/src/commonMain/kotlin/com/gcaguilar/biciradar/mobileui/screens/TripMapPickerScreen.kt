package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.GeoPoint
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.destinationPlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.favoritesAvailabilitySummary
import com.gcaguilar.biciradar.mobile_ui.generated.resources.suggestions
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tapMapToPickDestination
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tapMapToPickStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripConfirmDestination
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripConfirmStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapDestinationTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapStationTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSelectedPoint
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.PlatformStationMap
import com.gcaguilar.biciradar.mobileui.components.inputs.StationSearchField
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.TripMapPickerMode
import com.gcaguilar.biciradar.mobileui.viewmodel.TripUiState
import org.jetbrains.compose.resources.stringResource

private fun tripGeoSuggestionSecondaryText(
  name: String,
  address: String,
): String? {
  val trimmedAddress = address.trim()
  if (trimmedAddress.isBlank()) return null
  if (trimmedAddress.equals(name.trim(), ignoreCase = true)) return null
  return trimmedAddress
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TripMapPickerScreen(
  state: TripUiState,
  mobilePlatform: MobileUiPlatform,
  pickerMode: TripMapPickerMode,
  isMapReady: Boolean,
  paddingValues: PaddingValues,
  onBack: () -> Unit,
  onCancelMapPicker: () -> Unit,
  onEnterMapPicker: (TripMapPickerMode) -> Unit,
  onStationPickedFromMap: (Station) -> Unit,
  onLocationPicked: (GeoPoint) -> Unit,
  onQueryChange: (String) -> Unit,
  onSuggestionSelected: (com.gcaguilar.biciradar.core.geo.GeoResult) -> Unit,
  onConfirmMapSelection: () -> Unit,
) {
  val c = LocalBiziColors.current
  var hasActivatedPicker by remember { mutableStateOf(false) }
  val requestClose: () -> Unit = {
    onCancelMapPicker()
    if (!hasActivatedPicker) {
      onBack()
    }
  }

  PlatformBackHandler(
    enabled = true,
    onBack = requestClose,
  )

  LaunchedEffect(pickerMode) {
    onEnterMapPicker(pickerMode)
  }

  LaunchedEffect(state.mapPickerMode) {
    if (state.mapPickerMode == pickerMode) {
      hasActivatedPicker = true
    } else if (hasActivatedPicker && !state.isReverseGeocoding) {
      onBack()
    }
  }

  val titleText =
    if (pickerMode == TripMapPickerMode.Destination) {
      stringResource(Res.string.tripMapDestinationTitle)
    } else {
      stringResource(Res.string.tripMapStationTitle)
    }

  Scaffold(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = titleText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(onClick = requestClose) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(Res.string.back),
            )
          }
        },
      )
    },
    containerColor = pageBackgroundColor(mobilePlatform),
  ) { innerPadding ->
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .padding(innerPadding),
    ) {
      PlatformStationMap(
        modifier = Modifier.fillMaxSize(),
        stations = state.stations,
        userLocation = state.userLocation,
        highlightedStationId = state.selectedMapStation?.id,
        isMapReady = isMapReady,
        onStationSelected = { station ->
          if (pickerMode == TripMapPickerMode.Station) {
            onStationPickedFromMap(station)
          }
        },
        onMapClick =
          if (pickerMode == TripMapPickerMode.Destination) {
            { tappedLocation -> onLocationPicked(tappedLocation) }
          } else {
            null
          },
        pinLocation = state.pickedLocation,
      )

      Column(
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .responsivePageWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text =
            if (pickerMode == TripMapPickerMode.Destination) {
              stringResource(Res.string.tapMapToPickDestination)
            } else {
              stringResource(Res.string.tapMapToPickStation)
            },
          style = MaterialTheme.typography.bodySmall,
          color = c.muted,
        )

        if (pickerMode == TripMapPickerMode.Destination) {
          Card(
            colors = CardDefaults.cardColors(containerColor = c.surface),
            border = BorderStroke(1.dp, c.panel),
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              StationSearchField(
                mobilePlatform = mobilePlatform,
                value = state.query,
                onValueChange = onQueryChange,
                label = stringResource(Res.string.destinationPlaceholder),
              )
              when {
                state.suggestions.isNotEmpty() -> {
                  Text(
                    text = stringResource(Res.string.suggestions),
                    style = MaterialTheme.typography.labelMedium,
                    color = c.muted,
                  )
                  Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    state.suggestions.take(5).forEach { prediction ->
                      Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = c.background,
                        border = BorderStroke(1.dp, c.panel),
                        modifier =
                          Modifier
                            .fillMaxWidth()
                            .clickable {
                              onSuggestionSelected(prediction)
                            },
                      ) {
                        Row(
                          modifier = Modifier.padding(12.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                          Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = c.muted,
                            modifier = Modifier.size(18.dp),
                          )
                          Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                          ) {
                            Text(
                              text = prediction.name,
                              style = MaterialTheme.typography.bodyMedium,
                              fontWeight = FontWeight.SemiBold,
                              maxLines = 1,
                              overflow = TextOverflow.Ellipsis,
                            )
                            tripGeoSuggestionSecondaryText(
                              prediction.name,
                              prediction.address,
                            )?.let { secondaryText ->
                              Text(
                                text = secondaryText,
                                style = MaterialTheme.typography.bodySmall,
                                color = c.muted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }

                state.isLoadingSuggestions -> {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                  ) {
                    CircularProgressIndicator(
                      modifier = Modifier.size(22.dp),
                      strokeWidth = 2.dp,
                      color = c.red,
                    )
                  }
                }

                state.suggestionsError != null -> {
                  Text(
                    text = state.suggestionsError,
                    style = MaterialTheme.typography.bodySmall,
                    color = c.red,
                  )
                }
              }
            }
          }
        }
      }

      if (state.isReverseGeocoding) {
        Box(
          modifier =
            Modifier
              .fillMaxSize()
              .background(c.background.copy(alpha = 0.58f)),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(color = c.red)
        }
      }

      if (state.canConfirmMapSelection) {
        Card(
          modifier =
            Modifier
              .align(Alignment.BottomCenter)
              .responsivePageWidth()
              .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = c.surface),
          border = BorderStroke(1.dp, c.panel),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            if (pickerMode == TripMapPickerMode.Station) {
              val selectedStation = state.selectedMapStation
              if (selectedStation != null) {
                Text(
                  text = selectedStation.name,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
                Text(
                  text =
                    stringResource(
                      Res.string.favoritesAvailabilitySummary,
                      selectedStation.bikesAvailable,
                      selectedStation.slotsFree,
                    ),
                  style = MaterialTheme.typography.bodySmall,
                  color = c.muted,
                )
              }
            } else {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = stringResource(Res.string.tripSelectedPoint),
                  style = MaterialTheme.typography.bodyMedium,
                  color = c.muted,
                )
                Text(
                  text =
                    state.selectedMapLocationLabel
                      ?: state.pickedLocation?.let { "${it.latitude}, ${it.longitude}" }
                      ?: "",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold,
                )
              }
            }
            Button(
              onClick = onConfirmMapSelection,
              modifier = Modifier.fillMaxWidth(),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = if (pickerMode == TripMapPickerMode.Destination) c.red else c.blue,
                ),
            ) {
              Text(
                text =
                  if (pickerMode == TripMapPickerMode.Destination) {
                    stringResource(Res.string.tripConfirmDestination)
                  } else {
                    stringResource(Res.string.tripConfirmStation)
                  },
              )
            }
          }
        }
      }
    }
  }
}
