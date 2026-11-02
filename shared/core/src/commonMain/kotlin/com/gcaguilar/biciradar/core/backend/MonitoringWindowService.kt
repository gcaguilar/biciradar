package com.gcaguilar.biciradar.core.backend

import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.TripMonitoringFailure
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthState
import com.gcaguilar.biciradar.core.push.DeviceTokenProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException

/** Resultado de abrir una ventana de vigilancia en el backend. */
sealed interface MonitoringWindowStart {
  data class Started(
    val windowId: String,
  ) : MonitoringWindowStart

  data class Failed(
    val failure: TripMonitoringFailure,
  ) : MonitoringWindowStart
}

/**
 * Abre y cierra las ventanas de vigilancia del backend, que son las que hacen
 * llegar los avisos push.
 *
 * Agrupa la comprobación de sesión, el registro del dispositivo y la llamada a
 * `/v1/monitoring-windows` para que la pestaña Viaje, la monitorización de una
 * estación desde atajos, widgets o el reloj compartan el mismo camino.
 */
interface MonitoringWindowService {
  /**
   * Abre una ventana para [stationId]. Nunca lanza por fallos de red o de
   * configuración: los devuelve como [MonitoringWindowStart.Failed].
   */
  suspend fun start(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
    threshold: Int? = null,
  ): MonitoringWindowStart

  /** Cancela una ventana ya abierta. Idempotente y silencioso ante fallos. */
  suspend fun cancel(windowId: String)
}

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class MonitoringWindowServiceImpl(
  private val apiClient: BiciRadarApiClient,
  private val authProvider: AuthProvider,
  private val settingsRepository: SettingsRepository,
  private val deviceTokenProvider: DeviceTokenProvider,
) : MonitoringWindowService {
  override suspend fun start(
    stationId: String,
    durationSeconds: Int,
    kind: SurfaceMonitoringKind,
    threshold: Int?,
  ): MonitoringWindowStart {
    if (authProvider.credentialsState.value != AuthState.Authenticated) {
      return MonitoringWindowStart.Failed(TripMonitoringFailure.SignInRequired)
    }
    // The backend drops alerts for users without a registered device, so bind
    // this device right before opening the window instead of trusting that an
    // earlier subscription sync managed to do it.
    val pushToken =
      deviceTokenProvider.currentToken()?.takeIf { it.isNotBlank() }
        ?: return MonitoringWindowStart.Failed(TripMonitoringFailure.NotificationsUnavailable)
    if (!apiClient.isConfigured) {
      return MonitoringWindowStart.Failed(TripMonitoringFailure.BackendUnavailable)
    }
    val windowId =
      runCatching {
        apiClient.registerDevice(
          RegisterDeviceRequest(
            token = pushToken,
            platform = deviceTokenProvider.platform,
            appId = deviceTokenProvider.appId,
            locale = deviceTokenProvider.locale,
            pushEnvironment = deviceTokenProvider.pushEnvironment,
          ),
        )
        apiClient.startMonitoringWindow(
          StartMonitoringWindowRequest(
            cityId = settingsRepository.currentSelectedCity().id,
            stationId = stationId,
            kind = kind.toWireKind(),
            durationSeconds = durationSeconds,
            threshold = threshold,
          ),
        )
      }.onFailure { if (it is CancellationException) throw it }
        .getOrNull()
        ?: return MonitoringWindowStart.Failed(TripMonitoringFailure.BackendUnavailable)
    return MonitoringWindowStart.Started(windowId)
  }

  override suspend fun cancel(windowId: String) {
    if (!apiClient.isConfigured) return
    runCatching { apiClient.cancelMonitoringWindow(windowId) }
      .onFailure { if (it is CancellationException) throw it }
  }
}
