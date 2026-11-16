package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.pageBackgroundColor

/**
 * Top app bar colors for secondary screens: the bar blends with the page background
 * and switches to `surfaceContainer` once content scrolls under it (pair it with a
 * `TopAppBarDefaults.pinnedScrollBehavior()` wired to the Scaffold's nested scroll).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun biziTopAppBarColors(mobilePlatform: MobileUiPlatform): TopAppBarColors =
  TopAppBarDefaults.topAppBarColors(
    containerColor = pageBackgroundColor(mobilePlatform),
    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
  )
