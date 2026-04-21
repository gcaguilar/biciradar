package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.gcaguilar.biciradar.core.ThemePreference

// --- Light-mode palette raw tokens ---
private val BiziLight = Color(0xFFF8F6F6)
private val BiziGrouped = Color(0xFFF2F2F7)
private val BiziInk = Color(0xFF0D1B2A)
private val BiziMuted = Color(0xFF64779D)
private val BiziPanel = Color(0xFFE8EDF4)
private val BiziPrimary = Color(0xFF1D74BD)
private val BiziSecondary = Color(0xFF64C23A)
private val BiziTertiary = Color(0xFF0D1B2A)
private val BiziOrange = Color(0xFFF28000)
private val BiziNeutral = Color(0xFF64779D)

// --- Dark-mode palette raw tokens ---
private val BiziDarkBackground = Color(0xFF0F172A)
private val BiziDarkGrouped = Color(0xFF1C1C1E)
private val BiziDarkSurface = Color(0xFF1E1E1E)
private val BiziDarkInk = Color(0xFFF1EDED)
private val BiziDarkMuted = Color(0xFF94A3B8)
private val BiziDarkPanel = Color(0xFF2A2A2C)
private val BiziDarkPrimary = Color(0xFF1070CA)
private val BiziDarkSecondary = Color(0xFF64C832)
private val BiziDarkTertiary = Color(0xFFA05ABA)
private val BiziDarkNeutral = Color(0xFF0F172A)

/**
 * Semantic color scheme consumed by every composable in the app.
 * Two instances exist: [LightBiziColors] and [DarkBiziColors].
 */
internal data class BiziColors(
  val background: Color,
  val groupedBackground: Color,
  val surface: Color,
  val ink: Color,
  val muted: Color,
  val panel: Color,
  val red: Color,
  val blue: Color,
  val green: Color,
  val orange: Color,
  val purple: Color,
  val onAccent: Color,
  val navBar: Color,
  val navBarIos: Color,
  val fieldSurfaceIos: Color,
  val fieldSurfaceAndroid: Color,
  val dismissAlphaBase: Float,
)

internal val LightBiziColors =
  BiziColors(
    background = BiziLight,
    groupedBackground = BiziGrouped,
    surface = Color.White,
    ink = BiziInk,
    muted = BiziMuted,
    panel = BiziPanel,
    red = BiziPrimary,
    blue = BiziTertiary,
    green = BiziSecondary,
    orange = BiziOrange,
    purple = BiziNeutral,
    onAccent = Color.White,
    navBar = Color.White,
    navBarIos = Color.White.copy(alpha = 0.96f),
    fieldSurfaceIos = Color.White,
    fieldSurfaceAndroid = BiziPanel,
    dismissAlphaBase = 0.10f,
  )

internal val DarkBiziColors =
  BiziColors(
    background = BiziDarkBackground,
    groupedBackground = BiziDarkGrouped,
    surface = BiziDarkSurface,
    ink = BiziDarkInk,
    muted = BiziDarkMuted,
    panel = BiziDarkPanel,
    red = BiziDarkPrimary,
    blue = BiziDarkTertiary,
    green = BiziDarkSecondary,
    orange = BiziOrange,
    purple = BiziDarkNeutral,
    onAccent = Color.White,
    navBar = BiziDarkSurface,
    navBarIos = BiziDarkSurface.copy(alpha = 0.96f),
    fieldSurfaceIos = BiziDarkSurface,
    fieldSurfaceAndroid = BiziDarkPanel,
    dismissAlphaBase = 0.16f,
  )

internal val LocalBiziColors = staticCompositionLocalOf { LightBiziColors }
internal val LocalIsDarkTheme = staticCompositionLocalOf { false }

// Shape token for cards / station rows (platform-aware: iOS uses slightly smaller radius)
internal val LocalBiziCardShape = staticCompositionLocalOf<Shape> { RoundedCornerShape(24.dp) }

internal val BiziShapes =
  Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

internal enum class BiziWindowLayout {
  Compact,
  Medium,
  Expanded,
}

