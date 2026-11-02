package com.gcaguilar.biciradar.appfunctions

import com.gcaguilar.biciradar.core.StationRequirement
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppFunctionArgumentsTest {
  @Test
  fun `requirement parsing accepts every documented value in any case`() {
    assertEquals(StationRequirement.ANY, parseStationRequirement("ANY"))
    assertEquals(StationRequirement.BIKES, parseStationRequirement("BIKES"))
    assertEquals(StationRequirement.EBIKES, parseStationRequirement("EBIKES"))
    assertEquals(StationRequirement.SLOTS, parseStationRequirement("SLOTS"))
    assertEquals(StationRequirement.BIKES, parseStationRequirement("bikes"))
    assertEquals(StationRequirement.EBIKES, parseStationRequirement("Ebikes"))
    assertEquals(StationRequirement.SLOTS, parseStationRequirement("  slots  "))
  }

  @Test
  fun `requirement parsing rejects unknown values and blanks`() {
    assertNull(parseStationRequirement(""))
    assertNull(parseStationRequirement("   "))
    assertNull(parseStationRequirement("scooters"))
    assertNull(parseStationRequirement("BIKE"))
    assertNull(parseStationRequirement("ANYTHING"))
  }

  @Test
  fun `limit validation accepts the documented range only`() {
    assertTrue(isValidResultLimit(1))
    assertTrue(isValidResultLimit(5))
    assertTrue(isValidResultLimit(20))
    assertFalse(isValidResultLimit(0))
    assertFalse(isValidResultLimit(21))
    assertFalse(isValidResultLimit(-1))
  }

  @Test
  fun `origin parsing accepts both coordinates or none`() {
    assertEquals(OriginArgument.Absent, parseOriginArgument(null, null))

    val present = parseOriginArgument(41.6488, -0.8751)
    assertTrue(present is OriginArgument.Present)
    assertEquals(41.6488, (present as OriginArgument.Present).point.latitude, 0.0)
    assertEquals(-0.8751, present.point.longitude, 0.0)
  }

  @Test
  fun `origin parsing rejects a partial coordinate pair`() {
    assertEquals(OriginArgument.Invalid, parseOriginArgument(41.6488, null))
    assertEquals(OriginArgument.Invalid, parseOriginArgument(null, -0.8751))
  }

  @Test
  fun `origin parsing rejects out of range and non finite coordinates`() {
    assertEquals(OriginArgument.Invalid, parseOriginArgument(90.1, 0.0))
    assertEquals(OriginArgument.Invalid, parseOriginArgument(-90.1, 0.0))
    assertEquals(OriginArgument.Invalid, parseOriginArgument(0.0, 180.1))
    assertEquals(OriginArgument.Invalid, parseOriginArgument(0.0, -180.1))
    assertEquals(OriginArgument.Invalid, parseOriginArgument(Double.NaN, 0.0))
    assertEquals(OriginArgument.Invalid, parseOriginArgument(0.0, Double.POSITIVE_INFINITY))
  }

  @Test
  fun `origin parsing accepts the coordinate boundaries`() {
    assertTrue(parseOriginArgument(90.0, 180.0) is OriginArgument.Present)
    assertTrue(parseOriginArgument(-90.0, -180.0) is OriginArgument.Present)
  }

  @Test
  fun `pinned alias detection matches the shared alias vocabulary`() {
    assertTrue(isPinnedStationAlias("casa"))
    assertTrue(isPinnedStationAlias("Mi Casa"))
    assertTrue(isPinnedStationAlias("home"))
    assertTrue(isPinnedStationAlias("trabajo"))
    assertTrue(isPinnedStationAlias("mi trabajo"))
    assertTrue(isPinnedStationAlias("work"))
    assertTrue(isPinnedStationAlias("oficina"))
    assertTrue(isPinnedStationAlias("mi oficina"))
  }

  @Test
  fun `pinned alias detection ignores regular station queries`() {
    assertFalse(isPinnedStationAlias("Plaza España"))
    assertFalse(isPinnedStationAlias("casa de campo"))
    assertFalse(isPinnedStationAlias("workstation"))
    assertFalse(isPinnedStationAlias(""))
  }

  @Test
  fun `monitoring kind parsing accepts bikes and docks in any case`() {
    assertEquals(SurfaceMonitoringKind.Bikes, parseMonitoringKind("BIKES"))
    assertEquals(SurfaceMonitoringKind.Bikes, parseMonitoringKind(" bikes "))
    assertEquals(SurfaceMonitoringKind.Docks, parseMonitoringKind("Docks"))
    assertEquals(SurfaceMonitoringKind.Docks, parseMonitoringKind("docks"))
    assertNull(parseMonitoringKind(""))
    assertNull(parseMonitoringKind("EBIKES"))
  }

  @Test
  fun `monitoring duration validation accepts one to sixty only`() {
    assertTrue(isValidMonitoringDuration(1))
    assertTrue(isValidMonitoringDuration(10))
    assertTrue(isValidMonitoringDuration(60))
    assertFalse(isValidMonitoringDuration(0))
    assertFalse(isValidMonitoringDuration(61))
    assertFalse(isValidMonitoringDuration(-1))
  }

  @Test
  fun `alert target parsing accepts every documented saved place`() {
    assertEquals(AlertTargetArgument.FAVORITE, parseAlertTargetArgument("FAVORITE"))
    assertEquals(AlertTargetArgument.FAVORITE, parseAlertTargetArgument(" favorite "))
    assertEquals(AlertTargetArgument.HOME, parseAlertTargetArgument("Home"))
    assertEquals(AlertTargetArgument.WORK, parseAlertTargetArgument("work"))
    assertNull(parseAlertTargetArgument("CATEGORY"))
    assertNull(parseAlertTargetArgument(""))
  }

  @Test
  fun `alert condition parsing accepts every documented condition`() {
    assertEquals(
      AlertConditionArgument.BIKES_AT_LEAST,
      parseAlertConditionArgument("BIKES_AT_LEAST"),
    )
    assertEquals(
      AlertConditionArgument.DOCKS_AT_LEAST,
      parseAlertConditionArgument(" docks_at_least "),
    )
    assertEquals(
      AlertConditionArgument.BIKES_EQUALS_ZERO,
      parseAlertConditionArgument("Bikes_Equals_Zero"),
    )
    assertEquals(
      AlertConditionArgument.DOCKS_EQUALS_ZERO,
      parseAlertConditionArgument("docks_equals_zero"),
    )
    assertNull(parseAlertConditionArgument("BIKES_ABOVE"))
    assertNull(parseAlertConditionArgument(""))
  }

  @Test
  fun `alert count validation accepts one to one hundred only`() {
    assertTrue(isValidAlertCount(1))
    assertTrue(isValidAlertCount(100))
    assertFalse(isValidAlertCount(0))
    assertFalse(isValidAlertCount(101))
  }
}
