package com.gcaguilar.biciradar.core.crypto

import kotlinx.cinterop.COpaquePointerVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFNumberSInt32Type
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Foundation.dataWithBytes
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecKeyCopyExternalRepresentation
import platform.Security.SecKeyCopyPublicKey
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyCreateSignature
import platform.Security.SecKeyRef
import platform.Security.errSecSuccess
import platform.Security.kSecAttrApplicationTag
import platform.Security.kSecAttrIsPermanent
import platform.Security.kSecAttrKeySizeInBits
import platform.Security.kSecAttrKeyType
import platform.Security.kSecAttrKeyTypeRSA
import platform.Security.kSecClass
import platform.Security.kSecClassKey
import platform.Security.kSecKeyAlgorithmRSASignatureMessagePKCS1v15SHA256
import platform.Security.kSecPrivateKeyAttrs
import platform.Security.kSecReturnRef

@OptIn(ExperimentalForeignApi::class)
actual class PlatformKeyPair(private val alias: String) {

    private fun loadPrivateKey(): SecKeyRef? = memScoped {
        val query = buildCFDictionary {
            put(kSecClass, kSecClassKey)
            put(kSecAttrApplicationTag, alias.toNSData())
            put(kSecReturnRef, kCFBooleanTrue)
        }
        val result = alloc<COpaquePointerVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        if (status == errSecSuccess) {
            @Suppress("UNCHECKED_CAST")
            result.value as? SecKeyRef
        } else {
            null
        }
    }

    actual val publicKeyDerBase64: String
        get() {
            val privKey = loadPrivateKey() ?: error("No private key found for alias '$alias'")
            val pubKey = SecKeyCopyPublicKey(privKey)
                ?: error("Cannot derive public key from alias '$alias'")
            return memScoped {
                val err = alloc<CFErrorRefVar>()
                val data = SecKeyCopyExternalRepresentation(pubKey, err.ptr)
                    ?: error("Cannot export public key: ${err.value}")
                CFRelease(pubKey)
                (CFBridgingRelease(data) as? NSData)?.base64EncodedStringWithOptions(0u)
                    ?: error("Cannot convert public key data")
            }
        }

    actual fun sign(data: ByteArray): String = memScoped {
        val privKey = loadPrivateKey() ?: error("No private key for alias '$alias'")
        val nsData = data.toNSData()
        val cfData = nsData.asCFData() ?: error("Cannot bridge NSData to CFData")
        try {
            val err = alloc<CFErrorRefVar>()
            val sigData = SecKeyCreateSignature(
                privKey,
                kSecKeyAlgorithmRSASignatureMessagePKCS1v15SHA256,
                cfData,
                err.ptr,
            ) ?: error("Signing failed: ${err.value}")
            (CFBridgingRelease(sigData) as? NSData)?.base64EncodedStringWithOptions(0u)
                ?: error("Cannot convert signature data")
        } finally {
            CFRelease(cfData)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
actual class SecureKeyStore {

    actual fun getOrCreateKeyPair(alias: String): PlatformKeyPair {
        if (!keyExists(alias)) {
            generateKeyPair(alias)
        }
        return PlatformKeyPair(alias)
    }

    actual fun deleteKeyPair(alias: String) = memScoped {
        val query = buildCFDictionary {
            put(kSecClass, kSecClassKey)
            put(kSecAttrApplicationTag, alias.toNSData())
        }
        SecItemDelete(query)
        Unit
    }

    private fun keyExists(alias: String): Boolean = memScoped {
        val query = buildCFDictionary {
            put(kSecClass, kSecClassKey)
            put(kSecAttrApplicationTag, alias.toNSData())
            put(kSecReturnRef, kCFBooleanTrue)
        }
        val result = alloc<COpaquePointerVar>()
        SecItemCopyMatching(query, result.ptr) == errSecSuccess
    }

    private fun generateKeyPair(alias: String) = memScoped {
        val privateKeyAttrs = buildCFDictionary {
            put(kSecAttrIsPermanent, kCFBooleanTrue)
            put(kSecAttrApplicationTag, alias.toNSData())
        }
        val keySpec = buildCFDictionary {
            put(kSecAttrKeyType, kSecAttrKeyTypeRSA)
            put(kSecAttrKeySizeInBits, cfNumber(2048))
            put(kSecPrivateKeyAttrs, privateKeyAttrs)
        }
        val err = alloc<CFErrorRefVar>()
        SecKeyCreateRandomKey(keySpec, err.ptr)
            ?: error("Key generation failed: ${err.value}")
        Unit
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

@OptIn(ExperimentalForeignApi::class)
private fun String.toNSData(): NSData = encodeToByteArray().toNSData()

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    usePinned { pinned ->
        NSData.dataWithBytes(pinned.addressOf(0), size.convert())!!
    }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.asCFData(): platform.CoreFoundation.CFDataRef? =
    CFBridgingRetain(this)?.let { it as platform.CoreFoundation.CFDataRef }

@OptIn(ExperimentalForeignApi::class)
private fun MemScope.cfNumber(value: Int): CFTypeRef? {
    val n = alloc<IntVar>().also { it.value = value }
    return CFNumberCreate(kCFAllocatorDefault, kCFNumberSInt32Type, n.ptr)
}

/**
 * Minimal DSL wrapper to build a CFMutableDictionary for Security framework calls.
 * Keys/values are CF types; the returned dictionary is autoreleased via ARC bridging.
 */
@OptIn(ExperimentalForeignApi::class)
private fun buildCFDictionary(block: CFDictionaryBuilder.() -> Unit): platform.CoreFoundation.CFDictionaryRef? {
    val dict = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null)
    CFDictionaryBuilder(dict).block()
    return dict
}

@OptIn(ExperimentalForeignApi::class)
private class CFDictionaryBuilder(
    private val dict: platform.CoreFoundation.CFMutableDictionaryRef?,
) {
    fun put(key: CFStringRef?, value: Any?) {
        CFDictionarySetValue(dict, key, value as CFTypeRef?)
    }

    fun put(key: CFStringRef?, value: NSData?) {
        val bridgedValue = value?.let { CFBridgingRetain(it) }
        try {
            CFDictionarySetValue(dict, key, bridgedValue)
        } finally {
            if (bridgedValue != null) CFRelease(bridgedValue)
        }
    }

    fun put(key: CFStringRef?, value: platform.CoreFoundation.CFDictionaryRef?) {
        CFDictionarySetValue(dict, key, value as CFTypeRef?)
    }
}
