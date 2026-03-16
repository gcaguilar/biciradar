package com.gcaguilar.bizizaragoza.core.crypto

/**
 * Platform-specific RSA key pair backed by secure hardware storage
 * (Android Keystore on Android, Secure Enclave/Keychain on iOS).
 *
 * The key pair is generated once and never leaves the secure enclave in
 * plain form. [publicKeyDerBase64] is the only exportable material — it
 * is sent to the server during installation registration.
 *
 * Use [sign] to produce an RSA-SHA256-PKCS1v1.5 signature over arbitrary
 * bytes. The private key never leaves the platform security boundary.
 */
expect class PlatformKeyPair {
    /** DER-encoded RSA public key, Base64-encoded (no line breaks). */
    val publicKeyDerBase64: String

    /** Signs [data] with the private RSA key. Returns Base64-encoded signature. */
    fun sign(data: ByteArray): String
}

/**
 * Platform-specific factory and secure storage for [PlatformKeyPair].
 *
 * A single canonical alias (`bizi_installation_key`) is used per device.
 * If a key pair already exists under that alias it is returned without
 * re-generating, making this function idempotent.
 */
expect class SecureKeyStore {
    /**
     * Returns the existing key pair for [alias] if one exists,
     * or generates and stores a new one.
     */
    fun getOrCreateKeyPair(alias: String): PlatformKeyPair

    /** Deletes the key pair stored under [alias], if any. */
    fun deleteKeyPair(alias: String)
}

internal const val INSTALLATION_KEY_ALIAS = "bizi_installation_key"
