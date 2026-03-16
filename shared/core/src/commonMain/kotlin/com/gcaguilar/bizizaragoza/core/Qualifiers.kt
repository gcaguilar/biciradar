package com.gcaguilar.bizizaragoza.core

import dev.zacsweers.metro.Qualifier

/** Qualifier for the app version string (e.g. "1.2.3"). */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class AppVersion

/** Qualifier for the platform string (e.g. "android" or "ios"). */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Platform

/** Qualifier for the OS version string (e.g. "Android 14" or "iOS 17.2"). */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class OsVersion
