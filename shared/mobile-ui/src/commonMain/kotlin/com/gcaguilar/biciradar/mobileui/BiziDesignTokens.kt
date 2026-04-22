package com.gcaguilar.biciradar.mobileui

import androidx.compose.animation.core.Spring
import androidx.compose.ui.unit.dp

internal object BiziSpacing {
  val xxSmall = 2.dp
  val xSmall = 4.dp
  val small = 6.dp
  val medium = 8.dp
  val large = 10.dp
  val xLarge = 12.dp
  val xxLarge = 14.dp
  val screenPadding = 16.dp
  val cardPadding = 18.dp
}

internal object BiziAlpha {
  const val subtleTint = 0.10f
  const val selectedTint = 0.10f
  const val selectedBorder = 0.18f
  const val strongSelectedBorder = 0.25f
  const val overlay = 0.58f
  const val accentTrack = 0.15f
}

internal object BiziMotion {
  const val quickDurationMillis = 180
  const val chipSelectionDampingRatio = 0.82f
  const val chipSelectionStiffness = 700f
  const val emphasizedSelectionDampingRatio = 0.78f
  const val emphasizedSelectionStiffness = 720f
}

internal val BiziChipSpring =
  Spring.DampingRatioNoBouncy to Spring.StiffnessMedium
