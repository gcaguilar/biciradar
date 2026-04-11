package com.gcaguilar.biciradar.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.util.Base64

actual class PlatformKeyPair(
  private val alias: String,
) {
  private val keyStore: KeyStore
    get() = KeyStore.getInstance(ANDROID_KEYSTORE).also { it.load(null) }

  actual val publicKeyDerBase64: String
    get() {
      val pub =
        keyStore.getCertificate(alias)?.publicKey as? RSAPublicKey
          ?: error("No RSA public key found for alias '$alias'")
      return Base64.getEncoder().encodeToString(pub.encoded) // DER via getEncoded()
    }

  actual fun sign(data: ByteArray): String {
    val privateKey =
      keyStore.getKey(alias, null)
        ?: error("No private key found for alias '$alias'")
    val sig = Signature.getInstance("SHA256withRSA")
    sig.initSign(privateKey as java.security.PrivateKey)
    sig.update(data)
    return Base64.getEncoder().encodeToString(sig.sign())
  }

  companion object {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
  }
}

actual class SecureKeyStore {
  private val keyStore: KeyStore
    get() = KeyStore.getInstance(ANDROID_KEYSTORE).also { it.load(null) }

  actual fun getOrCreateKeyPair(alias: String): PlatformKeyPair {
    if (!keyStore.containsAlias(alias)) {
      generateKeyPair(alias)
    }
    return PlatformKeyPair(alias)
  }

  actual fun deleteKeyPair(alias: String) {
    if (keyStore.containsAlias(alias)) {
      keyStore.deleteEntry(alias)
    }
  }

  private fun generateKeyPair(alias: String) {
    val spec =
      KeyGenParameterSpec
        .Builder(
          alias,
          KeyProperties.PURPOSE_SIGN,
        ).apply {
          setKeySize(2048)
          setDigests(KeyProperties.DIGEST_SHA256)
          setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
        }.build()

    KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE).also {
      it.initialize(spec)
      it.generateKeyPair()
    }
  }

  companion object {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
  }
}
