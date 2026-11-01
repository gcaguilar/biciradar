package com.gcaguilar.biciradar.mobileui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.DarkBiziColors
import com.gcaguilar.biciradar.mobileui.LightBiziColors
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.LocalIsDarkTheme
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.biziColorScheme
import com.gcaguilar.biciradar.mobileui.dynamicBiziColors
import com.gcaguilar.biciradar.mobileui.layout.LocalBiziWindowInfo
import com.gcaguilar.biciradar.mobileui.layout.rememberBiziWindowInfo
import com.gcaguilar.biciradar.mobileui.platformDynamicColorScheme
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor as biziPageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth as biziResponsivePageWidth

// Re-export theme components from BiziTheme.kt for centralized access

/**
 * Theme provider that applies Material3 theme with Bizi styling.
 * Handles dark/light mode based on theme preference and provides window layout.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeProvider(
  mobilePlatform: MobileUiPlatform,
  themePreference: ThemePreference = ThemePreference.System,
  content: @Composable () -> Unit,
) {
  val isDark =
    when (themePreference) {
      ThemePreference.Light -> false
      ThemePreference.Dark -> true
      ThemePreference.System -> androidx.compose.foundation.isSystemInDarkTheme()
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
  val windowInfo = rememberBiziWindowInfo()

  CompositionLocalProvider(
    LocalBiziColors provides colors,
    LocalIsDarkTheme provides isDark,
    LocalBiziWindowInfo provides windowInfo,
  ) {
    MaterialTheme(
      colorScheme =
        dynamicColorScheme ?: biziColorScheme(
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
 * Public re-export of the shared implementation in `BiziTheme.kt`.
 */
@Composable
fun pageBackgroundColor(platform: MobileUiPlatform): Color = biziPageBackgroundColor(platform)

/**
 * Modifier that constrains the width based on the current window layout.
 * Public re-export of the shared implementation in `BiziTheme.kt`.
 */
@Composable
fun Modifier.responsivePageWidth(): Modifier = biziResponsivePageWidth()
