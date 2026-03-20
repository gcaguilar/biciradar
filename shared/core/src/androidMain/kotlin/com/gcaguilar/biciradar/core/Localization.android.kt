package com.gcaguilar.biciradar.core

import java.util.Locale

actual fun currentAppLanguage(): AppLanguage = when (Locale.getDefault().language.lowercase()) {
  "en" -> AppLanguage.EN
  "ca" -> AppLanguage.CA
  "eu", "eus", "baq" -> AppLanguage.EU
  "gl" -> AppLanguage.GL
  else -> AppLanguage.ES
}
