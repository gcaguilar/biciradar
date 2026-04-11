package com.gcaguilar.biciradar.core.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json

class LegacyBlobToRelationalMigrationJvmTest {
  @Test
  fun `ensure creates saved place alerts table for pre-alert upgrade databases`() {
    val driver = legacyDriver()
    driver.execute(
      null,
      """
        CREATE TABLE IF NOT EXISTS stations (
          id TEXT PRIMARY KEY NOT NULL,
          name TEXT NOT NULL,
          address TEXT,
          latitude REAL NOT NULL,
          longitude REAL NOT NULL,
          bikes_available INTEGER NOT NULL,
          slots_free INTEGER NOT NULL,
          ebikes_available INTEGER NOT NULL DEFAULT 0,
          regular_bikes_available INTEGER NOT NULL DEFAULT 0,
          updated_at INTEGER NOT NULL
        )
      """.trimIndent(),
      0,
    )
    driver.execute(
      null,
      """
        CREATE TABLE IF NOT EXISTS cache_metadata (
          city_id TEXT PRIMARY KEY NOT NULL,
          last_updated INTEGER NOT NULL
        )
      """.trimIndent(),
      0,
    )

    val database = BiciRadarDatabase(driver)

    LegacyBlobToRelationalMigration.ensure(driver, database, Json)

    database.biciradarQueries.getAllSavedPlaceAlertRules().executeAsList()
  }

  @Test
  fun `ensure creates the full relational schema for pre-relational upgrade databases`() {
    val driver = legacyDriver()
    driver.execute(
      null,
      """
        CREATE TABLE IF NOT EXISTS stations (
          id TEXT PRIMARY KEY NOT NULL,
          name TEXT NOT NULL,
          address TEXT,
          latitude REAL NOT NULL,
          longitude REAL NOT NULL,
          bikes_available INTEGER NOT NULL,
          slots_free INTEGER NOT NULL,
          ebikes_available INTEGER NOT NULL DEFAULT 0,
          regular_bikes_available INTEGER NOT NULL DEFAULT 0,
          updated_at INTEGER NOT NULL
        )
      """.trimIndent(),
      0,
    )
    driver.execute(
      null,
      """
        CREATE TABLE IF NOT EXISTS cache_metadata (
          city_id TEXT PRIMARY KEY NOT NULL,
          last_updated INTEGER NOT NULL
        )
      """.trimIndent(),
      0,
    )

    val database = BiciRadarDatabase(driver)

    LegacyBlobToRelationalMigration.ensure(driver, database, Json)

    val actualTables = driver.executeQuery(
      null,
      "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
      { cursor ->
        val tables = mutableListOf<String>()
        while (cursor.next().value) {
          cursor.getString(0)?.let(tables::add)
        }
        app.cash.sqldelight.db.QueryResult.Value(tables)
      },
      0,
    ).value

    assertEquals(
      listOf(
        "app_settings",
        "cache_metadata",
        "environmental_cache",
        "favorite_categories",
        "favorite_ids",
        "favorite_roles",
        "favorite_station_category",
        "saved_place_alert_rules",
        "settings_map_filter_name",
        "stations",
        "surface_header",
        "surface_monitoring",
        "surface_station_row",
      ),
      actualTables,
    )

    database.biciradarQueries.getAllStations().executeAsList()
    database.biciradarQueries.getCacheMetadata().executeAsOneOrNull()
    database.biciradarQueries.getEnvironmentalReadingByZoneKey("test-zone").executeAsOneOrNull()
    database.biciradarQueries.getAllFavoriteIds().executeAsList()
    database.biciradarQueries.getFavoriteRoles().executeAsOneOrNull()
    database.biciradarQueries.getAllFavoriteCategories().executeAsList()
    database.biciradarQueries.getAllFavoriteStationCategories().executeAsList()
    database.biciradarQueries.getAllSavedPlaceAlertRules().executeAsList()
    database.biciradarQueries.getAppSettings().executeAsOneOrNull()
    database.biciradarQueries.getAllSettingsMapFilterNames().executeAsList()
    database.biciradarQueries.getSurfaceHeader().executeAsOneOrNull()
    database.biciradarQueries.getAllSurfaceStationRows().executeAsList()
    database.biciradarQueries.getSurfaceMonitoring().executeAsOneOrNull()
  }

  private fun legacyDriver(): JdbcSqliteDriver {
    val dbPath = "${File(System.getProperty("java.io.tmpdir")).absolutePath}/biciradar-legacy-${Random.nextInt()}.db"
    File(dbPath).delete()
    return JdbcSqliteDriver(url = "jdbc:sqlite:$dbPath")
  }
}
