package com.gcaguilar.biciradar.core.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File
import java.sql.SQLException

fun createJdbcDriver(databasePath: String): SqlDriver {
  File(databasePath).parentFile?.mkdirs()
  return JdbcSqliteDriver(
    url = "jdbc:sqlite:$databasePath",
  ).also { driver ->
    try {
      BiciRadarDatabase.Schema.create(driver)
    } catch (error: SQLException) {
      val message = error.message.orEmpty()
      if (!message.contains("already exists", ignoreCase = true)) {
        throw error
      }
    }
  }
}
