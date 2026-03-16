package com.gcaguilar.bizizaragoza.core.geo

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256Hex(data: ByteArray): String = memScoped {
    val digest = allocArray<UByteVar>(CC_SHA256_DIGEST_LENGTH)
    data.usePinned { pinned ->
        CC_SHA256(pinned.addressOf(0), data.size.toUInt(), digest)
    }
    (0 until CC_SHA256_DIGEST_LENGTH).joinToString("") {
        (digest[it].toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}
