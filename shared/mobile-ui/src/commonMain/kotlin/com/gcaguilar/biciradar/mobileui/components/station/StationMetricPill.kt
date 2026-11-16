package com.gcaguilar.biciradar.mobileui.components.station

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
            animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
          ),
      verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      Text(label, style = MaterialTheme.typography.labelMedium, color = tint)
      // The figure is what users scan for: emphasized title type makes it stand out
      // from the label at a glance.
      Text(
        value,
        style = MaterialTheme.typography.titleMediumEmphasized,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
