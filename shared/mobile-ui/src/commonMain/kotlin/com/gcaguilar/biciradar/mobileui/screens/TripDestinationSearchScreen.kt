package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.components.inputs.SuggestionRow
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
      LazyColumn(
        modifier =
          Modifier
            .fillMaxSize()
            .responsivePageWidth()
            .padding(innerPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        item {
          StationSearchField(
            mobilePlatform = mobilePlatform,
            value = state.query,
            onValueChange = onQueryChange,
            label = stringResource(Res.string.destinationPlaceholder),
          )
        }

        when {
          state.suggestions.isNotEmpty() -> {
            item {
              Text(
                text = stringResource(Res.string.suggestions),
                style = MaterialTheme.typography.labelMedium,
                color = colors.muted
              )
            }

            items(state.suggestions, key = { "${it.name}-${it.address}" }) { prediction ->
              SuggestionRow(
                title = prediction.name,
                secondaryText = tripDestinationSecondaryText(prediction.name, prediction.address),
                onClick = {
                  onSuggestionSelected(prediction)
                  onBack()
                },
              )
            }
          }

          state.isLoadingSuggestions -> {
            item {
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
          }

          state.suggestionsError != null -> {
            item {
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
