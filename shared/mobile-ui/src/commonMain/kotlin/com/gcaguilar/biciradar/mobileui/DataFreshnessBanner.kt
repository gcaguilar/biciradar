package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.DataFreshness
import com.gcaguilar.biciradar.core.epochMillisForUi
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

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
  val minutes = remember(lastUpdatedEpoch, minuteTick) {
    lastUpdatedEpoch?.let { last ->
      ((epochMillisForUi() - last).coerceAtLeast(0L) / 60_000L).toInt().coerceAtLeast(1)
    }
  }

  val (containerColor, textColor, message) = when (freshness) {
    DataFreshness.Fresh -> {
      val m = minutes ?: 1
      Triple(
        LocalBiziColors.current.surface,
        LocalBiziColors.current.muted,
        stringResource(Res.string.dataFreshnessUpdatedMinutes, m),
      )
    }
    DataFreshness.StaleUsable -> {
      val m = minutes ?: 1
      Triple(
        LocalBiziColors.current.orange.copy(alpha = 0.12f),
        LocalBiziColors.current.orange,
        stringResource(Res.string.dataFreshnessStale, m),
      )
    }
    DataFreshness.Expired -> Triple(
      LocalBiziColors.current.red.copy(alpha = 0.1f),
      LocalBiziColors.current.red,
      stringResource(Res.string.dataFreshnessExpired),
    )
    DataFreshness.Unavailable -> Triple(
      LocalBiziColors.current.red.copy(alpha = 0.14f),
      LocalBiziColors.current.red,
      stringResource(Res.string.dataFreshnessUnavailable),
    )
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onRefresh),
    color = containerColor,
    shape = MaterialTheme.shapes.small,
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = textColor,
      )
    }
  }
}
