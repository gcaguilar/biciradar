# Plan de Mejoras SOLID para BiciRadar

## Resumen

Este documento describe los cambios necesarios para mejorar la adherencia del proyecto BiciRadar a los principios SOLID. Los cambios están priorizados por impacto y complejidad.

---

## 🚨 Cambios Críticos (Alta Prioridad)

### 1. Eliminar Reflection en TripMonitorService (DIP)

**Problema:** `TripMonitorService` usa reflection para acceder a dependencias privadas, violando el principio de Inversión de Dependencias (DIP).

**Archivo:** `androidApp/src/main/kotlin/com/gcaguilar/biciradar/TripMonitorService.kt`

**Código problemático:**
```kotlin
// Líneas 50-58
favoritesRepository = SurfaceMonitoringRepositoryHolder.repository?.let { repo ->
    try {
        val field = repo.javaClass.getDeclaredField("favoritesRepository")
        field.isAccessible = true
        field.get(repo) as? FavoritesRepository
    } catch (e: Exception) { null }
}
```

**Solución implementada:**
1. Crear `TripMonitorServiceProvider.kt` - Un provider Parcelable que inyecta dependencias
2. Modificar `TripMonitorService` para recibir dependencias vía Intent extras
3. Actualizar `MainActivity` para crear el provider con las dependencias inyectadas
4. Actualizar `BiziMobileApp.kt` para pasar `FavoritesRepository` en el callback

**Archivos a modificar:**
- ✅ `TripMonitorServiceProvider.kt` (nuevo archivo)
- ✅ `TripMonitorService.kt`
- ✅ `MainActivity.kt`
- ✅ `BiziMobileApp.kt`

---

## 🟡 Cambios Importantes (Media Prioridad)

### 2. Crear Abstracciones para StationsRepository (SRP)

**Problema:** `StationsRepositoryImpl` tiene 403 líneas y maneja múltiples responsabilidades:
- Lógica de red
- Lógica de caché
- Manejo de estado
- Lógica de ubicación

**Archivo:** `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/StationsRepository.kt`

**Solución propuesta:**
Crear abstracciones dedicadas:

```kotlin
// StationsRemoteDataSource.kt
interface StationsRemoteDataSource {
    suspend fun fetchStations(origin: GeoPoint): List<Station>
    suspend fun fetchAvailability(stationIds: List<String>): Map<String, StationAvailability>
}

// StationsCacheManager.kt
interface StationsCacheManager {
    fun loadStations(cityId: String): List<StationEntity>?
    suspend fun save(cityId: String, stations: List<Station>)
    suspend fun updateAvailability(availability: Map<String, Pair<Int, Int>>, refreshedAt: Long)
    fun isFresh(cityId: String): Boolean
    val stationsFlow: Flow<List<StationEntity>>?
    val metadataFlow: Flow<CacheMetadata?>?
}
```

**Beneficios:**
- Cada clase tiene una única responsabilidad (SRP)
- Facilita testing con mocks
- Permite cambiar implementaciones sin modificar el repositorio principal (OCP)

---

### 3. Dividir AppRootViewModel (SRP)

**Problema:** `AppRootViewModel` tiene 557 líneas y maneja:
- Inicialización de la app
- Onboarding
- Changelog
- Actualizaciones de la app
- Reviews
- Feedback nudges

**Archivo:** `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/biciradar/mobileui/viewmodel/AppRootViewModel.kt`

**Solución propuesta:**
Crear ViewModels especializados:

```kotlin
// AppRootViewModel.kt - Solo coordinación e inicialización
class AppRootViewModel(
    private val startupUseCase: StartupUseCase,
    private val appInitializer: AppInitializer,
) : ViewModel()

// OnboardingViewModel.kt - Solo onboarding
class OnboardingViewModel(
    private val startupUseCase: StartupUseCase,
    private val resolveOnboardingPresentationUseCase: ResolveOnboardingPresentationUseCase,
) : ViewModel()

// EngagementViewModel.kt - Reviews, feedback, updates
class EngagementViewModel(
    private val appLifecycleUseCase: AppLifecycleUseCase,
    private val appVersion: String,
) : ViewModel()

// ChangelogViewModel.kt - Solo changelog
class ChangelogViewModel(
    private val appLifecycleUseCase: AppLifecycleUseCase,
) : ViewModel()
```

---

### 4. Extraer Componentes de FavoritesScreen.kt (SRP)

**Problema:** `FavoritesScreen.kt` tiene 1,021 líneas con múltiples componentes inline.

**Archivo:** `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/biciradar/mobileui/screens/FavoritesScreen.kt`

**Solución propuesta:**
Extraer componentes a archivos separados:

```
screens/
├── FavoritesScreen.kt (solo coordinación)
├── components/
│   ├── DismissibleFavoriteStationRow.kt
│   ├── SavedPlaceCard.kt
│   ├── SavedPlaceQuickAction.kt
│   ├── FavoritesOverviewCard.kt
│   └── SectionHeader.kt
```

