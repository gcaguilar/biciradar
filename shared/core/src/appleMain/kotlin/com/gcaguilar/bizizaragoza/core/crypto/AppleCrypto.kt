package com.gcaguilar.bizizaragoza.core.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionarySetValue
import platform.CoreFoundation.CFErrorRefVar
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.base64EncodedStringWithOptions
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.SecKeyCreateRandomKey
import platform.Security.SecKeyCopyExternalRepresentation
import platform.Security.SecKeyCreateSignature
import platform.Security.SecKeyCopyPublicKey
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
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocPointerTo
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.Foundation.CFBridgingRelease
import platform.CoreFoundation.CFNumberCreate
import platform.CoreFoundation.kCFNumberSInt32Type
import kotlinx.cinterop.IntVar

@OptIn(ExperimentalForeignApi::class)
actual class PlatformKeyPair(private val alias: String) {

    private fun loadPrivateKey(): SecKeyRef? = memScoped {
        val query = buildCFDictionary {
            put(kSecClass, kSecClassKey)
            put(kSecAttrApplicationTag, alias.toNSData())
            put(kSecReturnRef, kCFBooleanTrue)
        }
        val result = allocPointerTo<ObjCObjectVar<*>>()
        val status = SecItemCopyMatching(query, result.ptr.reinterpret())
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
                (CFBridgingRelease(data) as NSData).base64EncodedStringWithOptions(0u)
            }
        }

    actual fun sign(data: ByteArray): String = memScoped {
        val privKey = loadPrivateKey() ?: error("No private key for alias '$alias'")
        val nsData = data.toNSData()
        val err = alloc<CFErrorRefVar>()
        val sigData = SecKeyCreateSignature(
            privKey,
            kSecKeyAlgorithmRSASignatureMessagePKCS1v15SHA256,
            nsData.asCFData(),
            err.ptr,
        ) ?: error("Signing failed: ${err.value}")
        val result = (CFBridgingRelease(sigData) as NSData)
            .base64EncodedStringWithOptions(0u)
        result
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
        val result = allocPointerTo<ObjCObjectVar<*>>()
        SecItemCopyMatching(query, result.ptr.reinterpret()) == errSecSuccess
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
private fun String.toNSData(): NSData =
    (this as platform.Foundation.NSString)
        .dataUsingEncoding(platform.Foundation.NSUTF8StringEncoding)!!

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData =
    this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }

@OptIn(ExperimentalForeignApi::class)
private fun NSData.asCFData() = this as platform.CoreFoundation.CFDataRef?

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
        CFDictionarySetValue(dict, key, value as CFTypeRef?)
    }

    fun put(key: CFStringRef?, value: platform.CoreFoundation.CFDictionaryRef?) {
        CFDictionarySetValue(dict, key, value as CFTypeRef?)
    }
}
