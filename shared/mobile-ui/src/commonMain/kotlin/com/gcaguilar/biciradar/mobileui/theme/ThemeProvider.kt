package com.gcaguilar.biciradar.mobileui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.BiziColors
import com.gcaguilar.biciradar.mobileui.DarkBiziColors
import com.gcaguilar.biciradar.mobileui.LightBiziColors
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.LocalIsDarkTheme
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.biziColorScheme
import com.gcaguilar.biciradar.mobileui.dynamicBiziColors
import com.gcaguilar.biciradar.mobileui.platformDynamicColorScheme

/**
 * Window layout variants for responsive design.
 */
enum class BiziWindowLayout {
    Compact,
    Medium,
    Expanded,
}

/**
 * CompositionLocal for accessing the current window layout.
 */
val LocalBiziWindowLayout = staticCompositionLocalOf { BiziWindowLayout.Compact }

/**
 * Determines the window layout based on the current window size class.
 */
@Composable
fun rememberBiziWindowLayout(): BiziWindowLayout {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
            windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> BiziWindowLayout.Expanded
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
            windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) -> BiziWindowLayout.Medium
        else -> BiziWindowLayout.Compact
    }
}

/**
 * Theme provider that applies Material3 theme with Bizi styling.
 * Handles dark/light mode based on theme preference.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeProvider(
    mobilePlatform: MobileUiPlatform,
    themePreference: ThemePreference = ThemePreference.System,
    content: @Composable () -> Unit,
) {
    val isDark = when (themePreference) {
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
        ThemePreference.System -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    val dynamicColorScheme = platformDynamicColorScheme(isDark)
    val colors = dynamicColorScheme?.let { dynamicScheme ->
        dynamicBiziColors(dynamicScheme, mobilePlatform, isDark)
    } ?: if (isDark) {
        DarkBiziColors
    } else {
        LightBiziColors
    }
    CompositionLocalProvider(
        LocalBiziColors provides colors,
        LocalIsDarkTheme provides isDark,
    ) {
        MaterialTheme(
            colorScheme = dynamicColorScheme ?: biziColorScheme(
                isDark = isDark,
                colors = colors,
                mobilePlatform = mobilePlatform,
            ),
            motionScheme = MotionScheme.expressive(),
            content = content,
        )
    }
}

/**
 * Returns the appropriate background color for the current platform.
 */
@Composable
fun pageBackgroundColor(platform: MobileUiPlatform): Color {
    val c = LocalBiziColors.current
    return if (platform == MobileUiPlatform.IOS) c.groupedBackground else c.background
}

/**
 * Modifier that constrains the width based on the current window layout.
 */
@Composable
fun Modifier.responsivePageWidth(): Modifier {
    val maxWidth = when (LocalBiziWindowLayout.current) {
        BiziWindowLayout.Compact -> null
        BiziWindowLayout.Medium -> 760.dp
        BiziWindowLayout.Expanded -> 920.dp
    }
    return if (maxWidth == null) {
        androidx.compose.foundation.layout.fillMaxSize()
    } else {
        androidx.compose.foundation.layout.fillMaxSize().then(
            androidx.compose.ui.unit.dp.let { androidx.compose.foundation.layout.widthIn(max = maxWidth) }
        )
    }
}
