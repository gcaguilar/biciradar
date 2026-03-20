package com.gcaguilar.biciradar.core

import platform.Foundation.NSLocale

actual fun currentAppLanguage(): AppLanguage {
  val code = NSLocale.preferredLanguages.firstOrNull()?.toString()?.substringBefore('-')?.lowercase()
  return when (code) {
    "en" -> AppLanguage.EN
    "ca" -> AppLanguage.CA
    "eu" -> AppLanguage.EU
    "gl" -> AppLanguage.GL
    else -> AppLanguage.ES
  }
}
