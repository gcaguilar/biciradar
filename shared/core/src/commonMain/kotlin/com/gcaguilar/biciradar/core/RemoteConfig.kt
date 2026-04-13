package com.gcaguilar.biciradar.core

interface RemoteConfigProvider {
  suspend fun getString(key: String): String?
}

object NoOpRemoteConfigProvider : RemoteConfigProvider {
  override suspend fun getString(key: String): String? = null
}
