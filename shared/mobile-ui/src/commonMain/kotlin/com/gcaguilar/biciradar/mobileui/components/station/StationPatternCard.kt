package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.StationHourlyPattern
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.averageBikesHour
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mostBikes
import com.gcaguilar.biciradar.mobile_ui.generated.resources.mostSlots
import com.gcaguilar.biciradar.mobile_ui.generated.resources.noDataForDayType
import com.gcaguilar.biciradar.mobile_ui.generated.resources.noUsagePatternData
import com.gcaguilar.biciradar.mobile_ui.generated.resources.usagePattern
import com.gcaguilar.biciradar.mobileui.BiziCard
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
internal fun StationPatternCard(
  patterns: List<StationHourlyPattern>,
  isLoading: Boolean,
  isError: Boolean,
  showWeekend: Boolean,
  onToggleDayType: () -> Unit,
) {
  val colors = LocalBiziColors.current
  BiziCard {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(stringResource(Res.string.usagePattern), fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = !showWeekend,
            onClick = { if (showWeekend) onToggleDayType() },
            label = { Text("L-V", style = MaterialTheme.typography.labelSmall) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.red,
                selectedLabelColor = colors.onAccent,
              ),
          )
          FilterChip(
            selected = showWeekend,
            onClick = { if (!showWeekend) onToggleDayType() },
            label = { Text("S-D", style = MaterialTheme.typography.labelSmall) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = colors.red,
                selectedLabelColor = colors.onAccent,
              ),
          )
        }
      }
      when {
        isLoading -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(color = colors.red, modifier = Modifier.size(24.dp))
          }
        }
        isError || patterns.isEmpty() -> {
          Box(
            modifier = Modifier.fillMaxWidth().height(80.dp),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              stringResource(Res.string.noUsagePatternData),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
          }
        }
        else -> {
          val dayType = if (showWeekend) "WEEKEND" else "WEEKDAY"
          val filtered = patterns.filter { it.dayType == dayType }.sortedBy { it.hour }
          if (filtered.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxWidth().height(80.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                stringResource(Res.string.noDataForDayType),
                style = MaterialTheme.typography.bodySmall,
                color = colors.muted,
              )
            }
          } else {
            Text(
              stringResource(Res.string.averageBikesHour),
              style = MaterialTheme.typography.bodySmall,
              color = colors.muted,
            )
            StationPatternChart(
              patterns = filtered,
              modifier = Modifier.fillMaxWidth().height(160.dp),
            )
            val bestBikesHour = filtered.maxByOrNull { it.bikesAvg }
            val bestSlotsHour = filtered.maxByOrNull { it.anchorsAvg }
            if (bestBikesHour != null && bestSlotsHour != null) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                PatternHintPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.mostBikes),
                  value = "${bestBikesHour.hour}:00h (~${bestBikesHour.bikesAvg.roundToInt()})",
                  tint = colors.red,
                )
                PatternHintPill(
                  modifier = Modifier.weight(1f),
                  label = stringResource(Res.string.mostSlots),
                  value = "${bestSlotsHour.hour}:00h (~${bestSlotsHour.anchorsAvg.roundToInt()})",
                  tint = colors.blue,
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PatternHintPill(
  label: String,
  value: String,
  tint: androidx.compose.ui.graphics.Color,
  modifier: Modifier = Modifier,
) {
  androidx.compose.material3.Surface(
    modifier = modifier,
    shape =
      MaterialTheme.shapes.medium,
    color = tint.copy(alpha = 0.12f),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
      Text(
        value,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
      )
    }
  }
}
