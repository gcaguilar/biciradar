# Recomendaciones de Arquitectura Metro - Futuras Mejoras

Este documento recoge recomendaciones de mejora para la arquitectura DI de BiciRadar,
basadas en las mejores prácticas de Metro y los patterns observados en la codebase actual.

---

## 1. Graph Extensions (@GraphExtension)

### Contexto
Actualmente usamos un único `@DependencyGraph(AppScope::class)` para toda la aplicación.
A medida que la app crece en funcionalidades, esto puede volverse difícil de mantener.

### Recomendación
Usar `@GraphExtension` para crear grafos especializados que extienden el grafo padre:

```kotlin
// Grafo base de la aplicación
@DependencyGraph(AppScope::class)
interface AppGraph {
    // Accesor a la factory del grafo extendido
    val loggedInGraphFactory: LoggedInGraph.Factory
}

// Grafo extendido para usuario autenticado
@GraphExtension(LoggedInScope::class)
interface LoggedInGraph {
    val userRepository: UserRepository
    val userSettings: UserSettingsRepository
    
    @GraphExtension.Factory
    interface Factory {
        fun create(@Provides userId: String): LoggedInGraph
    }
}
```

### Beneficios
- **Encapsulación**: Lógica de autenticación aislada en su propio grafo
- **Ciclo de vida**: El grafo extendido puede crearse/destruirse independientemente
- **Testing**: Permite testear flujos autenticados sin el grafo completo
- **Compilación incremental**: Cambios en un grafo no recompilan el otro

### Cuándo implementar
- Cuando se añada autenticación de usuario
- Cuando se creen flujos de onboarding complejos
- Cuando se añadan features premium/pagadas

---

## 2. Binding Containers

### Contexto
Actualmente todas las dependencias se declaran directamente en `SharedGraph.kt` o
mediante `@ContributesBinding`. Esto distribuye la configuración DI por todo el proyecto.

### Recomendación
Organizar dependencias en `@BindingContainer` por capa funcional:

```kotlin
// NetworkBindings.kt - Capa de red
@BindingContainer
object NetworkBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideHttpClient(factory: BiziHttpClientFactory, json: Json): HttpClient =
        factory.create(json)
    
    @SingleIn(AppScope::class)
    @Provides
    fun provideBiziApi(...): BiziApi = ...
    
    @SingleIn(AppScope::class)
    @Provides
    fun provideGooglePlacesApi(...): GooglePlacesApi = ...
}

// DatabaseBindings.kt - Capa de persistencia
@BindingContainer
object DatabaseBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideDatabase(...): BiciRadarDatabase? = ...
    
    @SingleIn(AppScope::class)
    @Provides
    fun provideCacheManager(...): StationsCacheManager = ...
}

// GeoBindings.kt - Capa de geolocalización
@BindingContainer(includes = [NetworkBindings::class])  // Puede incluir otros
object GeoBindings {
    @SingleIn(AppScope::class)
    @Provides
    fun provideGeoApi(...): GeoApi = ...
    
    @SingleIn(AppScope::class)
    @Provides
    fun provideGeoSearchUseCase(...): GeoSearchUseCase = ...
}

// Uso en el grafo principal
@DependencyGraph(
    AppScope::class,
    bindingContainers = [NetworkBindings::class, DatabaseBindings::class, GeoBindings::class]
)
interface SharedGraph
```

### Beneficios
- **Organización**: Dependencias agrupadas por capa funcional
- **Reutilización**: Los containers pueden incluirse en múltiples grafos
- **Testabilidad**: Fácil reemplazar un container completo en tests
- **Legibilidad**: SharedGraph.kt se reduce a solo accessors públicos

### Alternativa: @ContributesTo
También pueden contribuir automáticamente:

```kotlin
@ContributesTo(AppScope::class)
@BindingContainer
object NetworkBindings { ... }

// No necesita declararse en SharedGraph
@DependencyGraph(AppScope::class)
interface SharedGraph  // NetworkBindings se incluye automáticamente
```

### Cuándo implementar
- Cuando SharedGraph.kt supere las 200 líneas
- Cuando se añadan tests de integración por capa
- Cuando se necesiten variantes de grafos (debug, release, staging)

---

## 3. Eliminar onGraphCreated() en iOS

### Contexto
Actualmente `IOSPlatformBindings` usa late wiring para inyectar `SettingsRepository`
en `IOSRouteLauncher`:

