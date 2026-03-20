package com.gcaguilar.biciradar.core

import platform.Foundation.NSLocale
import platform.Foundation.NSLocale.Companion.currentLocale

actual fun currentAppLanguage(): AppLanguage {
  val code = currentLocale.languageCode?.lowercase()
  return when (code) {
    "en" -> AppLanguage.EN
    "ca" -> AppLanguage.CA
    "eu" -> AppLanguage.EU
    "gl" -> AppLanguage.GL
    else -> AppLanguage.ES
  }
}
