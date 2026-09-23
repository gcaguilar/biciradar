package com.gcaguilar.biciradar.core.di

import com.gcaguilar.biciradar.core.BiziApi
import com.gcaguilar.biciradar.core.BiziHttpClientFactory
import com.gcaguilar.biciradar.core.CityBikesBiziApi
import com.gcaguilar.biciradar.core.CityRegistry
import com.gcaguilar.biciradar.core.DatosBiziApi
import com.gcaguilar.biciradar.core.DatosBiziApiImpl
import com.gcaguilar.biciradar.core.GbfsBiziApi
import com.gcaguilar.biciradar.core.GooglePlacesApi
import com.gcaguilar.biciradar.core.GooglePlacesApiImpl
import com.gcaguilar.biciradar.core.Logger
import com.gcaguilar.biciradar.core.RoutingBiziApi
import com.gcaguilar.biciradar.core.SettingsRepository
import com.gcaguilar.biciradar.core.auth.AuthProvider
import com.gcaguilar.biciradar.core.auth.AuthTokenProvider
import com.gcaguilar.biciradar.core.auth.asTokenProvider
import com.gcaguilar.biciradar.core.backend.BackendConfig
import com.gcaguilar.biciradar.core.backend.BiciRadarApiClient
import com.gcaguilar.biciradar.core.backend.RoutineBackendApi
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
    cityRegistry: CityRegistry,
    logger: Logger,
  ): BiziApi =
    RoutingBiziApi(
      settingsRepository = settingsRepository,
      cityRegistry = cityRegistry,
      gbfsBiziApi = GbfsBiziApi(httpClient, appConfiguration, settingsRepository, logger),
      cityBikesBiziApi = CityBikesBiziApi(httpClient, appConfiguration),
    )

  @SingleIn(AppScope::class)
  @Provides
  fun provideDatosBiziApi(httpClient: HttpClient): DatosBiziApi = DatosBiziApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideGooglePlacesApi(httpClient: HttpClient): GooglePlacesApi = GooglePlacesApiImpl(httpClient)

  @SingleIn(AppScope::class)
  @Provides
  fun provideBackendConfig(appConfiguration: com.gcaguilar.biciradar.core.AppConfiguration): BackendConfig =
    BackendConfig(appConfiguration.backendBaseUrl)

  @SingleIn(AppScope::class)
  @Provides
  fun provideAuthTokenProvider(authProvider: AuthProvider): AuthTokenProvider = authProvider.asTokenProvider()

  @SingleIn(AppScope::class)
  @Provides
  fun provideBiciRadarApiClient(
    httpClient: HttpClient,
    json: Json,
    backendConfig: BackendConfig,
    authTokenProvider: AuthTokenProvider,
    logger: Logger,
  ): BiciRadarApiClient =
    BiciRadarApiClient(
      httpClient = httpClient,
      json = json,
      config = backendConfig,
      tokenProvider = authTokenProvider,
      logger = logger,
    )

  @SingleIn(AppScope::class)
  @Provides
  fun provideRoutineBackendApi(client: BiciRadarApiClient): RoutineBackendApi = client
}
