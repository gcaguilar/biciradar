package com.gcaguilar.biciradar

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager

class BiciRadarApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    initializeWorkManagerSafely()
  }

  private fun initializeWorkManagerSafely() {
    val configuration = Configuration.Builder().build()
    runCatching {
      WorkManager.initialize(this, configuration)
    }.recoverCatching {
      // Some upgrades can leave WorkManager's internal Room DB in a bad state.
      purgeWorkManagerDatabaseFiles()
      WorkManager.initialize(this, configuration)
    }
  }

  private fun purgeWorkManagerDatabaseFiles() {
    val baseName = "androidx.work.workdb"
    listOf(baseName, "$baseName-wal", "$baseName-shm").forEach { name ->
      deleteDatabase(name)
      getDatabasePath(name)?.delete()
    }
  }
}
