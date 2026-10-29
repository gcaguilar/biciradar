package com.gcaguilar.biciradar.core.backend

import kotlinx.serialization.Serializable

/**
 * Wire DTOs mirroring the backend `openapi.json` served from `/openapi.json`.
 * Kept dumb on purpose: mapping to app models lives in [RoutineDtoMapping].
 */

@Serializable
data class RoutineStepDto(
  val id: String,
  val stationId: String,
  val cityId: String,
  /** `DEPARTURE` needs bikes; `ARRIVAL` needs docks. */
  val kind: String,
  val durationSeconds: Int,
  val threshold: Int? = null,
  val fallbackStationId: String? = null,
  val alternativeRadiusMeters: Int? = null,
)

@Serializable
data class RoutineDto(
  val id: String,
  val name: String = "",
  /** ISO weekdays: 0 = Monday … 6 = Sunday. */
  val daysOfWeek: List<Int> = emptyList(),
  val hour: Int,
  val minute: Int,
  val enabled: Boolean = true,
  val steps: List<RoutineStepDto> = emptyList(),
)

@Serializable
data class RoutinesResponse(
  val routines: List<RoutineDto> = emptyList(),
)

/**
 * Hourly usage pattern, wire-compatible with the app's
 * `StationHourlyPattern` (public endpoint, no auth).
 */
@Serializable
data class UsagePatternDto(
  val stationId: String,
  /** `WEEKDAY` (Mon–Fri) or `WEEKEND` (Sat–Sun). */
  val dayType: String,
  val hour: Int,
  val bikesAvg: Double,
  val anchorsAvg: Double,
  val occupancyAvg: Double,
  val sampleCount: Int,
)

@Serializable
data class RegisterDeviceRequest(
  val token: String,
  val platform: String,
  val appId: String? = null,
  val locale: String? = null,
  /** `sandbox` for development builds, `production` for TestFlight/App Store. */
  val pushEnvironment: String? = null,
)

@Serializable
data class DeviceDto(
  val id: Long,
  val platform: String,
  val appId: String? = null,
  val locale: String? = null,
  val pushEnvironment: String,
  val lastSeen: Long,
)

@Serializable
data class SubscriptionDto(
  val id: String,
  val stationId: String,
  val cityId: String,
  val kind: String,
  val condition: String? = null,
  val threshold: Int? = null,
  val enabled: Boolean = true,
)

@Serializable
data class SubscriptionsResponse(
  val subscriptions: List<SubscriptionDto> = emptyList(),
)

@Serializable
data class ReplaceSubscriptionsRequest(
  val subscriptions: List<SubscriptionDto>,
)

@Serializable
data class StartMonitoringWindowRequest(
  val cityId: String,
  val stationId: String,
  /** `DEPARTURE` needs bikes; `ARRIVAL` needs docks. */
  val kind: String,
  val durationSeconds: Int,
  val threshold: Int? = null,
  val fallbackStationId: String? = null,
  val alternativeRadiusMeters: Int? = null,
)

@Serializable
data class MonitoringWindowResponse(
  val id: String,
)

@Serializable
data class ErrorResponse(
  val error: String = "error",
  val message: String = "Unexpected backend error",
)
