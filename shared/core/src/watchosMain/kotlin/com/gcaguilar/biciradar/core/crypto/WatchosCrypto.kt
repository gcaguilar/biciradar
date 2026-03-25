package com.gcaguilar.biciradar.core.crypto

actual class PlatformKeyPair(private val alias: String) {
    actual val publicKeyDerBase64: String
        get() = error("Crypto not supported on watchOS")

    actual fun sign(data: ByteArray): String = error("Crypto not supported on watchOS")
}

actual class SecureKeyStore {
    actual fun getOrCreateKeyPair(alias: String): PlatformKeyPair =
        error("Crypto not supported on watchOS")

    actual fun deleteKeyPair(alias: String): Unit = error("Crypto not supported on watchOS")
}
