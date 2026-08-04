package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private data class DataFreshnessStyle(
  val containerColor: Color,
  val textColor: Color,
  val dotColor: Color,
  val message: String,
)

@Composable
fun DataFreshnessBanner(
  freshness: DataFreshness,
  lastUpdatedEpoch: Long?,
  loading: Boolean,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (loading) return
  if (freshness == DataFreshness.Fresh && lastUpdatedEpoch == null) return

  var minuteTick by remember { mutableIntStateOf(0) }
  LaunchedEffect(lastUpdatedEpoch) { minuteTick++ }
  LaunchedEffect(Unit) {
    while (true) {
      delay(60_000)
      minuteTick++
    }
  }
  val minutes =
    remember(lastUpdatedEpoch, minuteTick) {
      lastUpdatedEpoch?.let { last ->
        ((epochMillisForUi() - last).coerceAtLeast(0L) / 60_000L).toInt().coerceAtLeast(1)
      }
    }

  val (containerColor, textColor, dotColor, message) =
    when (freshness) {
      DataFreshness.Fresh -> {
        val m = minutes ?: 1
        DataFreshnessStyle(
          LocalBiziColors.current.surface,
          LocalBiziColors.current.muted,
          LocalBiziColors.current.green,
          stringResource(Res.string.dataFreshnessUpdatedMinutes, m),
        )
      }
      DataFreshness.StaleUsable -> {
        val m = minutes ?: 1
        DataFreshnessStyle(
          LocalBiziColors.current.orange.copy(alpha = 0.12f),
          LocalBiziColors.current.orange,
          LocalBiziColors.current.orange,
          stringResource(Res.string.dataFreshnessStale, m),
        )
      }
      DataFreshness.Expired ->
        DataFreshnessStyle(
          LocalBiziColors.current.red.copy(alpha = 0.1f),
          LocalBiziColors.current.red,
          LocalBiziColors.current.red,
          stringResource(Res.string.dataFreshnessExpired),
        )
      DataFreshness.Unavailable ->
        DataFreshnessStyle(
          LocalBiziColors.current.red.copy(alpha = 0.14f),
          LocalBiziColors.current.red,
          LocalBiziColors.current.red,
          stringResource(Res.string.dataFreshnessUnavailable),
        )
    }

  Surface(
    modifier =
      modifier
        .fillMaxWidth()
        .clickable(onClick = onRefresh),
    color = containerColor,
    shape = MaterialTheme.shapes.small,
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(horizontal = BiziSpacing.xLarge, vertical = BiziSpacing.medium),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(BiziSpacing.small, Alignment.CenterHorizontally),
    ) {
      Box(
        modifier =
          Modifier
            .size(6.dp)
            .background(dotColor, CircleShape),
      )
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
      )
    }
  }
}
