package com.gcaguilar.biciradar.mobileui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.gcaguilar.biciradar.core.AssistantAction
import com.gcaguilar.biciradar.core.PlatformBindings
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform

/**
 * Configuration data class for NavigationHost dependencies.
 * Groups all required dependencies to simplify parameter passing.
 */
internal data class NavigationHostConfig(
  val navController: NavHostController,
  val mobilePlatform: MobileUiPlatform,
  val canSelectGoogleMapsInIos: Boolean,
  val isMapReady: Boolean,
  val onOpenAssistant: () -> Unit,
  val platformBindings: PlatformBindings,
  val initialAssistantAction: AssistantAction?,
  val onInitialActionConsumed: () -> Unit,
  val initialMapSearchQuery: String?,
  val onInitialMapSearchQueryConsumed: () -> Unit,
  val onOpenOnboarding: () -> Unit,
  val onShowChangelogManual: () -> Unit,
  val paddingValues: PaddingValues,
)

/**
 * Main navigation host composable that wraps BiziNavHost.
 * Simplifies the call site by accepting a configuration object.
 */
@Composable
internal fun NavigationHost(config: NavigationHostConfig) {
  BiziNavHost(
    navController = config.navController,
    mobilePlatform = config.mobilePlatform,
    canSelectGoogleMapsInIos = config.canSelectGoogleMapsInIos,
    isMapReady = config.isMapReady,
    onOpenAssistant = config.onOpenAssistant,
    platformBindings = config.platformBindings,
    initialAssistantAction = config.initialAssistantAction,
    onInitialActionConsumed = config.onInitialActionConsumed,
    initialMapSearchQuery = config.initialMapSearchQuery,
    onInitialMapSearchQueryConsumed = config.onInitialMapSearchQueryConsumed,
    onOpenOnboarding = config.onOpenOnboarding,
    onShowChangelogManual = config.onShowChangelogManual,
    paddingValues = config.paddingValues,
  )
}
