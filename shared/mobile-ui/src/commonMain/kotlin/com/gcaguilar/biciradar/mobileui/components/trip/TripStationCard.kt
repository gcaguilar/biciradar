package com.gcaguilar.biciradar.mobileui.components.trip

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
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
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.bikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.freeSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripStationMetersLabel
import com.gcaguilar.biciradar.mobile_ui.generated.resources.tripSuggestedStation
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.components.favorites.AvailabilityStatCell
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TripStationCard(
  station: Station,
  distanceMeters: Int?,
) {
  // Nota: en este design system `colors.red` es el azul primario de marca (#1D74BD) y
  // `colors.blue` es un tono neutro/tinta oscura — mismos roles que usa StationRow.
  val c = LocalBiziColors.current
  BiziCard {
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
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AvailabilityStatCell(
          modifier = Modifier.weight(1f),
          value = station.slotsFree.toString(),
          caption = stringResource(Res.string.freeSlots),
          tint = c.blue,
        )
        AvailabilityStatCell(
          modifier = Modifier.weight(1f),
          value = station.bikesAvailable.toString(),
          caption = stringResource(Res.string.bikes),
          tint = c.red,
        )
        if (distanceMeters != null) {
          AvailabilityStatCell(
            modifier = Modifier.weight(1f),
            value = formatDistance(distanceMeters),
            caption = stringResource(Res.string.tripStationMetersLabel),
            tint = c.green,
          )
        }
      }
    }
  }
}
