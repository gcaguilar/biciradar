package com.gcaguilar.biciradar.wear

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.wear.compose.material3.ColorScheme
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.material3.ColorScheme as TileColorScheme

// Brand palette (same hues as the phone app's dark scheme). The watch is always dark.
internal val WearPrimary = Color(0xFF1D74BD)
internal val WearSecondary = Color(0xFF64C23A)
internal val WearNeutral = Color(0xFF64779D)
internal val WearError = Color(0xFFCF6679)

/** Brand primary lightened for small text on dark surfaces (~6.5:1 on the card color). */
internal val WearPrimaryText = Color(0xFF6CA5D4)

internal val WearWarning = Color(0xFFF28000)
internal val WearSurface = Color(0xFF1A1A2E)
internal val WearOnSurface = Color(0xFFE8EDF4)
private val WearDarkTertiary = Color(0xFFA05ABA)
private val WearInk = Color(0xFF0D1B2A)
private val WearDarkMuted = Color(0xFF94A3B8)

/**
 * Wear Material 3 color scheme built from the BiciRadar brand colors.
 *
 * Without it every Wear component (cards, buttons, progress, edge button, dialogs…) falls
 * back to the stock Wear purple/grey palette. `onSecondary` is the dark ink color on
 * purpose: white on the brand green is only ~2:1, ink on it is ~8:1.
 */
internal val WearColorScheme =
  ColorScheme(
    primary = WearPrimary,
    primaryDim = Color(0xFF175F9A),
    primaryContainer = Color(0xFF0E3A5E),
    onPrimary = Color.White,
    onPrimaryContainer = Color(0xFFCFE4F8),
    secondary = WearSecondary,
    secondaryDim = Color(0xFF50A02E),
    secondaryContainer = Color(0xFF1F3F12),
    onSecondary = WearInk,
    onSecondaryContainer = Color(0xFFD3F0C3),
    tertiary = WearDarkTertiary,
    tertiaryDim = Color(0xFF8647A0),
    tertiaryContainer = Color(0xFF3A2145),
    onTertiary = Color.White,
    onTertiaryContainer = Color(0xFFE9D3F2),
    surfaceContainerLow = Color(0xFF11111F),
    surfaceContainer = WearSurface,
    surfaceContainerHigh = Color(0xFF26263F),
    onSurface = WearOnSurface,
    onSurfaceVariant = WearDarkMuted,
    outline = WearNeutral,
    outlineVariant = Color(0xFF2E3A52),
    background = Color.Black,
    onBackground = WearOnSurface,
    error = WearError,
    errorDim = Color(0xFFA9505F),
    errorContainer = Color(0xFF4A1A24),
    onError = Color(0xFF2B0A12),
    onErrorContainer = Color(0xFFFFD9DE),
  )

@Composable
internal fun BiciRadarWearTheme(content: @Composable () -> Unit) {
  MaterialTheme(colorScheme = WearColorScheme, content = content)
}

/**
 * Fallback scheme for tiles. Tiles keep following the user's dynamic theme when the watch
 * provides one (`allowDynamicTheme = true`); this only applies when it doesn't.
 */
internal val WearTileColorScheme =
  TileColorScheme(
    primary = LayoutColor(WearPrimary.toArgb()),
    onPrimary = LayoutColor(Color.White.toArgb()),
    primaryContainer = LayoutColor(0xFF0E3A5E.toInt()),
    onPrimaryContainer = LayoutColor(0xFFCFE4F8.toInt()),
    secondary = LayoutColor(WearSecondary.toArgb()),
    onSecondary = LayoutColor(WearInk.toArgb()),
    secondaryContainer = LayoutColor(0xFF1F3F12.toInt()),
    onSecondaryContainer = LayoutColor(0xFFD3F0C3.toInt()),
    surfaceContainer = LayoutColor(WearSurface.toArgb()),
    onSurface = LayoutColor(WearOnSurface.toArgb()),
    onSurfaceVariant = LayoutColor(WearDarkMuted.toArgb()),
    background = LayoutColor(Color.Black.toArgb()),
    onBackground = LayoutColor(WearOnSurface.toArgb()),
    error = LayoutColor(WearError.toArgb()),
  )
