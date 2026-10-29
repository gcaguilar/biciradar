package com.gcaguilar.biciradar.core.geo

import platform.Foundation.NSCalendar
import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

// `NSDateComponents.hour`/`weekday` are `NSInteger`, whose width differs between watchOS
// arm64_32 and the other Apple targets. Kotlin 2.5 rejects such types in code shared across
// those targets, so the fields are read as formatted strings instead.
private fun formatter(pattern: String): NSDateFormatter =
  NSDateFormatter().apply {
    calendar = NSCalendar.currentCalendar
    locale = NSLocale(localeIdentifier = "en_US_POSIX")
    dateFormat = pattern
  }

actual fun currentLocalTimeSnapshot(): LocalTimeSnapshot {
  val now = NSDate()
  val weekday = formatter("EEE").stringFromDate(now)
  return LocalTimeSnapshot(
    hour = formatter("H").stringFromDate(now).toInt(),
    isWeekend = weekday == "Sat" || weekday == "Sun",
  )
}
