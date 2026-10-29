package com.gcaguilar.biciradar.core

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RoutineRepositoryJvmTest {
  @Test
  fun `upsert, reload and delete preserve routine fields and step order`() =
    runTest {
      val driver = legacyDriver()
      val database = BiciRadarDatabase(driver)
      LegacyBlobToRelationalMigration.ensure(driver, database, Json)
      val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

      val repo = RoutineRepositoryImpl(scope, database)
      repo.bootstrap()
      val routine =
        Routine(
          id = "r-1",
          name = "Trabajo",
          daysOfWeek = setOf(RoutineDay.MONDAY, RoutineDay.FRIDAY),
          hour = 8,
          minute = 30,
          isEnabled = true,
          steps =
            listOf(
              RoutineStep(
                id = "s-1",
                stationId = "station-1",
                stationName = "Estación A",
                cityId = "zaragoza",
                durationSeconds = 600,
                kind = SurfaceMonitoringKind.Bikes,
                threshold = 3,
                fallbackStationId = "station-9",
                alternativeRadiusMeters = 250,
              ),
              RoutineStep(
                id = "s-2",
                stationId = "station-2",
                stationName = "Estación B",
                cityId = "zaragoza",
                durationSeconds = 900,
                kind = SurfaceMonitoringKind.Docks,
              ),
            ),
        )

      repo.upsertRoutine(routine)
      // Esperar a que el flujo de SQLDelight actualice rutina y pasos en memoria.
      repo.state.first { state -> state.any { it.id == "r-1" && it.steps.size == 2 } }
      assertEquals(routine, repo.routineById("r-1"))

      // Nuevo "proceso": se lee desde SQLDelight vía bootstrap.
      val reloaded = RoutineRepositoryImpl(scope, database)
      reloaded.bootstrap()
      assertEquals(routine, reloaded.routineById("r-1"))
      assertEquals(listOf("s-1", "s-2"), reloaded.routineById("r-1")!!.steps.map { it.id })

      repo.setEnabled("r-1", false)
      repo.state.first { it.singleOrNull()?.isEnabled == false }
      assertEquals(false, repo.routineById("r-1")?.isEnabled ?: true)

      repo.deleteRoutine("r-1")
      repo.state.first { it.isEmpty() }
      val afterDelete = RoutineRepositoryImpl(scope, database)
      afterDelete.bootstrap()
      assertNull(afterDelete.routineById("r-1"))
      assertEquals(emptyList(), afterDelete.state.value)

      scope.cancel()
    }

  private fun legacyDriver(): JdbcSqliteDriver {
    val dbPath = "${File(System.getProperty("java.io.tmpdir")).absolutePath}/biciradar-routines-${Random.nextInt()}.db"
    File(dbPath).delete()
    return JdbcSqliteDriver(url = "jdbc:sqlite:$dbPath")
  }
}
