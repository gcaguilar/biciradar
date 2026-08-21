package com.gcaguilar.biciradar.core.geo

import dev.zacsweers.metro.Inject

/**
 * Produces the Ed25519-signed request shape accepted by datosbizi.com.
 *
 * The server verifies `"$timestamp.$bodyWithoutSignature"`, where the timestamp is
 * expressed in milliseconds and is also part of the JSON body. Keeping this wire
 * representation here prevents Android and iOS from drifting apart.
 */
@Inject
class RequestSigner(
  private val identityRepo: InstallationIdentityRepository,
) {
  suspend fun signedBody(unsignedBodyJson: String): SignedRequestBody {
    check(unsignedBodyJson.startsWith('{') && unsignedBodyJson.endsWith('}')) {
      "Geo request body must be a JSON object"
    }
    val (identity, keyPair) = identityRepo.getOrRegister()
    val timestamp = currentTimeMs()
    val bodyWithoutSignature =
      unsignedBodyJson.dropLast(1) +
        if (unsignedBodyJson == "{}") {
          "\"timestamp\":$timestamp}"
        } else {
          ",\"timestamp\":$timestamp}"
        }
    val signature = keyPair.sign("$timestamp.$bodyWithoutSignature".encodeToByteArray())
    val bodyJson = bodyWithoutSignature.dropLast(1) + ",\"signature\":\"$signature\"}"

    return SignedRequestBody(
      installationId = identity.installationId,
      bodyJson = bodyJson,
    )
  }
}

data class SignedRequestBody(
  val installationId: String,
  val bodyJson: String,
)

/** Retained for the existing platform digest implementations. */
internal expect fun sha256Hex(data: ByteArray): String
