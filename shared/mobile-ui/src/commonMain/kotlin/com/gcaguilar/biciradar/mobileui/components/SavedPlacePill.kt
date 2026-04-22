package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.components.BiziSelectableChip
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SavedPlacePill(
  active: Boolean,
  label: String,
  onClick: () -> Unit,
) {
  val tint =
    if (label ==
      stringResource(Res.string.home)
    ) {
      LocalBiziColors.current.green
    } else {
      LocalBiziColors.current.blue
    }
  BiziSelectableChip(
    selected = active,
    onClick = onClick,
    tint = tint,
    selectedContainerColor = tint.copy(alpha = BiziAlpha.selectedTint),
    selectedBorderColor = tint.copy(alpha = BiziAlpha.selectedBorder),
    unselectedBorderColor = LocalBiziColors.current.panel,
    selectedContentColor = tint,
    unselectedContentColor = tint,
    selectedScale = 1f,
    unselectedScale = 0.97f,
  ) {
    Text(
      text = label,
      color = tint,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}