---

## 🟢 Mejoras Adicionales (Baja Prioridad)

### 5. Segregar PlatformBindings (ISP)

**Problema:** `PlatformBindings` es una interfaz "dios" con 15+ dependencias.

**Archivo:** `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/PlatformContracts.kt`

**Solución propuesta:**
```kotlin
interface CorePlatformBindings {
    val locationProvider: LocationProvider
    val databaseFactory: DatabaseFactory?
    val localNotifier: LocalNotifier
}

interface ExperiencePlatformBindings {
    val reviewPrompter: ReviewPrompter
    val appUpdatePrompter: AppUpdatePrompter
    val feedbackPrompter: FeedbackPrompter
}

interface FeaturePlatformBindings {
    val watchSyncBridge: WatchSyncBridge?
    val assistantIntentResolver: AssistantIntentResolver?
}
```

---

### 6. Extraer NotificationBuilder (SRP)

**Problema:** `TripMonitorService` mezcla lógica de servicio con construcción de notificaciones.

**Solución propuesta:**
```kotlin
class MonitoringNotificationBuilder(
    private val context: Context,
) {
    fun buildNotification(session: SurfaceMonitoringSession?): Notification
    private fun createStopAction(): NotificationCompat.Action
    private fun createFavoriteAction(stationId: String, isFavorite: Boolean): NotificationCompat.Action
    private fun createShareAction(stationId: String, stationName: String): NotificationCompat.Action
}
```

---

### 7. Segregar SettingsRepository (ISP)

**Problema:** `SettingsRepository` tiene 21 métodos en una sola interfaz.

**Solución propuesta:**
```kotlin
interface UserPreferenceSettings {
    suspend fun setMapType(type: MapType)
    suspend fun setSearchRadius(radius: Int)
}

interface OnboardingSettings {
    suspend fun markOnboardingComplete()
    suspend fun isOnboardingComplete(): Boolean
}

interface EngagementSettings {
    suspend fun recordReviewRequest()
    suspend fun getLastReviewRequestDate(): Long?
}
```

---

## Métricas de Éxito

Después de implementar estas mejoras:

1. **Tamaño máximo de clase:** Ninguna clase >300 líneas
2. **Inyección de dependencias:** 100% de dependencias vía constructor
3. **Interfaces cohesivas:** Interfaces con <10 métodos
4. **Testabilidad:** Todas las clases testeables con mocks

---

## Priorización

| Prioridad | Tarea | Esfuerzo estimado | Impacto SOLID |
|-----------|-------|-------------------|---------------|
| 🔴 P0 | Eliminar reflection en TripMonitorService | 2-3h | DIP crítico |
| 🟡 P1 | Crear StationsCache abstraction | 3-4h | SRP moderado |
| 🟡 P1 | Dividir AppRootViewModel | 4-6h | SRP moderado |
| 🟡 P1 | Extraer FavoritesScreen components | 3-4h | SRP moderado |
| 🟢 P2 | Segregar PlatformBindings | 2-3h | ISP menor |
| 🟢 P2 | Extraer NotificationBuilder | 2h | SRP menor |
| 🟢 P2 | Segregar SettingsRepository | 3-4h | ISP menor |

---

## Notas de Implementación

### Cambio 1: TripMonitorService (Implementado)

**Nuevos archivos:**
- `androidApp/src/main/kotlin/com/gcaguilar/biciradar/TripMonitorServiceProvider.kt`

**Archivos modificados:**
- `androidApp/src/main/kotlin/com/gcaguilar/biciradar/TripMonitorService.kt`
- `androidApp/src/main/kotlin/com/gcaguilar/biciradar/MainActivity.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/biciradar/mobileui/BiziMobileApp.kt`

**Cambios clave:**
1. `TripMonitorServiceProvider` actúa como wrapper Parcelable para inyectar dependencias
2. `TripMonitorServiceDependenciesHolder` mantiene referencias temporales (no es ideal, pero elimina reflection)
3. El callback en `BiziMobileApp` ahora pasa `(SurfaceMonitoringRepository, FavoritesRepository)`

---

## Conclusión

El proyecto BiciRadar tiene buenas prácticas generales de arquitectura (Clean Architecture, MVVM, Repository Pattern), pero hay oportunidades claras de mejora SOLID, especialmente:

1. **SRP:** Varias clases tienen múltiples responsabilidades (AppRootViewModel, StationsRepository, FavoritesScreen)
2. **DIP:** El uso de reflection en TripMonitorService es una violación crítica que debe arreglarse
3. **ISP:** PlatformBindings es una interfaz "dios" que debería segregarse

Los cambios propuestos mejorarán significativamente la mantenibilidad, testabilidad y extensibilidad del código.
