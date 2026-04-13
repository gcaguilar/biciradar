package com.gcaguilar.biciradar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gcaguilar.biciradar.core.RefreshWidgetDataUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Foreground service que refresca los datos de los widgets de Android
 * cada [INTERVAL_MS] milisegundos mientras el servicio está activo.
 *
 * Se inicia desde [BiciRadarApplication] al crear la aplicación y se mantiene
 * corriendo como foreground service para garantizar actualizaciones periódicas
 * incluso cuando la actividad principal no está visible.
 *
 * El servicio bootstrapará el grafo lazily si aún no se ha inicializado,
 * por lo que es seguro arrancarlo antes de que el usuario haya abierto la app.
 */
class WidgetRefreshService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val notificationManager by lazy {
    getSystemService(NotificationManager::class.java)
  }

  private var refreshJob: Job? = null

  private val refreshWidgetDataUseCase: RefreshWidgetDataUseCase?
    get() =
      runCatching { BiziAppGraph.graph }
        .getOrNull()
        ?.refreshWidgetDataUseCase

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    ensureNotificationChannel()
    startForeground(FOREGROUND_NOTIFICATION_ID, buildNotification())
    startRefreshLoop()
  }

  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int,
  ): Int {
    when (intent?.action) {
      ACTION_STOP -> {
        stopSelf()
        return START_NOT_STICKY
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    refreshJob?.cancel()
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun startRefreshLoop() {
    refreshJob?.cancel()
    refreshJob =
      serviceScope.launch {
        while (isActive) {
          refreshWidgetData()
          delay(INTERVAL_MS)
        }
      }
  }

  private fun refreshWidgetData() {
    serviceScope.launch(Dispatchers.IO) {
      val success = refreshWidgetDataUseCase?.execute() ?: false
      if (success) {
        updateWidgets()
      }
    }
  }

  private fun updateWidgets() {
    FavoriteStationWidgetProvider.updateAll(applicationContext)
    NearbyStationsWidgetProvider.updateAll(applicationContext)
    QuickActionsWidgetProvider.updateAll(applicationContext)
    CommuteWidgetProvider.updateAll(applicationContext)
  }

  private fun buildNotification(): Notification {
    val stopIntent =
      Intent(this, WidgetRefreshService::class.java).apply {
        action = ACTION_STOP
      }
    val stopPending =
      PendingIntent.getService(
        this,
        STOP_REQUEST_CODE,
        stopIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    return NotificationCompat
      .Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_rotate)
      .setContentTitle(getString(R.string.app_name))
      .setContentText(getString(R.string.widget_refresh_service_notification))
      .setContentIntent(stopPending)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setShowWhen(false)
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        getString(R.string.notification_action_stop),
        stopPending,
      ).build()
  }

  private fun ensureNotificationChannel() {
    val channel =
      NotificationChannel(
        CHANNEL_ID,
        getString(R.string.notification_channel_widget_refresh_title),
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = getString(R.string.notification_channel_widget_refresh_description)
        setShowBadge(false)
        enableLights(false)
        enableVibration(false)
      }
    notificationManager.createNotificationChannel(channel)
  }

  companion object {
    private const val ACTION_STOP = "com.gcaguilar.biciradar.action.STOP_WIDGET_REFRESH"
    private const val CHANNEL_ID = "bizi_widget_refresh_v1"
    private const val FOREGROUND_NOTIFICATION_ID = 1002
    private const val STOP_REQUEST_CODE = 501
    const val INTERVAL_MS = 2 * 60 * 1000L

    fun start(context: Context) {
      val intent = Intent(context, WidgetRefreshService::class.java)
      context.startForegroundService(intent)
    }

    fun stop(context: Context) {
      val intent = Intent(context, WidgetRefreshService::class.java)
      context.stopService(intent)
    }
  }
}
