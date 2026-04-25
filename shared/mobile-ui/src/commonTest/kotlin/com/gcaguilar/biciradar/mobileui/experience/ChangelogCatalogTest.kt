package com.gcaguilar.biciradar.mobileui.experience

import kotlin.test.Test
import kotlin.test.assertEquals

class ChangelogCatalogTest {
  @Test
  fun `latestVersionAtOrBefore reuses the newest compatible catalog entry`() {
    assertEquals("0.19.0", ChangelogCatalog.latestVersionAtOrBefore("0.19.1"))
    assertEquals("0.18.1", ChangelogCatalog.latestVersionAtOrBefore("0.18.9"))
    assertEquals("0.19.0", ChangelogCatalog.latestVersionAtOrBefore("0.20.0"))
    assertEquals("0.21.0", ChangelogCatalog.latestVersionAtOrBefore("0.21.0"))
    assertEquals("0.21.1", ChangelogCatalog.latestVersionAtOrBefore("0.22.0"))
  }

  @Test
  fun `history is sorted from newest to oldest`() {
    assertEquals(
      listOf("0.22.9", "0.22.2", "0.21.1", "0.21.0", "0.19.0", "0.18.1"),
      ChangelogCatalog.history().map { it.versionName },
    )
  }
}
