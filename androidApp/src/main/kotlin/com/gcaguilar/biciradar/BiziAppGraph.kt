package com.gcaguilar.biciradar

import android.app.Application
import com.gcaguilar.biciradar.core.AppConfiguration
import com.gcaguilar.biciradar.core.SharedGraph
import com.gcaguilar.biciradar.core.platform.AndroidPlatformBindings
import com.gcaguilar.biciradar.mobileui.di.createMobileGraph

/**
 * Application singleton que mantiene el SharedGraph único para toda la app.
 *
 * Esto resuelve:
 * - Duplicación de instancias @SingleIn(AppScope)
 * - Problemas de DI en Services que se recrean
 * - Acceso consistente a dependencias desde cualquier componente
 */
object BiziAppGraph {
  private var _graph: SharedGraph? = null
  private var _platformBindings: AndroidPlatformBindings? = null

  /**
   * Inicializa el grafo de dependencias. Debe llamarse desde Application.onCreate()
   */
  fun initialize(application: Application) {
    if (_graph != null) return // Ya inicializado

    val configuration =
      AppConfiguration(
        backendBaseUrl = BuildConfig.BACKEND_BASE_URL,
        auth0Domain = BuildConfig.AUTH0_DOMAIN,
        auth0ClientId = BuildConfig.AUTH0_CLIENT_ID,
        auth0Audience = BuildConfig.AUTH0_AUDIENCE,
      )
    val bindings =
      AndroidPlatformBindings(
        context = application.applicationContext,
        appConfiguration = configuration,
      )
    if (configuration.auth0Configured) {
      bindings.authProviderOverride =
        AndroidAuth0Provider(
          context = application.applicationContext,
          configuration = configuration,
          activityProvider = { bindings.experienceActivity },
        )
    } else if (BuildConfig.DEBUG && configuration.backendConfigured) {
      // Local smoke testing against a backend with AUTH_DISABLED=true.
      bindings.authProviderOverride = com.gcaguilar.biciradar.core.auth.DevAuthProvider
    }
    _platformBindings = bindings
    _graph = createMobileGraph(bindings)
    bindings.onGraphCreated(graph)
  }

  /**
   * El grafo de dependencias compartido.
   * Lanza IllegalStateException si no se ha inicializado.
   */
  val graph: SharedGraph
    get() =
      _graph ?: throw IllegalStateException(
        "BiziAppGraph no inicializado. Llama a initialize() desde Application.onCreate()",
      )

  /**
   * Los platform bindings de Android.
   */
  val platformBindings: AndroidPlatformBindings
    get() =
      _platformBindings ?: throw IllegalStateException(
        "BiziAppGraph no inicializado. Llama a initialize() desde Application.onCreate()",
      )

  /**
   * Verifica si el grafo está inicializado.
   */
  fun isInitialized(): Boolean = _graph != null
}
