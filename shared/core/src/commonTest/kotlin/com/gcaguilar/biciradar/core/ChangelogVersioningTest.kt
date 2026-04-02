package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChangelogVersioningTest {
  @Test
  fun compareAppVersionStrings_ordersSemver() {
    assertEquals(0, compareAppVersionStrings("1.0.0", "1.0.0"))
    assertTrue(compareAppVersionStrings("0.18.1", "0.19.0") < 0)
    assertTrue(compareAppVersionStrings("0.19.0", "0.18.1") > 0)
    assertTrue(compareAppVersionStrings("1.10.0", "1.9.0") > 0)
  }

  @Test
  fun pendingChangelogVersion_requiresCatalogAndNewerLastSeen() {
    val catalog = setOf("0.18.1", "0.19.0")
    assertNull(pendingChangelogVersion("0.19.0", null, catalog))
    assertNull(pendingChangelogVersion("0.19.0", "0.19.0", catalog))
    assertEquals("0.19.0", pendingChangelogVersion("0.19.0", "0.18.1", catalog))
    assertNull(pendingChangelogVersion("0.18.1", "0.18.0", setOf("0.19.0")))
    assertEquals("0.19.0", pendingChangelogVersion("0.19.1", "0.18.1", catalog))
    assertNull(pendingChangelogVersion("0.19.1", "0.19.0", catalog))
  }
}
