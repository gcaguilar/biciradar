package com.gcaguilar.biciradar

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.startup.Initializer

class AndroidAppStartupInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

    val notificationManager = context.getSystemService(NotificationManager::class.java) ?: return
    val channel =
      NotificationChannel(
        TRIP_NOTIFICATION_CHANNEL_ID,
        "Bizi Viaje",
        NotificationManager.IMPORTANCE_HIGH,
      ).apply {
        description = "Notificaciones de monitorizacion de viaje en Bizi"
      }
    notificationManager.createNotificationChannel(channel)

    val monitoringChannel =
      NotificationChannel(
        SURFACE_MONITORING_CHANNEL_ID,
        "Bici Radar monitorizacion",
        NotificationManager.IMPORTANCE_LOW,
      ).apply {
        description = "Notificacion persistente durante la monitorizacion de estaciones"
      }
    notificationManager.createNotificationChannel(monitoringChannel)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()

  private companion object {
    const val TRIP_NOTIFICATION_CHANNEL_ID = "bizi_trip"
    const val SURFACE_MONITORING_CHANNEL_ID = "bizi_station_monitoring"
  }
}
