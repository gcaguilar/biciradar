# Android App Functions - BiciRadar

## Descripción

BiciRadar ahora expone funcionalidades mediante Android App Functions, permitiendo integración avanzada con Google Assistant, búsqueda del sistema y modelos de IA como Gemini.

## Functions Disponibles

### 1. findNearbyStation

Busca estaciones de bicicletas cercanas a la ubicación del usuario.

**Parámetros:**
- `preference` (enum): Tipo de estación preferida
  - `ANY`: Cualquier estación
  - `WITH_BIKES`: Solo con bicicletas disponibles
  - `WITH_SLOTS`: Solo con plazas libres
- `maxDistance` (int, opcional): Distancia máxima en metros

**Ejemplo:**
```kotlin
val params = FindNearbyStationParams(
    preference = StationPreference.WITH_BIKES,
    maxDistance = 1000
)
```

**Respuesta:** Lista de `StationResult` con información de cada estación.

### 2. getStationStatus

Obtiene el estado detallado de una estación específica.

**Parámetros:**
- `stationId` (string): ID de la estación
- `detailLevel` (enum): Nivel de detalle
  - `BASIC`: Información esencial
  - `FULL`: Información completa con UI

**Ejemplo:**
```kotlin
val params = GetStationStatusParams(
    stationId = "station_123",
    detailLevel = DetailLevel.FULL
)
```

**Respuesta:** `StationStatusResult` con datos de la estación.

### 3. getFavorites

Obtiene la lista de estaciones favoritas del usuario.

**Parámetros:** Ninguno

**Respuesta:** `FavoritesListResult` con lista de favoritos, estación home y work.

## Arquitectura

### Estructura de Directorios

```
appfunctions/
├── BiciRadarAppFunctionService.kt    # Servicio principal
├── functions/                         # Implementaciones
│   ├── FindNearbyStationFunction.kt
│   ├── GetStationStatusFunction.kt
│   └── GetFavoritesFunction.kt
├── parameters/                        # Parámetros tipados
│   ├── StationPreference.kt
│   ├── DetailLevel.kt
│   └── FindNearbyStationParams.kt
├── results/                           # Resultados tipados
│   ├── StationResult.kt
│   └── StationStatusResult.kt
└── mapping/                           # Mapeo con sistema existente
    └── AppFunctionMapper.kt
```

### Compatibilidad con Sistema Existente

Las App Functions se integran con el sistema de `AssistantAction` existente mediante `AppFunctionMapper`, permitiendo:
- Uso gradual de App Functions
- Compatibilidad con shortcuts XML actuales
- Migración suave sin breaking changes

## Testing

### Ejecutar Tests

```bash
./gradlew :androidApp:test
```

### Tests Implementados

- `FindNearbyStationFunctionTest`: Tests de filtrado, ordenamiento y favoritos
- `AppFunctionMapperTest`: Tests de mapeo bidireccional

## Uso con Assistant

### Ejemplos de Frases

- "Encuentra una estación de BiciRadar cerca"
- "Busca estaciones con bicis disponibles"
- "Cuántas bicis hay en la estación Plaza España"
- "Muéstrame mis estaciones favoritas"

## Configuración

### Dependencias

Agregar en `build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.androidx.appfunctions)
    implementation(libs.androidx.appfunctions.runtime)
}
```

### Permisos

El servicio requiere permiso `androidx.appfunctions.BIND_APP_FUNCTION_SERVICE`.

## Limitaciones

- Requiere Android 14+ para funcionalidad completa
- Runtime library necesaria para versiones anteriores
- API en alpha, puede cambiar

## Roadmap

### Completado ✅
- Setup de dependencias
- Estructura de directorios
- 3 functions core implementadas
- Mapeo con sistema existente
- Tests unitarios

### Pendiente 🚧
- StartTripFunction
- SetAlertFunction
- SearchStationFunction
- Integración con Gemini

## Referencias

- [Android App Functions Documentation](https://developer.android.com/jetpack/androidx/releases/appfunctions)
- [BiciRadar Assistant Shortcuts](shortcuts.md)
