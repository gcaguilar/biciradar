package com.gcaguilar.biciradar.core

enum class LogLevel {
  Debug,
  Info,
  Warning,
  Error,
}

/**
 * Platform-specific crashlytics reporter.
 *
 * On Android: sends non-fatals to Firebase Crashlytics.
 * On iOS: delegates to a Swift bridge that calls Crashlytics SDK.
 * On Desktop/JVM: no-op (no crashlytics SDK available).
 */
interface CrashlyticsReporter {
  fun reportNonFatal(throwable: Throwable)
}

object NoOpCrashlyticsReporter : CrashlyticsReporter {
  override fun reportNonFatal(throwable: Throwable) = Unit
}

interface Logger {
  fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable? = null,
  )

  fun debug(
    tag: String,
    message: String,
  ) = log(LogLevel.Debug, tag, message)

  fun info(
    tag: String,
    message: String,
  ) = log(LogLevel.Info, tag, message)

  fun warn(
    tag: String,
    message: String,
    throwable: Throwable? = null,
  ) = log(LogLevel.Warning, tag, message, throwable)

  fun error(
    tag: String,
    message: String,
    throwable: Throwable? = null,
  ) = log(LogLevel.Error, tag, message, throwable)
}

object NoOpLogger : Logger {
  override fun log(
    level: LogLevel,
    tag: String,
    message: String,
    throwable: Throwable?,
  ) = Unit
}
