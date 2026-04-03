package com.gcaguilar.biciradar.mobileui.usecases

import com.gcaguilar.biciradar.core.compareAppVersionStrings
import com.gcaguilar.biciradar.core.normalizeAppVersionForCatalog
import com.gcaguilar.biciradar.core.pendingChangelogVersion
import com.gcaguilar.biciradar.mobileui.experience.ChangelogCatalog
import com.gcaguilar.biciradar.mobileui.experience.ChangelogVersionSection
import com.gcaguilar.biciradar.mobileui.viewmodel.ChangelogPresentation

/**
 * Result of checking if changelog should be suppressed.
 */
internal data class ChangelogSuppressionResult(
  val suppressed: Boolean,
  val shouldMarkCurrentVersionSeen: Boolean = false,
  val currentVersionToMark: String? = null,
)

/**
 * Use case that handles changelog management and display logic.
 */
internal class ChangelogUseCase(
  private val settingsRepository: com.gcaguilar.biciradar.core.SettingsRepository,
  private val appVersion: String,
) {

  /**
   * Gets the pending changelog if one should be shown to the user.
   * @return ChangelogPresentation if there's a pending changelog, null otherwise
   */
  fun getPendingChangelog(): ChangelogPresentation? {
    val suppression = checkSuppression()
    if (suppression.suppressed) return null

    val lastSeen = settingsRepository.currentLastSeenChangelogAppVersion() ?: "0.0.0"
    val pending = pendingChangelogVersion(
      appVersion,
      lastSeen,
      ChangelogCatalog.catalogVersionSet(),
    )
    val entries = pending?.let { ChangelogCatalog.entriesFor(it) }.orEmpty()

    return if (pending != null && entries.isNotEmpty()) {
      ChangelogPresentation(
        sections = ChangelogCatalog.history(),
        highlightedVersion = pending,
        persistSeenVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion,
      )
    } else null
  }

  /**
   * Checks if changelog should be suppressed and if current version should be marked as seen.
   * This is used when onboarding is pending.
   */
  fun checkSuppression(): ChangelogSuppressionResult {
    val onboardingCompleted = settingsRepository.onboardingChecklist.value.isCompleted() ||
      settingsRepository.hasCompletedOnboarding.value
    if (onboardingCompleted) {
      return ChangelogSuppressionResult(suppressed = false)
    }

    val normalizedCurrentVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion
    val normalizedLastSeen = normalizeAppVersionForCatalog(
      settingsRepository.currentLastSeenChangelogAppVersion(),
    )

    val shouldMarkSeen = normalizedLastSeen == null ||
      compareAppVersionStrings(normalizedLastSeen, normalizedCurrentVersion) < 0

    return ChangelogSuppressionResult(
      suppressed = true,
      shouldMarkCurrentVersionSeen = shouldMarkSeen,
      currentVersionToMark = normalizedCurrentVersion,
    )
  }

  /**
   * Gets the full changelog history for manual viewing.
   */
  fun getChangelogHistory(): ChangelogPresentation? {
    val sections = ChangelogCatalog.history()
    if (sections.isEmpty()) return null

    return ChangelogPresentation(
      sections = sections,
      highlightedVersion = ChangelogCatalog.latestVersionAtOrBefore(appVersion),
      persistSeenVersion = normalizeAppVersionForCatalog(appVersion) ?: appVersion,
    )
  }

  /**
   * Marks the changelog as seen for the given version.
   */
  suspend fun markChangelogSeen(version: String) {
    settingsRepository.setLastSeenChangelogAppVersion(version)
  }

  /**
   * Ensures the changelog baseline is set for first-time users.
   * Should be called during initialization.
   */
  suspend fun ensureChangelogBaseline() {
    settingsRepository.ensureChangelogStringBaseline(appVersion)
  }
}
