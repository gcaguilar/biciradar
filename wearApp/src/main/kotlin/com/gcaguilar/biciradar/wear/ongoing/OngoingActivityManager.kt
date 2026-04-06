package com.gcaguilar.biciradar.wear.ongoing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.gcaguilar.biciradar.wear.WearActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manager para mostrar notificaciones persistentes durante monitorización.
 * 
 * En Wear OS, las notificaciones ongoing aparecen en la cara del reloj
 * permitiendo acceso rápido a la app.
 */
class OngoingActivityManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private var updateJob: Job? = null
    private var currentStationId: String? = null
    private var currentStationName: String? = null
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Inicia una notificación persistente para monitorización.
     */
    fun startMonitoringActivity(
        stationId: String,
        stationName: String,
        remainingSeconds: Int
    ) {
        stopCurrentActivity()
        
        currentStationId = stationId
        currentStationName = stationName
        
        // Crear intent para abrir la app
        val intent = Intent(context, WearActivity::class.java).apply {
            action = WearActivity.ACTION_OPEN_STATION
            putExtra(WearActivity.EXTRA_OPEN_STATION_ID, stationId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Mostrar notificación inicial
        showNotification(stationName, remainingSeconds, pendingIntent)
        
        // Actualizar cada segundo
        var remaining = remainingSeconds
        updateJob = scope.launch {
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                showNotification(stationName, remaining, pendingIntent)
            }
            
            if (remaining <= 0) {
                stopCurrentActivity()
            }
        }
    }
    
    /**
     * Actualiza el tiempo mostrado.
     */
    fun updateRemainingTime(stationName: String, remainingSeconds: Int) {
        val intent = Intent(context, WearActivity::class.java).apply {
            action = WearActivity.ACTION_OPEN_STATION
            putExtra(WearActivity.EXTRA_OPEN_STATION_ID, currentStationId ?: "")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        showNotification(stationName, remainingSeconds, pendingIntent)
    }
    
    /**
     * Detiene la notificación.
     */
    fun stopCurrentActivity() {
        updateJob?.cancel()
        updateJob = null
        notificationManager.cancel(NOTIFICATION_ID)
        currentStationId = null
        currentStationName = null
    }
    
    private fun showNotification(
        stationName: String,
        remainingSeconds: Int,
        pendingIntent: PendingIntent
    ) {
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
            .setOnlyAlertOnce(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitorización de Estaciones",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificaciones persistentes durante la monitorización"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    companion object {
        private const val CHANNEL_ID = "monitoring_ongoing"
        private const val NOTIFICATION_ID = 1001
        
        @Volatile
        private var instance: OngoingActivityManager? = null
        
        fun getInstance(context: Context): OngoingActivityManager {
            return instance ?: synchronized(this) {
                instance ?: OngoingActivityManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}