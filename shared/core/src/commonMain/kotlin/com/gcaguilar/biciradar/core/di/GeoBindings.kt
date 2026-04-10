package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.AppVersion
import com.gcaguilar.biciradar.core.Platform
import com.gcaguilar.biciradar.core.OsVersion
import com.gcaguilar.biciradar.core.StorageDirectoryProvider
import com.gcaguilar.biciradar.core.crypto.SecureKeyStore
import com.gcaguilar.biciradar.core.geo.GeoApi
import com.gcaguilar.biciradar.core.geo.GeoApiImpl
import com.gcaguilar.biciradar.core.geo.GeoSearchUseCase
import com.gcaguilar.biciradar.core.geo.InstallationIdentityRepository
import com.gcaguilar.biciradar.core.geo.RequestSigner
import com.gcaguilar.biciradar.core.geo.ReverseGeocodeUseCase
import com.gcaguilar.biciradar.core.geo.TokenManager
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import okio.FileSystem

/**
 * Binding Container para dependencias de geolocalización y APIs geo.
 *
 * Provee:
 * - InstallationIdentityRepository (identidad de instalación)
 * - TokenManager (gestión de tokens)
 * - RequestSigner (firma de peticiones)
 * - GeoApi (API de geolocalización)
 * - GeoSearchUseCase (búsqueda de lugares)
 * - ReverseGeocodeUseCase (geocodificación inversa)
 */
@BindingContainer
object GeoBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideInstallationIdentityRepository(
        httpClient: HttpClient,
        json: Json,
        fileSystem: FileSystem,
        storageDirectoryProvider: StorageDirectoryProvider,
        secureKeyStore: SecureKeyStore,
        @AppVersion appVersion: String,
        @OsVersion osVersion: String,
        @Platform platform: String,
    ): InstallationIdentityRepository = InstallationIdentityRepository(
        httpClient = httpClient,
        json = json,
        fileSystem = fileSystem,
        storageDirectoryProvider = storageDirectoryProvider,
        secureKeyStore = secureKeyStore,
        appVersion = appVersion,
        osVersion = osVersion,
        platform = platform,
    )

    @SingleIn(AppScope::class)
    @Provides
    fun provideTokenManager(
        httpClient: HttpClient,
        json: Json,
        identityRepo: InstallationIdentityRepository,
    ): TokenManager = TokenManager(
        httpClient = httpClient,
        json = json,
        identityRepo = identityRepo,
    )

    @SingleIn(AppScope::class)
    @Provides
    fun provideRequestSigner(
        identityRepo: InstallationIdentityRepository,
    ): RequestSigner = RequestSigner(identityRepo = identityRepo)

    // GeoApi, GeoSearchUseCase y ReverseGeocodeUseCase se proveen automáticamente
    // por Metro mediante sus constructores anotados con @Inject
}