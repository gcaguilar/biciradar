package com.gcaguilar.biciradar.core

enum class LogLevel {
  Debug,
  Info,
  Warning,
  Error,
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
