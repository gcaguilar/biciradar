package com.gcaguilar.biciradar.core

import platform.Foundation.NSLocale
import platform.Foundation.NSLocale.Companion.preferredLanguages

actual fun currentAppLanguage(): AppLanguage {
  val code = preferredLanguages.firstOrNull()?.toString()?.substringBefore('-')?.lowercase()
  return when (code) {
    "en" -> AppLanguage.EN
    "ca" -> AppLanguage.CA
    "eu" -> AppLanguage.EU
    "gl" -> AppLanguage.GL
    else -> AppLanguage.ES
  }
}
