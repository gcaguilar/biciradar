package com.gcaguilar.biciradar.core.geo

import com.gcaguilar.biciradar.core.AppScope
import com.gcaguilar.biciradar.core.GoogleMapsApiKey
import com.gcaguilar.biciradar.core.GooglePlacesApi
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Executes a geographic place search against datosbizi.com.
 *
 * Deduplication: if a search for the exact same (normalized) query is
 * already in-flight, the second caller waits and reuses the result.
 * This is enforced via a per-key Mutex (one slot — not a full cache).
 *
 * Registrado automáticamente en el grafo vía @ContributesBinding.
 */
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GeoSearchUseCase(
    private val geoApi: GeoApi,
    private val googlePlacesApi: GooglePlacesApi,
    @param:GoogleMapsApiKey private val googleMapsApiKey: String?,
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
        val key = normalizeSearchKey(query)
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
        val candidateQueries = buildCandidateQueries(key)
        var lastError: Throwable? = null

        for (candidateQuery in candidateQueries) {
            val result = try {
                geoApi.search(candidateQuery)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Throwable) {
                lastError = error
                println("[GeoSearch] datosbizi search failed for '$candidateQuery': ${error::class.simpleName} ${error.message}")
                continue
            }

            if (result.isNotEmpty()) {
                val deduplicated = deduplicateResults(result)
                lastKey = key
                lastResult = deduplicated
                return deduplicated
            }
        }

        val fallbackQuery = candidateQueries.firstOrNull().orEmpty()
        val fallback = fallbackToGooglePlaces(
            query = fallbackQuery,
            originalError = lastError ?: GeoError.Server(404, "No search results"),
        )
        if (fallback != null) {
            val deduplicated = deduplicateResults(fallback)
            lastKey = key
            lastResult = deduplicated
            return deduplicated
        }

        if (lastError != null) throw lastError
        lastKey = key
        lastResult = emptyList()
        return emptyList()
    }

    private suspend fun fallbackToGooglePlaces(query: String, originalError: Throwable): List<GeoResult>? {
        val apiKey = googleMapsApiKey?.takeIf { it.isNotBlank() } ?: return null
        println("[GeoSearch] Falling back to Google Places for '$query' after ${originalError::class.simpleName}: ${originalError.message}")

        val predictions = googlePlacesApi.autocomplete(query, biasLocation = null, apiKey = apiKey)
            .take(FALLBACK_RESULT_LIMIT)
        if (predictions.isEmpty()) return emptyList()

        return predictions.mapNotNull { prediction ->
            val details = googlePlacesApi.placeDetails(prediction.placeId, apiKey) ?: return@mapNotNull null
            GeoResult(
                id = details.placeId,
                name = details.name.ifBlank {
                    prediction.description.substringBefore(',').trim().ifBlank { prediction.description }
                },
                address = prediction.description,
                latitude = details.location.latitude,
                longitude = details.location.longitude,
            )
        }
    }

    private companion object {
        const val FALLBACK_RESULT_LIMIT = 5
    }
}

private fun deduplicateResults(results: List<GeoResult>): List<GeoResult> = results.distinctBy { geoResult ->
    geoResult.name.trim().lowercase() to geoResult.address.trim().lowercase()
}

private fun normalizeSearchKey(query: String): String = query
    .replace(Regex("\\s+"), " ")
    .replace(Regex("\\s*,\\s*"), ", ")
    .trim()

private fun buildCandidateQueries(query: String): List<String> {
    val normalized = collapseDuplicateAddressParts(normalizeSearchKey(query))
    val canonical = applyZaragozaCanonicalTerms(normalized)
    val canonicalWithCity = ensureZaragozaContext(canonical)
    val normalizedWithCity = ensureZaragozaContext(normalized)

    return listOf(
        canonicalWithCity,
        normalizedWithCity,
        canonical,
        normalized,
    )
        .map { it.trim().trim(',').trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase() }
}

private fun collapseDuplicateAddressParts(query: String): String {
    val parts = query.split(',')
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (parts.isEmpty()) return query.trim()

    return parts.fold(mutableListOf<String>()) { acc, part ->
        if (acc.none { it.equals(part, ignoreCase = true) }) {
            acc += part
        }
        acc
    }.joinToString(", ")
}

private fun applyZaragozaCanonicalTerms(query: String): String {
    return query.replace(
        Regex("\\bplaza\\s+(de\\s+)?espa(?:ñ|n)a\\b", RegexOption.IGNORE_CASE),
        "Plaza de España",
    )
}

private fun ensureZaragozaContext(query: String): String {
    if (query.contains("zaragoza", ignoreCase = true)) return query
    return "$query, Zaragoza"
}
