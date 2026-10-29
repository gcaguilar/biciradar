package com.gcaguilar.biciradar.core.backend

/**
 * Backend location. An empty [baseUrl] means the app runs in anonymous/local
 * mode: the existing device-only behaviour is preserved and no `/v1` call is
 * ever attempted.
 */
data class BackendConfig(
  val baseUrl: String? = null,
) {
  val isConfigured: Boolean get() = !baseUrl.isNullOrBlank()

  val normalizedBaseUrl: String get() = (baseUrl ?: "").trim().trimEnd('/')

  fun url(path: String): String = normalizedBaseUrl + path
}
