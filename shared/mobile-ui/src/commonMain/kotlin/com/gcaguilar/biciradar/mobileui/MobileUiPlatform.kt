package com.gcaguilar.biciradar.mobileui

internal enum class MobileUiPlatform {
  Android,
  IOS,
}

internal expect fun currentMobileUiPlatform(): MobileUiPlatform
