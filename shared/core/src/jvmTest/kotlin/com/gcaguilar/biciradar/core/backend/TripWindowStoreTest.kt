package com.gcaguilar.biciradar.core.backend

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.gcaguilar.biciradar.core.local.BiciRadarDatabase
import com.gcaguilar.biciradar.core.local.LegacyBlobToRelationalMigration
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TripWindowStoreTest {
  @Test
  fun `persists and clears the open trip window`() {
    val driver = legacyDriver()
    val database = BiciRadarDatabase(driver)
    LegacyBlobToRelationalMigration.ensure(driver, database, Json)
    val store = TripWindowStore(database)

    assertNull(store.current())
    store.save(windowId = "mwindow-1", stationId = "42", startedAt = 1_000L)

    val persisted = store.current()
    assertEquals("mwindow-1", persisted?.windowId)
    assertEquals("42", persisted?.stationId)
    assertEquals(1_000L, persisted?.startedAt)

    store.clear()
    assertNull(store.current())
  }

  @Test
  fun `is a no-op without a database`() {
    val store = TripWindowStore(null)
    store.save(windowId = "w", stationId = "1", startedAt = 0)
    assertNull(store.current())
    store.clear()
  }

  private fun legacyDriver(): JdbcSqliteDriver {
    val dbPath = "${File(System.getProperty("java.io.tmpdir")).absolutePath}/biciradar-trip-${Random.nextInt()}.db"
    File(dbPath).delete()
    return JdbcSqliteDriver(url = "jdbc:sqlite:$dbPath")
  }
}
