package com.gcaguilar.biciradar.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineModelsTest {
  @Test
  fun `bitmap roundtrip preserves days`() {
    val days = setOf(RoutineDay.MONDAY, RoutineDay.WEDNESDAY, RoutineDay.SUNDAY)
    assertEquals(days, routineDaysFromBitmap(routineDaysToBitmap(days)))
    assertEquals(emptySet(), routineDaysFromBitmap(0L))
    assertTrue(routineDaysToBitmap(setOf(RoutineDay.SATURDAY)) and (1L shl 5) != 0L)
  }

  @Test
  fun `daysUntilNextRoutineTrigger picks next day within the week`() {
    val days = setOf(RoutineDay.FRIDAY, RoutineDay.SUNDAY) // iso 4 y 6
    assertEquals(1, daysUntilNextRoutineTrigger(3, days)) // jueves -> viernes
    assertEquals(0, daysUntilNextRoutineTrigger(4, days)) // viernes -> viernes
    assertEquals(1, daysUntilNextRoutineTrigger(5, days)) // sábado -> domingo
    assertEquals(4, daysUntilNextRoutineTrigger(0, days)) // lunes -> viernes
    assertNull(daysUntilNextRoutineTrigger(0, emptySet()))
  }

  @Test
  fun `trigger time subtracts 10 minutes`() {
    assertEquals(
      RoutineTriggerTime(hour = 7, minute = 50, daysBefore = 0),
      routineTriggerTime(hour = 8, minute = 0),
    )
  }

  @Test
  fun `trigger time rolls to previous day`() {
    assertEquals(
      RoutineTriggerTime(hour = 23, minute = 50, daysBefore = 1),
      routineTriggerTime(hour = 0, minute = 0),
    )
  }

  @Test
  fun `routine is configured only with days and steps`() {
    val base =
      Routine(
        id = "r1",
        name = "Trabajo",
        daysOfWeek = setOf(RoutineDay.MONDAY),
        hour = 8,
        minute = 0,
      )
    assertTrue(!base.isConfigured)
    val withStep = base.copy(steps = listOf(step("s1")))
    assertTrue(withStep.isConfigured)
    val withoutDays = base.copy(daysOfWeek = emptySet(), steps = listOf(step("s1")))
    assertTrue(!withoutDays.isConfigured)
    val disabled = withStep.copy(isEnabled = false)
    assertTrue(disabled.isConfigured)
  }

  private fun step(id: String): RoutineStep =
    RoutineStep(
      id = id,
      stationId = "station-$id",
      stationName = "Station $id",
      durationSeconds = 600,
      kind = SurfaceMonitoringKind.Bikes,
    )
}
