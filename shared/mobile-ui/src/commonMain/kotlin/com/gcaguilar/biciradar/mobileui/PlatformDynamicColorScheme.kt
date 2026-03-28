package com.gcaguilar.biciradar.mobileui

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal expect fun platformDynamicColorScheme(isDark: Boolean): ColorScheme?
