package com.gcaguilar.biciradar.core.backend

/**
 * Narrow seam for the account lifecycle calls. Implemented by
 * [BiciRadarApiClient]; tests substitute a fake without any HTTP machinery.
 */
interface AccountBackendApi {
  val isConfigured: Boolean

  /**
   * Deletes the signed-in account and every server-side row tied to it
   * (`DELETE /v1/me`): routines, subscriptions, devices and monitoring
   * windows. Throws [BackendException] when the backend rejects the call.
   */
  suspend fun deleteAccount()
}
