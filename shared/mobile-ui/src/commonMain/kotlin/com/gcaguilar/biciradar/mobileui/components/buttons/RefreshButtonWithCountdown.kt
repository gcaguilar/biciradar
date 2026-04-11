package com.gcaguilar.biciradar.mobileui.components.buttons

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.refreshStations
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource

/**
 * Botón de refresco con cuenta regresiva.
 * Muestra el tiempo restante para la próxima actualización.
 */
@Composable
internal fun RefreshButtonWithCountdown(
  countdown: Int,
  loading: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    IconButton(onClick = onRefresh, enabled = !loading) {
      Icon(Icons.Filled.Sync, contentDescription = stringResource(Res.string.refreshStations))
    }
    if (countdown > 0 && !loading) {
      val minutes = countdown / 60
      val seconds = countdown % 60
      Text(
        text = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s",
        style = MaterialTheme.typography.labelSmall,
        color = LocalBiziColors.current.muted,
      )
    }
  }
}
