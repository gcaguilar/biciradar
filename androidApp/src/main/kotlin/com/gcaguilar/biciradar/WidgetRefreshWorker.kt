package com.gcaguilar.biciradar

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Refresca los datos usados por los widgets y vuelve a pintar sus RemoteViews.
 * Solo se programa mientras exista al menos un widget activo.
 *
 * Importante: no llamar a reconcile() desde onUpdate() de los AppWidgetProvider.
 * WorkManager habilita/deshabilita su RescheduleReceiver al encolar/cancelar trabajo,
 * lo que dispara ACTION_PACKAGE_CHANGED → onUpdate() → loop infinito.
 * reconcile() solo debe llamarse desde onEnabled()/onDisabled().
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
    private const val IMMEDIATE_WORK_NAME = "widget-refresh-immediate"
    private const val REPEAT_INTERVAL_MINUTES = 15L

    fun reconcile(context: Context) {
      if (hasAnyWidgets(context)) {
        schedule(context)
      } else {
        cancel(context)
      }
    }

    fun scheduleImmediate(context: Context) {
      val request =
        OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
          .setConstraints(
            Constraints
              .Builder()
              .setRequiredNetworkType(NetworkType.CONNECTED)
              .build(),
          ).build()
      WorkManager.getInstance(context).enqueueUniqueWork(
        IMMEDIATE_WORK_NAME,
        ExistingWorkPolicy.REPLACE,
        request,
      )
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
        ExistingPeriodicWorkPolicy.KEEP,
        request,
      )
    }

    private fun cancel(context: Context) {
      WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
      WorkManager.getInstance(context).cancelUniqueWork(IMMEDIATE_WORK_NAME)
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
