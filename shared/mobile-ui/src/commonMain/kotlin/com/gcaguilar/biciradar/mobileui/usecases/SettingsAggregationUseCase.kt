package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.OnboardingChecklistSnapshot
import com.gcaguilar.biciradar.core.SettingsRepository
import kotlinx.coroutines.flow.StateFlow

/**
 * Use case that aggregates settings-related operations.
 * Groups all settings repository operations to reduce direct dependencies.
 */
internal class SettingsAggregationUseCase(
  private val settingsRepository: SettingsRepository,
) {
  // region Onboarding State

  /**
   * Flow of onboarding completion state.
   */
  val hasCompletedOnboarding: StateFlow<Boolean> = settingsRepository.hasCompletedOnboarding

  /**
   * Flow of onboarding checklist state.
   */
  val onboardingChecklist: StateFlow<OnboardingChecklistSnapshot> = settingsRepository.onboardingChecklist

  /**
   * Checks if onboarding is completed based on settings.
   */
  fun isOnboardingCompleted(): Boolean =
    settingsRepository.hasCompletedOnboarding.value ||
      settingsRepository.onboardingChecklist.value.isCompleted()

  /**
   * Determines if settings should display the onboarding setup section.
   */
  fun shouldShowProfileSetupSection(checklist: OnboardingChecklistSnapshot): Boolean = !checklist.isCompleted()

  /**
   * Gets the current onboarding checklist state.
   */
  fun currentOnboardingChecklist(): OnboardingChecklistSnapshot = settingsRepository.onboardingChecklist.value

  /**
   * Updates the onboarding checklist with the given transform.
   */
  suspend fun updateOnboardingChecklist(transform: (OnboardingChecklistSnapshot) -> OnboardingChecklistSnapshot) {
    settingsRepository.updateOnboardingChecklist(transform)
  }

  // endregion

  // region Changelog

  /**
   * Gets the current last seen changelog app version.
   */
  fun currentLastSeenChangelogAppVersion(): String? = settingsRepository.currentLastSeenChangelogAppVersion()

  /**
   * Marks the changelog as seen for the given version.
   */
  suspend fun setLastSeenChangelogAppVersion(version: String) {
    settingsRepository.setLastSeenChangelogAppVersion(version)
  }

  /**
   * Ensures the changelog baseline is set for first-time users.
   */
  suspend fun ensureChangelogStringBaseline(appVersion: String) {
    settingsRepository.ensureChangelogStringBaseline(appVersion)
  }

  // endregion

  // region Bootstrap

  /**
   * Bootstraps settings repository.
   */
  suspend fun bootstrapSettings(): Boolean {
    runCatching { settingsRepository.bootstrap() }
    return true
  }

  // endregion
}
