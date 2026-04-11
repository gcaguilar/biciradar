package com.gcaguilar.biciradar.core.crypto

import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

/**
 * JVM actual — backed by in-memory key store (used for unit tests only;
 * the JVM target is not shipped as a production app binary).
 */
actual class PlatformKeyPair(
  private val alias: String,
) {
  actual val publicKeyDerBase64: String
    get() {
      val pub =
        JvmKeyStore.getPublicKey(alias)
          ?: error("No RSA public key found for alias '$alias'")
      return Base64.getEncoder().encodeToString(pub.encoded)
    }

  actual fun sign(data: ByteArray): String {
    val priv =
      JvmKeyStore.getPrivateKey(alias)
        ?: error("No private key found for alias '$alias'")
    val sig = Signature.getInstance("SHA256withRSA")
    sig.initSign(priv)
    sig.update(data)
    return Base64.getEncoder().encodeToString(sig.sign())
  }
}

actual class SecureKeyStore {
  actual fun getOrCreateKeyPair(alias: String): PlatformKeyPair {
    if (!JvmKeyStore.contains(alias)) {
      val gen = KeyPairGenerator.getInstance("RSA")
      gen.initialize(2048)
      val kp = gen.generateKeyPair()
      JvmKeyStore.store(alias, kp.private, kp.public)
    }
    return PlatformKeyPair(alias)
  }

  actual fun deleteKeyPair(alias: String) {
    JvmKeyStore.delete(alias)
  }
}

private object JvmKeyStore {
  private val privateKeys = ConcurrentHashMap<String, PrivateKey>()
  private val publicKeys = ConcurrentHashMap<String, PublicKey>()

  fun store(
    alias: String,
    priv: PrivateKey,
    pub: PublicKey,
  ) {
    privateKeys[alias] = priv
    publicKeys[alias] = pub
  }

  fun getPrivateKey(alias: String): PrivateKey? = privateKeys[alias]

  fun getPublicKey(alias: String): PublicKey? = publicKeys[alias]

  fun contains(alias: String): Boolean = privateKeys.containsKey(alias)

  fun delete(alias: String) {
    privateKeys.remove(alias)
    publicKeys.remove(alias)
  }
}
