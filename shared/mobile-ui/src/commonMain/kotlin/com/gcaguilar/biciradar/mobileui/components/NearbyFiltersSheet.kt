package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFilterFavoritesOnly
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersBikeTypeTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersButton
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersFavoritesTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersMaxDistanceTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersReset
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersSortTitle
import com.gcaguilar.biciradar.mobile_ui.generated.resources.nearbyFiltersTitle
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.NearbyBikeType
import com.gcaguilar.biciradar.mobileui.NearbyFilters
import com.gcaguilar.biciradar.mobileui.NearbyMaxDistance
import com.gcaguilar.biciradar.mobileui.NearbySort
import org.jetbrains.compose.resources.stringResource

/** Botón que abre el panel de filtros y orden, con contador de filtros activos. */
@Composable
internal fun NearbyFiltersButton(
  activeCount: Int,
  onClick: () -> Unit,
) {
  OutlinedButton(onClick = onClick) {
    if (activeCount > 0) {
      BadgedBox(
        badge = {
          Badge {
            Text(activeCount.toString())
          }
        },
      ) {
        Icon(Icons.Filled.Tune, contentDescription = null)
      }
    } else {
      Icon(Icons.Filled.Tune, contentDescription = null)
    }
    Spacer(Modifier.width(8.dp))
    Text(stringResource(Res.string.nearbyFiltersButton))
  }
}

/**
 * Panel inferior con los filtros de la pantalla de Cerca: tipo de bicicleta,
 * distancia máxima, favoritas y criterio de orden. Todo con componentes Material3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NearbyFiltersSheet(
  filters: NearbyFilters,
  showBikeTypeFilter: Boolean,
  onBikeTypeSelected: (NearbyBikeType) -> Unit,
  onMaxDistanceSelected: (NearbyMaxDistance) -> Unit,
  onFavoritesOnlyToggled: () -> Unit,
  onSortSelected: (NearbySort) -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit,
) {
  val colors = LocalBiziColors.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = colors.surface,
  ) {
    Column(
      modifier =
        Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp)
          .padding(bottom = 24.dp)
          .navigationBarsPadding(),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = stringResource(Res.string.nearbyFiltersTitle),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )

      if (showBikeTypeFilter) {
        FilterChipSection(title = stringResource(Res.string.nearbyFiltersBikeTypeTitle)) {
          NearbyBikeType.entries.forEach { option ->
            SingleChoiceFilterChip(
              label = stringResource(option.labelKey),
              selected = option == filters.bikeType,
              onClick = { onBikeTypeSelected(option) },
            )
          }
        }
      }

      FilterChipSection(title = stringResource(Res.string.nearbyFiltersMaxDistanceTitle)) {
        NearbyMaxDistance.entries.forEach { option ->
          SingleChoiceFilterChip(
            label = stringResource(option.labelKey),
            selected = option == filters.maxDistance,
            onClick = { onMaxDistanceSelected(option) },
          )
        }
      }

      FilterChipSection(title = stringResource(Res.string.nearbyFiltersFavoritesTitle)) {
        SingleChoiceFilterChip(
          label = stringResource(Res.string.nearbyFilterFavoritesOnly),
          selected = filters.favoritesOnly,
          onClick = onFavoritesOnlyToggled,
        )
      }

      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle(stringResource(Res.string.nearbyFiltersSortTitle))
        NearbySort.entries.forEach { option ->
          SortOptionRow(
            label = stringResource(option.labelKey),
            selected = option == filters.sort,
            onClick = { onSortSelected(option) },
          )
        }
      }

      if (!filters.isDefault) {
        TextButton(
          onClick = onReset,
          modifier = Modifier.align(Alignment.End),
        ) {
          Text(stringResource(Res.string.nearbyFiltersReset))
        }
      }
    }
  }
}

@Composable
private fun FilterChipSection(
  title: String,
  content: @Composable RowScope.() -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    SectionTitle(title)
    Row(
      modifier = Modifier.horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      content = content,
    )
  }
}

@Composable
private fun SectionTitle(text: String) {
  Text(
    text = text,
    style = MaterialTheme.typography.titleSmall,
    fontWeight = FontWeight.SemiBold,
    color = LocalBiziColors.current.muted,
  )
}

@Composable
private fun SingleChoiceFilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  val colors = LocalBiziColors.current
  FilterChip(
    selected = selected,
    onClick = onClick,
    label = { Text(label) },
    colors =
      FilterChipDefaults.filterChipColors(
        selectedContainerColor = colors.red,
        selectedLabelColor = colors.onAccent,
      ),
  )
}

@Composable
private fun SortOptionRow(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .clickable(onClick = onClick)
        .padding(vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    RadioButton(selected = selected, onClick = onClick)
    Text(text = label, style = MaterialTheme.typography.bodyLarge)
  }
}
