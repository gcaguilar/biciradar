package com.gcaguilar.biciradar.mobileui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.formatRoutineTime
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.cancel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.delete
import com.gcaguilar.biciradar.mobile_ui.generated.resources.edit
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineAccountLocalOnly
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineAccountLogin
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineAccountLogout
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineAccountSynced
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineCreateTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayFri
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayMon
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDaySat
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDaySun
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayThu
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayTue
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDayWed
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDeleteConfirmMessage
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineDeleteConfirmTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineEmptyDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineEmptyTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineMonitoringNote
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routineStepsCount
import com.gcaguilar.biciradar.mobile_ui.generated.resources.routinesTitle
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.PlatformBackHandler
import com.gcaguilar.biciradar.mobileui.components.EmptyStatePlaceholder
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth
import com.gcaguilar.biciradar.mobileui.viewmodel.RoutineListUiState
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoutineListScreen(
  state: RoutineListUiState,
  mobilePlatform: MobileUiPlatform,
  paddingValues: PaddingValues,
  onBack: () -> Unit,
  onCreateRoutine: () -> Unit,
  onEditRoutine: (String) -> Unit,
  onToggleEnabled: (String, Boolean) -> Unit,
  onDeleteRoutine: (String) -> Unit,
  onRetry: () -> Unit,
  onLogin: () -> Unit,
  onLogout: () -> Unit,
) {
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
            text = stringResource(Res.string.routinesTitle),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(Res.string.cancel),
            )
          }
        },
      )
    },
    floatingActionButton = {
      FloatingActionButton(onClick = onCreateRoutine) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(Res.string.routineCreateTitle))
      }
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
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        if (state.isBackendConfigured) {
          item {
            RoutineAccountCard(
              state = state,
              onLogin = onLogin,
              onLogout = onLogout,
            )
          }
        }
        if (state.routines.isEmpty()) {
          item {
            EmptyStatePlaceholder(
              title = stringResource(Res.string.routineEmptyTitle),
              description = stringResource(Res.string.routineEmptyDescription),
            )
          }
          item {
            Button(
              onClick = onCreateRoutine,
              modifier = Modifier.fillMaxWidth(),
            ) {
              Text(stringResource(Res.string.routineCreateTitle))
            }
          }
        } else {
          items(state.routines, key = { it.id }) { routine ->
            RoutineListCard(
              routine = routine,
              onEdit = { onEditRoutine(routine.id) },
              onToggleEnabled = { enabled -> onToggleEnabled(routine.id, enabled) },
              onDelete = { onDeleteRoutine(routine.id) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RoutineAccountCard(
  state: RoutineListUiState,
  onLogin: () -> Unit,
  onLogout: () -> Unit,
) {
  val colors = LocalBiziColors.current
  val isAuthenticated = state.authState == AuthState.Authenticated
  Card(modifier = Modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text =
          if (isAuthenticated) {
            stringResource(Res.string.routineAccountSynced)
          } else {
            stringResource(Res.string.routineAccountLocalOnly)
          },
        style = MaterialTheme.typography.bodyMedium,
        color = colors.muted,
        modifier = Modifier.weight(1f),
      )
      TextButton(onClick = if (isAuthenticated) onLogout else onLogin) {
        Text(
          if (isAuthenticated) {
            stringResource(Res.string.routineAccountLogout)
          } else {
            stringResource(Res.string.routineAccountLogin)
          },
        )
      }
    }
  }
}

@Composable
private fun RoutineListCard(
  routine: Routine,
  onEdit: () -> Unit,
  onToggleEnabled: (Boolean) -> Unit,
  onDelete: () -> Unit,
) {
  val colors = LocalBiziColors.current
  var askDelete by remember(routine.id) { mutableStateOf(false) }
  Card(
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          Text(
            text = routine.displayName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = routineScheduleLabel(routine),
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
          )
          Text(
            text =
              stringResource(
                Res.string.routineStepsCount,
                routine.steps.size,
              ),
            style = MaterialTheme.typography.bodySmall,
            color = colors.muted,
          )
        }
        Switch(
          checked = routine.isEnabled,
          onCheckedChange = onToggleEnabled,
        )
      }
      Text(
        text = stringResource(Res.string.routineMonitoringNote),
        style = MaterialTheme.typography.bodySmall,
        color = colors.muted,
      )
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IconButton(onClick = onEdit) {
          Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(Res.string.edit),
          )
        }
        IconButton(onClick = { askDelete = true }) {
          Icon(
            imageVector = Icons.Filled.Delete,
            contentDescription = stringResource(Res.string.delete),
            tint = colors.red,
          )
        }
      }
    }
  }

  if (askDelete) {
    AlertDialog(
      onDismissRequest = { askDelete = false },
      title = { Text(stringResource(Res.string.routineDeleteConfirmTitle)) },
      text = { Text(stringResource(Res.string.routineDeleteConfirmMessage, routine.displayName)) },
      confirmButton = {
        TextButton(
          onClick = {
            askDelete = false
            onDelete()
          },
        ) {
          Text(stringResource(Res.string.delete))
        }
      },
      dismissButton = {
        TextButton(onClick = { askDelete = false }) {
          Text(stringResource(Res.string.cancel))
        }
      },
    )
  }
}

@Composable
private fun routineScheduleLabel(routine: Routine): String {
  val dayLabels =
    mapOf(
      RoutineDay.MONDAY to stringResource(Res.string.routineDayMon),
      RoutineDay.TUESDAY to stringResource(Res.string.routineDayTue),
      RoutineDay.WEDNESDAY to stringResource(Res.string.routineDayWed),
      RoutineDay.THURSDAY to stringResource(Res.string.routineDayThu),
      RoutineDay.FRIDAY to stringResource(Res.string.routineDayFri),
      RoutineDay.SATURDAY to stringResource(Res.string.routineDaySat),
      RoutineDay.SUNDAY to stringResource(Res.string.routineDaySun),
    )
  val days = routine.daysOfWeek.sortedBy { it.isoValue }.joinToString(" ") { dayLabels[it] ?: "" }
  return "$days · ${formatRoutineTime(routine.hour, routine.minute)}"
}
