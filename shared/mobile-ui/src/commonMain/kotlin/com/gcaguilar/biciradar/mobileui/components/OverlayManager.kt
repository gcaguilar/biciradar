package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.gcaguilar.biciradar.mobileui.ChangelogHistoryScreen
import com.gcaguilar.biciradar.mobileui.EngagementTopOverlays
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection

/**
 * Data class representing the state of all overlays.
 */
internal data class OverlayState(
  val updateBanner: TopUpdateBanner = TopUpdateBanner.Hidden,
  val changelogSections: List<ChangelogVersionSection> = emptyList(),
  val highlightedVersion: String? = null,
  val showChangelog: Boolean = false,
)

/**
 * Callbacks for overlay user interactions.
 */
internal data class OverlayCallbacks(
  val onDismissAvailableUpdate: (String) -> Unit = {},
  val onDismissDownloadedUpdate: () -> Unit = {},
  val onStartUpdate: () -> Unit = {},
  val onRestartToUpdate: () -> Unit = {},
  val onChangelogDismiss: () -> Unit = {},
)

/**
 * Manages all overlay UI components:
 * - Update banners (available/downloaded)
 * - Changelog history screen
 *
 * This component centralizes overlay management to reduce complexity in the main app composable.
 */
@Composable
internal fun BoxScope.OverlayManager(
  mobilePlatform: MobileUiPlatform,
  state: OverlayState,
  callbacks: OverlayCallbacks,
) {
  // Top overlays: update banners and feedback nudge
  EngagementTopOverlays(
    updateBanner = state.updateBanner,
    showFeedbackNudge = false,
    onDismissAvailableUpdate = callbacks.onDismissAvailableUpdate,
    onDismissDownloadedUpdate = callbacks.onDismissDownloadedUpdate,
    onStartUpdate = callbacks.onStartUpdate,
    onRestartToUpdate = callbacks.onRestartToUpdate,
    onFeedbackSend = {},
    onFeedbackDismiss = {},
  )

  // Changelog history screen
  if (state.showChangelog && state.changelogSections.isNotEmpty()) {
    ChangelogHistoryScreen(
      mobilePlatform = mobilePlatform,
      sections = state.changelogSections,
      highlightedVersion = state.highlightedVersion,
      onBack = callbacks.onChangelogDismiss,
    )
  }
}

/**
 * Convenience overload that accepts individual parameters.
 * Useful when not all state is grouped in OverlayState.
 */
@Composable
internal fun BoxScope.OverlayManager(
  mobilePlatform: MobileUiPlatform,
  updateBanner: TopUpdateBanner,
  changelogSections: List<ChangelogVersionSection>,
  highlightedVersion: String?,
  showChangelog: Boolean,
  onDismissAvailableUpdate: (String) -> Unit,
  onDismissDownloadedUpdate: () -> Unit,
  onStartUpdate: () -> Unit,
  onRestartToUpdate: () -> Unit,
  onChangelogDismiss: () -> Unit,
) {
  OverlayManager(
    mobilePlatform = mobilePlatform,
    state =
      OverlayState(
        updateBanner = updateBanner,
        changelogSections = changelogSections,
        highlightedVersion = highlightedVersion,
        showChangelog = showChangelog,
      ),
    callbacks =
      OverlayCallbacks(
        onDismissAvailableUpdate = onDismissAvailableUpdate,
        onDismissDownloadedUpdate = onDismissDownloadedUpdate,
        onStartUpdate = onStartUpdate,
        onRestartToUpdate = onRestartToUpdate,
        onChangelogDismiss = onChangelogDismiss,
      ),
  )
}
