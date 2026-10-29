package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.RoutineStep
import com.gcaguilar.biciradar.core.SavedPlaceAlertCondition
import com.gcaguilar.biciradar.core.SavedPlaceAlertRule
import com.gcaguilar.biciradar.core.SavedPlaceAlertTarget
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Contract tests against the exact JSON shapes of the backend `openapi.json`.
 */
class BackendContractTest {
  private val json =
    Json {
      ignoreUnknownKeys = true
      explicitNulls = false
    }

  @Test
  fun `decodes a routine payload from the backend`() {
    val payload =
      """
      {
        "routines": [
          {
            "id": "routine-1",
            "name": "Commute",
            "daysOfWeek": [0, 1, 2, 3, 4],
            "hour": 8,
            "minute": 0,
            "enabled": true,
            "steps": [
              {
                "id": "step-1",
                "stationId": "42",
                "cityId": "zaragoza",
                "kind": "DEPARTURE",
                "durationSeconds": 600,
                "threshold": 3,
                "fallbackStationId": "57",
                "alternativeRadiusMeters": 400
              }
            ]
          }
        ]
      }
      """.trimIndent()

    val routines = json.decodeFromString(RoutinesResponse.serializer(), payload).routines
    val routine = routines.single().toRoutine(stationNames = mapOf("42" to "Estación 42"))

    assertEquals("routine-1", routine.id)
    assertEquals(
      setOf(RoutineDay.MONDAY, RoutineDay.TUESDAY, RoutineDay.WEDNESDAY, RoutineDay.THURSDAY, RoutineDay.FRIDAY),
      routine.daysOfWeek,
    )
    assertEquals(8, routine.hour)
    assertEquals(1, routine.steps.size)
    val step = routine.steps.single()
    assertEquals(SurfaceMonitoringKind.Bikes, step.kind)
    assertEquals("zaragoza", step.cityId)
    assertEquals(3, step.threshold)
    assertEquals("57", step.fallbackStationId)
    assertEquals(400, step.alternativeRadiusMeters)
    assertEquals("Estación 42", step.stationName)
  }

  @Test
  fun `maps app routine to the backend wire shape`() {
    val routine =
      Routine(
        id = "r-1",
        name = "Llegada",
        daysOfWeek = setOf(RoutineDay.MONDAY, RoutineDay.SUNDAY),
        hour = 18,
        minute = 30,
        isEnabled = false,
        steps =
          listOf(
            RoutineStep(
              id = "s-1",
              stationId = "42",
              stationName = "A",
              cityId = "zaragoza",
              durationSeconds = 600,
              kind = SurfaceMonitoringKind.Docks,
              threshold = 1,
            ),
          ),
      )

    val dto = routine.toRoutineDto()
    assertEquals(listOf(0, 6), dto.daysOfWeek)
    assertFalse(dto.enabled)
    assertEquals("ARRIVAL", dto.steps.single().kind)
    assertEquals("zaragoza", dto.steps.single().cityId)
    assertEquals(1, dto.steps.single().threshold)
    assertNull(dto.steps.single().fallbackStationId)
  }

  @Test
  fun `encodes monitoring window request exactly as documented`() {
    val request =
      StartMonitoringWindowRequest(
        cityId = "zaragoza",
        stationId = "42",
        kind = "ARRIVAL",
        durationSeconds = 600,
        threshold = 1,
      )
    val encoded = json.encodeToString(StartMonitoringWindowRequest.serializer(), request)
    assertTrue(encoded.contains("\"kind\":\"ARRIVAL\""))
    assertTrue(encoded.contains("\"threshold\":1"))
    // Null fields are omitted thanks to explicitNulls = false.
    assertFalse(encoded.contains("fallbackStationId"))
  }

  @Test
  fun `decodes the backend usage pattern payload`() {
    val payload =
      """
      [
        {
          "stationId": "42",
          "dayType": "WEEKDAY",
          "hour": 8,
          "bikesAvg": 3.0,
          "anchorsAvg": 5.0,
          "occupancyAvg": 0.375,
          "sampleCount": 12
        }
      ]
      """.trimIndent()

    val patterns =
      json
        .decodeFromString(ListSerializer(UsagePatternDto.serializer()), payload)
        .map { it.toStationHourlyPattern() }

    val pattern = patterns.single()
    assertEquals("42", pattern.stationId)
    assertEquals("WEEKDAY", pattern.dayType)
    assertEquals(8, pattern.hour)
    assertEquals(3.0, pattern.bikesAvg)
    assertEquals(5.0, pattern.anchorsAvg)
    assertEquals(0.375, pattern.occupancyAvg)
    assertEquals(12, pattern.sampleCount)
  }

  @Test
  fun `decodes an empty usage pattern payload`() {
    val patterns = json.decodeFromString(ListSerializer(UsagePatternDto.serializer()), "[]")

    assertTrue(patterns.isEmpty())
  }

  @Test
  fun `decodes backend error bodies`() {
    val decoded =
      json.decodeFromString(
        ErrorResponse.serializer(),
        """{"error":"unauthorized","message":"Authentication required"}""",
      )
    assertEquals("unauthorized", decoded.error)
    assertEquals("Authentication required", decoded.message)
  }

  @Test
  fun `backend config is only configured with a non blank url`() {
    assertFalse(BackendConfig().isConfigured)
    assertFalse(BackendConfig("   ").isConfigured)
    val configured = BackendConfig("https://api.example.com/")
    assertTrue(configured.isConfigured)
    assertEquals("https://api.example.com", configured.normalizedBaseUrl)
    assertEquals("https://api.example.com/v1/routines", configured.url("/v1/routines"))
  }

  @Test
  fun `maps favourites and saved-place alerts to subscriptions`() {
    val snapshot =
      InterestSnapshot(
        favoriteIds = setOf("42", "57"),
        homeStationId = "42",
        workStationId = null,
        rules =
          listOf(
            SavedPlaceAlertRule(
              id = "rule-1",
              target =
                SavedPlaceAlertTarget.FavoriteStation(
                  stationId = "57",
                  cityId = "zaragoza",
                  stationName = "Estación 57",
                ),
              condition = SavedPlaceAlertCondition.BikesAtLeast(count = 3),
              isEnabled = true,
            ),
          ),
      )

    val subscriptions = buildSubscriptions(snapshot, cityId = "zaragoza")

    // Favourites are de-duplicated (42 is both favourite and home).
    val favorites = subscriptions.filter { it.kind == "FAVORITE" }
    assertEquals(setOf("favorite:42", "favorite:57"), favorites.map { it.id }.toSet())
    val alert = subscriptions.single { it.kind == "ALERT" }
    assertEquals("rule-1", alert.id)
    assertEquals("BIKES_AT_LEAST", alert.condition)
    assertEquals(3, alert.threshold)
    assertEquals("zaragoza", alert.cityId)
  }

  @Test
  fun `a missing patterns city becomes a typed failure and other errors stay`() {
    val notOffered = BackendException("not found", status = 404).toPatternsFailure("murcia")
    assertTrue(notOffered is PatternsNotOfferedException)
    assertEquals("murcia", notOffered.cityId)

    val serverError = BackendException("boom", status = 500).toPatternsFailure("madrid")
    assertTrue(serverError is BackendException)
    assertEquals(500, serverError.status)
  }
}
