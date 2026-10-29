package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.backend.UsagePatternsBackendApi
import io.ktor.client.HttpClient
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RoutingDatosBiziApiTest {
  private val zaragozaPattern = pattern(stationId = "42", bikesAvg = 3.0)

  @Test
  fun `datosbizi provider only covers zaragoza`() {
    val provider = DatosBiziApiImpl(HttpClient())

    assertTrue(provider.supports(City.ZARAGOZA.id))
    assertFalse(provider.supports(City.MADRID.id))
  }

  @Test
  fun `keeps zaragoza on datosbizi even when the backend is configured`() =
    runTest {
      val datosBizi =
        FakeDatosBiziProvider(supportedCityIds = setOf(City.ZARAGOZA.id), patterns = listOf(zaragozaPattern))
      val backend =
        FakeUsagePatternsBackendApi(isConfigured = true, patterns = listOf(pattern(stationId = "42", bikesAvg = 9.0)))
      val api = RoutingDatosBiziApi(datosBiziApi = datosBizi, backendPatternsApi = backend)

      assertTrue(api.supports(City.ZARAGOZA.id))
      assertEquals(listOf(zaragozaPattern), api.fetchPatterns(City.ZARAGOZA.id, "42"))
      assertEquals(listOf(City.ZARAGOZA.id to "42"), datosBizi.calls)
      assertTrue(backend.calls.isEmpty())
    }

  @Test
  fun `serves other cities from the backend when it is configured`() =
    runTest {
      val backendPattern = pattern(stationId = "7", bikesAvg = 4.5)
      val datosBizi = FakeDatosBiziProvider(supportedCityIds = setOf(City.ZARAGOZA.id))
      val backend = FakeUsagePatternsBackendApi(isConfigured = true, patterns = listOf(backendPattern))
      val api = RoutingDatosBiziApi(datosBiziApi = datosBizi, backendPatternsApi = backend)

      assertTrue(api.supports(City.MADRID.id))
      assertEquals(listOf(backendPattern), api.fetchPatterns(City.MADRID.id, "7"))
      assertEquals(listOf(City.MADRID.id to "7"), backend.calls)
      assertTrue(datosBizi.calls.isEmpty())
    }

  @Test
  fun `reports no patterns for cities without a provider`() =
    runTest {
      val datosBizi = FakeDatosBiziProvider(supportedCityIds = setOf(City.ZARAGOZA.id))
      val backend = FakeUsagePatternsBackendApi(isConfigured = false)
      val api = RoutingDatosBiziApi(datosBiziApi = datosBizi, backendPatternsApi = backend)

      assertFalse(api.supports(City.MADRID.id))
      assertTrue(api.fetchPatterns(City.MADRID.id, "7").isEmpty())
      assertTrue(datosBizi.calls.isEmpty())
      assertTrue(backend.calls.isEmpty())
    }

  @Test
  fun `zaragoza keeps working without a backend`() =
    runTest {
      val datosBizi =
        FakeDatosBiziProvider(supportedCityIds = setOf(City.ZARAGOZA.id), patterns = listOf(zaragozaPattern))
      val api =
        RoutingDatosBiziApi(
          datosBiziApi = datosBizi,
          backendPatternsApi = FakeUsagePatternsBackendApi(isConfigured = false),
        )

      assertTrue(api.supports(City.ZARAGOZA.id))
      assertEquals(listOf(zaragozaPattern), api.fetchPatterns(City.ZARAGOZA.id, "42"))
    }

  private fun pattern(
    stationId: String,
    bikesAvg: Double,
  ) = StationHourlyPattern(
    stationId = stationId,
    dayType = "WEEKDAY",
    hour = 8,
    bikesAvg = bikesAvg,
    anchorsAvg = 2.0,
    occupancyAvg = 0.5,
    sampleCount = 3,
  )
}

private class FakeDatosBiziProvider(
  private val supportedCityIds: Set<String>,
  private val patterns: List<StationHourlyPattern> = emptyList(),
) : DatosBiziApi {
  val calls = mutableListOf<Pair<String, String>>()

  override fun supports(cityId: String): Boolean = cityId in supportedCityIds

  override suspend fun fetchPatterns(
    cityId: String,
    stationId: String,
  ): List<StationHourlyPattern> {
    calls += cityId to stationId
    return patterns
  }
}

private class FakeUsagePatternsBackendApi(
  override val isConfigured: Boolean,
  private val patterns: List<StationHourlyPattern> = emptyList(),
) : UsagePatternsBackendApi {
  val calls = mutableListOf<Pair<String, String>>()

  override suspend fun fetchPatterns(
    cityId: String,
    stationId: String,
  ): List<StationHourlyPattern> {
    calls += cityId to stationId
    return patterns
  }
}
