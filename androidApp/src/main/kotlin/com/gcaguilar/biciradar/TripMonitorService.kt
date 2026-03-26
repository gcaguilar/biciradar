package com.gcaguilar.biciradar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.remainingSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class TripMonitorService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val notificationManager by lazy {
    getSystemService(NotificationManager::class.java)
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    ensureNotificationChannel()
    startForeground(
      FOREGROUND_NOTIFICATION_ID,
      buildForegroundNotification(text = "Preparando monitorizacion..."),
    )
    observeMonitoringState()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_STOP_MONITORING -> {
        SurfaceMonitoringRepositoryHolder.repository?.stopMonitoring()
        stopSelf()
        return START_NOT_STICKY
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun observeMonitoringState() {
    val repository = SurfaceMonitoringRepositoryHolder.repository ?: return
    repository.state
      .onEach { session ->
        if (session == null || !session.isActive) {
          FavoriteStationWidgetProvider.updateAll(applicationContext)
          NearbyStationsWidgetProvider.updateAll(applicationContext)
          QuickActionsWidgetProvider.updateAll(applicationContext)
          stopSelf()
          return@onEach
        }
        notificationManager.notify(
          FOREGROUND_NOTIFICATION_ID,
          buildForegroundNotification(session = session),
        )
        FavoriteStationWidgetProvider.updateAll(applicationContext)
        NearbyStationsWidgetProvider.updateAll(applicationContext)
        QuickActionsWidgetProvider.updateAll(applicationContext)
      }
      .launchIn(serviceScope)
  }

  private fun buildForegroundNotification(
    session: SurfaceMonitoringSession? = null,
    text: String = session?.let { monitoringNotificationBody(it, it.remainingSeconds()) }.orEmpty(),
  ): Notification {
    val openIntent = session?.stationId?.let(::stationPendingIntent)
      ?: appPendingIntent(Uri.parse("biciradar://favorites"))
    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_directions)
      .setContentTitle(session?.stationName ?: "BiciRadar")
      .setContentText(text)
      .setContentIntent(openIntent)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        "Detener",
        stopPendingIntent(),
      )

    session?.stationId?.let { stationId ->
      builder.addAction(
        android.R.drawable.ic_menu_view,
        "Abrir",
        stationPendingIntent(stationId),
      )
    }
    builder.addAction(
      android.R.drawable.ic_dialog_map,
      "Mapa",
      appPendingIntent(Uri.parse("biciradar://map")),
    )
    session?.alternativeStationId?.let { stationId ->
      builder.addAction(
        android.R.drawable.ic_menu_mylocation,
        "Alternativa",
        stationPendingIntent(stationId),
      )
    }
    return builder.build()
  }

  private fun stopPendingIntent(): PendingIntent {
    val intent = Intent(this, TripMonitorService::class.java).apply {
      action = ACTION_STOP_MONITORING
    }
    return PendingIntent.getService(
      this,
      STOP_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun stationPendingIntent(stationId: String): PendingIntent =
    appPendingIntent(Uri.parse("biciradar://station/$stationId"))

  private fun appPendingIntent(uri: Uri): PendingIntent {
    val intent = Intent(Intent.ACTION_VIEW, uri, this, MainActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }
    return PendingIntent.getActivity(
      this,
      uri.hashCode(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun ensureNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      "Bici Radar monitorizacion",
      NotificationManager.IMPORTANCE_LOW,
    ).apply {
      description = "Notificacion persistente durante la monitorizacion de estaciones"
    }
    notificationManager.createNotificationChannel(channel)
  }

  companion object {
    private const val ACTION_STOP_MONITORING = "com.gcaguilar.biciradar.action.STOP_MONITORING"
    private const val CHANNEL_ID = "bizi_station_monitoring"
    private const val FOREGROUND_NOTIFICATION_ID = 1001
    private const val STOP_REQUEST_CODE = 401

    fun start(context: Context) {
      val intent = Intent(context, TripMonitorService::class.java)
      context.startForegroundService(intent)
    }

    fun stop(context: Context) {
      val intent = Intent(context, TripMonitorService::class.java)
      context.stopService(intent)
    }
  }
}

object SurfaceMonitoringRepositoryHolder {
  @Volatile
  var repository: SurfaceMonitoringRepository? = null
}
