# Plan de Refactorización - ViewModels y Navegación

## Estado Actual

### Completado ✅

1. **Investigación de librerías de navegación**
   - Recomendación: Jetpack Navigation Compose (oficial, bien mantenida)
   - Soporte completo para deep links

2. **Dependencias añadidas**
   - Añadido `navigation-compose` en `gradle/libs.versions.toml`
   - Añadido en `shared/mobile-ui/build.gradle.kts`

3. **ViewModels creados**
   - `TripViewModel.kt` - Para pantalla de viaje
   - `TripViewModelFactory.kt` - Factory para crear el ViewModel
   - `NearbyViewModel.kt` - Para pantalla "Cerca"
   - `ProfileViewModel.kt` - Para pantalla de perfil
   - `FavoritesViewModel.kt` - Para pantalla de favoritos

4. **Navegación**
   - Creado `navigation/Screen.kt` - Definición de rutas y deep links
   - Creado `navigation/BiziNavHost.kt` - Host de navegación base

5. **Bugfix**
   - Corregido debounce en TripScreen (faltaba verificar si el query cambió después del delay)

---

## Pendiente

### 1. Refactorizar TripScreen para usar TripViewModel
- Extraer la lógica del composable `TripScreen` al `TripViewModel`
- El ViewModel ya está creado pero falta conectarlo
- La lógica de autocompletado ya está en el ViewModel

### 2. Crear funciones de contenido público en BiziMobileApp
Para poder usar con navegación:
```kotlin
object BiziMobileApp {
  @Composable
  fun TripScreenContent(...) // Público para navegación
  @Composable
  fun NearbyScreenContent(...)
  // etc.
}
```

### 3. Integrar navegación en BiziMobileApp
- Reemplazar el sistema actual de tabs con NavHost
- Implementar deep links para cada pantalla
- Manejar navegación desde notificaciones

### 4. ViewModels restantes
- `MapViewModel` - Para pantalla de mapa
- factory functions para crear los ViewModels

---

## Archivos Modificados/Creados

### Nuevos archivos
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/viewmodel/TripViewModel.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/viewmodel/TripViewModelFactory.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/viewmodel/NearbyViewModel.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/viewmodel/ProfileViewModel.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/viewmodel/FavoritesViewModel.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/navigation/Screen.kt`
- `shared/mobile-ui/src/commonMain/kotlin/com/gcaguilar/bizizaragoza/mobileui/navigation/BiziNavHost.kt`

### Archivos con cambios menores
- `gradle/libs.versions.toml` - Añadido navigation-compose
- `shared/mobile-ui/build.gradle.kts` - Añadidas dependencias

### Archivos con bugfix
- `BiziMobileApp.kt` - Corregido debounce en TripScreen (línea ~2377)

---

## Deep Links Planificados

```
bizi://nearby           - PantallaCerca
bizi://map               - Pantalla Mapa
bizi://favorites         - Pantalla Favoritos
bizi://trip              - Pantalla Viaje
bizi://profile           - Pantalla Perfil
bizi://station/{id}       - Detalle de estación
bizi://trip?name=X&lat=Y&lng=Z - Viaje con destino
```

---

## Siguiente Paso Recomendado

Continuar con la refactorización de TripScreen:
1. Hacer públicas las funciones de contenido en BiziMobileApp
2. Conectar TripViewModel con TripScreen
3. Probar que funciona correctamente
4. Luego integrar navegación completa
