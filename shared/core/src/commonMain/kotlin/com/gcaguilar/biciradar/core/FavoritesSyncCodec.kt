package com.gcaguilar.biciradar.core

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Shared codec for the favorites snapshot exchanged with a paired device
 * (Android DataLayer `payload_json`, WatchConnectivity dictionaries).
 *
 * Lives in common code so every platform encodes and decodes the same payload
 * and so the JSON shape can be tested deterministically. Unknown keys are
 * ignored to stay forward compatible with newer peers.
 */
object FavoritesSyncCodec {
  private val codecJson = Json { ignoreUnknownKeys = true }

  fun encode(snapshot: FavoritesSyncSnapshot): String = codecJson.encodeToString(snapshot)

  /** Returns `null` when [json] is not a valid [FavoritesSyncSnapshot] payload. */
  fun decode(json: String): FavoritesSyncSnapshot? =
    runCatching {
      codecJson.decodeFromString<FavoritesSyncSnapshot>(json)
    }.getOrNull()
}