```kotlin
class IOSPlatformBindings : PlatformBindings {
    private val iosRouteLauncher = IOSRouteLauncher()  // Creado ANTES del grafo
    
    override fun onGraphCreated(graph: SharedGraph) {
        iosRouteLauncher.settingsRepository = graph.settingsRepository  // Late wiring
    }
}
```

Esto es un anti-pattern en DI puro porque:
1. `IOSRouteLauncher` se crea sin sus dependencias
2. Requiere mutabilidad (var en lugar de val)
3. Rompe la garantía de inmutabilidad de Metro

### Recomendación
Mover `IOSRouteLauncher` al grafo y recibirlo vía constructor:

```kotlin
// IOSRouteLauncher.kt - Ahora parte del grafo
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class, binding = binding<RouteLauncher>())
@Inject
class IOSRouteLauncher(
    private val settingsRepository: SettingsRepository,  // Inyectado vía constructor
    private val mapSupport: MapSupport,
) : RouteLauncher { ... }

// IOSPlatformBindings.kt - Solo provee bindings de plataforma
class IOSPlatformBindings @Inject constructor(
    private val routeLauncher: RouteLauncher,  // Inyectado del grafo
) : PlatformBindings {
    // Ya no necesita onGraphCreated()!
    override fun onGraphCreated(graph: SharedGraph) { }
}
```

### Cambios necesarios
1. **IOSRouteLauncher**: Añadir `@Inject` y recibir dependencias vía constructor
2. **PlatformBindings**: Convertir de clase abstracta a interface pura o recibir bindings inyectados
3. **SharedGraph**: Eliminar accessor `routeLauncher` (ya es @ContributesBinding)
4. **BiziAppleGraph**: Eliminar llamada a `onGraphCreated()` (ya no es necesaria)

### Desafíos
- `PlatformBindings` se crea ANTES del grafo para pasarse a la factory
- Necesitamos un approach donde PlatformBindings sea un @Includes que recibe bindings del grafo
- Posible solución: Dos niveles de PlatformBindings (core y específicos)

### Opción A: PlatformBindings como @Includes con binding

```kotlin
// CorePlatformBindings.kt - Lo que el grafo necesita de la plataforma
interface CorePlatformBindings {
    val locationProvider: LocationProvider
    val localNotifier: LocalNotifier
    val mapSupport: MapSupport
    // ... (sin RouteLauncher)
}

// IOSPlatformBindings.kt - Lo que la plataforma provee
@BindingContainer
class IOSPlatformBindings @Inject constructor(
    override val locationProvider: LocationProvider,
    override val localNotifier: LocalNotifier,
    override val mapSupport: MapSupport,
) : CorePlatformBindings

// SharedGraph.kt
@DependencyGraph(AppScope::class)
interface SharedGraph {
    // RouteLauncher ahora es @ContributesBinding, no viene de PlatformBindings
    val routeLauncher: RouteLauncher  // IOSRouteLauncher inyectado
}
```

### Opción B: Provider<RouteLauncher>

```kotlin
// IOSPlatformBindings.kt - Recibe lazy
class IOSPlatformBindings @Inject constructor(
    private val routeLauncherProvider: Provider<RouteLauncher>,  // Lazy
) : PlatformBindings {
    
    override val routeLauncher: RouteLauncher
        get() = routeLauncherProvider.get()  // Obtiene del grafo cuando se necesita
}
```

### Beneficios
- **Immutabilidad**: Todos los campos son `val` en lugar de `var`
- **Testabilidad**: Fácil mockear RouteLauncher en tests
- **Seguridad**: No hay riesgo de NullPointerException por late initialization
- **Metro puro**: Sigue el principio de inyección por constructor

### Cuándo implementar
- Cuando se refactorice PlatformBindings para KMP (Kotlin Multiplatform)
- Cuando se añadan tests unitarios exhaustivos de iOS
- Cuando se migre completamente a Metro sin código legacy

---

## Prioridad de Implementación

1. **Binding Containers** (Media) - Mejora organización inmediata
2. **Graph Extensions** (Baja) - Solo cuando se añadan features grandes
3. **Eliminar onGraphCreated()** (Baja) - Requiere refactor mayor, hacerlo con calma

## Referencias

- [Metro Dependency Graphs](https://zacsweers.github.io/metro/latest/dependency-graphs/)
- [Metro Graph Extensions](https://zacsweers.github.io/metro/latest/dependency-graphs/#graph-extensions)
- [Metro Binding Containers](https://zacsweers.github.io/metro/latest/dependency-graphs/#binding-containers)
- [Metro Scopes](https://zacsweers.github.io/metro/latest/scopes/)