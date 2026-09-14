package com.gcaguilar.biciradar.core

/** Opens external URLs (feedback form, store listings, etc.). */
interface ExternalLinks {
  fun openFeedbackForm()
}

/** Location permission prompts for guided onboarding (Android uses Activity-backed requester). */
interface PermissionPrompter {
  suspend fun hasLocationPermission(): Boolean

  suspend fun requestLocationPermission(): Boolean
}

/** In-app review (fire-and-forget). Manual profile CTA should use [openStoreWriteReview]. */
interface ReviewPrompter {
  /**
   * Attempts to show the platform in-app review prompt.
   *
   * @return true when the platform call was actually made (the OS may still decide not to
   *   display the dialog), false when the prompt could not be attempted at all (e.g. no
   *   foreground host, unsupported platform). Callers use this to avoid consuming the
   *   once-per-version slot on a call that never reached the platform.
   */
  suspend fun requestInAppReview(): Boolean

  suspend fun requestInAppReviewOrStoreFallback() {
    requestInAppReview()
  }

  fun openStoreWriteReview()
}

/** Play In-App Updates on Android; no-op or store listing on other platforms. */
interface AppUpdatePrompter {
  suspend fun checkForUpdate(): UpdateAvailabilityState

  suspend fun startFlexibleUpdate(): Boolean

  suspend fun completeFlexibleUpdateIfReady(): Boolean

  fun openStoreListing()
}

object NoOpPermissionPrompter : PermissionPrompter {
  override suspend fun hasLocationPermission(): Boolean = true

  override suspend fun requestLocationPermission(): Boolean = true
}

object NoOpReviewPrompter : ReviewPrompter {
  override suspend fun requestInAppReview(): Boolean = false

  override fun openStoreWriteReview() = Unit
}

object NoOpAppUpdatePrompter : AppUpdatePrompter {
  override suspend fun checkForUpdate(): UpdateAvailabilityState = UpdateAvailabilityState.Unknown

  override suspend fun startFlexibleUpdate(): Boolean = false

  override suspend fun completeFlexibleUpdateIfReady(): Boolean = false

  override fun openStoreListing() = Unit
}

object NoOpExternalLinks : ExternalLinks {
  override fun openFeedbackForm() = Unit
}
