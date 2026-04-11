package com.gcaguilar.biciradar.mobileui.components.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.details
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mapMyLocation
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource

/**
 * Controles flotantes del mapa (zoom y localización).
 *
 * @param enabled Si el botón de recentrar está habilitado
 * @param showEnvironmentalButton Si mostrar el botón de capa ambiental
 * @param onRecenterClick Callback al presionar recentrar
 * @param onEnvironmentalClick Callback al presionar capa ambiental
 */
@Composable
internal fun MapControls(
  enabled: Boolean,
  showEnvironmentalButton: Boolean,
  onRecenterClick: () -> Unit,
  onEnvironmentalClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (showEnvironmentalButton) {
      MapEnvironmentalSheetButton(
        onClick = onEnvironmentalClick,
      )
    }
    MapRecenterButton(
      enabled = enabled,
      onClick = onRecenterClick,
    )
  }
}

@Composable
private fun MapRecenterButton(
  enabled: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  Surface(
    modifier = Modifier.clickable(enabled = enabled, onClick = onClick),
    shape = CircleShape,
    color = c.surface.copy(alpha = if (enabled) 0.96f else 0.88f),
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Icon(
      imageVector = Icons.Filled.MyLocation,
      contentDescription = stringResource(Res.string.mapMyLocation),
      tint = if (enabled) c.green else c.muted,
      modifier = Modifier.padding(14.dp).size(22.dp),
    )
  }
}

@Composable
private fun MapEnvironmentalSheetButton(onClick: () -> Unit) {
  val c = LocalBiziColors.current
  Surface(
    modifier = Modifier.clickable(onClick = onClick),
    shape = CircleShape,
    color = c.surface.copy(alpha = 0.96f),
    tonalElevation = 4.dp,
    shadowElevation = 6.dp,
  ) {
    Icon(
      imageVector = Icons.Filled.Tune,
      contentDescription = stringResource(Res.string.details),
      tint = c.blue,
      modifier = Modifier.padding(14.dp).size(22.dp),
    )
  }
}
