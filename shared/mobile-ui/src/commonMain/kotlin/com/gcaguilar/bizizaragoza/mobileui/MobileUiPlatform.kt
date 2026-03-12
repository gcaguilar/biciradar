package com.gcaguilar.bizizaragoza.mobileui

internal enum class MobileUiPlatform {
  Android,
  IOS,
}

internal expect fun currentMobileUiPlatform(): MobileUiPlatform
