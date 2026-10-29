package com.gcaguilar.biciradar.core

import kotlinx.serialization.Serializable

/**
 * Antelación con la que arranca la monitorización de una rutina respecto a la
 * hora indicada por el usuario. Fijo en 10 minutos.
 */
const val ROUTINE_LEAD_SECONDS: Int = 10 * 60

/**
 * Día de la semana de una rutina, en convención ISO (0 = lunes … 6 = domingo).
 */
@Serializable
enum class RoutineDay(
  val isoValue: Int,
) {
  MONDAY(0),
  TUESDAY(1),
  WEDNESDAY(2),
  THURSDAY(3),
  FRIDAY(4),
  SATURDAY(5),
  SUNDAY(6),
  ;

  companion object {
    fun fromIso(value: Int): RoutineDay? = entries.firstOrNull { it.isoValue == value }
  }
}

/**
 * Una estación incluida en una rutina. Comparte el horario de la [Routine] pero
 * conserva su propia duración de monitorización y el tipo (bicis o huecos).
 *
 * Los campos opcionales ([threshold], [fallbackStationId] y
 * [alternativeRadiusMeters]) se envían al backend tal cual: `null` significa
 * "aplica tus defaults". La UI avanzada para editarlos queda fuera de alcance.
 */
@Serializable
data class RoutineStep(
  val id: String,
  val stationId: String,
  val stationName: String,
  /** Ciudad de la estación; el backend la exige para resolver alternativas. */
  val cityId: String = "",
  val durationSeconds: Int = DEFAULT_SURFACE_MONITORING_DURATION_SECONDS,
  val kind: SurfaceMonitoringKind = SurfaceMonitoringKind.Bikes,
  val threshold: Int? = null,
  val fallbackStationId: String? = null,
  val alternativeRadiusMeters: Int? = null,
)

/**
 * Rutina: un horario compartido (días de la semana + hora) que arranca la
 * monitorización de una o varias estaciones [ROUTINE_LEAD_SECONDS] antes.
 */
@Serializable
data class Routine(
  val id: String,
  val name: String,
  val daysOfWeek: Set<RoutineDay>,
  val hour: Int,
  val minute: Int,
  val isEnabled: Boolean = true,
  val steps: List<RoutineStep> = emptyList(),
) {
  val isConfigured: Boolean get() = daysOfWeek.isNotEmpty() && steps.isNotEmpty()

  val displayName: String get() = name.ifBlank { "Rutina" }
}

fun routineDaysToBitmap(days: Set<RoutineDay>): Long = days.fold(0L) { acc, day -> acc or (1L shl day.isoValue) }

fun routineDaysFromBitmap(bitmap: Long): Set<RoutineDay> =
  RoutineDay.entries.filter { bitmap and (1L shl it.isoValue) != 0L }.toSet()

/**
 * Días que faltan (0..6) desde [fromDay] (ISO 0..6) hasta el próximo día incluido
 * en [days]. Devuelve `null` si [days] está vacío.
 */
fun daysUntilNextRoutineTrigger(
  fromDay: Int,
  days: Set<RoutineDay>,
): Int? {
  if (days.isEmpty()) return null
  val targets = days.map { it.isoValue }.toSet()
  return (0..6).firstOrNull { offset -> (fromDay + offset).mod(7) in targets }
}

/**
 * Hora/minuto locales en los que debe arrancar la monitorización: [hour]:[minute]
 * menos [ROUTINE_LEAD_SECONDS], con desbordamiento de día hacia atrás.
 *
 * @return par (hour, minute) y cuántos días hay que restar (0 o 1).
 */
fun routineTriggerTime(
  hour: Int,
  minute: Int,
  leadSeconds: Int = ROUTINE_LEAD_SECONDS,
): RoutineTriggerTime {
  val totalMinutes = (hour * 60 + minute - leadSeconds / 60).let { it.mod(24 * 60) }
  val rolledBack = hour * 60 + minute - leadSeconds / 60 < 0
  return RoutineTriggerTime(
    hour = totalMinutes / 60,
    minute = totalMinutes % 60,
    daysBefore = if (rolledBack) 1 else 0,
  )
}

data class RoutineTriggerTime(
  val hour: Int,
  val minute: Int,
  val daysBefore: Int,
)

fun formatRoutineTime(
  hour: Int,
  minute: Int,
): String = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
