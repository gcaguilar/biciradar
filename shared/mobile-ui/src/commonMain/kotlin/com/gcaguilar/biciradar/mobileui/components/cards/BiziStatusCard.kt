package com.gcaguilar.biciradar.mobileui.components.cards

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.mobileui.BiziAlpha
import com.gcaguilar.biciradar.mobileui.BiziSpacing

@Composable
internal fun BiziStatusCard(
  tint: Color,
  modifier: Modifier = Modifier,
  containerAlpha: Float = 0.08f,
  borderAlpha: Float = BiziAlpha.selectedBorder,
  contentPadding: Dp = BiziSpacing.screenPadding,
  contentSpacing: Dp = BiziSpacing.large,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = containerAlpha)),
    border = BorderStroke(1.dp, tint.copy(alpha = borderAlpha)),
  ) {
    Column(
      modifier = Modifier.padding(contentPadding),
      verticalArrangement = Arrangement.spacedBy(contentSpacing),
      content = content,
    )
  }
}
