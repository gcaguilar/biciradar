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
  CardDefaults.cardColors(
    containerColor = LocalBiziColors.current.surface,
    // Disabled cards (e.g. a quick-route card with no station yet) keep their surface;
    // their content already explains why there is nothing to open.
    disabledContainerColor = LocalBiziColors.current.surface,
    disabledContentColor = LocalBiziColors.current.ink,
  )

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

/**
 * Outlined surface card used across the app.
 *
 * Pass [onClick] instead of a `clickable` modifier so the ripple and the pressed state
 * are clipped to the card [shape] and the card is announced as a button.
 */
@Composable
internal fun BiziCard(
  modifier: Modifier = Modifier,
  shape: Shape = LocalBiziCardShape.current,
  onClick: (() -> Unit)? = null,
  enabled: Boolean = true,
  content: @Composable ColumnScope.() -> Unit,
) {
  if (onClick != null) {
    Card(
      onClick = onClick,
      modifier = modifier,
      enabled = enabled,
      shape = shape,
      colors = biziCardColors(),
      border = biziCardBorder(),
      elevation = biziCardElevation(),
      content = content,
    )
  } else {
    Card(
      modifier = modifier,
      shape = shape,
      colors = biziCardColors(),
      border = biziCardBorder(),
      elevation = biziCardElevation(),
      content = content,
    )
  }
}
