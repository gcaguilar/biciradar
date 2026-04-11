package com.gcaguilar.biciradar.mobileui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.BiziWindowLayout
import com.gcaguilar.biciradar.mobileui.DarkBiziColors
import com.gcaguilar.biciradar.mobileui.LightBiziColors
import com.gcaguilar.biciradar.mobileui.LocalBiziColors
import com.gcaguilar.biciradar.mobileui.LocalBiziWindowLayout
import com.gcaguilar.biciradar.mobileui.LocalIsDarkTheme
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.biziColorScheme
import com.gcaguilar.biciradar.mobileui.dynamicBiziColors
import com.gcaguilar.biciradar.mobileui.platformDynamicColorScheme
import com.gcaguilar.biciradar.mobileui.rememberBiziWindowLayout

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
  val windowLayout = rememberBiziWindowLayout()

  CompositionLocalProvider(
    LocalBiziColors provides colors,
    LocalIsDarkTheme provides isDark,
    LocalBiziWindowLayout provides windowLayout,
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
  val maxWidth =
    when (LocalBiziWindowLayout.current) {
      BiziWindowLayout.Compact -> null
      BiziWindowLayout.Medium -> 760.dp
      BiziWindowLayout.Expanded -> 920.dp
    }
  return if (maxWidth == null) {
    this.fillMaxSize()
  } else {
    this.fillMaxSize().widthIn(max = maxWidth)
  }
}
