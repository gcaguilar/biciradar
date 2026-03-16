package com.gcaguilar.bizizaragoza.core.geo

import dev.zacsweers.metro.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Executes a geographic place search against datosbizi.com.
 *
 * Deduplication: if a search for the exact same (normalized) query is
 * already in-flight, the second caller waits and reuses the result.
 * This is enforced via a per-key Mutex (one slot — not a full cache).
 */
@Inject
class GeoSearchUseCase(
    private val geoApi: GeoApi,
) {
    private val inFlight = mutableMapOf<String, Mutex>()
    private val inFlightLock = Mutex()

    @kotlin.concurrent.Volatile
    private var lastKey: String? = null

    @kotlin.concurrent.Volatile
    private var lastResult: List<GeoResult>? = null

    /**
     * @param query Raw user input (will be trimmed + lowercased for dedup purposes).
     */
    suspend fun execute(query: String): List<GeoResult> {
        val key = query.trim().lowercase()
        if (key.isBlank()) return emptyList()

        // Return cached result immediately if the key matches the last completed search
        if (key == lastKey) lastResult?.let { return it }

        val mutex = inFlightLock.withLock {
            inFlight.getOrPut(key) { Mutex() }
        }

        return mutex.withLock {
            // Another coroutine may have completed the same search while we waited
            if (key == lastKey) lastResult ?: fetchAndCache(key)
            else fetchAndCache(key)
        }.also {
            inFlightLock.withLock { inFlight.remove(key) }
        }
    }

    private suspend fun fetchAndCache(key: String): List<GeoResult> {
        val result = geoApi.search(key)
        lastKey = key
        lastResult = result
        return result
    }
}
