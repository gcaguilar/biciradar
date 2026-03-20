package com.gcaguilar.biciradar

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.gcaguilar.biciradar.core.TripRepository
import com.gcaguilar.biciradar.core.localizedText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Foreground service that keeps trip monitoring alive while the app is in the background.
 *
 * Lifecycle: started by [TripMonitorServiceController] when monitoring begins, stopped when
 * monitoring ends or the user navigates away.
 */
class TripMonitorService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val notificationManager by lazy {
    getSystemService(NotificationManager::class.java)
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    ensureNotificationChannel()
    startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification(localizedText("Vigilando estación Bizi…")))
    observeMonitoringState()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
  }

  override fun onDestroy() {
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun observeMonitoringState() {
    val repo = TripRepositoryHolder.tripRepository ?: return

    repo.state
      .onEach { state ->
        if (!state.monitoring.isActive && state.alert == null) {
          // Monitoring stopped without an alert — stop the service
          stopSelf()
          return@onEach
        }
        val station = state.nearestStationWithSlots
        if (station != null && state.monitoring.isActive) {
          val remaining = state.monitoring.remainingSeconds
          val minutes = remaining / 60
          val seconds = remaining % 60
          val timeText = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
          val body = localizedText("%s · %s free slot(s) · %s remaining", station.name, station.slotsFree, timeText)
          notificationManager.notify(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification(body))
        }
      }
      .launchIn(serviceScope)
  }

  private fun buildForegroundNotification(text: String): Notification =
    NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_directions)
      .setContentTitle(localizedText("Bizi Viaje"))
      .setContentText(text)
      .setOngoing(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .build()

  private fun ensureNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      localizedText("Bizi Viaje (activo)"),
      NotificationManager.IMPORTANCE_LOW,
    ).apply {
      description = localizedText("Notificación persistente durante la monitorización de viaje")
    }
    notificationManager.createNotificationChannel(channel)
  }

  companion object {
    private const val CHANNEL_ID = "bizi_trip_foreground"
    private const val FOREGROUND_NOTIFICATION_ID = 1001

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

/**
 * Singleton holder that allows [TripMonitorService] to access [TripRepository]
 * without requiring the full DI graph to be passed across process boundaries.
 *
 * Set by the mobile-ui layer when the SharedGraph is created (in BiziMobileApp),
 * and cleared when the graph is disposed.
 */
object TripRepositoryHolder {
  @Volatile
  var tripRepository: TripRepository? = null
}
