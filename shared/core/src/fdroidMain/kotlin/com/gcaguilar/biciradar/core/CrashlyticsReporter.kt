package com.gcaguilar.biciradar.core

/**
 * F-Droid compliant CrashlyticsReporter that does nothing (no-op)
 * instead of using Firebase Crashlytics
 */
class FdroidCrashlyticsReporter : CrashlyticsReporter {
    override fun reportNonFatal(throwable: Throwable) {
        // No-op for F-Droid build
        // In a real implementation, you might log to file or use another open-source crash reporter
    }
}