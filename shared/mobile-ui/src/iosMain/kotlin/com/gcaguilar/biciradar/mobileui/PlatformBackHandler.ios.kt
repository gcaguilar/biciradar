package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformBackHandler(
  enabled: Boolean,
  onBack: () -> Unit,
) {
  // iOS handles back navigation via swipe gesture and the TopAppBar navigationIcon — no-op here
}
