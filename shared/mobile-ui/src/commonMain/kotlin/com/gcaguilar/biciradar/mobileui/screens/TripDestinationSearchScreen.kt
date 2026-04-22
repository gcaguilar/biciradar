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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.destinationPlaceholder
import com.gcaguilar.biciradar.mobile_ui.generated.resources.suggestions
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripMapDestinationTitle
import com.gcaguilar.biciradar.mobileui.LocalBiziCardShape
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.biziCardBorder
import com.gcaguilar.biciradar.mobileui.biziCardColors
import com.gcaguilar.biciradar.mobileui.biziCardElevation
import com.gcaguilar.biciradar.mobileui.components.inputs.StationSearchField
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.TripUiState
import org.jetbrains.compose.resources.stringResource

private fun tripDestinationSecondaryText(
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
internal fun TripDestinationSearchScreen(
  state: TripUiState,
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  onBack: () -> Unit,
  onQueryChange: (String) -> Unit,
  onSuggestionSelected: (com.gcaguilar.biciradar.core.geo.GeoResult) -> Unit,
) {
  val colors = LocalBiziColors.current

  PlatformBackHandler(enabled = true, onBack = onBack)

  Scaffold(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.tripMapDestinationTitle),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
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
          .background(pageBackgroundColor(mobilePlatform)),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier
            .responsivePageWidth()
            .padding(innerPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
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
                  color = colors.muted,
                )
                Column(
                  modifier = Modifier.fillMaxWidth(),
                  verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                  state.suggestions.take(8).forEach { prediction ->
                    Surface(
                      shape = MaterialTheme.shapes.medium,
                      color = colors.background,
                      border = BorderStroke(1.dp, colors.panel),
                      modifier =
                        Modifier
                          .fillMaxWidth()
                          .clickable {
                            onSuggestionSelected(prediction)
                            onBack()
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
                          tint = colors.muted,
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
                          tripDestinationSecondaryText(
                            prediction.name,
                            prediction.address,
                          )?.let { secondaryText ->
                            Text(
                              text = secondaryText,
                              style = MaterialTheme.typography.bodySmall,
                              color = colors.muted,
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
                    color = colors.red,
                  )
                }
              }

              state.suggestionsError != null -> {
                Text(
                  text = state.suggestionsError,
                  style = MaterialTheme.typography.bodySmall,
                  color = colors.red,
                )
              }
            }
          }
        
      }
    }
  }
}
