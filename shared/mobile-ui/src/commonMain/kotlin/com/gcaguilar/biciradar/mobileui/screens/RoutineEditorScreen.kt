package com.gcaguilar.biciradar.mobileui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.RoutineStep
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineAddStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineCreateTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayFri
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayMon
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDaySat
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDaySun
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayThu
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayTue
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayWed
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDaysLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDurationLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDurationMinutes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineEditTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineKindBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineKindDocks
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineKindLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineMonitoringNote
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineNameLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineRemoveStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineSave
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineStationsLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineTimeLabel
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.RoutineEditorUiState
import com.gcaguilar.biciradar.mobileui.viewmodel.RoutineStationPickerMode
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineEditorScreen(
  state: RoutineEditorUiState,
  mobilePlatform: MobileUiPlatform,
  isMapReady: Boolean,
  paddingValues: PaddingValues,
  onBack: () -> Unit,
  onNameChange: (String) -> Unit,
  onDayToggle: (RoutineDay) -> Unit,
  onTimeChanged: (Int, Int) -> Unit,
  onAddStation: () -> Unit,
  onRemoveStep: (String) -> Unit,
  onStepDurationSelected: (String, Int) -> Unit,
  onStepKindSelected: (String, SurfaceMonitoringKind) -> Unit,
  onSave: () -> Unit,
  onExitPicker: () -> Unit,
  onPickerModeChange: (RoutineStationPickerMode) -> Unit,
  onPickerQueryChange: (String) -> Unit,
  onStationPicked: (com.gcaguilar.biciradar.core.Station) -> Unit,
) {
  if (state.pickerMode != null) {
    PlatformBackHandler(enabled = true, onBack = onExitPicker)
    RoutineStationPickerScreen(
      state = state,
      mobilePlatform = mobilePlatform,
      isMapReady = isMapReady,
      paddingValues = paddingValues,
      onBack = onExitPicker,
      onModeChange = onPickerModeChange,
      onQueryChange = onPickerQueryChange,
      onStationPicked = onStationPicked,
    )
    return
  }

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
            text =
              stringResource(
                if (state.routineId == null) Res.string.routineCreateTitle else Res.string.routineEditTitle,
              ),
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
        actions = {
          TextButton(
            onClick = onSave,
            enabled = state.canSave,
          ) {
            Text(stringResource(Res.string.routineSave))
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
      LazyColumn(
        modifier =
          Modifier
            .fillMaxSize()
            .responsivePageWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
      ) {
        item("name") {
          OutlinedTextField(
            value = state.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.routineNameLabel)) },
            singleLine = true,
          )
        }

        item("days") {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = stringResource(Res.string.routineDaysLabel),
              style = MaterialTheme.typography.labelLarge,
            )
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              RoutineDay.entries.forEach { day ->
                FilterChip(
                  selected = day in state.selectedDays,
                  onClick = { onDayToggle(day) },
                  label = { Text(routineDayLabel(day)) },
                )
              }
            }
          }
        }

        item("time") {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = stringResource(Res.string.routineTimeLabel),
              style = MaterialTheme.typography.labelLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              TimeDropdown(
                label = state.hour.toString().padStart(2, '0'),
                options = (0..23).map { it.toString().padStart(2, '0') },
                onSelected = { onTimeChanged(it.toInt(), state.minute) },
              )
              TimeDropdown(
                label = state.minute.toString().padStart(2, '0'),
                options = (0..55 step 5).map { it.toString().padStart(2, '0') },
                onSelected = { onTimeChanged(state.hour, it.toInt()) },
              )
            }
            Text(
              text = stringResource(Res.string.routineMonitoringNote),
              style = MaterialTheme.typography.bodySmall,
              color = LocalBiziColors.current.muted,
            )
          }
        }

        item("stations-header") {
          Text(
            text = stringResource(Res.string.routineStationsLabel),
            style = MaterialTheme.typography.labelLarge,
          )
        }

        state.steps.forEach { step ->
          item("step-${step.id}") {
            RoutineStepCard(
              step = step,
              onRemove = { onRemoveStep(step.id) },
              onDurationSelected = { onStepDurationSelected(step.id, it) },
              onKindSelected = { onStepKindSelected(step.id, it) },
            )
          }
        }

        item("add-station") {
          OutlinedButton(
            onClick = onAddStation,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(imageVector = Icons.Filled.Add, contentDescription = null)
            Text(
              text = stringResource(Res.string.routineAddStation),
              modifier = Modifier.padding(start = 8.dp),
            )
          }
        }

        item("save") {
          Button(
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(stringResource(Res.string.routineSave))
          }
        }
      }
    }
  }
}

