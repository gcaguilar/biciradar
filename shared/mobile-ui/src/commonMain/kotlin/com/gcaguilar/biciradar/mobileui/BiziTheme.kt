package com.gcaguilar.biciradar.mobileui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.layout.BiziWindowLayout
import com.gcaguilar.biciradar.mobileui.layout.LocalBiziWindowInfo

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

/**
 * Shape scale. The five baseline tokens keep their values; the Expressive tokens
 * (`largeIncreased`, `extraLargeIncreased`, `extraExtraLarge`) are used by hero
 * surfaces such as the station detail header and the onboarding cards.
 */
internal val BiziShapes =
  Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp),
    largeIncreased = RoundedCornerShape(20.dp),
    extraLargeIncreased = RoundedCornerShape(32.dp),
    extraExtraLarge = RoundedCornerShape(48.dp),
  )

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
    MaterialExpressiveTheme(
      colorScheme =
        dynamicColorScheme ?: biziColorScheme(
          isDark = isDark,
          colors = colors,
          mobilePlatform = mobilePlatform,
        ),
      shapes = BiziShapes,
      typography = Typography(),
      motionScheme = MotionScheme.expressive(),
      content = content,
    )
  }
}

/**
 * Container tone of [accent] laid over [base]: the brand hue stays recognisable while
 * the result is light enough (or dark enough in dark mode) to carry text.
 */
private fun containerOf(
  accent: Color,
  base: Color,
  isDark: Boolean,
): Color = accent.copy(alpha = if (isDark) 0.30f else 0.16f).compositeOver(base)

/** Readable content color for a [containerOf] background of the same [accent]. */
private fun onContainerOf(
  accent: Color,
  ink: Color,
  isDark: Boolean,
): Color = if (isDark) lerp(accent, Color.White, 0.62f) else lerp(accent, ink, 0.55f)

/**
 * Builds the full Material 3 role set from the Bizi palette. Only the brand tokens
 * are inputs: container, surface-container and outline roles are derived from them
 * so no component falls back to the baseline (purple) Material scheme.
 */
internal fun biziColorScheme(
  isDark: Boolean,
  colors: BiziColors,
  mobilePlatform: MobileUiPlatform,
): ColorScheme {
  val background = if (mobilePlatform == MobileUiPlatform.IOS) colors.groupedBackground else colors.background
  val surface = colors.surface
  val primary = colors.red
  val secondary = colors.green
  val tertiary = colors.blue
  val primaryContainer = containerOf(primary, surface, isDark)
  val secondaryContainer = containerOf(secondary, surface, isDark)
  val tertiaryContainer = containerOf(tertiary, surface, isDark)
  val outline = lerp(colors.muted, surface, if (isDark) 0.35f else 0.25f)
  return if (isDark) {
    darkColorScheme(
      primary = primary,
      onPrimary = colors.onAccent,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onContainerOf(primary, colors.ink, isDark = true),
      inversePrimary = lerp(primary, Color.Black, 0.25f),
      secondary = secondary,
      onSecondary = colors.onAccent,
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = onContainerOf(secondary, colors.ink, isDark = true),
      tertiary = tertiary,
      onTertiary = colors.onAccent,
      tertiaryContainer = tertiaryContainer,
      onTertiaryContainer = onContainerOf(tertiary, colors.ink, isDark = true),
      background = background,
      onBackground = colors.ink,
      surface = surface,
      onSurface = colors.ink,
      surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
      onSurfaceVariant = colors.muted,
      surfaceTint = primary,
      surfaceBright = lerp(surface, colors.panel, 0.9f),
      surfaceDim = lerp(surface, Color.Black, 0.2f),
      surfaceContainerLowest = lerp(surface, Color.Black, 0.25f),
      surfaceContainerLow = lerp(surface, background, 0.4f),
      surfaceContainer = surface,
      surfaceContainerHigh = lerp(surface, colors.panel, 0.55f),
      surfaceContainerHighest = colors.panel,
      outline = outline,
      outlineVariant = colors.panel,
      inverseSurface = colors.ink,
      inverseOnSurface = surface,
    )
  } else {
    lightColorScheme(
      primary = primary,
      onPrimary = colors.onAccent,
      primaryContainer = primaryContainer,
      onPrimaryContainer = onContainerOf(primary, colors.ink, isDark = false),
      inversePrimary = lerp(primary, Color.White, 0.45f),
      secondary = secondary,
      onSecondary = colors.onAccent,
      secondaryContainer = secondaryContainer,
      onSecondaryContainer = onContainerOf(secondary, colors.ink, isDark = false),
      tertiary = tertiary,
      onTertiary = colors.onAccent,
      tertiaryContainer = tertiaryContainer,
      onTertiaryContainer = onContainerOf(tertiary, colors.ink, isDark = false),
      background = background,
      onBackground = colors.ink,
      surface = surface,
      onSurface = colors.ink,
      surfaceVariant = if (mobilePlatform == MobileUiPlatform.IOS) colors.panel else colors.background,
      onSurfaceVariant = colors.muted,
      surfaceTint = primary,
      surfaceBright = surface,
      surfaceDim = lerp(colors.panel, colors.muted, 0.12f),
      surfaceContainerLowest = Color.White,
      surfaceContainerLow = lerp(surface, colors.panel, 0.3f),
      surfaceContainer = lerp(surface, colors.panel, 0.55f),
      surfaceContainerHigh = lerp(surface, colors.panel, 0.8f),
      surfaceContainerHighest = colors.panel,
      outline = outline,
      outlineVariant = colors.panel,
      inverseSurface = colors.ink,
      inverseOnSurface = surface,
    )
  }
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
    when (LocalBiziWindowInfo.current.layout) {
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

/**
 * Como [responsivePageWidth] pero solo limita el ancho: el contenido mide lo que
 * necesite de alto. Úsalo para overlays que no deben ocupar toda la pantalla, como
 * tarjetas ancladas a un borde.
 */
@Composable
internal fun Modifier.responsiveContentWidth(): Modifier {
  val maxWidth =
    when (LocalBiziWindowInfo.current.layout) {
      BiziWindowLayout.Compact -> null
      BiziWindowLayout.Medium -> 760.dp
      BiziWindowLayout.Expanded -> 920.dp
    }
  return if (maxWidth == null) {
    fillMaxWidth()
  } else {
    fillMaxWidth().widthIn(max = maxWidth)
  }
}
