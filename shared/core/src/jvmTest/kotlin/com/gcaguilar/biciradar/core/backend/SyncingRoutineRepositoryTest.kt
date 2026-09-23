package com.gcaguilar.biciradar.core.backend

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.gcaguilar.biciradar.core.LocalRoutineRepository
import com.gcaguilar.biciradar.core.NoOpLogger
import com.gcaguilar.biciradar.core.Routine
import com.gcaguilar.biciradar.core.RoutineDay
import com.gcaguilar.biciradar.core.RoutineRepositoryImpl
import com.gcaguilar.biciradar.core.RoutineStep
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import com.gcaguilar.biciradar.testutils.FakeSettingsRepository
import com.gcaguilar.biciradar.testutils.FakeStationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SyncingRoutineRepositoryTest {
  @Test
  fun `first sync uploads local routines`() =
    runTest {
      val env = environment(backgroundScope)
      env.local.seed(routine("r-1"))

      env.syncing.syncNow()

      assertEquals(setOf("r-1"), env.backend.routines.keys)
      assertTrue(env.syncStore.hasSynced())
    }

  @Test
  fun `downloads routines that only exist on the server`() =
    runTest {
      val env = environment(backgroundScope)
      env.backend.routines["r-2"] = routine("r-2")

      env.syncing.syncNow()

      val downloaded = env.local.awaitHas("r-2")
      assertEquals("zaragoza", downloaded.steps.single().cityId)
    }

  @Test
  fun `removes clean local routines deleted on another device`() =
    runTest {
      val env = environment(backgroundScope)
      env.local.seed(routine("r-1"))
      env.syncing.syncNow()
      env.local.awaitHas("r-1")

      // Another device deletes it; the next reconcile must mirror that.
      env.backend.routines.remove("r-1")
      env.syncing.syncNow()

      env.local.awaitGone("r-1")
    }

  @Test
  fun `a dirty local edit wins until pushed`() =
    runTest {
      val env = environment(backgroundScope)
      env.local.seed(routine("r-1"))
      env.syncing.syncNow()

      val edited = routine("r-1").copy(name = "Editada")
      env.syncStore.markDirty("r-1")
      env.local.seed(edited)
      // The server still holds the old copy.
      env.syncing.syncNow()

      assertEquals("Editada", env.backend.routines["r-1"]?.name)
      assertEquals("Editada", env.local.awaitHas("r-1").name)
    }

  @Test
  fun `pending local deletions are pushed`() =
    runTest {
      val env = environment(backgroundScope)
      env.local.seed(routine("r-1"))
      env.syncing.syncNow()

      env.syncStore.markPendingDelete("r-1")
      env.local.deleteRoutine("r-1")
      env.syncing.syncNow()

      assertTrue("r-1" in env.backend.deleted)
      env.local.awaitGone("r-1")
    }

  @Test
  fun `anonymous users never touch the backend`() =
    runTest {
      val env = environment(backgroundScope, authenticated = false)
      env.local.seed(routine("r-1"))

      env.syncing.syncNow()

      assertTrue(env.backend.routines.isEmpty())
      assertTrue(env.backend.deleted.isEmpty())
    }

  private suspend fun LocalRoutineRepository.seed(routine: Routine) {
    upsertRoutine(routine)
    state.first { list -> list.any { it == routine } }
  }

  private suspend fun LocalRoutineRepository.awaitHas(id: String): Routine =
    state.first { list -> list.any { it.id == id } }.first { it.id == id }

  private suspend fun LocalRoutineRepository.awaitGone(id: String) {
    state.first { list -> list.none { it.id == id } }
  }

  private fun environment(
    scope: kotlinx.coroutines.CoroutineScope,
    authenticated: Boolean = true,
  ): Environment {
    val driver = legacyDriver()
    val database = BiciRadarDatabase(driver)
    LegacyBlobToRelationalMigration.ensure(driver, database, Json)
    val local: LocalRoutineRepository = RoutineRepositoryImpl(scope, database)
    val backend = FakeRoutineBackendApi()
    val syncStore = RoutineSyncStore(database)
    val syncing =
      SyncingRoutineRepository(
        local = local,
        apiClient = backend,
        authProvider = FakeAuthProvider(authenticated),
        syncStore = syncStore,
        settingsRepository = FakeSettingsRepository(),
        stationsRepository = FakeStationsRepository(),
        logger = NoOpLogger,
        scope = scope,
      )
    return Environment(local, backend, syncStore, syncing)
  }

  private data class Environment(
    val local: LocalRoutineRepository,
    val backend: FakeRoutineBackendApi,
    val syncStore: RoutineSyncStore,
    val syncing: SyncingRoutineRepository,
  )

  private fun routine(id: String): Routine =
    Routine(
      id = id,
      name = "Rutina $id",
      daysOfWeek = setOf(RoutineDay.MONDAY),
      hour = 8,
      minute = 0,
      isEnabled = true,
      steps =
        listOf(
          RoutineStep(
            id = "$id-step",
            stationId = "station-1",
            stationName = "Plaza Espana",
            cityId = "zaragoza",
            durationSeconds = 600,
            kind = SurfaceMonitoringKind.Bikes,
          ),
        ),
    )

  private fun legacyDriver(): JdbcSqliteDriver {
    val dbPath = "${File(System.getProperty("java.io.tmpdir")).absolutePath}/biciradar-sync-${Random.nextInt()}.db"
    File(dbPath).delete()
    return JdbcSqliteDriver(url = "jdbc:sqlite:$dbPath")
  }

  private class FakeAuthProvider(
    authenticated: Boolean,
  ) : AuthProvider {
    override val credentialsState =
      MutableStateFlow(if (authenticated) AuthState.Authenticated else AuthState.Anonymous)

    override suspend fun login(): Boolean = true

    override suspend fun logout() = Unit

    override suspend fun accessToken(): String? = "token"
  }

  private class FakeRoutineBackendApi : RoutineBackendApi {
    override val isConfigured: Boolean = true
    val routines = linkedMapOf<String, Routine>()
    val deleted = mutableListOf<String>()

    override suspend fun listRoutines(): List<RoutineDto> = routines.values.map { it.toRoutineDto() }

    override suspend fun upsertRoutine(routine: Routine): RoutineDto {
      routines[routine.id] = routine
      return routine.toRoutineDto()
    }

    override suspend fun deleteRoutine(id: String) {
      routines.remove(id)
      deleted += id
    }
  }
}
