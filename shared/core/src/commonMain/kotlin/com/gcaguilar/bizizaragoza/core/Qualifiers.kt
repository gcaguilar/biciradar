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
