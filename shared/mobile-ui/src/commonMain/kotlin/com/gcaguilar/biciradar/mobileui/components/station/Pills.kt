package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.BiziMotion
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.components.BiziSelectableChip

@Composable
internal fun RoutePill(
  label: String,
  onClick: () -> Unit,
  onDarkBackground: Boolean = false,
) {
  val c = LocalBiziColors.current
  val pillColor = if (onDarkBackground) c.onAccent else c.blue
  BiziSelectableChip(
    selected = true,
    onClick = onClick,
    tint = pillColor,
    selectedContainerColor = if (onDarkBackground) c.onAccent.copy(alpha = 0.14f) else c.blue.copy(alpha = 0.08f),
    selectedBorderColor = if (onDarkBackground) c.onAccent.copy(alpha = 0.32f) else c.blue.copy(alpha = 0.16f),
    selectedContentColor = pillColor,
    unselectedContainerColor = Color.Transparent,
    unselectedBorderColor = Color.Transparent,
  ) { contentColor ->
      Icon(
        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = contentColor, style = MaterialTheme.typography.labelMedium)
  }
}

@Composable
internal fun FavoritePill(
  active: Boolean,
  onClick: () -> Unit,
  label: String,
) {
  BiziSelectableChip(
    selected = active,
    onClick = onClick,
    tint = LocalBiziColors.current.red,
    selectedContainerColor = LocalBiziColors.current.red.copy(alpha = BiziAlpha.selectedTint),
    selectedBorderColor = LocalBiziColors.current.red.copy(alpha = 0.16f),
    unselectedBorderColor = LocalBiziColors.current.panel,
    selectedContentColor = LocalBiziColors.current.red,
    unselectedContentColor = LocalBiziColors.current.red,
    selectedScale = 1f,
    unselectedScale = 0.97f,
  ) { contentColor ->
      Icon(
        imageVector = if (active) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        contentDescription = null,
        tint = contentColor,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = contentColor, style = MaterialTheme.typography.labelMedium)
  }
}

@Composable
internal fun OutlineActionPill(
  label: String,
  tint: Color,
  borderTint: Color,
  onClick: () -> Unit,
) {
  BiziSelectableChip(
    selected = false,
    onClick = onClick,
    tint = tint,
    selectedContainerColor = Color.Transparent,
    unselectedContainerColor = Color.Transparent,
    selectedBorderColor = borderTint,
    unselectedBorderColor = borderTint,
    selectedContentColor = tint,
    unselectedContentColor = tint,
    selectedScale = 1f,
    unselectedScale = 1f,
  ) {
      Text(
        label,
        color = tint,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
      )
  }
}
