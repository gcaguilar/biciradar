package com.gcaguilar.biciradar.core

import com.gcaguilar.biciradar.core.geo.currentTimeMs
import kotlinx.serialization.Serializable

const val DEFAULT_LOW_AVAILABILITY_THRESHOLD = 3
const val DEFAULT_SURFACE_MONITORING_DURATION_SECONDS = 10 * 60

@Serializable
enum class SurfaceStatusLevel {
  Good,
  Low,
  Empty,
  Full,
  Unavailable,
}

@Serializable
enum class SurfaceMonitoringKind {
  Bikes,
  Docks,
}

@Serializable
enum class SurfaceMonitoringStatus {
  Monitoring,
  ChangedToEmpty,
  ChangedToFull,
  AlternativeAvailable,
  Ended,
  Expired,
}

@Serializable
data class SurfaceStationSnapshot(
  val id: String,
  val nameShort: String,
  val nameFull: String,
  val cityId: String,
  val latitude: Double,
  val longitude: Double,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val statusTextShort: String,
  val statusLevel: SurfaceStatusLevel,
  val lastUpdatedEpoch: Long,
  val distanceMeters: Int? = null,
  val isFavorite: Boolean = false,
  val alternativeStationId: String? = null,
  val alternativeStationName: String? = null,
  val alternativeDistanceMeters: Int? = null,
)

@Serializable
data class SurfaceMonitoringSession(
  val stationId: String,
  val stationName: String,
  val cityId: String,
  val kind: SurfaceMonitoringKind,
  val status: SurfaceMonitoringStatus,
  val bikesAvailable: Int,
  val docksAvailable: Int,
  val statusLevel: SurfaceStatusLevel,
  val startedAtEpoch: Long,
  val expiresAtEpoch: Long,
  val lastUpdatedEpoch: Long,
  val isActive: Boolean,
  val alternativeStationId: String? = null,
  val alternativeStationName: String? = null,
  val alternativeDistanceMeters: Int? = null,
)

@Serializable
data class SurfaceState(
  val hasLocationPermission: Boolean,
  val hasNotificationPermission: Boolean,
  val hasFavoriteStation: Boolean,
  val isDataFresh: Boolean,
  val lastSyncEpoch: Long? = null,
  val cityId: String,
  val cityName: String,
  val userLatitude: Double? = null,
  val userLongitude: Double? = null,
)

@Serializable
data class SurfaceSnapshotBundle(
  val generatedAtEpoch: Long,
  val favoriteStation: SurfaceStationSnapshot? = null,
  val homeStation: SurfaceStationSnapshot? = null,
  val workStation: SurfaceStationSnapshot? = null,
  val nearbyStations: List<SurfaceStationSnapshot> = emptyList(),
  val monitoringSession: SurfaceMonitoringSession? = null,
  val state: SurfaceState,
)

fun Station.toSurfaceSnapshot(
  cityId: String,
  lastUpdatedEpoch: Long,
  isFavorite: Boolean = false,
  alternative: Station? = null,
): SurfaceStationSnapshot =
  SurfaceStationSnapshot(
    id = id,
    nameShort = surfaceStationShortName(name),
    nameFull = name,
    cityId = cityId,
    latitude = location.latitude,
    longitude = location.longitude,
    bikesAvailable = bikesAvailable,
    docksAvailable = slotsFree,
    statusTextShort = surfaceStatusTextShort(),
    statusLevel = surfaceStatusLevel(),
    lastUpdatedEpoch = lastUpdatedEpoch,
    distanceMeters = distanceMeters.takeIf { it >= 0 },
    isFavorite = isFavorite,
    alternativeStationId = alternative?.id,
    alternativeStationName = alternative?.name,
    alternativeDistanceMeters = alternative?.distanceMeters,
  )

fun Station.surfaceStatusLevel(
  lowAvailabilityThreshold: Int = DEFAULT_LOW_AVAILABILITY_THRESHOLD,
): SurfaceStatusLevel =
  when {
    bikesAvailable < 0 || slotsFree < 0 -> SurfaceStatusLevel.Unavailable
    bikesAvailable == 0 -> SurfaceStatusLevel.Empty
    slotsFree == 0 -> SurfaceStatusLevel.Full
    bikesAvailable <= lowAvailabilityThreshold || slotsFree <= lowAvailabilityThreshold -> SurfaceStatusLevel.Low
    else -> SurfaceStatusLevel.Good
  }

fun Station.surfaceStatusTextShort(lowAvailabilityThreshold: Int = DEFAULT_LOW_AVAILABILITY_THRESHOLD): String =
  when (surfaceStatusLevel(lowAvailabilityThreshold)) {
    SurfaceStatusLevel.Good -> "Disponible"
    SurfaceStatusLevel.Low -> "Pocas"
    SurfaceStatusLevel.Empty -> "Sin bicis"
    SurfaceStatusLevel.Full -> "Sin huecos"
    SurfaceStatusLevel.Unavailable -> "No disp."
  }

fun surfaceStationShortName(
  name: String,
  maxLength: Int = 20,
): String {
  val compact = name.trim().replace(Regex("\\s+"), " ")
  if (compact.length <= maxLength) return compact
  return compact.take(maxLength - 1).trimEnd() + "…"
}

fun formatRelativeMinutes(
  lastUpdatedEpoch: Long?,
  nowEpoch: Long = currentTimeMs(),
): String? {
  val timestamp = lastUpdatedEpoch ?: return null
  val elapsedSeconds = ((nowEpoch - timestamp) / 1000L).coerceAtLeast(0L)
  val elapsedMinutes = elapsedSeconds / 60L
  return when {
    elapsedSeconds < 60L -> "Ahora"
    elapsedMinutes == 1L -> "Hace 1 min"
    elapsedMinutes < 60L -> "Hace $elapsedMinutes min"
    else -> "Hace ${elapsedMinutes / 60L} h"
  }
}

fun SurfaceMonitoringSession.remainingSeconds(nowEpoch: Long = currentTimeMs()): Int =
  ((expiresAtEpoch - nowEpoch) / 1000L).toInt().coerceAtLeast(0)
