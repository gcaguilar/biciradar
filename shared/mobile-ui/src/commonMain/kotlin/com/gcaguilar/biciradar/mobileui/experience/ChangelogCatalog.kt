package com.gcaguilar.biciradar.mobileui.experience

import com.gcaguilar.biciradar.mobile_ui.generated.resources.Res
import com.gcaguilar.biciradar.mobile_ui.generated.resources.*
import org.jetbrains.compose.resources.StringResource

data class ChangelogCatalogEntry(
  val titleKey: StringResource,
  val descriptionKey: StringResource,
)

/**
 * Local catalog keyed by [com.gcaguilar.biciradar.core.PlatformBindings.appVersion] strings.
 * Add a block per shipped release that should show the "What's new" dialog once.
 */
object ChangelogCatalog {
  private val entriesByVersion: Map<String, List<ChangelogCatalogEntry>> = mapOf(
    "0.18.1" to listOf(
      ChangelogCatalogEntry(Res.string.changelogUiImprovementsTitle, Res.string.changelogUiImprovementsDescription),
      ChangelogCatalogEntry(Res.string.changelogPerformanceImprovementsTitle, Res.string.changelogPerformanceImprovementsDescription),
      ChangelogCatalogEntry(Res.string.changelogLocalCacheTitle, Res.string.changelogLocalCacheDescription),
      ChangelogCatalogEntry(Res.string.changelogDataSourcesTitle, Res.string.changelogDataSourcesDescription),
    ),
    "0.19.0" to listOf(
      ChangelogCatalogEntry(Res.string.changelogUiImprovementsTitle, Res.string.changelogUiImprovementsDescription),
      ChangelogCatalogEntry(Res.string.changelogPerformanceImprovementsTitle, Res.string.changelogPerformanceImprovementsDescription),
      ChangelogCatalogEntry(Res.string.changelogLocalCacheTitle, Res.string.changelogLocalCacheDescription),
      ChangelogCatalogEntry(Res.string.changelogDataSourcesTitle, Res.string.changelogDataSourcesDescription),
      ChangelogCatalogEntry(Res.string.changelogSavedPlaceAlertsTitle, Res.string.changelogSavedPlaceAlertsDescription),
    ),
  )

  fun catalogVersionSet(): Set<String> = entriesByVersion.keys

  fun entriesFor(versionName: String): List<ChangelogCatalogEntry> =
    entriesByVersion[versionName].orEmpty()
}
