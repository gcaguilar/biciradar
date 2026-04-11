package com.gcaguilar.biciradar.mobileui.components

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import com.gcaguilar.biciradar.mobileui.ChangelogHistoryScreen
import com.gcaguilar.biciradar.mobileui.EngagementTopOverlays
import com.gcaguilar.biciradar.mobileui.FeedbackDialog
import com.gcaguilar.biciradar.mobileui.MobileUiPlatform
import com.gcaguilar.biciradar.mobileui.TopUpdateBanner
import com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection

/**
 * Data class representing the state of all overlays.
 */
internal data class OverlayState(
  val updateBanner: TopUpdateBanner = TopUpdateBanner.Hidden,
  val showFeedbackNudge: Boolean = false,
  val showFeedbackDialog: Boolean = false,
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
  val onFeedbackSend: () -> Unit = {},
  val onFeedbackDismiss: () -> Unit = {},
  val onFeedbackDialogDismiss: () -> Unit = {},
  val onOpenFeedbackForm: () -> Unit = {},
  val onChangelogDismiss: () -> Unit = {},
)

/**
 * Manages all overlay UI components:
 * - Update banners (available/downloaded)
 * - Feedback nudges
 * - Feedback dialog
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
    showFeedbackNudge = state.showFeedbackNudge,
    onDismissAvailableUpdate = callbacks.onDismissAvailableUpdate,
    onDismissDownloadedUpdate = callbacks.onDismissDownloadedUpdate,
    onStartUpdate = callbacks.onStartUpdate,
    onRestartToUpdate = callbacks.onRestartToUpdate,
    onFeedbackSend = callbacks.onFeedbackSend,
    onFeedbackDismiss = callbacks.onFeedbackDismiss,
  )

  // Feedback dialog
  if (state.showFeedbackDialog) {
    FeedbackDialog(
      onDismiss = callbacks.onFeedbackDialogDismiss,
      onOpenFeedbackForm = callbacks.onOpenFeedbackForm,
    )
  }

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
  showFeedbackNudge: Boolean,
  showFeedbackDialog: Boolean,
  changelogSections: List<ChangelogVersionSection>,
  highlightedVersion: String?,
  showChangelog: Boolean,
  onDismissAvailableUpdate: (String) -> Unit,
  onDismissDownloadedUpdate: () -> Unit,
  onStartUpdate: () -> Unit,
  onRestartToUpdate: () -> Unit,
  onFeedbackSend: () -> Unit,
  onFeedbackDismiss: () -> Unit,
  onFeedbackDialogDismiss: () -> Unit,
  onOpenFeedbackForm: () -> Unit,
  onChangelogDismiss: () -> Unit,
) {
  OverlayManager(
    mobilePlatform = mobilePlatform,
    state =
      OverlayState(
        updateBanner = updateBanner,
        showFeedbackNudge = showFeedbackNudge,
        showFeedbackDialog = showFeedbackDialog,
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
        onFeedbackSend = onFeedbackSend,
        onFeedbackDismiss = onFeedbackDismiss,
        onFeedbackDialogDismiss = onFeedbackDialogDismiss,
        onOpenFeedbackForm = onOpenFeedbackForm,
        onChangelogDismiss = onChangelogDismiss,
      ),
  )
}
