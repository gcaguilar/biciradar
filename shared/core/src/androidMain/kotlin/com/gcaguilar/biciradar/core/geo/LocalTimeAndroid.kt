package com.gcaguilar.biciradar.core.geo

import java.util.Calendar

actual fun currentLocalTimeSnapshot(): LocalTimeSnapshot {
  val calendar = Calendar.getInstance()
  val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
  return LocalTimeSnapshot(
    hour = calendar.get(Calendar.HOUR_OF_DAY),
    isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY,
  )
}