@Composable
private fun RoutineStepCard(
  step: RoutineStep,
  onRemove: () -> Unit,
  onDurationSelected: (Int) -> Unit,
  onKindSelected: (SurfaceMonitoringKind) -> Unit,
) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = step.stationName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onRemove) {
          Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(Res.string.routineRemoveStation),
          )
        }
      }
      Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LabeledDropdown(
          label = stringResource(Res.string.routineDurationLabel),
          value = stringResource(Res.string.routineDurationMinutes, step.durationSeconds / 60),
          options = MONITORING_DURATION_OPTIONS_SECONDS.map { "${it / 60}" },
          onSelected = { index -> onDurationSelected(MONITORING_DURATION_OPTIONS_SECONDS[index]) },
        )
        LabeledDropdown(
          label = stringResource(Res.string.routineKindLabel),
          value =
            when (step.kind) {
              SurfaceMonitoringKind.Bikes -> stringResource(Res.string.routineKindBikes)
              SurfaceMonitoringKind.Docks -> stringResource(Res.string.routineKindDocks)
            },
          options =
            listOf(
              stringResource(Res.string.routineKindBikes),
              stringResource(Res.string.routineKindDocks),
            ),
          onSelected = { index ->
            onKindSelected(
              if (index == 1) SurfaceMonitoringKind.Docks else SurfaceMonitoringKind.Bikes,
            )
          },
        )
      }
    }
  }
}

@Composable
private fun LabeledDropdown(
  label: String,
  value: String,
  options: List<String>,
  onSelected: (Int) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelMedium,
      color = LocalBiziColors.current.muted,
    )
    Box {
      OutlinedButton(onClick = { expanded = true }) {
        Text(value)
        Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
      }
      DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEachIndexed { index, option ->
          DropdownMenuItem(
            text = { Text(option) },
            onClick = {
              expanded = false
              onSelected(index)
            },
          )
        }
      }
    }
  }
}

@Composable
private fun TimeDropdown(
  label: String,
  options: List<String>,
  onSelected: (String) -> Unit,
) {
  var expanded by remember { mutableStateOf(false) }
  Box {
    OutlinedButton(onClick = { expanded = true }) {
      Text(label, style = MaterialTheme.typography.titleMedium)
      Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null)
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier.padding(vertical = 4.dp),
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = { Text(option) },
          onClick = {
            expanded = false
            onSelected(option)
          },
        )
      }
    }
  }
}

@Composable
private fun routineDayLabel(day: RoutineDay): String =
  when (day) {
    RoutineDay.MONDAY -> stringResource(Res.string.routineDayMon)
    RoutineDay.TUESDAY -> stringResource(Res.string.routineDayTue)
    RoutineDay.WEDNESDAY -> stringResource(Res.string.routineDayWed)
    RoutineDay.THURSDAY -> stringResource(Res.string.routineDayThu)
    RoutineDay.FRIDAY -> stringResource(Res.string.routineDayFri)
    RoutineDay.SATURDAY -> stringResource(Res.string.routineDaySat)
    RoutineDay.SUNDAY -> stringResource(Res.string.routineDaySun)
  }
