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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.TripMonitoringState
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitoringActive
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitoringActiveDescription
import com.gcaguilar.biciradar.mobile_ui.generated.resources.remainingTime
import com.gcaguilar.biciradar.mobile_ui.generated.resources.stopMonitoring
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TripMonitoringActiveCard(
  monitoring: TripMonitoringState,
  onStop: () -> Unit,
) {
  val c = LocalBiziColors.current
  val remaining = monitoring.remainingSeconds
  val total = monitoring.totalSeconds
  val minutes = remaining / 60
  val seconds = remaining % 60
  val progress = if (total > 0) remaining.toFloat() / total.toFloat() else 0f

  Card(
    colors = CardDefaults.cardColors(containerColor = c.blue.copy(alpha = 0.07f)),
    border = BorderStroke(1.dp, c.blue.copy(alpha = 0.18f)),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        CircularProgressIndicator(
          progress = { progress },
          modifier = Modifier.size(22.dp),
          strokeWidth = 3.dp,
          color = c.blue,
          trackColor = c.blue.copy(alpha = 0.15f),
        )
        Text(
          stringResource(Res.string.monitoringActive),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = c.blue,
        )
      }
      Text(
        stringResource(
          Res.string.remainingTime,
          "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
        ),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        stringResource(Res.string.monitoringActiveDescription),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      OutlinedButton(
        onClick = onStop,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(stringResource(Res.string.stopMonitoring))
      }
    }
  }
}
