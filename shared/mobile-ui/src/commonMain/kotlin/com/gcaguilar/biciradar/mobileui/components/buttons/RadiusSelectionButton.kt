package com.gcaguilar.biciradar.mobileui.components.buttons

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

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
  val containerColor by animateColorAsState(
    targetValue =
      if (selected) {
        LocalBiziColors.current.red.copy(
          alpha = 0.10f,
        )
      } else {
        LocalBiziColors.current.surface
      },
    animationSpec = tween(180),
    label = "radius-container",
  )
  val borderColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red.copy(alpha = 0.25f) else LocalBiziColors.current.panel,
    animationSpec = tween(180),
    label = "radius-border",
  )
  val textColor by animateColorAsState(
    targetValue = if (selected) LocalBiziColors.current.red else LocalBiziColors.current.ink,
    animationSpec = tween(180),
    label = "radius-text",
  )
  val selectionScale by animateFloatAsState(
    targetValue = if (selected) 1f else 0.98f,
    animationSpec = spring(dampingRatio = 0.82f, stiffness = 700f),
    label = "radius-scale",
  )
  Surface(
    modifier =
      modifier
        .graphicsLayer {
          scaleX = selectionScale
          scaleY = selectionScale
        }.clickable(onClick = onClick),
    shape = RoundedCornerShape(18.dp),
    color = containerColor,
    border = BorderStroke(1.dp, borderColor),
  ) {
    Text(
      text = label,
      modifier =
        Modifier
          .padding(horizontal = 14.dp, vertical = 12.dp)
          .animateContentSize(animationSpec = tween(180)),
      color = textColor,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}
