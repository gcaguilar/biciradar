package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
internal fun biziCardColors(): CardColors =
  CardDefaults.cardColors(containerColor = LocalBiziColors.current.surface)

@Composable
internal fun biziCardElevation(): CardElevation =
  CardDefaults.cardElevation(
    defaultElevation = 0.dp,
    pressedElevation = 1.dp,
    focusedElevation = 0.dp,
    hoveredElevation = 0.dp,
  )

@Composable
internal fun biziCardBorder(): BorderStroke =
  BorderStroke(
    width = 1.dp,
    color =
      if (LocalIsDarkTheme.current) {
        LocalBiziColors.current.panel.copy(alpha = 0.90f)
      } else {
        LocalBiziColors.current.panel.copy(alpha = 0.65f)
      },
  )

@Composable
internal fun BiziCard(
  modifier: Modifier = Modifier,
  shape: Shape = LocalBiziCardShape.current,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = modifier,
    shape = shape,
    colors = biziCardColors(),
    border = biziCardBorder(),
    elevation = biziCardElevation(),
    content = content,
  )
}
