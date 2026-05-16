package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs

/**
 * Deep interface for surface presentation logic.
 *
 * All formatting and status computation for watch widgets, Android widgets,
 * and wear tiles flows through this single seam. Platform adapters call
 * [SurfacePresentation] methods and receive pre-formatted values.
 *
 * This eliminates the previous duplication where AndroidSurfaceRendering,
 * WearStationPresentation, and Apple watch widgets each reimplemented
 * the same Spanish-language formatting rules.
 */
interface SurfacePresentation {
  /** Compute the status level for a station (Good/Low/Empty/Full/Unavailable). */
  fun computeStatusLevel(
    bikesAvailable: Int,
    slotsFree: Int,
    lowAvailabilityThreshold: Int = DEFAULT_LOW_AVAILABILITY_THRESHOLD,
  ): SurfaceStatusLevel

  /** Compute the short status text for a station. */
  fun computeStatusTextShort(
    bikesAvailable: Int,
    slotsFree: Int,
    lowAvailabilityThreshold: Int = DEFAULT_LOW_AVAILABILITY_THRESHOLD,
  ): String

  /** Compact station name (truncated with ellipsis). */
  fun formatStationShortName(
    name: String,
    maxLength: Int = 20,
  ): String

  /** Relative time string ("Ahora", "Hace 3 min", "Hace 2 h"). */
  fun formatRelativeMinutes(
    lastUpdatedEpoch: Long?,
    nowEpoch: Long = currentTimeMs(),
  ): String?

  /** Monitoring countdown ("1m 30s" or "45s"). */
  fun formatMonitoringCountdown(remainingSeconds: Int): String

  /** Monitoring status text ("Monitorizando bicis", "Sin huecos", etc.). */
  fun formatMonitoringStatusText(
    status: SurfaceMonitoringStatus,
    kind: SurfaceMonitoringKind = SurfaceMonitoringKind.Bikes,
  ): String

  /** Monitoring alternative station text ("Alt: StationName (500m)"). */
  fun formatMonitoringAlternativeText(
    session: SurfaceMonitoringSession,
    distanceFormatter: (Int) -> String = ::formatDistance,
  ): String?

  /** Notification title for monitoring events. */
  fun formatMonitoringNotificationTitle(session: SurfaceMonitoringSession): String

  /** Notification body for monitoring events. */
  fun formatMonitoringNotificationBody(
    session: SurfaceMonitoringSession,
    remainingSeconds: Int,
    distanceFormatter: (Int) -> String = ::formatDistance,
  ): String

  /** Remaining seconds for a monitoring session. */
  fun remainingSeconds(
    session: SurfaceMonitoringSession,
    nowEpoch: Long = currentTimeMs(),
  ): Int
}

/**
 * Default implementation of [SurfacePresentation] using the pure formatting functions.
 */
class DefaultSurfacePresentation : SurfacePresentation {
  override fun computeStatusLevel(
    bikesAvailable: Int,
    slotsFree: Int,
    lowAvailabilityThreshold: Int,
  ): SurfaceStatusLevel =
    when {
      bikesAvailable < 0 || slotsFree < 0 -> SurfaceStatusLevel.Unavailable
      bikesAvailable == 0 -> SurfaceStatusLevel.Empty
      slotsFree == 0 -> SurfaceStatusLevel.Full
      bikesAvailable <= lowAvailabilityThreshold || slotsFree <= lowAvailabilityThreshold -> SurfaceStatusLevel.Low
      else -> SurfaceStatusLevel.Good
    }

  override fun computeStatusTextShort(
    bikesAvailable: Int,
    slotsFree: Int,
    lowAvailabilityThreshold: Int,
  ): String {
    val level = computeStatusLevel(bikesAvailable, slotsFree, lowAvailabilityThreshold)
    return when (level) {
      SurfaceStatusLevel.Good -> "Disponible"
      SurfaceStatusLevel.Low -> "Pocas"
      SurfaceStatusLevel.Empty -> "Sin bicis"
      SurfaceStatusLevel.Full -> "Sin huecos"
      SurfaceStatusLevel.Unavailable -> "No disp."
    }
  }

  override fun formatStationShortName(
    name: String,
    maxLength: Int,
  ): String = surfaceStationShortName(name, maxLength)

  override fun formatRelativeMinutes(
    lastUpdatedEpoch: Long?,
    nowEpoch: Long,
  ): String? = formatRelativeMinutes(lastUpdatedEpoch, nowEpoch)

  override fun formatMonitoringCountdown(remainingSeconds: Int): String = formatMonitoringCountdown(remainingSeconds)

  override fun formatMonitoringStatusText(
    status: SurfaceMonitoringStatus,
    kind: SurfaceMonitoringKind,
  ): String = formatMonitoringStatusText(status, kind)

  override fun formatMonitoringAlternativeText(
    session: SurfaceMonitoringSession,
    distanceFormatter: (Int) -> String,
  ): String? = formatMonitoringAlternativeText(session, distanceFormatter)

  override fun formatMonitoringNotificationTitle(session: SurfaceMonitoringSession): String =
    formatMonitoringNotificationTitle(session)

  override fun formatMonitoringNotificationBody(
    session: SurfaceMonitoringSession,
    remainingSeconds: Int,
    distanceFormatter: (Int) -> String,
  ): String = formatMonitoringNotificationBody(session, remainingSeconds, distanceFormatter)

  override fun remainingSeconds(
    session: SurfaceMonitoringSession,
    nowEpoch: Long,
  ): Int = session.remainingSeconds(nowEpoch)
}
