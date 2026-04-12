package com.gcaguilar.biciradar

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gcaguilar.biciradar.core.notificationBody
import com.gcaguilar.biciradar.core.notificationTitle
import java.util.concurrent.TimeUnit

/**
 * Worker que evalúa reglas de alertas de lugares guardados.
 * Usa BiziAppGraph para obtener dependencias, evitando duplicar instancias.
 */
class SavedPlaceAlertsWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result =
    runCatching {
      // Asegurar que el grafo está inicializado
      if (!BiziAppGraph.isInitialized()) {
        BiziAppGraph.initialize(applicationContext as android.app.Application)
      }

      val graph = BiziAppGraph.graph
      val platformBindings = BiziAppGraph.platformBindings

      graph.bootstrapSession.execute()

      val triggers = graph.evaluateSavedPlaceAlerts.execute()

      if (triggers.isNotEmpty() && platformBindings.localNotifier.hasPermission()) {
        triggers.forEach { trigger ->
          platformBindings.localNotifier.notify(
            trigger.notificationTitle(),
            trigger.notificationBody(),
          )
        }
      }

      Result.success()
    }.getOrElse {
      Result.retry()
    }

  companion object {
    private const val UNIQUE_WORK_NAME = "saved-place-alerts-worker"

    fun schedule(context: Context) {
      val request =
        PeriodicWorkRequestBuilder<SavedPlaceAlertsWorker>(15, TimeUnit.MINUTES)
          .setConstraints(
            Constraints
              .Builder()
              .setRequiredNetworkType(NetworkType.CONNECTED)
              .build(),
          ).build()
      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
      )
    }
  }
}
