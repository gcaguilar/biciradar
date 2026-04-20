package com.gcaguilar.biciradar.core

/**
 * F-Droid compliant RemoteConfigProvider that returns null (no remote config)
 * instead of using Firebase Remote Config
 */
class FdroidRemoteConfigProvider : RemoteConfigProvider {
    override suspend fun getString(key: String): String? {
        // No remote config in F-Droid build
        return null
    }
}