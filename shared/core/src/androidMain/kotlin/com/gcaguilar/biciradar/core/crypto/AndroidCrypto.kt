package com.gcaguilar.biciradar.core.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.io.File
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec

private val bouncyCastleProvider = BouncyCastleProvider()

actual class PlatformKeyPair(
  internal val privateKeyDer: ByteArray,
  internal val publicKeyDer: ByteArray,
) {
  actual val publicKeyDerBase64: String
    get() = Base64.getEncoder().encodeToString(publicKeyDer)

  actual suspend fun sign(data: ByteArray): String {
    val signature = Signature.getInstance("Ed25519", bouncyCastleProvider)
    signature.initSign(
      java.security.KeyFactory
        .getInstance("Ed25519", bouncyCastleProvider)
        .generatePrivate(java.security.spec.PKCS8EncodedKeySpec(privateKeyDer)),
    )
    signature.update(data)
    return Base64.getEncoder().encodeToString(signature.sign())
  }
}

actual class SecureKeyStore actual constructor(
  private val storageDirectoryProvider: StorageDirectoryProvider,
) {
  actual suspend fun getOrCreateKeyPair(alias: String): PlatformKeyPair =
    load(alias) ?: generate().also { save(alias, it) }

  actual fun deleteKeyPair(alias: String) {
    keyFile(alias).delete()
    keyStore.deleteEntry(wrappingKeyAlias(alias))
  }

  private fun load(alias: String): PlatformKeyPair? {
    val file = keyFile(alias)
    if (!file.exists()) return null

    return runCatching {
      val encrypted = file.readBytes()
      require(encrypted.size > IV_SIZE_BYTES)
      val cipher = Cipher.getInstance("AES/GCM/NoPadding")
      cipher.init(
        Cipher.DECRYPT_MODE,
        keyStore.getKey(wrappingKeyAlias(alias), null),
        GCMParameterSpec(GCM_TAG_SIZE_BITS, encrypted.copyOfRange(0, IV_SIZE_BYTES)),
      )
      val parts = cipher.doFinal(encrypted.copyOfRange(IV_SIZE_BYTES, encrypted.size)).decodeToString().split('\n')
      require(parts.size == 2)
      PlatformKeyPair(
        privateKeyDer = Base64.getDecoder().decode(parts[0]),
        publicKeyDer = Base64.getDecoder().decode(parts[1]),
      )
    }.getOrElse {
      deleteKeyPair(alias)
      null
    }
  }

  private fun generate(): PlatformKeyPair {
    val keyPair = KeyPairGenerator.getInstance("Ed25519", bouncyCastleProvider).generateKeyPair()
    return PlatformKeyPair(keyPair.private.encoded, keyPair.public.encoded)
  }

  private fun save(
    alias: String,
    keyPair: PlatformKeyPair,
  ) {
    val plaintext =
      listOf(keyPair.privateKeyDer, keyPair.publicKeyDer)
        .joinToString("\n") { Base64.getEncoder().encodeToString(it) }
        .encodeToByteArray()
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey(alias))
    val encrypted = cipher.iv + cipher.doFinal(plaintext)
    keyFile(alias).apply {
      parentFile?.mkdirs()
      writeBytes(encrypted)
    }
  }

  private fun getOrCreateWrappingKey(alias: String) =
    keyStore.getKey(wrappingKeyAlias(alias), null)
      ?: KeyGenerator
        .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        .apply {
          init(
            KeyGenParameterSpec
              .Builder(
                wrappingKeyAlias(alias),
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
              ).setKeySize(256)
              .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
              .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
              .build(),
          )
        }.generateKey()

  private fun keyFile(alias: String) = File(storageDirectoryProvider.rootPath, "$alias.bin")

  private val keyStore: KeyStore
    get() = KeyStore.getInstance(ANDROID_KEYSTORE).also { it.load(null) }

  private fun wrappingKeyAlias(alias: String) = "${alias}_wrapping"

  private companion object {
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val GCM_TAG_SIZE_BITS = 128
    const val IV_SIZE_BYTES = 12
  }
}
