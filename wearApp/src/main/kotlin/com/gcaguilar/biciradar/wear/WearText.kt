package com.gcaguilar.biciradar.wear

import android.content.Context
import androidx.annotation.StringRes
import com.gcaguilar.biciradar.core.Station
import com.gcaguilar.biciradar.core.SurfaceMonitoringKind
import com.gcaguilar.biciradar.core.SurfaceMonitoringStatus
import com.gcaguilar.biciradar.core.SurfaceStatusLevel
import com.gcaguilar.biciradar.core.surfaceStatusLevel

/**
 * Acceso a textos localizados desde código que no es un `@Composable`
 * (view model, tiles, complications, notificaciones) y desde funciones de
 * presentación puras, que así se pueden probar con un proveedor falso.
 */
internal fun interface WearText {
  fun get(
    @StringRes id: Int,
    vararg args: Any,
  ): String
}

internal class ContextWearText(
  private val context: Context,
) : WearText {
  override fun get(
    id: Int,
    vararg args: Any,
  ): String = if (args.isEmpty()) context.getString(id) else context.getString(id, *args)
}

internal fun WearText.statusLabel(level: SurfaceStatusLevel): String =
  get(
    when (level) {
      SurfaceStatusLevel.Good -> R.string.wear_status_good
      SurfaceStatusLevel.Low -> R.string.wear_status_low
      SurfaceStatusLevel.Empty -> R.string.wear_status_empty
      SurfaceStatusLevel.Full -> R.string.wear_status_full
      SurfaceStatusLevel.Unavailable -> R.string.wear_status_unavailable
    },
  )

internal fun WearText.statusLabel(station: Station): String = statusLabel(station.surfaceStatusLevel())

internal fun WearText.monitoringStatusLabel(
  status: SurfaceMonitoringStatus,
  kind: SurfaceMonitoringKind,
): String =
  get(
    when (status) {
      SurfaceMonitoringStatus.Monitoring -> {
        if (kind == SurfaceMonitoringKind.Docks) {
          R.string.wear_monitoring_docks
        } else {
          R.string.wear_monitoring_bikes
        }
      }

      SurfaceMonitoringStatus.ChangedToEmpty -> R.string.wear_status_empty
      SurfaceMonitoringStatus.ChangedToFull -> R.string.wear_status_full
      SurfaceMonitoringStatus.AlternativeAvailable -> R.string.wear_monitoring_alternative
      SurfaceMonitoringStatus.Ended -> R.string.wear_monitoring_ended
      SurfaceMonitoringStatus.Expired -> R.string.wear_monitoring_expired
    },
  )

internal fun WearText.relativeMinutes(
  lastUpdatedEpoch: Long?,
  nowEpoch: Long,
): String? {
  val timestamp = lastUpdatedEpoch ?: return null
  val elapsedSeconds = ((nowEpoch - timestamp) / 1000L).coerceAtLeast(0L)
  val elapsedMinutes = elapsedSeconds / 60L
  return when {
    elapsedSeconds < 60L -> get(R.string.wear_time_now)
    elapsedMinutes == 1L -> get(R.string.wear_time_minute_ago)
    elapsedMinutes < 60L -> get(R.string.wear_time_minutes_ago, elapsedMinutes.toInt())
    else -> get(R.string.wear_time_hours_ago, (elapsedMinutes / 60L).toInt())
  }
}

internal fun WearText.alternativeLabel(
  name: String,
  formattedDistance: String?,
): String = get(R.string.wear_alternative, name, formattedDistance?.let { " ($it)" }.orEmpty())
