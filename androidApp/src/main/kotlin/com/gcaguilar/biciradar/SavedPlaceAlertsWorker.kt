package com.gcaguilar.biciradar

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.notificationBody
import com.gcaguilar.biciradar.core.notificationTitle
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings
import java.util.concurrent.TimeUnit

class SavedPlaceAlertsWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    return runCatching {
      val platformBindings = AndroidPlatformBindings(applicationContext)
      val graph = SharedGraph.Companion.create(platformBindings)

      graph.settingsRepository.bootstrap()
      graph.savedPlaceAlertsRepository.bootstrap()
      graph.stationsRepository.forceRefresh()

      val evaluation = graph.savedPlaceAlertsEvaluator.evaluate(
        rules = graph.savedPlaceAlertsRepository.currentRules(),
        stationsState = graph.stationsRepository.state.value,
      )

      if (evaluation.updatedRules != graph.savedPlaceAlertsRepository.currentRules()) {
        graph.savedPlaceAlertsRepository.replaceAll(evaluation.updatedRules)
      }

      if (evaluation.triggers.isNotEmpty() && platformBindings.localNotifier.hasPermission()) {
        evaluation.triggers.forEach { trigger ->
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
  }

  companion object {
    private const val UNIQUE_WORK_NAME = "saved-place-alerts-worker"

    fun schedule(context: Context) {
      val request = PeriodicWorkRequestBuilder<SavedPlaceAlertsWorker>(15, TimeUnit.MINUTES)
        .setConstraints(
          Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build(),
        )
        .build()
      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        UNIQUE_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
      )
    }
  }
}
