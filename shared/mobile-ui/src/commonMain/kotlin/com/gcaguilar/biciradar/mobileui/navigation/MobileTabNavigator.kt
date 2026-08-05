package com.gcaguilar.biciradar.mobileui.navigation

/**
 * Handle handed to a native host so it can drive tab switches on the *single* Compose
 * [androidx.navigation.NavHostController] that backs the whole app.
 *
 * This exists for the iOS Liquid Glass shell (`NativeNavContentView.swift`): there, the
 * SwiftUI `TabView` only provides the chrome (the floating glass tab bar), while Compose
 * keeps owning all navigation exactly as it does on Android. Tapping a native tab calls
 * [selectTab], which runs the same [navigateToPrimaryDestination] transaction the Compose
 * bottom bar uses — so tab state saving/restoring behaves identically on both platforms.
 *
 * Deliberately *not* used on Android, where [com.gcaguilar.biciradar.mobileui.components.BiziBottomBar]
 * already talks to the NavController directly.
 */
class MobileTabNavigator internal constructor(
  private val onSelectTab: (Screen) -> Unit,
) {
  /**
   * Switches to a top-level destination ([Screen.Nearby], [Screen.Map], [Screen.Favorites],
   * [Screen.Trip], [Screen.Profile]). Must be called from the main thread.
   */
  fun selectTab(tab: Screen) {
    onSelectTab(tab)
  }
}
