package com.gcaguilar.biciradar.mobileui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.gcaguilar.biciradar.core.ThemePreference
import com.gcaguilar.biciradar.mobileui.BiziTheme
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.layout.LocalBiziWindowInfo
import com.gcaguilar.biciradar.mobileui.layout.rememberBiziWindowInfo
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor as biziPageBackgroundColor
import com.gcaguilar.biciradar.mobileui.responsivePageWidth as biziResponsivePageWidth

// Re-export theme components from BiziTheme.kt for centralized access

/**
 * Theme provider that applies the Material 3 Expressive Bizi theme (see [BiziTheme])
 * and the current window layout.
 */
@Composable
fun ThemeProvider(
  mobilePlatform: MobileUiPlatform,
  themePreference: ThemePreference = ThemePreference.System,
  content: @Composable () -> Unit,
) {
  val windowInfo = rememberBiziWindowInfo()
  CompositionLocalProvider(LocalBiziWindowInfo provides windowInfo) {
    BiziTheme(
      mobilePlatform = mobilePlatform,
      themePreference = themePreference,
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
