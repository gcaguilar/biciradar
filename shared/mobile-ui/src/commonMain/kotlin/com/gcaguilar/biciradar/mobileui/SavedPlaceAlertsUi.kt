package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.alertPresetBikesAtLeast1
import com.gcaguilar.biciradar.mobile_ui.generated.resources.alertPresetBikesZero
import com.gcaguilar.biciradar.mobile_ui.generated.resources.alertPresetDocksAtLeast1
import com.gcaguilar.biciradar.mobile_ui.generated.resources.alertPresetDocksZero
import com.gcaguilar.biciradar.mobile_ui.generated.resources.back
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsConditionTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsEmpty
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsRemove
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTargetFavorite
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTargetHome
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTargetWork
import com.gcaguilar.biciradar.mobile_ui.generated.resources.savedPlaceAlertsTitle
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun savedPlaceAlertTargetLabel(target: SavedPlaceAlertTarget): String = when (target) {
  is SavedPlaceAlertTarget.Home -> stringResource(Res.string.savedPlaceAlertsTargetHome)
  is SavedPlaceAlertTarget.Work -> stringResource(Res.string.savedPlaceAlertsTargetWork)
  is SavedPlaceAlertTarget.FavoriteStation ->
    stringResource(Res.string.savedPlaceAlertsTargetFavorite, target.stationName ?: target.stationId)
  is SavedPlaceAlertTarget.CategoryStation ->
    target.categoryLabel ?: target.categoryId
}

@Composable
private fun savedPlaceAlertConditionLabel(condition: SavedPlaceAlertCondition): String = when (condition) {
  is SavedPlaceAlertCondition.BikesAtLeast ->
    if (condition.count == 1) stringResource(Res.string.alertPresetBikesAtLeast1) else "${condition.count}+"
  is SavedPlaceAlertCondition.DocksAtLeast ->
    if (condition.count == 1) stringResource(Res.string.alertPresetDocksAtLeast1) else "${condition.count}+"
  SavedPlaceAlertCondition.BikesEqualsZero -> stringResource(Res.string.alertPresetBikesZero)
  SavedPlaceAlertCondition.DocksEqualsZero -> stringResource(Res.string.alertPresetDocksZero)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavedPlaceAlertEditorSheet(
  target: SavedPlaceAlertTarget,
  existingRule: SavedPlaceAlertRule?,
  onDismiss: () -> Unit,
  onSave: (SavedPlaceAlertCondition) -> Unit,
  onRemove: () -> Unit,
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
  ) {
    Column(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp)
        .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        savedPlaceAlertTargetLabel(target),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        stringResource(Res.string.savedPlaceAlertsConditionTitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(Modifier.padding(8.dp))
      val presets: List<Pair<String, SavedPlaceAlertCondition>> = listOf(
        stringResource(Res.string.alertPresetBikesAtLeast1) to SavedPlaceAlertCondition.BikesAtLeast(1),
        stringResource(Res.string.alertPresetDocksAtLeast1) to SavedPlaceAlertCondition.DocksAtLeast(1),
        stringResource(Res.string.alertPresetBikesZero) to SavedPlaceAlertCondition.BikesEqualsZero,
        stringResource(Res.string.alertPresetDocksZero) to SavedPlaceAlertCondition.DocksEqualsZero,
      )
      presets.forEach { (label, cond) ->
        TextButton(
          onClick = {
            onSave(cond)
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(label, modifier = Modifier.fillMaxWidth())
        }
      }
      if (existingRule != null) {
        TextButton(
          onClick = {
            onRemove()
            onDismiss()
          },
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            stringResource(Res.string.savedPlaceAlertsRemove),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      }
      Spacer(Modifier.padding(16.dp))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SavedPlaceAlertsListScreen(
  mobilePlatform: MobileUiPlatform,
  rules: List<SavedPlaceAlertRule>,
  paddingValues: PaddingValues,
  onBack: () -> Unit,
  onSetEnabled: (String, Boolean) -> Unit,
  onUpsert: (SavedPlaceAlertTarget, SavedPlaceAlertCondition) -> Unit,
  onRemoveRule: (String) -> Unit,
) {
  PlatformBackHandler(enabled = true, onBack = onBack)
  var editor: Pair<SavedPlaceAlertTarget, SavedPlaceAlertRule?>? by remember { mutableStateOf(null) }
  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .padding(paddingValues),
    topBar = {
      TopAppBar(
        title = {
          if (mobilePlatform == MobileUiPlatform.IOS) {
            Text("")
          } else {
            Text(stringResource(Res.string.savedPlaceAlertsTitle))
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.back))
          }
        },
      )
    },
    containerColor = pageBackgroundColor(mobilePlatform),
  ) { inner ->
    Column(
      Modifier
        .fillMaxSize()
        .padding(inner),
    ) {
      if (mobilePlatform == MobileUiPlatform.IOS) {
        Text(
          stringResource(Res.string.savedPlaceAlertsTitle),
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
      }
      if (rules.isEmpty()) {
        Text(
          stringResource(Res.string.savedPlaceAlertsEmpty),
          style = MaterialTheme.typography.bodyMedium,
          color = LocalBiziColors.current.muted,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
        )
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          items(rules, key = { it.id }) { rule ->
            Card(
              colors = CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface),
              modifier = Modifier.clickable { editor = rule.target to rule },
            ) {
              Row(
                Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(Modifier.weight(1f)) {
                  Text(
                    savedPlaceAlertTargetLabel(rule.target),
                    fontWeight = FontWeight.SemiBold,
                  )
                  Text(
                    savedPlaceAlertConditionLabel(rule.condition),
                    style = MaterialTheme.typography.bodySmall,
                    color = LocalBiziColors.current.muted,
                  )
                }
                Switch(
                  checked = rule.isEnabled,
                  onCheckedChange = { onSetEnabled(rule.id, it) },
                )
              }
            }
          }
        }
      }
    }
  }
  editor?.let { (target, rule) ->
    SavedPlaceAlertEditorSheet(
      target = target,
      existingRule = rule,
      onDismiss = { editor = null },
      onSave = { cond ->
        onUpsert(target, cond)
        editor = null
      },
      onRemove = {
        val id = rule?.id ?: return@SavedPlaceAlertEditorSheet
        onRemoveRule(id)
        editor = null
      },
    )
  }
}
