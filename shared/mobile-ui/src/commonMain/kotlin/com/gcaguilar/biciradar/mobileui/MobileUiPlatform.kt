package com.gcaguilar.biciradar.mobileui

internal enum class MobileUiPlatform {
  Android,
  IOS,
  Desktop,
}

internal expect fun currentMobileUiPlatform(): MobileUiPlatform
