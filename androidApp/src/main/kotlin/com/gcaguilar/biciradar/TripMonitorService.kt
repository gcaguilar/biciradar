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
import com.gcaguilar.biciradar.core.FavoritesRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringSession
import com.gcaguilar.biciradar.core.remainingSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Servicio de monitorización mejorado con:
 * - Cuenta regresiva actualizándose cada segundo
 * - Estilo Material You
 * - Acciones de compartir y favoritos
 * - Integración con App Functions
 * - DI robusto vía BiziAppGraph (sin holders temporales)
 *
 * Las dependencias se obtienen del singleton BiziAppGraph, eliminando
 * la fragilidad de los holders temporales y soportando recreación del Service.
 */
class TripMonitorService : Service() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val notificationManager by lazy {
    getSystemService(NotificationManager::class.java)
  }

  // Dependencias obtenidas del singleton de aplicación
  private val surfaceMonitoringRepository: SurfaceMonitoringRepository
    get() = BiziAppGraph.graph.surfaceMonitoringRepository
  private val favoritesRepository: FavoritesRepository
    get() = BiziAppGraph.graph.favoritesRepository

  private var countdownJob: Job? = null
  private var currentSession: SurfaceMonitoringSession? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onCreate() {
    super.onCreate()
    ensureNotificationChannel()
    startForeground(
      FOREGROUND_NOTIFICATION_ID,
      buildForegroundNotification(text = "Preparando monitorización..."),
    )
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Observar estado de monitorización
    observeMonitoringState()

    when (intent?.action) {
      ACTION_STOP_MONITORING -> {
        surfaceMonitoringRepository.stopMonitoring()
        stopSelf()
        return START_NOT_STICKY
      }
      ACTION_TOGGLE_FAVORITE -> {
        val stationId = intent.getStringExtra(EXTRA_STATION_ID)
        if (stationId != null) {
          serviceScope.launch {
            favoritesRepository.toggle(stationId)
            updateNotification()
          }
        }
        return START_STICKY
      }
      ACTION_SHARE -> {
        val stationId = intent.getStringExtra(EXTRA_STATION_ID)
        val stationName = intent.getStringExtra(EXTRA_STATION_NAME)
        if (stationId != null && stationName != null) {
          shareStation(stationId, stationName)
        }
        return START_STICKY
      }
    }
    return START_STICKY
  }

  override fun onDestroy() {
    countdownJob?.cancel()
    serviceScope.cancel()
    super.onDestroy()
  }

  private fun observeMonitoringState() {
    surfaceMonitoringRepository.state
      .onEach { session: SurfaceMonitoringSession? ->
        currentSession = session
        if (session == null || !session.isActive) {
          countdownJob?.cancel()
          updateWidgets()
          stopSelf()
          return@onEach
        }
        startCountdown(session)
        updateWidgets()
      }
      .launchIn(serviceScope)
  }

  /**
   * Inicia la cuenta regresiva que actualiza la notificación cada segundo
   */
  private fun startCountdown(session: SurfaceMonitoringSession) {
    countdownJob?.cancel()
    countdownJob = serviceScope.launch {
      while (isActive && session.isActive) {
        notificationManager.notify(
          FOREGROUND_NOTIFICATION_ID,
          buildForegroundNotification(session = session),
        )
        delay(1000)
      }
    }
  }

  private fun updateNotification() {
    currentSession?.let { session ->
      notificationManager.notify(
        FOREGROUND_NOTIFICATION_ID,
        buildForegroundNotification(session = session),
      )
    }
  }

  private fun updateWidgets() {
    FavoriteStationWidgetProvider.updateAll(applicationContext)
    NearbyStationsWidgetProvider.updateAll(applicationContext)
    QuickActionsWidgetProvider.updateAll(applicationContext)
    CommuteWidgetProvider.updateAll(applicationContext)
  }

  /**
   * Construye la notificación con estilo Material You
   */
  private fun buildForegroundNotification(
    session: SurfaceMonitoringSession? = null,
    text: String = session?.let {
      monitoringNotificationBody(it, it.remainingSeconds())
    }.orEmpty(),
  ): Notification {
    val openIntent = session?.stationId?.let(::stationPendingIntent)
      ?: appPendingIntent(Uri.parse("biciradar://favorites"))

    val isFavorite = session?.stationId?.let {
      favoritesRepository.favoriteIds.value.contains(it)
    } ?: false

    val builder = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_menu_directions)
      .setContentTitle(session?.let(::monitoringNotificationTitle) ?: "BiciRadar")
      .setContentText(text)
      .setContentIntent(openIntent)
      .setOngoing(true)
      .setOnlyAlertOnce(true)
      .setPriority(NotificationCompat.PRIORITY_LOW)
      // Material You styling
      .setColor(getColor(R.color.brand_primary))
      .setColorized(true)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setShowWhen(false)

    // Acción: Detener
    builder.addAction(
      android.R.drawable.ic_menu_close_clear_cancel,
      getString(R.string.notification_action_stop),
      stopPendingIntent(),
    )

    // Acción: Favorito (si hay sesión)
    session?.stationId?.let { stationId ->
      val favoriteIcon = if (isFavorite)
        android.R.drawable.btn_star_big_on
      else
        android.R.drawable.btn_star_big_off
      val favoriteLabel = if (isFavorite)
        getString(R.string.notification_action_remove_favorite)
      else
        getString(R.string.notification_action_add_favorite)

      builder.addAction(
        favoriteIcon,
        favoriteLabel,
        toggleFavoritePendingIntent(stationId),
      )
    }

    // Acción: Compartir
    session?.let { s ->
      builder.addAction(
        android.R.drawable.ic_menu_share,
        getString(R.string.notification_action_share),
        sharePendingIntent(s.stationId, s.stationName),
      )
    }

    // Acción: Mapa
    builder.addAction(
      android.R.drawable.ic_dialog_map,
      getString(R.string.notification_action_map),
      appPendingIntent(Uri.parse("biciradar://map")),
    )

    // Acción: Alternativa (si existe)
    session?.alternativeStationId?.let { stationId ->
      builder.addAction(
        android.R.drawable.ic_menu_mylocation,
        getString(R.string.notification_action_alternative),
        stationPendingIntent(stationId),
      )
    }

    // Estilo expandido con toda la información
    if (text.isNotBlank()) {
      builder.setStyle(
        NotificationCompat.BigTextStyle()
          .bigText(text)
          .setBigContentTitle(session?.let(::monitoringNotificationTitle))
      )
    }

    return builder.build()
  }

  private fun shareStation(stationId: String, stationName: String) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
      type = "text/plain"
      putExtra(
        Intent.EXTRA_TEXT,
        "Mira esta estación de BiciMAD: $stationName - https://biciradar.com/station/$stationId"
      )
      putExtra(Intent.EXTRA_SUBJECT, "Estación de BiciMAD: $stationName")
    }
    val chooser = Intent.createChooser(shareIntent, "Compartir estación")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(chooser)
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

  private fun toggleFavoritePendingIntent(stationId: String): PendingIntent {
    val intent = Intent(this, TripMonitorService::class.java).apply {
      action = ACTION_TOGGLE_FAVORITE
      putExtra(EXTRA_STATION_ID, stationId)
    }
    return PendingIntent.getService(
      this,
      TOGGLE_FAVORITE_REQUEST_CODE,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

  private fun sharePendingIntent(stationId: String, stationName: String): PendingIntent {
    val intent = Intent(this, TripMonitorService::class.java).apply {
      action = ACTION_SHARE
      putExtra(EXTRA_STATION_ID, stationId)
      putExtra(EXTRA_STATION_NAME, stationName)
    }
    return PendingIntent.getService(
      this,
      SHARE_REQUEST_CODE,
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
      getString(R.string.notification_channel_monitoring_title),
      NotificationManager.IMPORTANCE_LOW,
    ).apply {
      description = getString(R.string.notification_channel_monitoring_description)
      setShowBadge(false)
      enableLights(false)
      enableVibration(false)
    }
    notificationManager.createNotificationChannel(channel)
  }

  companion object {
    private const val ACTION_STOP_MONITORING = "com.gcaguilar.biciradar.action.STOP_MONITORING"
    private const val ACTION_TOGGLE_FAVORITE = "com.gcaguilar.biciradar.action.TOGGLE_FAVORITE"
    private const val ACTION_SHARE = "com.gcaguilar.biciradar.action.SHARE"
    private const val EXTRA_STATION_ID = "station_id"
    private const val EXTRA_STATION_NAME = "station_name"
    private const val CHANNEL_ID = "bizi_station_monitoring_v2"
    private const val FOREGROUND_NOTIFICATION_ID = 1001
    private const val STOP_REQUEST_CODE = 401
    private const val TOGGLE_FAVORITE_REQUEST_CODE = 402
    private const val SHARE_REQUEST_CODE = 403

    /**
     * Inicia el servicio de monitorización.
     * Las dependencias se obtienen automáticamente de BiziAppGraph.
     *
     * @param context El contexto de Android
     */
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