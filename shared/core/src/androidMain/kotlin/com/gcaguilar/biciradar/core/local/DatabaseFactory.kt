package com.gcaguilar.biciradar.core.local

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

fun createAndroidDriver(context: Context): SqlDriver =
  AndroidSqliteDriver(
    schema = BiciRadarDatabase.Schema,
    context = context,
    name = "biciradar.db",
  )
