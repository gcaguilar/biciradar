package com.gcaguilar.biciradar.mobileui.components.trip

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.TripMonitoringState
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitoringActive
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitoringActiveDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitoringChecksEvery30s
import com.gcaguilar.biciradar.mobile_ui.generated.resources.remainingTimeLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stopMonitoring
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TripMonitoringActiveCard(
  monitoring: TripMonitoringState,
  onStop: () -> Unit,
) {
  // Nota: en este design system `colors.red` es el azul primario de marca (#1D74BD).
  // Lo usamos aquí para el tinte "azul" de la tarjeta de vigilancia, igual que hace
  // el resto de la app para acentos primarios (ver StationRow / TripStationCard).
  val c = LocalBiziColors.current
  val remaining = monitoring.remainingSeconds
  val total = monitoring.totalSeconds
  val minutes = remaining / 60
  val seconds = remaining % 60
  val progress = if (total > 0) remaining.toFloat() / total.toFloat() else 0f

  Card(
    colors = CardDefaults.cardColors(containerColor = c.red.copy(alpha = 0.07f)),
    border = BorderStroke(1.dp, c.red.copy(alpha = BiziAlpha.selectedBorder)),
  ) {
    Column(
      modifier = Modifier.padding(BiziSpacing.screenPadding),
      verticalArrangement = Arrangement.spacedBy(BiziSpacing.large),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BiziSpacing.medium),
      ) {
        CircularProgressIndicator(
          progress = { 1f - progress },
          modifier = Modifier.size(28.dp),
          strokeWidth = 3.dp,
          color = c.red,
          trackColor = c.red.copy(alpha = BiziAlpha.accentTrack),
        )
        Column {
          Text(
            stringResource(Res.string.monitoringActive),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = c.red,
          )
          Text(
            stringResource(Res.string.monitoringChecksEvery30s),
            style = MaterialTheme.typography.bodySmall,
            color = c.muted,
          )
        }
      }
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BiziSpacing.small),
      ) {
        Text(
          text = stringResource(Res.string.remainingTimeLabel).uppercase(),
          style = MaterialTheme.typography.labelMedium,
          color = c.muted,
          textAlign = TextAlign.Center,
        )
        Text(
          text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
          style = MaterialTheme.typography.displaySmall,
          fontWeight = FontWeight.Bold,
          color = c.red,
          textAlign = TextAlign.Center,
        )
        LinearProgressIndicator(
          progress = { progress },
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(top = BiziSpacing.xSmall),
          color = c.red,
          trackColor = c.red.copy(alpha = BiziAlpha.accentTrack),
        )
      }
      Text(
        stringResource(Res.string.monitoringActiveDescription),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
      )
      OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, c.red),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = c.red),
      ) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(BiziSpacing.small))
        Text(stringResource(Res.string.stopMonitoring))
      }
    }
  }
}
