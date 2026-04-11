package com.gcaguilar.biciradar.mobileui

import androidx.compose.runtime.Composable

@Composable
internal actual fun PlatformBackHandler(
  enabled: Boolean,
  onBack: () -> Unit,
) {
  // Desktop windows do not expose a system back action.
}
