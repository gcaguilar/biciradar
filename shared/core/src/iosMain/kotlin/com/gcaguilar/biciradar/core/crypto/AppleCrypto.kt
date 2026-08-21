package com.gcaguilar.biciradar.core.crypto

import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.EdDSA
import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.dataWithBytes
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual class PlatformKeyPair(
  internal val privateKeyDer: ByteArray,
  internal val publicKeyDer: ByteArray,
) {
  actual val publicKeyDerBase64: String
    get() = publicKeyDer.toNSData().base64EncodedStringWithOptions(0u)

  actual suspend fun sign(data: ByteArray): String {
    val privateKey =
      edDsa
        .privateKeyDecoder(EdDSA.Curve.Ed25519)
        .decodeFromByteArray(EdDSA.PrivateKey.Format.DER, privateKeyDer)
    return privateKey
      .signatureGenerator()
      .generateSignature(data)
      .toNSData()
      .base64EncodedStringWithOptions(0u)
  }
}

@OptIn(ExperimentalForeignApi::class)
actual class SecureKeyStore actual constructor(
  storageDirectoryProvider: StorageDirectoryProvider,
) {
  actual suspend fun getOrCreateKeyPair(alias: String): PlatformKeyPair =
    load(alias) ?: generate().also { save(alias, it) }

  actual fun deleteKeyPair(alias: String) {
    withKeychainQuery(alias) { query -> SecItemDelete(query) }
  }

  private fun load(alias: String): PlatformKeyPair? =
    memScoped {
      val result = alloc<COpaquePointerVar>()
      val status =
        withKeychainQuery(alias, returnData = true) { query ->
          SecItemCopyMatching(query, result.ptr)
        }
      if (status != errSecSuccess) {
        return@memScoped null
      }
      val encoded = (CFBridgingRelease(result.value) as? NSData)?.toByteArray() ?: return@memScoped null
      unpack(encoded)
    }

  private suspend fun generate(): PlatformKeyPair {
    val keyPair = edDsa.keyPairGenerator(EdDSA.Curve.Ed25519).generateKey()
    return PlatformKeyPair(
      privateKeyDer = keyPair.privateKey.encodeToByteArray(EdDSA.PrivateKey.Format.DER),
      publicKeyDer = keyPair.publicKey.encodeToByteArray(EdDSA.PublicKey.Format.DER),
    )
  }

  private fun save(
    alias: String,
    keyPair: PlatformKeyPair,
  ) {
    deleteKeyPair(alias)
    val value = pack(keyPair).toNSData()
    val status =
      withKeychainQuery(alias, value = value) { query ->
        SecItemAdd(query, null)
      }
    check(status == errSecSuccess) {
      "Unable to save Ed25519 private key to Keychain: $status"
    }
  }
}

private val edDsa by lazy { CryptographyProvider.Default.get(EdDSA) }

private fun pack(keyPair: PlatformKeyPair): ByteArray {
  require(keyPair.privateKeyDer.size <= UShort.MAX_VALUE.toInt())
  return byteArrayOf(
    (keyPair.privateKeyDer.size ushr 8).toByte(),
    keyPair.privateKeyDer.size.toByte(),
  ) + keyPair.privateKeyDer + keyPair.publicKeyDer
}

private fun unpack(value: ByteArray): PlatformKeyPair? {
  if (value.size < 3) return null
  val privateKeySize = ((value[0].toInt() and 0xff) shl 8) or (value[1].toInt() and 0xff)
  if (privateKeySize == 0 || value.size <= privateKeySize + 2) return null
  return PlatformKeyPair(
    privateKeyDer = value.copyOfRange(2, privateKeySize + 2),
    publicKeyDer = value.copyOfRange(privateKeySize + 2, value.size),
  )
}

@OptIn(ExperimentalForeignApi::class)
private fun <T> withKeychainQuery(
  alias: String,
  returnData: Boolean = false,
  value: NSData? = null,
  block: (CFDictionaryRef?) -> T,
): T {
  // Security's keys and values are CoreFoundation types, so the query must be a real
  // CFDictionary. Building it as an NSMutableDictionary means casting CFStringRef
  // constants to NSString, which throws ClassCastException at runtime.
  val owned = mutableListOf<CFTypeRef>()
  val query =
    CFDictionaryCreateMutable(
      kCFAllocatorDefault,
      0,
      kCFTypeDictionaryKeyCallBacks.ptr,
      kCFTypeDictionaryValueCallBacks.ptr,
    )
  fun cfString(text: String): CFStringRef? =
    CFStringCreateWithCString(kCFAllocatorDefault, text, kCFStringEncodingUTF8)
      ?.also { owned += it }
  return try {
    CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
    CFDictionarySetValue(query, kSecAttrService, cfString(KEYCHAIN_SERVICE))
    CFDictionarySetValue(query, kSecAttrAccount, cfString(alias))
    if (returnData) {
      CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
      CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
    }
    if (value != null) {
      CFDictionarySetValue(
        query,
        kSecAttrAccessible,
        kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
      )
      CFDictionarySetValue(query, kSecValueData, CFBridgingRetain(value)?.also { owned += it })
    }
    block(query)
  } finally {
    owned.forEach { CFRelease(it) }
    if (query != null) CFRelease(query)
  }
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
  usePinned { pinned -> NSData.dataWithBytes(pinned.addressOf(0), size.convert())!! }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray =
  ByteArray(length.toInt()).also { destination ->
    destination.usePinned { pinned -> memcpy(pinned.addressOf(0), bytes, length) }
  }

private const val KEYCHAIN_SERVICE = "com.gcaguilar.biciradar.installation-key"
