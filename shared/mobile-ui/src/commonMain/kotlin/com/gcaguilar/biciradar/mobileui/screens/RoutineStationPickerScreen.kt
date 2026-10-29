package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineAddStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineNoFavorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routinePickFavorites
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routinePickMap
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routinePickSearch
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineSearchStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.slots
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformStationMap
import com.gcaguilar.biciradar.mobileui.components.inputs.StationSearchField
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.RoutineEditorUiState
import com.gcaguilar.biciradar.mobileui.viewmodel.RoutineStationPickerMode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineStationPickerScreen(
  state: RoutineEditorUiState,
  mobilePlatform: MobileUiPlatform,
  isMapReady: Boolean,
  paddingValues: PaddingValues,
  onBack: () -> Unit,
  onModeChange: (RoutineStationPickerMode) -> Unit,
  onQueryChange: (String) -> Unit,
  onStationPicked: (Station) -> Unit,
) {
  val mode = state.pickerMode ?: RoutineStationPickerMode.Favorites
  Scaffold(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(paddingValues),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = stringResource(Res.string.routineAddStation),
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
          .padding(innerPadding)
          .imePadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      when (mode) {
        RoutineStationPickerMode.Map -> {
          PlatformStationMap(
            modifier = Modifier.fillMaxSize(),
            stations = state.allStations,
            userLocation = state.userLocation,
            highlightedStationId = null,
            isMapReady = isMapReady,
            onStationSelected = onStationPicked,
            onMapClick = null,
            pinLocation = null,
          )
        }

        else -> {
          Column(
            modifier =
              Modifier
                .fillMaxSize()
                .responsivePageWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            PickerModeChips(mode = mode, onModeChange = onModeChange)

            if (mode == RoutineStationPickerMode.Search) {
              StationSearchField(
                mobilePlatform = mobilePlatform,
                value = state.pickerQuery,
                onValueChange = onQueryChange,
                label = stringResource(Res.string.routineSearchStation),
              )
            }

            val stations =
              if (mode == RoutineStationPickerMode.Favorites) {
                state.favoriteStations
              } else {
                state.filteredStations
              }

            if (mode == RoutineStationPickerMode.Favorites && stations.isEmpty()) {
              Text(
                text = stringResource(Res.string.routineNoFavorites),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalBiziColors.current.muted,
              )
            }

            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(stations, key = { it.id }) { station ->
                StationPickerRow(
                  station = station,
                  onClick = { onStationPicked(station) },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PickerModeChips(
  mode: RoutineStationPickerMode,
  onModeChange: (RoutineStationPickerMode) -> Unit,
) {
  androidx.compose.foundation.layout.Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RoutineStationPickerMode.entries.forEach { candidate ->
      FilterChip(
        selected = candidate == mode,
        onClick = { onModeChange(candidate) },
        label = { Text(pickerModeLabel(candidate)) },
      )
    }
  }
}

@Composable
private fun pickerModeLabel(mode: RoutineStationPickerMode): String =
  when (mode) {
    RoutineStationPickerMode.Favorites -> stringResource(Res.string.routinePickFavorites)
    RoutineStationPickerMode.Search -> stringResource(Res.string.routinePickSearch)
    RoutineStationPickerMode.Map -> stringResource(Res.string.routinePickMap)
  }

@Composable
private fun StationPickerRow(
  station: Station,
  onClick: () -> Unit,
) {
  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick),
  ) {
    androidx.compose.foundation.layout.Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = station.name,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
        )
        Text(
          text =
            "${stringResource(Res.string.bikes)}: ${station.bikesAvailable} · " +
              "${stringResource(Res.string.slots)}: ${station.slotsFree}",
          style = MaterialTheme.typography.bodySmall,
          color = LocalBiziColors.current.muted,
        )
      }
      TextButton(onClick = onClick) {
        Text(stringResource(Res.string.routineAddStation))
      }
    }
  }
}
