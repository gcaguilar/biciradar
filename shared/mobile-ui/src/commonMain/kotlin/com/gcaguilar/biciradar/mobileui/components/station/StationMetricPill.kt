package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun StationMetricPill(
  modifier: Modifier = Modifier,
  label: String,
  value: String,
  tint: Color,
) {
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.large,
    color = tint.copy(alpha = 0.09f),
  ) {
    Column(
      modifier =
        Modifier
          .padding(horizontal = 12.dp, vertical = 10.dp)
          .animateContentSize(
            animationSpec =
              androidx.compose.animation.core
                .spring(dampingRatio = 0.9f, stiffness = 550f),
          ),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
      Text(
        value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
