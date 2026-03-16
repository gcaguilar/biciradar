package com.gcaguilar.bizizaragoza.core.geo

import com.gcaguilar.bizizaragoza.core.StorageDirectoryProvider
import com.gcaguilar.bizizaragoza.core.crypto.INSTALLATION_KEY_ALIAS
import com.gcaguilar.bizizaragoza.core.crypto.SecureKeyStore
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Manages the device's installation identity.
 *
 * On first call to [getOrRegister] it:
 *  1. Generates (or loads) an RSA key pair via [SecureKeyStore].
 *  2. POSTs the public key to `/install/register`.
 *  3. Persists the returned [InstallationIdentity] to disk.
 *
 * Subsequent calls return the cached identity instantly.
 */
@Inject
class InstallationIdentityRepository(
    private val httpClient: HttpClient,
    private val json: Json,
    private val fileSystem: FileSystem,
    private val storageDirectoryProvider: StorageDirectoryProvider,
    private val secureKeyStore: SecureKeyStore,
    private val appVersion: String,
    private val platform: String,
) {
    @kotlin.concurrent.Volatile
    private var cached: Pair<InstallationIdentity, com.gcaguilar.bizizaragoza.core.crypto.PlatformKeyPair>? = null

    /**
     * Returns the current [InstallationIdentity] and its associated key pair.
     * Registers with the server if no identity is persisted yet.
     *
     * @throws GeoError.Network on network issues
     * @throws GeoError.Server on non-2xx responses
     */
    suspend fun getOrRegister(): Pair<InstallationIdentity, com.gcaguilar.bizizaragoza.core.crypto.PlatformKeyPair> {
        cached?.let {
            println("[InstallRepo] returning cached identity installationId=${it.first.installationId}")
            return it
        }

        // 1. Try loading from disk
        println("[InstallRepo] loading identity from disk...")
        val persisted = loadFromDisk()

        println("[InstallRepo] generating/loading key pair...")
        val keyPair = runCatching {
            secureKeyStore.getOrCreateKeyPair(INSTALLATION_KEY_ALIAS)
        }.getOrElse { ex ->
            println("[InstallRepo] KEY PAIR ERROR: ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Unknown(ex)
        }
        println("[InstallRepo] key pair OK publicKey=${keyPair.publicKeyDerBase64.take(20)}...")

        if (persisted != null) {
            println("[InstallRepo] found persisted identity installationId=${persisted.installationId}")
            val result = persisted to keyPair
            cached = result
            return result
        }

        // 2. Register with the server
        println("[InstallRepo] no persisted identity — registering with server platform=$platform appVersion=$appVersion")
        val publicKeyBase64 = keyPair.publicKeyDerBase64
        val response = runCatching {
            httpClient.post("$BASE_URL/install/register") {
                contentType(ContentType.Application.Json)
                setBody(
                    json.encodeToString(
                        RegisterRequest(
                            publicKey = publicKeyBase64,
                            appVersion = appVersion,
                            platform = platform,
                        ),
                    ),
                )
            }
        }.getOrElse { ex ->
            println("[InstallRepo] NETWORK ERROR /install/register: ${ex::class.simpleName} — ${ex.message}")
            throw GeoError.Network(ex)
        }

        println("[InstallRepo] /install/register status=${response.status.value}")
        if (!response.status.isSuccess()) {
            println("[InstallRepo] SERVER ERROR /install/register: ${response.status.value} ${response.status.description}")
            throw GeoError.Server(response.status.value, response.status.description)
        }

        val registerResponse = runCatching { response.body<RegisterResponse>() }
            .getOrElse { ex ->
                println("[InstallRepo] PARSE ERROR /install/register: ${ex::class.simpleName} — ${ex.message}")
                throw GeoError.Unknown(ex)
            }

        val identity = InstallationIdentity(
            installationId = registerResponse.installationId,
            publicKeyBase64 = publicKeyBase64,
        )
        println("[InstallRepo] registered installationId=${identity.installationId}")

        // 3. Persist and cache
        saveToDisk(identity)
        println("[InstallRepo] identity persisted to disk")
        val result = identity to keyPair
        cached = result
        return result
    }

    /** Clears the persisted identity (e.g., for re-registration after a device restore). */
    suspend fun clear() {
        println("[InstallRepo] clearing identity from cache and disk")
        cached = null
        val path = identityPath()
        if (fileSystem.exists(path)) fileSystem.delete(path)
        secureKeyStore.deleteKeyPair(INSTALLATION_KEY_ALIAS)
        println("[InstallRepo] identity cleared")
    }

    // ------------------------------------------------------------------
    // Disk I/O
    // ------------------------------------------------------------------

    private fun identityPath() = "${storageDirectoryProvider.rootPath}/installation_identity.json".toPath()

    private fun loadFromDisk(): InstallationIdentity? {
        val path = identityPath()
        if (!fileSystem.exists(path)) {
            println("[InstallRepo] no identity file at $path")
            return null
        }
        return runCatching {
            json.decodeFromString<InstallationIdentity>(fileSystem.read(path) { readUtf8() })
        }.getOrElse { ex ->
            println("[InstallRepo] PARSE ERROR loading identity from disk: ${ex::class.simpleName} — ${ex.message}")
            null
        }
    }

    private fun saveToDisk(identity: InstallationIdentity) {
        val path = identityPath()
        val dir = path.parent ?: return
        fileSystem.createDirectories(dir)
        fileSystem.write(path) {
            writeUtf8(json.encodeToString(identity))
        }
    }

    companion object {
        internal const val BASE_URL = "https://datosbizi.com/api"
    }
}
