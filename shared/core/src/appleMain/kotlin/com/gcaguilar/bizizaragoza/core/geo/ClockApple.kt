package com.gcaguilar.bizizaragoza.core.geo

import platform.Foundation.NSDate

internal actual fun currentTimeMs(): Long =
    (NSDate().timeIntervalSince1970 * 1000.0).toLong()
