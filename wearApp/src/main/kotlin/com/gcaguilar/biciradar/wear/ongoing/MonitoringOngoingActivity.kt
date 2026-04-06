package com.gcaguilar.biciradar.wear.ongoing

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gcaguilar.biciradar.wear.R
import com.gcaguilar.biciradar.wear.WearActivity

/**
 * Helper class para crear y gestionar la ongoing activity específica
 * para la monitorización de estaciones.
 * 
 * Esta clase se encarga de:
 * - Crear el canal de notificación
 * - Construir la notificación persistente
 * - Vincular con OngoingActivityManager
 */
class MonitoringOngoingActivity(private val context: Context) {

    private val manager = OngoingActivityManager.getInstance(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Inicia la ongoing activity para monitorización.
     */
    fun start(
        stationId: String,
        stationName: String,
        remainingSeconds: Int
    ) {
        // Crear intent para abrir la app al tocar
        val intent = Intent(context, WearActivity::class.java).apply {
            action = WearActivity.ACTION_OPEN_STATION
            putExtra(WearActivity.EXTRA_OPEN_STATION_ID, stationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Crear notificación
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Monitoreando huecos")
            .setContentText("$stationName • $timeText")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        // Mostrar notificación
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        // Iniciar ongoing activity
        manager.startMonitoringActivity(
            stationId = stationId,
            stationName = stationName,
            remainingSeconds = remainingSeconds,
            onTap = { /* La acción se maneja desde el PendingIntent */ }
        )
    }
    
    /**
     * Actualiza el tiempo restante mostrado.
     */
    fun updateTime(stationName: String, remainingSeconds: Int) {
        manager.updateRemainingTime(stationName, remainingSeconds)
        
        // Actualizar también la notificación
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Monitoreando huecos")
            .setContentText("$stationName • $timeText")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Detiene la ongoing activity.
     */
    fun stop() {
        manager.stopCurrentActivity()
        notificationManager.cancel(NOTIFICATION_ID)
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitorización de Estaciones",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificaciones persistentes durante la monitorización de huecos"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    companion object {
        private const val CHANNEL_ID = "monitoring_ongoing"
        private const val NOTIFICATION_ID = 1001
    }
}