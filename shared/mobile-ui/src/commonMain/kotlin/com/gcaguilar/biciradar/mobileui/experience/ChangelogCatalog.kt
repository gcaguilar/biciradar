package com.gcaguilar.biciradar.mobileui.experience

import com.gcaguilar.biciradar.core.compareAppVersionStrings
import com.gcaguilar.biciradar.core.normalizeAppVersionForCatalog
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import org.jetbrains.compose.resources.StringResource

data class ChangelogCatalogEntry(
  val titleKey: StringResource,
  val descriptionKey: StringResource,
)

data class ChangelogVersionSection(
  val versionName: String,
  val entries: List<ChangelogCatalogEntry>,
)

/**
 * Local catalog keyed by [com.gcaguilar.biciradar.core.PlatformBindings.appVersion] strings.
 * Add a block per shipped release that should show the "What's new" dialog once.
 */
object ChangelogCatalog {
  private val entriesByVersion: Map<String, List<ChangelogCatalogEntry>> =
    mapOf(
      "0.18.1" to
        listOf(
          ChangelogCatalogEntry(Res.string.changelogUiImprovementsTitle, Res.string.changelogUiImprovementsDescription),
          ChangelogCatalogEntry(
            Res.string.changelogPerformanceImprovementsTitle,
            Res.string.changelogPerformanceImprovementsDescription,
          ),
          ChangelogCatalogEntry(Res.string.changelogLocalCacheTitle, Res.string.changelogLocalCacheDescription),
          ChangelogCatalogEntry(Res.string.changelogDataSourcesTitle, Res.string.changelogDataSourcesDescription),
        ),
      "0.19.0" to
        listOf(
          ChangelogCatalogEntry(Res.string.changelogUiImprovementsTitle, Res.string.changelogUiImprovementsDescription),
          ChangelogCatalogEntry(
            Res.string.changelogPerformanceImprovementsTitle,
            Res.string.changelogPerformanceImprovementsDescription,
          ),
          ChangelogCatalogEntry(Res.string.changelogLocalCacheTitle, Res.string.changelogLocalCacheDescription),
          ChangelogCatalogEntry(Res.string.changelogDataSourcesTitle, Res.string.changelogDataSourcesDescription),
          ChangelogCatalogEntry(
            Res.string.changelogSavedPlaceAlertsTitle,
            Res.string.changelogSavedPlaceAlertsDescription,
          ),
        ),
      "0.21.0" to
        listOf(
          ChangelogCatalogEntry(
            Res.string.changelogSavedPlaceAlertsTitle,
            Res.string.changelogSavedPlaceAlertsDescription,
          ),
          ChangelogCatalogEntry(
            Res.string.changelogEnvironmentalLayersTitle,
            Res.string.changelogEnvironmentalLayersDescription,
          ),
          ChangelogCatalogEntry(Res.string.changelogDistanceKmTitle, Res.string.changelogDistanceKmDescription),
        ),
      "0.21.1" to
        listOf(
          ChangelogCatalogEntry(
            Res.string.changelogOnboardingAccessTitle,
            Res.string.changelogOnboardingAccessDescription,
          ),
          ChangelogCatalogEntry(Res.string.changelogCitySearchTitle, Res.string.changelogCitySearchDescription),
          ChangelogCatalogEntry(Res.string.changelogAndroidReviewTitle, Res.string.changelogAndroidReviewDescription),
        ),
      "0.22.2" to
        listOf(
          ChangelogCatalogEntry(
            Res.string.changelogFavoritesRedesignTitle,
            Res.string.changelogFavoritesRedesignDescription,
          ),
          ChangelogCatalogEntry(Res.string.changelogStabilityFixesTitle, Res.string.changelogStabilityFixesDescription),
        ),
      "0.22.9" to
        listOf(
          ChangelogCatalogEntry(
            Res.string.changelogNearbyScrollTitle,
            Res.string.changelogNearbyScrollDescription,
          ),
          ChangelogCatalogEntry(
            Res.string.changelogFeedbackFavoritesTitle,
            Res.string.changelogFeedbackFavoritesDescription,
          ),
        ),
    )

  fun catalogVersionSet(): Set<String> = entriesByVersion.keys

  fun entriesFor(versionName: String): List<ChangelogCatalogEntry> = entriesByVersion[versionName].orEmpty()

  fun latestVersionAtOrBefore(versionName: String): String? {
    val normalizedVersion = normalizeAppVersionForCatalog(versionName) ?: return null
    return entriesByVersion.keys
      .filter { compareAppVersionStrings(it, normalizedVersion) <= 0 }
      .maxWithOrNull { a, b -> compareAppVersionStrings(a, b) }
  }

  fun history(): List<ChangelogVersionSection> =
    entriesByVersion.keys
      .sortedWith { a, b -> compareAppVersionStrings(b, a) }
      .map { versionName ->
        ChangelogVersionSection(
          versionName = versionName,
          entries = entriesFor(versionName),
        )
      }
}
