package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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

/**
 * Pill used for filters, toggles and inline actions.
 *
 * Material 3 Expressive feedback: the shape morphs from a full pill to a rounded square
 * when selected and squeezes slightly while pressed; colors follow the theme's
 * [androidx.compose.material3.MotionScheme] effects spec and the shape its spatial spec.
 */
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
  val motion = MaterialTheme.motionScheme
  val interactionSource = remember { MutableInteractionSource() }
  val pressed by interactionSource.collectIsPressedAsState()
  val containerColor by animateColorAsState(
    targetValue = if (selected) selectedContainerColor else unselectedContainerColor,
    animationSpec = motion.fastEffectsSpec(),
    label = "bizi-chip-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) selectedBorderColor else unselectedBorderColor,
    animationSpec = motion.fastEffectsSpec(),
    label = "bizi-chip-border",
  )
  val contentColor by animateColorAsState(
    targetValue = if (selected) selectedContentColor else unselectedContentColor,
    animationSpec = motion.fastEffectsSpec(),
    label = "bizi-chip-content",
  )
  val scale by animateFloatAsState(
    targetValue =
      when {
        pressed -> PRESSED_SCALE
        selected -> selectedScale
        else -> unselectedScale
      },
    animationSpec =
      spring(
        dampingRatio = BiziMotion.chipSelectionDampingRatio,
        stiffness = BiziMotion.chipSelectionStiffness,
      ),
    label = "bizi-chip-scale",
  )
  val cornerPercent by animateIntAsState(
    targetValue = if (selected || pressed) SELECTED_CORNER_PERCENT else UNSELECTED_CORNER_PERCENT,
    animationSpec = motion.fastSpatialSpec(),
    label = "bizi-chip-corner",
  )
  Surface(
    onClick = onClick,
    modifier =
      modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
      },
    shape = RoundedCornerShape(percent = cornerPercent),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
    interactionSource = interactionSource,
  ) {
    Row(
      modifier =
        Modifier
          .padding(horizontal = horizontalPadding, vertical = verticalPadding)
          .animateContentSize(animationSpec = motion.fastSpatialSpec()),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(BiziSpacing.small),
    ) {
      content(contentColor)
    }
  }
}

private const val UNSELECTED_CORNER_PERCENT = 50
private const val SELECTED_CORNER_PERCENT = 30
private const val PRESSED_SCALE = 0.95f
