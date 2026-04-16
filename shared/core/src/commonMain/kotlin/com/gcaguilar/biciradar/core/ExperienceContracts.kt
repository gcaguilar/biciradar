package com.gcaguilar.biciradar.core

/** Opens external URLs (feedback form, store listings, etc.). */
interface ExternalLinks {
  fun openFeedbackForm()

  fun openGarminDevicePairing() = Unit
}

/** Location permission prompts for guided onboarding (Android uses Activity-backed requester). */
interface PermissionPrompter {
  suspend fun hasLocationPermission(): Boolean

  suspend fun requestLocationPermission(): Boolean
}

/** In-app review (fire-and-forget). Manual profile CTA should use [openStoreWriteReview]. */
interface ReviewPrompter {
  suspend fun requestInAppReview()

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
  override suspend fun requestInAppReview() = Unit

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
