package com.gcaguilar.biciradar.mobileui.components.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.formatDistance
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.StationMetricPill
import org.jetbrains.compose.resources.stringResource
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.distance
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.freeSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSuggestedStation

@Composable
internal fun TripStationCard(
  station: Station,
  distanceMeters: Int?,
) {
  val c = LocalBiziColors.current
  Card(colors = CardDefaults.cardColors(containerColor = c.surface)) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          Icons.AutoMirrored.Filled.DirectionsBike,
          contentDescription = null,
          tint = c.red,
        )
        Text(
          stringResource(Res.string.tripSuggestedStation),
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = c.muted,
        )
      }
      Text(
        station.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        StationMetricPill(
          label = stringResource(Res.string.freeSlots),
          value = station.slotsFree.toString(),
          tint = c.blue,
        )
        StationMetricPill(
          label = stringResource(Res.string.bikes),
          value = station.bikesAvailable.toString(),
          tint = c.red,
        )
        if (distanceMeters != null) {
          StationMetricPill(
            label = stringResource(Res.string.distance),
            value = formatDistance(distanceMeters),
            tint = c.green,
          )
        }
      }
    }
  }
}
