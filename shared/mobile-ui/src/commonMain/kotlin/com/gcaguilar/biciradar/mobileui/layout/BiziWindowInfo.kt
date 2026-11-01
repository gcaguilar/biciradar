package com.gcaguilar.biciradar.mobileui.layout

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

/**
 * Width class of the window, mirroring the Material 3 adaptive breakpoints:
 * Compact (< 600dp), Medium (600dp–839dp) and Expanded (>= 840dp).
 *
 * Height is taken into account too: a window shorter than the medium height
 * breakpoint stays [Compact] so landscape phones keep the bottom bar instead of
 * showing a cramped navigation rail.
 */
internal enum class BiziWindowLayout {
  Compact,
  Medium,
  Expanded,
}

/**
 * A display seam reported by the device (foldable hinge, dual screen separator).
 * Coordinates are relative to the window and only the axis that matters for the
 * seam is reported: [start]..[end] on the X axis for a vertical hinge and on the
 * Y axis for a horizontal one.
 */
@Immutable
internal data class BiziHinge(
  val isVertical: Boolean,
  val start: Dp,
  val end: Dp,
) {
  val size: Dp
    get() = end - start
}

/**
 * Everything the app needs to know about the current window: size class plus the
 * foldable posture. Screens should read it through [LocalBiziWindowInfo] instead
 * of querying `WindowSizeClass` directly, so breakpoints and posture handling stay
 * in one place.
 */
@Immutable
internal data class BiziWindowInfo(
  val layout: BiziWindowLayout = BiziWindowLayout.Compact,
  val isTabletop: Boolean = false,
  val separatingHinge: BiziHinge? = null,
) {
  /** True for Medium and Expanded windows, i.e. the navigation rail layouts. */
  val isWide: Boolean
    get() = layout != BiziWindowLayout.Compact

  companion object {
    val Compact = BiziWindowInfo()
  }
}

internal val LocalBiziWindowInfo = staticCompositionLocalOf { BiziWindowInfo.Compact }

/**
 * Minimum width of a station card when the station lists switch to a grid. On phones
 * this still yields a single column (identical to the pre-grid layout), while foldables
 * and wide windows get two or more columns automatically from the available width.
 */
internal val BiziStationGridMinCellWidth = 280.dp

/**
 * Pure breakpoint mapping, kept free of Compose/Android APIs so it can be unit tested.
 */
internal fun biziWindowLayoutFor(
  widthAtLeastMedium: Boolean,
  widthAtLeastExpanded: Boolean,
  heightAtLeastMedium: Boolean,
): BiziWindowLayout =
  when {
    !heightAtLeastMedium -> BiziWindowLayout.Compact
    widthAtLeastExpanded -> BiziWindowLayout.Expanded
    widthAtLeastMedium -> BiziWindowLayout.Medium
    else -> BiziWindowLayout.Compact
  }

@Composable
internal fun rememberBiziWindowInfo(): BiziWindowInfo {
  val adaptiveInfo = currentWindowAdaptiveInfo()
  val sizeClass = adaptiveInfo.windowSizeClass
  val layout =
    biziWindowLayoutFor(
      widthAtLeastMedium = sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND),
      widthAtLeastExpanded = sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND),
      heightAtLeastMedium = sizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND),
    )
  val posture = adaptiveInfo.windowPosture
  val density = LocalDensity.current
  return BiziWindowInfo(
    layout = layout,
    isTabletop = posture.isTabletop,
    separatingHinge = posture.separatingHinge(density),
  )
}

/**
 * First separating hinge reported by the device, if any. Non-separating and flat
 * folds are ignored: there is no physical gap to keep content away from.
 */
internal fun Posture.separatingHinge(density: Density): BiziHinge? =
  hingeList.firstOrNull { it.isSeparating }?.toBiziHinge(density)

internal fun HingeInfo.toBiziHinge(density: Density): BiziHinge =
  with(density) {
    if (isVertical) {
      BiziHinge(
        isVertical = true,
        start = bounds.left.toDp(),
        end = bounds.right.toDp(),
      )
    } else {
      BiziHinge(
        isVertical = false,
        start = bounds.top.toDp(),
        end = bounds.bottom.toDp(),
      )
    }
  }
