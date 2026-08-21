package com.gcaguilar.biciradar.core.crypto

import com.gcaguilar.biciradar.core.StorageDirectoryProvider

/**
 * Platform-specific Ed25519 key pair backed by secure platform storage
 * (Android Keystore-encrypted storage on Android, Keychain on iOS).
 *
 * The key pair is generated once and never leaves the secure enclave in
 * plain form. [publicKeyDerBase64] is the only exportable material — it
 * is sent to the server during installation registration.
 *
 * Use [sign] to produce an Ed25519 signature over arbitrary bytes.
 */
expect class PlatformKeyPair {
  /** DER-encoded (SPKI) Ed25519 public key, Base64-encoded (no line breaks). */
  val publicKeyDerBase64: String

  /** Signs [data] with the private Ed25519 key. Returns a Base64-encoded signature. */
  suspend fun sign(data: ByteArray): String
}

/**
 * Platform-specific factory and secure storage for [PlatformKeyPair].
 *
 * A single canonical alias (`bizi_installation_key`) is used per device.
 * If a key pair already exists under that alias it is returned without
 * re-generating, making this function idempotent.
 */
expect class SecureKeyStore(
  storageDirectoryProvider: StorageDirectoryProvider,
) {
  /**
   * Returns the existing key pair for [alias] if one exists,
   * or generates and stores a new one.
   */
  suspend fun getOrCreateKeyPair(alias: String): PlatformKeyPair

  /** Deletes the key pair stored under [alias], if any. */
  fun deleteKeyPair(alias: String)
}

internal const val INSTALLATION_KEY_ALIAS = "bizi_installation_ed25519_key"
