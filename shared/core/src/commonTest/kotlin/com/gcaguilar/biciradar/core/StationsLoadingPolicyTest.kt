package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals

class StationsLoadingPolicyTest {
  @Test
  fun `computeStationsFreshness returns Fresh when cache is recent`() {
    val now = 1_000_000L
    val lastUpdated = now - 30_000L // 30 seconds ago
    assertEquals(DataFreshness.Fresh, computeStationsFreshness(lastUpdated, now, false, false, false))
  }

  @Test
  fun `computeStationsFreshness returns StaleUsable when cache is between 1min and 1hour`() {
    val now = 1_000_000L
    val lastUpdated = now - 300_000L // 5 minutes ago
    assertEquals(DataFreshness.StaleUsable, computeStationsFreshness(lastUpdated, now, false, false, false))
  }

  @Test
  fun `computeStationsFreshness returns Expired when cache is older than 1 hour`() {
    val now = 1_000_000L
    val lastUpdated = now - 3_700_000L // 1 hour 1 minute ago
    assertEquals(DataFreshness.Expired, computeStationsFreshness(lastUpdated, now, false, false, false))
  }

  @Test
  fun `computeStationsFreshness returns StaleUsable when servingCacheAfterFailure with recent cache`() {
    val now = 1_000_000L
    val lastUpdated = now - 30_000L // 30 seconds ago
    assertEquals(DataFreshness.StaleUsable, computeStationsFreshness(lastUpdated, now, true, false, false))
  }

  @Test
  fun `computeStationsFreshness returns Expired when servingCacheAfterFailure with old cache`() {
    val now = 1_000_000L
    val lastUpdated = now - 3_700_000L // 1 hour 1 minute ago
    assertEquals(DataFreshness.Expired, computeStationsFreshness(lastUpdated, now, true, false, false))
  }

  @Test
  fun `computeStationsFreshness returns Unavailable when hardFailure and stationsEmpty`() {
    val now = 1_000_000L
    assertEquals(DataFreshness.Unavailable, computeStationsFreshness(null, now, false, true, true))
  }

  @Test
  fun `computeStationsFreshness returns Fresh when no lastUpdated and stations not empty`() {
    val now = 1_000_000L
    assertEquals(DataFreshness.Fresh, computeStationsFreshness(null, now, false, false, false))
  }

  @Test
  fun `computeStationsFreshness returns Unavailable when no lastUpdated and stations empty`() {
    val now = 1_000_000L
    assertEquals(DataFreshness.Unavailable, computeStationsFreshness(null, now, false, true, false))
  }
}
