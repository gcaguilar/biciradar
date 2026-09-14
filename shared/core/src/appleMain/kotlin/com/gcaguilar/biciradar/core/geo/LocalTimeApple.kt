package com.gcaguilar.biciradar.core.geo

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitWeekday
import platform.Foundation.NSDate

actual fun currentLocalTimeSnapshot(): LocalTimeSnapshot {
  val components =
    NSCalendar.currentCalendar.components(
      NSCalendarUnitHour or NSCalendarUnitWeekday,
      fromDate = NSDate(),
    )
  val weekday = components.weekday
  val weekdayValue: Long? = weekday?.toLong()
  return LocalTimeSnapshot(
    hour = components.hour.toInt(),
    isWeekend = weekdayValue == 1L || weekdayValue == 7L,
  )
}
