package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.LocalBiziColors

@Composable
internal fun RoutePill(
  label: String,
  onClick: () -> Unit,
  onDarkBackground: Boolean = false,
) {
  val c = LocalBiziColors.current
  val pillColor = if (onDarkBackground) c.onAccent else c.blue
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (onDarkBackground) c.onAccent.copy(alpha = 0.14f) else c.blue.copy(alpha = 0.08f),
    border =
      BorderStroke(
        1.dp,
        if (onDarkBackground) c.onAccent.copy(alpha = 0.32f) else c.blue.copy(alpha = 0.16f),
      ),
    modifier = Modifier.clickable(onClick = onClick),
  ) {
    Row(
      modifier =
        Modifier
          .padding(horizontal = 12.dp, vertical = 9.dp)
          .animateContentSize(animationSpec = tween(180)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.DirectionsBike,
        contentDescription = null,
        tint = pillColor,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = pillColor, style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Composable
internal fun FavoritePill(
  active: Boolean,
  onClick: () -> Unit,
  label: String,
) {
  val containerColor by animateFloatAsState(
    targetValue = if (active) 1f else 0f,
    animationSpec = tween(180),
    label = "favorite-pill-container",
  )
  val borderAlpha by animateFloatAsState(
    targetValue = if (active) 0.16f else 1f,
    animationSpec = tween(180),
    label = "favorite-pill-border",
  )
  val scale by animateFloatAsState(
    targetValue = if (active) 1f else 0.97f,
    animationSpec =
      androidx.compose.animation.core
        .spring(dampingRatio = 0.78f, stiffness = 720f),
    label = "favorite-pill-scale",
  )
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = LocalBiziColors.current.red.copy(alpha = 0.10f * containerColor),
    border =
      BorderStroke(
        1.dp,
        if (active) LocalBiziColors.current.red.copy(alpha = 0.16f) else LocalBiziColors.current.panel,
      ),
    modifier =
      Modifier
        .graphicsLayer {
          scaleX = scale
          scaleY = scale
        }.clickable(onClick = onClick),
  ) {
    Row(
      modifier =
        Modifier
          .padding(horizontal = 12.dp, vertical = 9.dp)
          .animateContentSize(animationSpec = tween(180)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Icon(
        imageVector = if (active) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
        contentDescription = null,
        tint = LocalBiziColors.current.red,
        modifier = Modifier.size(16.dp),
      )
      Text(label, color = LocalBiziColors.current.red, style = MaterialTheme.typography.labelMedium)
    }
  }
}

@Composable
internal fun OutlineActionPill(
  label: String,
  tint: Color,
  borderTint: Color,
  onClick: () -> Unit,
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = Color.Transparent,
    border = BorderStroke(1.dp, borderTint),
    modifier = Modifier.clickable(onClick = onClick),
  ) {
    Row(
      modifier =
        Modifier
          .padding(horizontal = 12.dp, vertical = 9.dp)
          .animateContentSize(animationSpec = tween(180)),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Text(
        label,
        color = tint,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
      )
    }
  }
}
