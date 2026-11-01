package com.gcaguilar.biciradar.mobileui.layout

import androidx.compose.material3.adaptive.HingeInfo
import androidx.compose.material3.adaptive.Posture
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BiziWindowInfoTest {
  @Test
  fun `narrow and short windows stay compact`() {
    assertEquals(
      BiziWindowLayout.Compact,
      biziWindowLayoutFor(
        widthAtLeastMedium = false,
        widthAtLeastExpanded = false,
        heightAtLeastMedium = true,
      ),
    )
    assertEquals(
      BiziWindowLayout.Compact,
      biziWindowLayoutFor(
        widthAtLeastMedium = true,
        widthAtLeastExpanded = true,
        heightAtLeastMedium = false,
      ),
    )
  }

  @Test
  fun `medium width uses the medium layout`() {
    assertEquals(
      BiziWindowLayout.Medium,
      biziWindowLayoutFor(
        widthAtLeastMedium = true,
        widthAtLeastExpanded = false,
        heightAtLeastMedium = true,
      ),
    )
  }

  @Test
  fun `expanded width uses the expanded layout`() {
    assertEquals(
      BiziWindowLayout.Expanded,
      biziWindowLayoutFor(
        widthAtLeastMedium = true,
        widthAtLeastExpanded = true,
        heightAtLeastMedium = true,
      ),
    )
  }

  @Test
  fun `wide layout only for medium and expanded`() {
    assertTrue(BiziWindowInfo(layout = BiziWindowLayout.Medium).isWide)
    assertTrue(BiziWindowInfo(layout = BiziWindowLayout.Expanded).isWide)
    assertEquals(false, BiziWindowInfo.Compact.isWide)
  }

  @Test
  fun `separating hinge is reported with dp coordinates`() {
    val density = Density(density = 2f, fontScale = 1f)
    val posture =
      Posture(
        isTabletop = false,
        hingeList =
          listOf(
            HingeInfo(
              bounds = Rect(left = 500f, top = 0f, right = 520f, bottom = 2000f),
              isFlat = false,
              isVertical = true,
              isSeparating = true,
              isOccluding = false,
            ),
          ),
      )

    val hinge = posture.separatingHinge(density)

    assertEquals(true, hinge?.isVertical)
    assertEquals(250.dp, hinge?.start)
    assertEquals(260.dp, hinge?.end)
    assertEquals(10.dp, hinge?.size)
  }

  @Test
  fun `flat hinges are not considered separating`() {
    val density = Density(density = 1f, fontScale = 1f)
    val posture =
      Posture(
        isTabletop = false,
        hingeList =
          listOf(
            HingeInfo(
              bounds = Rect(left = 0f, top = 900f, right = 1080f, bottom = 900f),
              isFlat = true,
              isVertical = false,
              isSeparating = false,
              isOccluding = false,
            ),
          ),
      )

    assertNull(posture.separatingHinge(density))
  }

  @Test
  fun `horizontal hinge reports vertical coordinates`() {
    val density = Density(density = 3f, fontScale = 1f)
    val posture =
      Posture(
        isTabletop = true,
        hingeList =
          listOf(
            HingeInfo(
              bounds = Rect(left = 0f, top = 600f, right = 1200f, bottom = 630f),
              isFlat = false,
              isVertical = false,
              isSeparating = true,
              isOccluding = false,
            ),
          ),
      )

    val hinge = posture.separatingHinge(density)

    assertEquals(false, hinge?.isVertical)
    assertEquals(200.dp, hinge?.start)
    assertEquals(210.dp, hinge?.end)
  }
}
