package com.gcaguilar.biciradar.core.geo

import dev.zacsweers.metro.Inject

/**
 * Signs geo API requests using the device's RSA private key.
 *
 * Signing payload format (as per datosbizi.com spec):
 *   METHOD + "\n" + PATH + "\n" + BODY_SHA256_HEX + "\n" + TIMESTAMP + "\n" + NONCE
 *
 * All timestamps are Unix seconds (not milliseconds).
 */
@Inject
class RequestSigner(
  private val identityRepo: InstallationIdentityRepository,
) {
  /**
   * Produces the four signed headers needed for every geo request:
   * `X-Installation-Id`, `X-Timestamp`, `X-Nonce`, `X-Signature`.
   *
   * @param method   HTTP method uppercased, e.g. "POST"
   * @param path     Absolute path, e.g. "/geo/search"
   * @param body     Raw UTF-8 request body bytes (used for SHA-256 digest)
   */
  suspend fun signedHeaders(
    method: String,
    path: String,
    body: ByteArray,
  ): SignedHeaders {
    val (identity, keyPair) = identityRepo.getOrRegister()
    val timestamp = currentTimeMs() / 1000L
    val nonce = TokenManager.generateNonce()
    val bodyHash = sha256Hex(body)

    val payload = "$method\n$path\n$bodyHash\n$timestamp\n$nonce"
    val signature = keyPair.sign(payload.encodeToByteArray())

    return SignedHeaders(
      installationId = identity.installationId,
      timestamp = timestamp,
      nonce = nonce,
      signature = signature,
    )
  }
}

data class SignedHeaders(
  val installationId: String,
  val timestamp: Long,
  val nonce: String,
  val signature: String,
)

/** Returns the lowercase hex-encoded SHA-256 digest of [data]. Platform-specific. */
internal expect fun sha256Hex(data: ByteArray): String
