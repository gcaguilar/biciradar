package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.gcaguilar.biciradar.core.TripMode
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripModeCyclist
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripModePedestrian
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Selector global de modo "a pie / en bici" con Material 3.
 *
 * El modo elegido se persiste en `SettingsRepository` y gobierna el orden de las
 * estaciones de la home y los filtros del mapa.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TripModeSelector(
  selectedMode: TripMode,
  onModeSelected: (TripMode) -> Unit,
  mobilePlatform: MobileUiPlatform,
  modifier: Modifier = Modifier,
) {
  val modes = TripMode.entries
  val colors =
    SegmentedButtonDefaults.colors(
      inactiveContainerColor = pageBackgroundColor(mobilePlatform),
    )
  SingleChoiceSegmentedButtonRow(modifier = modifier) {
    modes.forEachIndexed { index, mode ->
      SegmentedButton(
        selected = mode == selectedMode,
        onClick = { onModeSelected(mode) },
        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size),
        colors = colors,
        icon = {
          Icon(
            imageVector = mode.icon(),
            contentDescription = null,
          )
        },
      ) {
        Text(stringResource(mode.labelResource()))
      }
    }
  }
}

private fun TripMode.icon(): ImageVector =
  when (this) {
    TripMode.Pedestrian -> Icons.AutoMirrored.Filled.DirectionsWalk
    TripMode.Cyclist -> Icons.AutoMirrored.Filled.DirectionsBike
  }

private fun TripMode.labelResource(): StringResource =
  when (this) {
    TripMode.Pedestrian -> Res.string.tripModePedestrian
    TripMode.Cyclist -> Res.string.tripModeCyclist
  }
