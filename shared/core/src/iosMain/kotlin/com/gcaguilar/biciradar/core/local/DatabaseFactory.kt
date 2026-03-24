package com.gcaguilar.biciradar.core.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

fun createNativeDriver(): SqlDriver {
  return NativeSqliteDriver(
    schema = BiciRadarDatabase.Schema,
    name = "biciradar.db",
  )
}