package com.gcaguilar.biciradar.mobileui.components.trip

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.MONITORING_DURATION_OPTIONS_SECONDS
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.startMonitoring
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitorThisStation
import com.gcaguilar.biciradar.mobile_ui.generated.resources.monitorThisStationDescription

@Composable
internal fun TripMonitoringSetupCard(
  selectedDurationSeconds: Int,
  onDurationSelected: (Int) -> Unit,
  onStartMonitoring: () -> Unit,
) {
  val c = LocalBiziColors.current
  Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        stringResource(Res.string.monitorThisStation),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )
      Text(
        stringResource(Res.string.monitorThisStationDescription),
        style = MaterialTheme.typography.bodySmall,
        color = c.muted,
      )
      Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        MONITORING_DURATION_OPTIONS_SECONDS.forEach { durationSeconds ->
          val minutes = durationSeconds / 60
          FilterChip(
            selected = selectedDurationSeconds == durationSeconds,
            onClick = { onDurationSelected(durationSeconds) },
            label = { Text("${minutes} min") },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = c.red.copy(alpha = 0.12f),
              selectedLabelColor = c.red,
            ),
          )
        }
      }
      Button(
        onClick = onStartMonitoring,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text(stringResource(Res.string.startMonitoring))
      }
    }
  }
}
