package com.gcaguilar.biciradar.mobileui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.MapFilter
import com.gcaguilar.biciradar.mobileui.components.BiziSelectableChip
import org.jetbrains.compose.resources.stringResource

/**
 * Panel de filtros del mapa con chips seleccionables.
 *
 * @param activeFilters Conjunto de filtros actualmente activos
 * @param onToggleFilter Callback cuando se alterna un filtro
 */
@Composable
internal fun MapFiltersPanel(
  activeFilters: Set<MapFilter>,
  availableFilters: Set<MapFilter>,
  onToggleFilter: (MapFilter) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    MapFilter.entries.filter { it in availableFilters }.forEach { filter ->
      MapFilterChip(
        filter = filter,
        label = stringResource(filter.labelKey),
        selected = filter in activeFilters,
        onClick = { onToggleFilter(filter) },
      )
    }
  }
}

@Composable
private fun MapFilterChip(
  filter: MapFilter,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val c = LocalBiziColors.current
  val accent =
    when (filter) {
      MapFilter.BIKES_AND_SLOTS -> c.green
      MapFilter.ONLY_BIKES -> c.blue
      MapFilter.ONLY_SLOTS -> c.red
      MapFilter.ONLY_EBIKES -> c.orange
      MapFilter.ONLY_REGULAR_BIKES -> c.purple
      MapFilter.AIR_QUALITY -> c.green
      MapFilter.POLLEN -> c.orange
    }
  BiziSelectableChip(
    selected = selected,
    onClick = onClick,
    tint = accent,
  ) { contentColor ->
      MapColorDot(color = accent)
      Text(
        text = label,
        color = contentColor,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.SemiBold,
      )
  }
}

@Composable
private fun MapColorDot(
  color: Color,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .size(10.dp)
        .clip(CircleShape)
        .background(color),
  )
}
