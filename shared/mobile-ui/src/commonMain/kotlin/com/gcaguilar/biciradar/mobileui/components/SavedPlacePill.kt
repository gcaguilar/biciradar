package com.gcaguilar.biciradar.mobileui.components

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
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.home
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
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
  val containerColor by animateFloatAsState(
    targetValue = if (active) 1f else 0f,
    animationSpec = tween(180),
    label = "saved-place-pill-container",
  )
  val borderAlpha by animateFloatAsState(
    targetValue = if (active) 0.18f else 1f,
    animationSpec = tween(180),
    label = "saved-place-pill-border",
  )
  val scale by animateFloatAsState(
    targetValue = if (active) 1f else 0.97f,
    animationSpec = spring(dampingRatio = 0.78f, stiffness = 720f),
    label = "saved-place-pill-scale",
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = tint.copy(alpha = 0.10f * containerColor),
    border = BorderStroke(1.dp, if (active) tint.copy(alpha = 0.18f) else LocalBiziColors.current.panel),
    modifier =
      Modifier
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }.clickable(onClick = onClick),
  ) {
    Text(
      text = label,
      modifier =
        Modifier
          .padding(horizontal = 12.dp, vertical = 9.dp)
          .animateContentSize(animationSpec = tween(180)),
      color = tint,
      style = MaterialTheme.typography.labelMedium,
      fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
    )
  }
}
