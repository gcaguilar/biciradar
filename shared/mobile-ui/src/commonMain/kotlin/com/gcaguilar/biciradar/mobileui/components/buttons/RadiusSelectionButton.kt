package com.gcaguilar.biciradar.mobileui.components.buttons

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.components.BiziSelectableChip

/**
 * Botón de selección de radio de búsqueda.
 */
@Composable
internal fun RadiusSelectionButton(
  modifier: Modifier,
  selected: Boolean,
  label: String,
  onClick: () -> Unit,
) {
  BiziSelectableChip(
    modifier = modifier,
    selected = selected,
    onClick = onClick,
    tint = LocalBiziColors.current.red,
    selectedContainerColor = LocalBiziColors.current.red.copy(alpha = BiziAlpha.selectedTint),
    selectedBorderColor = LocalBiziColors.current.red.copy(alpha = BiziAlpha.strongSelectedBorder),
  ) { contentColor ->
    Text(
      text = label,
      color = contentColor,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}
