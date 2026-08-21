package com.gcaguilar.biciradar.core.crypto

import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import kotlinx.coroutines.test.runTest
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Ed25519CryptoTest {
  @Test
  fun signsWithThePersistedEd25519Key() =
    runTest {
      val keyStore =
        SecureKeyStore(
          object : StorageDirectoryProvider {
            override val rootPath = "/tmp/biciradar-test"
          },
        )
      val first = keyStore.getOrCreateKeyPair("ed25519-test")
      val signature = first.sign("signed payload".encodeToByteArray())

      val publicKey =
        KeyFactory.getInstance("Ed25519").generatePublic(
          X509EncodedKeySpec(Base64.getDecoder().decode(first.publicKeyDerBase64)),
        )
      val verifier = Signature.getInstance("Ed25519")
      verifier.initVerify(publicKey)
      verifier.update("signed payload".encodeToByteArray())

      assertTrue(verifier.verify(Base64.getDecoder().decode(signature)))
      assertEquals(first.publicKeyDerBase64, keyStore.getOrCreateKeyPair("ed25519-test").publicKeyDerBase64)
      keyStore.deleteKeyPair("ed25519-test")
    }
}
