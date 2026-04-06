package com.gcaguilar.biciradar.wear.ongoing

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import com.gcaguilar.biciradar.wear.WearActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manager para Ongoing Activities de monitorización en Wear OS.
 * 
 * Muestra una actividad persistente en la cara del reloj cuando
 * se está monitorizando una estación.
 */
class OngoingActivityManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private var currentOngoingActivity: OngoingActivity? = null
    private var updateJob: Job? = null
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Inicia una Ongoing Activity para monitorización de estación.
     */
    fun startMonitoringActivity(
        stationId: String,
        stationName: String,
        remainingSeconds: Int
    ) {
        stopCurrentActivity()
        
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
        
        // Crear notificación base
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Monitoreando huecos")
            .setContentText(stationName)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setPriority(NotificationCompat.PRIORITY_LOW)
        
        // Crear status para ongoing activity
        val status = Status.Builder()
            .addTemplate("Monitoreando huecos en {station}")
            .addPart("station", Status.TextPart(stationName))
            .build()
        
        // Crear ongoing activity
        currentOngoingActivity = OngoingActivity.Builder(
            context = context,
            notificationId = NOTIFICATION_ID,
            notificationBuilder = notificationBuilder
        )
            .setStatus(status)
            .build()
            .apply {
                apply(context)
            }
        
        // Iniciar actualizaciones periódicas
        var remaining = remainingSeconds
        updateJob = scope.launch {
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                
                val minutes = remaining / 60
                val seconds = remaining % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)
                
                // Actualizar status
                val updatedStatus = Status.Builder()
                    .addTemplate("{station} • {time}")
                    .addPart("station", Status.TextPart(stationName))
                    .addPart("time", Status.TextPart(timeText))
                    .build()
                
                currentOngoingActivity?.update(context, updatedStatus)
                
                // Actualizar notificación
                val updatedNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentTitle("Monitoreando huecos")
                    .setContentText("$stationName • $timeText")
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build()
                
                notificationManager.notify(NOTIFICATION_ID, updatedNotification)
            }
            
            if (remaining <= 0) {
                stopCurrentActivity()
            }
        }
    }
    
    /**
     * Detiene la ongoing activity actual.
     */
    fun stopCurrentActivity() {
        updateJob?.cancel()
        updateJob = null
        notificationManager.cancel(NOTIFICATION_ID)
        currentOngoingActivity = null
    }
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Monitorización",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Notificaciones de monitorización de estaciones"
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }
    
    companion object {
        private const val CHANNEL_ID = "monitoring_channel"
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