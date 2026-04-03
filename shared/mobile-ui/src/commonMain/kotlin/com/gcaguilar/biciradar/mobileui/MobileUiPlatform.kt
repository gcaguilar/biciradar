package com.gcaguilar.biciradar.mobileui

enum class MobileUiPlatform {
  Android,
  IOS,
  Desktop,
}

internal expect fun currentMobileUiPlatform(): MobileUiPlatform
