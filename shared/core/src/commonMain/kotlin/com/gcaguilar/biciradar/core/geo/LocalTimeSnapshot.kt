package com.gcaguilar.biciradar.core.geo

/**
 * Lectura del reloj local para funciones que dependen de la hora del día, como
 * casar los patrones horarios de uso de una estación con el momento actual.
 *
 * @property hour hora local en formato 0..23.
 * @property isWeekend `true` para sábado y domingo.
 */
data class LocalTimeSnapshot(
  val hour: Int,
  val isWeekend: Boolean,
)

expect fun currentLocalTimeSnapshot(): LocalTimeSnapshot