internal val LocalBiziWindowLayout = staticCompositionLocalOf { BiziWindowLayout.Compact }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BiziTheme(
  mobilePlatform: MobileUiPlatform,
  themePreference: ThemePreference = ThemePreference.System,
  content: @Composable () -> Unit,
) {
  val isDark =
    when (themePreference) {
      ThemePreference.Light -> false
      ThemePreference.Dark -> true
      ThemePreference.System -> isSystemInDarkTheme()
    }
  val dynamicColorScheme = platformDynamicColorScheme(isDark)
  val colors =
    dynamicColorScheme?.let { dynamicScheme ->
      dynamicBiziColors(dynamicScheme, mobilePlatform, isDark)
    } ?: if (isDark) {
      DarkBiziColors
    } else {
      LightBiziColors
    }
  val cardShape: Shape =
    if (mobilePlatform == MobileUiPlatform.IOS) RoundedCornerShape(22.dp) else RoundedCornerShape(24.dp)
  CompositionLocalProvider(
    LocalBiziColors provides colors,
    LocalIsDarkTheme provides isDark,
    LocalBiziCardShape provides cardShape,
  ) {
    MaterialTheme(
      colorScheme =
        dynamicColorScheme ?: biziColorScheme(
          isDark = isDark,
          colors = colors,
          mobilePlatform = mobilePlatform,
        ),
      shapes = BiziShapes,
      motionScheme = MotionScheme.expressive(),
      content = content,
    )
  }
}

@Composable
internal fun rememberBiziWindowLayout(): BiziWindowLayout {
  val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
  return when {
    windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
      windowSizeClass.isHeightAtLeastBreakpoint(
        WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND,
      ) -> BiziWindowLayout.Expanded
    windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
      windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> BiziWindowLayout.Medium
    else -> BiziWindowLayout.Compact
  }
}

internal fun biziColorScheme(
  isDark: Boolean,
  colors: BiziColors,
  mobilePlatform: MobileUiPlatform,
) = if (isDark) {
  darkColorScheme(
    primary = colors.red,
    onPrimary = colors.onAccent,
    secondary = colors.green,
    onSecondary = colors.onAccent,
    tertiary = colors.blue,
    onTertiary = colors.onAccent,
    background = if (mobilePlatform == MobileUiPlatform.IOS) colors.groupedBackground else colors.background,
    onBackground = colors.ink,
    surface = colors.surface,
    onSurface = colors.ink,
    surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
    onSurfaceVariant = colors.muted,
    outline = colors.panel,
    inverseSurface = colors.ink,
    inverseOnSurface = colors.surface,
  )
} else {
  lightColorScheme(
    primary = colors.red,
    onPrimary = colors.onAccent,
    secondary = colors.green,
    onSecondary = colors.onAccent,
    tertiary = colors.blue,
    onTertiary = colors.onAccent,
    background = if (mobilePlatform == MobileUiPlatform.IOS) colors.groupedBackground else colors.background,
    onBackground = colors.ink,
    surface = colors.surface,
    onSurface = colors.ink,
    surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
    onSurfaceVariant = colors.muted,
    outline = colors.panel,
    inverseSurface = colors.ink,
    inverseOnSurface = colors.surface,
  )
}

internal fun dynamicBiziColors(
  colorScheme: ColorScheme,
  mobilePlatform: MobileUiPlatform,
  isDark: Boolean,
): BiziColors =
  BiziColors(
    background = colorScheme.background,
    groupedBackground = if (mobilePlatform == MobileUiPlatform.IOS) colorScheme.surface else colorScheme.background,
    surface = colorScheme.surface,
    ink = colorScheme.onSurface,
    muted = colorScheme.onSurfaceVariant,
    panel = colorScheme.surfaceVariant,
    red = colorScheme.primary,
    blue = colorScheme.tertiary,
    green = colorScheme.secondary,
    orange = colorScheme.tertiary,
    purple = colorScheme.secondary,
    onAccent = colorScheme.onPrimary,
    navBar = colorScheme.surface,
    navBarIos = colorScheme.surface.copy(alpha = 0.96f),
    fieldSurfaceIos = colorScheme.surface,
    fieldSurfaceAndroid = colorScheme.surfaceVariant,
    dismissAlphaBase = if (isDark) 0.16f else 0.10f,
  )

@Composable
internal fun pageBackgroundColor(platform: MobileUiPlatform): Color {
  val c = LocalBiziColors.current
  return if (platform == MobileUiPlatform.IOS) c.groupedBackground else c.background
}

@Composable
internal fun Modifier.responsivePageWidth(): Modifier {
  val maxWidth =
    when (LocalBiziWindowLayout.current) {
      BiziWindowLayout.Compact -> null
      BiziWindowLayout.Medium -> 760.dp
      BiziWindowLayout.Expanded -> 920.dp
    }
  return if (maxWidth == null) {
    fillMaxSize()
  } else {
    fillMaxSize().widthIn(max = maxWidth)
  }
}
