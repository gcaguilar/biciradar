package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.BiziApi
import com.gcaguilar.biciradar.core.BiziHttpClientFactory
import com.gcaguilar.biciradar.core.DatadosBiziApi
import com.gcaguilar.biciradar.core.GbfsBiziApi
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.GooglePlacesApiImpl
import com.gcaguilar.biciradar.core.SettingsRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

/**
 * Binding Container para dependencias de red.
 *
 * Provee:
 * - HttpClient configurado
 * - APIs de Bizi (GBFS y datosbizi.com)
 * - API de Google Places
 */
@BindingContainer
object NetworkBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(
        httpClientFactory: BiziHttpClientFactory,
        json: Json,
    ): HttpClient = httpClientFactory.create(json)

    @SingleIn(AppScope::class)
    @Provides
    fun provideBiziApi(
        appConfiguration: com.gcaguilar.biciradar.core.AppConfiguration,
        httpClient: HttpClient,
        settingsRepository: SettingsRepository,
    ): BiziApi = GbfsBiziApi(httpClient, appConfiguration, settingsRepository)

    @SingleIn(AppScope::class)
    @Provides
    fun provideDatosBiziApi(httpClient: HttpClient): DatadosBiziApi =
        com.gcaguilar.biciradar.core.DatadosBiziApiImpl(httpClient)

    @SingleIn(AppScope::class)
    @Provides
    fun provideGooglePlacesApi(httpClient: HttpClient): GooglePlacesApi =
        GooglePlacesApiImpl(httpClient)
}