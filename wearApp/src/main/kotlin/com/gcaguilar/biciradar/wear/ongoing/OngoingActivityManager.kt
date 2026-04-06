package com.gcaguilar.biciradar.wear.ongoing

import android.content.Context
import androidx.wear.ongoing.OngoingActivity
import androidx.wear.ongoing.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manager central para gestionar Ongoing Activities en Wear OS.
 * 
 * Las Ongoing Activities permiten mostrar actividades en curso directamente
 * en la cara del reloj, dando acceso rápido a la app y mostrando información
 * en tiempo real.
 */
class OngoingActivityManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    
    private var currentOngoingActivity: OngoingActivity? = null
    private var updateJob: Job? = null
    
    private val _isActive = MutableStateFlow(false)
    val isActive: StateFlow<Boolean> = _isActive.asStateFlow()
    
    /**
     * Inicia una Ongoing Activity para monitorización de estación.
     * 
     * @param stationId ID de la estación siendo monitorizada
     * @param stationName Nombre de la estación
     * @param remainingSeconds Segundos restantes de monitorización
     * @param onTap Acción al tocar la ongoing activity
     */
    fun startMonitoringActivity(
        stationId: String,
        stationName: String,
        remainingSeconds: Int,
        onTap: () -> Unit
    ) {
        stopCurrentActivity()
        
        val initialStatus = Status.Builder()
            .addTemplate("Monitoreando huecos en {station_name}")
            .addPart("station_name", Status.TextPart(stationName))
            .build()
        
        currentOngoingActivity = OngoingActivity.Builder(context, NOTIFICATION_ID)
            .setStaticIcon(android.R.drawable.ic_menu_mylocation)
            .setTouchIntent(null) // Se configura desde la app
            .setStatus(initialStatus)
            .build()
            .apply {
                apply(context)
            }
        
        _isActive.value = true
        
        // Iniciar actualizaciones periódicas
        updateJob = scope.launch {
            var remaining = remainingSeconds
            while (isActive && remaining > 0) {
                delay(1000)
                remaining--
                
                val minutes = remaining / 60
                val seconds = remaining % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)
                
                val updatedStatus = Status.Builder()
                    .addTemplate("{station_name} • {time_remaining}")
                    .addPart("station_name", Status.TextPart(stationName))
                    .addPart("time_remaining", Status.TextPart(timeText))
                    .build()
                
                currentOngoingActivity?.update(updatedStatus)
            }
            
            // Cuando termina el tiempo, detener
            if (remaining <= 0) {
                stopCurrentActivity()
            }
        }
    }
    
    /**
     * Actualiza el tiempo restante de la ongoing activity activa.
     */
    fun updateRemainingTime(stationName: String, remainingSeconds: Int) {
        if (currentOngoingActivity == null) return
        
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        val updatedStatus = Status.Builder()
            .addTemplate("{station_name} • {time_remaining}")
            .addPart("station_name", Status.TextPart(stationName))
            .addPart("time_remaining", Status.TextPart(timeText))
            .build()
        
        currentOngoingActivity?.update(updatedStatus)
    }
    
    /**
     * Detiene la ongoing activity actual si existe.
     */
    fun stopCurrentActivity() {
        updateJob?.cancel()
        updateJob = null
        
        // La ongoing activity se detiene automáticamente cuando se cierra la notificación
        // o cuando se llama a dismiss desde el sistema
        currentOngoingActivity = null
        _isActive.value = false
    }
    
    companion object {
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