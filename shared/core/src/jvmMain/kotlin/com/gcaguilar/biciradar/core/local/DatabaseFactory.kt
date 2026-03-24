package com.gcaguilar.biciradar.core.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

fun createJdbcDriver(): SqlDriver {
  return JdbcSqliteDriver(
    url = "jdbc:sqlite:biciradar.db",
  ).also { driver ->
    BiciRadarDatabase.Schema.create(driver)
  }
}
