package com.gcaguilar.biciradar

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Refresca los datos usados por los widgets y vuelve a pintar sus RemoteViews.
 * Solo se programa mientras exista al menos un widget activo.
 */
class WidgetRefreshWorker(
  context: Context,
  params: WorkerParameters,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    if (!hasAnyWidgets(applicationContext)) {
      cancel(applicationContext)
      return Result.success()
    }

    return runCatching {
      if (!BiziAppGraph.isInitialized()) {
        BiziAppGraph.initialize(applicationContext as Application)
      }

      val graph = BiziAppGraph.graph
      graph.bootstrapSession.execute()
      graph.refreshStationDataIfNeeded.execute(forceRefresh = true)
      updateAllWidgets(applicationContext)

      Result.success()
    }.getOrElse {
      Result.retry()
    }
  }

  companion object {
    private const val UNIQUE_WORK_NAME = "widget-refresh-worker"
    private const val REPEAT_INTERVAL_MINUTES = 30L

    fun reconcile(context: Context) {
      if (hasAnyWidgets(context)) {
        schedule(context)
      } else {
        cancel(context)
      }
    }

    private fun schedule(context: Context) {
      val request =
        PeriodicWorkRequestBuilder<WidgetRefreshWorker>(REPEAT_INTERVAL_MINUTES, TimeUnit.MINUTES)
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

    private fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private fun hasAnyWidgets(context: Context): Boolean {
      val manager = AppWidgetManager.getInstance(context)
      return providers.any { provider ->
        manager.getAppWidgetIds(ComponentName(context, provider)).isNotEmpty()
      }
    }

    private fun updateAllWidgets(context: Context) {
      FavoriteStationWidgetProvider.updateAll(context)
      NearbyStationsWidgetProvider.updateAll(context)
      QuickActionsWidgetProvider.updateAll(context)
      CommuteWidgetProvider.updateAll(context)
    }

    private val providers =
      listOf(
        FavoriteStationWidgetProvider::class.java,
        NearbyStationsWidgetProvider::class.java,
        QuickActionsWidgetProvider::class.java,
        CommuteWidgetProvider::class.java,
      )
  }
}
