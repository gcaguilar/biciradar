package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.BiziMotion
import com.gcaguilar.biciradar.mobileui.BiziSpacing
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun BiziSelectableChip(
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  tint: Color,
  selectedContainerColor: Color = tint.copy(alpha = BiziAlpha.selectedTint),
  unselectedContainerColor: Color = LocalBiziColors.current.surface,
  selectedBorderColor: Color = tint.copy(alpha = BiziAlpha.selectedBorder),
  unselectedBorderColor: Color = LocalBiziColors.current.panel,
  selectedContentColor: Color = tint,
  unselectedContentColor: Color = LocalBiziColors.current.ink,
  horizontalPadding: Dp = BiziSpacing.xxLarge,
  verticalPadding: Dp = BiziSpacing.xLarge,
  selectedScale: Float = 1f,
  unselectedScale: Float = 0.98f,
  content: @Composable RowScope.(Color) -> Unit,
) {
  val containerColor by animateColorAsState(
    targetValue = if (selected) selectedContainerColor else unselectedContainerColor,
    animationSpec = tween(BiziMotion.quickDurationMillis),
    label = "bizi-chip-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) selectedBorderColor else unselectedBorderColor,
    animationSpec = tween(BiziMotion.quickDurationMillis),
    label = "bizi-chip-border",
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) selectedContentColor else unselectedContentColor,
    animationSpec = tween(BiziMotion.quickDurationMillis),
    label = "bizi-chip-content",
  )
  val scale by animateFloatAsState(
    targetValue = if (selected) selectedScale else unselectedScale,
    animationSpec =
      spring(
        dampingRatio = BiziMotion.chipSelectionDampingRatio,
        stiffness = BiziMotion.chipSelectionStiffness,
      ),
    label = "bizi-chip-scale",
  )
  Surface(
    modifier =
      modifier
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }.clickable(onClick = onClick),
    shape = MaterialTheme.shapes.large,
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Row(
      modifier =
        Modifier
          .padding(horizontal = horizontalPadding, vertical = verticalPadding)
          .animateContentSize(animationSpec = tween(BiziMotion.quickDurationMillis)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(BiziSpacing.small),
    ) {
      content(contentColor)
    }
  }
}
