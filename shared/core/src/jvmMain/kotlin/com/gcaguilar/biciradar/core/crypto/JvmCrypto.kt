package com.gcaguilar.biciradar.core.crypto

import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/** JVM actual — in-memory implementation used by desktop and tests. */
actual class PlatformKeyPair(
  private val alias: String,
) {
  actual val publicKeyDerBase64: String
    get() =
      Base64.getEncoder().encodeToString(
        JvmKeyStore.getPublicKey(alias)?.encoded ?: error("No public key for '$alias'"),
      )

  actual suspend fun sign(data: ByteArray): String {
    val signature = Signature.getInstance("Ed25519")
    signature.initSign(JvmKeyStore.getPrivateKey(alias) ?: error("No private key for '$alias'"))
    signature.update(data)
    return Base64.getEncoder().encodeToString(signature.sign())
  }
}

actual class SecureKeyStore actual constructor(
  storageDirectoryProvider: StorageDirectoryProvider,
) {
  actual suspend fun getOrCreateKeyPair(alias: String): PlatformKeyPair {
    if (!JvmKeyStore.contains(alias)) {
      val pair = KeyPairGenerator.getInstance("Ed25519").generateKeyPair()
      JvmKeyStore.store(alias, pair.private, pair.public)
    }
    return PlatformKeyPair(alias)
  }

  actual fun deleteKeyPair(alias: String) = JvmKeyStore.delete(alias)
}

private object JvmKeyStore {
  private val privateKeys = ConcurrentHashMap<String, PrivateKey>()
  private val publicKeys = ConcurrentHashMap<String, PublicKey>()

  fun store(
    alias: String,
    privateKey: PrivateKey,
    publicKey: PublicKey,
  ) {
    privateKeys[alias] = privateKey
    publicKeys[alias] = publicKey
  }

  fun getPrivateKey(alias: String): PrivateKey? = privateKeys[alias]

  fun getPublicKey(alias: String): PublicKey? = publicKeys[alias]

  fun contains(alias: String): Boolean = privateKeys.containsKey(alias)

  fun delete(alias: String) {
    privateKeys.remove(alias)
    publicKeys.remove(alias)
  }
}
