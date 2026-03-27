## Objetivo

Implementar en BiciRadar un conjunto de superficies nativas orientadas a disponibilidad rápida de estaciones:

* **iOS**

  * Home Screen Widgets
  * Lock Screen Widgets
  * Live Activities
  * App Intents / Shortcuts
  * Integración con Apple Watch existente
* **Android**

  * Home Screen Widgets
  * Notificación persistente de monitorización
  * Atajos equivalentes si aplica
  * Integración con wearables solo si la arquitectura ya lo permite

La prioridad funcional debe centrarse en el caso de uso principal:

1. Ver el estado de una estación favorita sin abrir la app
2. Monitorizar una estación y recibir cambios en tiempo real o casi real
3. Ver una sugerencia rápida de estación alternativa cuando una estación no tenga bicis o huecos

---

# Requisitos funcionales comunes

## Modelo común

Crear o consolidar un modelo compartido para representar el estado de una estación:

* `stationId`
* `stationName`
* `cityId`
* `latitude`
* `longitude`
* `bikesAvailable`
* `docksAvailable`
* `isInstalled`
* `isRenting`
* `isReturning`
* `lastUpdated`
* `distanceFromUser` si existe contexto local
* `isFavorite`
* `monitoringStatus`
* `statusLevel`:

  * `good`
  * `low`
  * `empty`
  * `full`
  * `unavailable`

## Casos de uso comunes

Implementar estos casos de uso reutilizables:

* `GetFavoriteStations`
* `GetNearestStations`
* `GetStationStatus`
* `StartStationMonitoring`
* `StopStationMonitoring`
* `GetSuggestedAlternativeStation`
* `GetCachedStationSnapshot`
* `RefreshStationDataIfNeeded`

## Reglas de negocio

Definir reglas unificadas:

* Una estación está **vacía** si `bikesAvailable == 0`
* Una estación está **llena** si `docksAvailable == 0`
* Una estación está **low** si bicis o huecos están por debajo de un umbral configurable
* La sugerencia alternativa debe:

  * estar dentro de un radio configurable
  * priorizar disponibilidad
  * luego cercanía

## Caché local

El agente debe reutilizar la caché local ya existente y exponer lecturas rápidas para widgets y actividades.
Evitar llamadas de red directas desde la UI del widget si no son necesarias.

Debe existir una capa de lectura rápida con:

* snapshot actual
* fecha de última actualización
* fallback cuando no haya red

---

# Fase 1 — Preparación de arquitectura

## Tareas

1. Revisar la arquitectura actual y localizar:

   * capa de datos
   * capa de dominio
   * sistema de favoritos
   * sistema de monitorización
   * sistema de notificaciones locales
2. Crear una API interna estable para superficies externas:

   * widgets
   * live activities
   * shortcuts
3. Asegurar que el estado de estación puede serializarse fácilmente
4. Crear una capa de `Widget/Surface DTOs` desacoplada del dominio

## Entregables

* DTO de estación simplificado
* repositorio de snapshots compartido
* helpers de formateo:

  * texto corto
  * estado semafórico
  * fecha relativa

---

# iOS — Plan por bloques

## Fase 2 iOS — Widgets de Home Screen

### Objetivo

Mostrar información útil sin abrir la app.

## Widgets a implementar

### 1. Favorite Station Widget

Mostrar:

* nombre de la estación
* bicis disponibles
* huecos libres
* estado
* última actualización

Tamaños:

* Small
* Medium

### 2. Nearby Stations Widget

Mostrar:

* 2 o 3 estaciones cercanas
* distancia
* bicis disponibles o huecos

Tamaño:

* Medium
* opcional Large

### 3. Commute / Quick Access Widget

Opcional posterior:

* estación de salida favorita
* estación de destino favorita

## Tareas técnicas

* Crear extensión WidgetKit
* Definir `TimelineProvider`
* Leer snapshots desde almacenamiento compartido
* Configuración por usuario con App Intent si aplica
* Soportar estados:

  * loading
  * empty
  * no permission
  * stale data

## Requisitos UX

* No saturar de texto
* Priorizar números grandes
* Usar estado visual consistente
* Mostrar “Actualizado hace X min” solo si cabe

## Entregables

* widget small favorito
* widget medium favorito
* widget medium cercanas
* placeholders y previews

---

## Fase 3 iOS — Lock Screen Widgets

### Objetivo

Permitir consulta ultrarrápida desde pantalla bloqueada.

## Widgets a implementar

### 1. Bikes Count Widget

* icono bici
* número de bicis de estación favorita

### 2. Docks Count Widget

* icono dock/hueco
* número de huecos libres

### 3. Station Status Widget

* nombre corto
* estado:

  * Disponible
  * Sin bicis
  * Sin huecos

## Tareas técnicas

* Añadir accesorios Lock Screen en WidgetKit
* Diseñar versiones:

  * inline
  * circular
  * rectangular
* Optimizar texto corto
* Fallback si no hay estación favorita configurada

## Entregables

* 3 lock screen widgets
* mapeo de contenido compacto
* previews por estado

---

## Fase 4 iOS — Live Activities

### Objetivo

Representar una monitorización activa de estación.

## Live Activity principal

### Station Monitoring Activity

Mostrar:

* nombre estación
* bicis disponibles
* huecos libres
* estado de monitorización
* sugerencia alternativa si la estación queda vacía o llena

## Estados

* monitoring
* changed_to_empty
* changed_to_full
* alternative_available
* ended
* expired

## Dynamic Island

Diseñar:

* compact leading: icono + número bicis
* compact trailing: huecos
* minimal: estado resumido
* expanded:

  * nombre estación
  * bicis
  * huecos
  * sugerencia alternativa

## Tareas técnicas

* Crear ActivityAttributes
* Crear ContentState
* Mapear monitorización actual a una única Live Activity por estación
* Decidir si solo se permite una Live Activity activa al mismo tiempo
* Actualización mediante:

  * timers locales
  * refresh basado en monitorización existente
* Finalizar actividad al acabar el periodo de monitorización

## Reglas

* No iniciar una nueva activity si ya existe una equivalente para la misma estación
* Finalizar la activity si:

  * usuario detiene monitorización
  * tiempo expira
  * estación deja de estar disponible
  * app decide fallback por datos obsoletos

## Entregables

* Live Activity básica
* Dynamic Island compact/expanded
* update cycle
* finalización limpia

---

## Fase 5 iOS — App Intents / Siri Shortcuts

### Objetivo

Exponer acciones rápidas del sistema.

## Shortcuts a implementar

* “Abrir estación favorita”
* “Ver estaciones cercanas”
* “Monitorizar estación favorita”
* “Buscar bicis cerca”
* “Buscar huecos cerca”

## App Intents

Crear intents parametrizables:

* ciudad
* estación
* tipo:

  * bicis
  * huecos

## Entregables

* app intents registrados
* frases sugeridas para Siri
* deep links a pantallas concretas

---

## Fase 6 iOS — Integración con Apple Watch existente

### Objetivo

Reforzar consistencia con lo que ya existe.

## Tareas

* Revisar la app Watch actual
* Compartir los mismos DTOs
* Alinear nombres de estado y formatos
* Añadir acceso rápido desde Live Activity o deep link si aplica
* Evaluar complication si no existe:

  * bicis disponibles en favorita
  * estado semáforo

## Entregables

* checklist de paridad entre iPhone y Watch
* complication opcional si no está hecha

---

# Android — Plan por bloques

## Fase 2 Android — Home Screen Widgets

### Objetivo

Replicar el acceso rápido principal en Android.

## Widgets a implementar

### 1. Favorite Station Widget

Mostrar:

* nombre estación
* bicis
* huecos
* última actualización

Tamaños:

* 2x1
* 4x2 o adaptables

### 2. Nearby Stations Widget

Mostrar:

* top 3 cercanas
* distancia
* disponibilidad

### 3. Quick Action Widget

Botones:

* abrir mapa
* ver favoritas
* monitorizar favorita

## Tareas técnicas

* Crear App Widgets
* Leer desde caché local
* Añadir acciones clickables por item
* Gestionar actualización programada y manual
* Diseñar estado vacío y error

## Entregables

* widget favorito
* widget cercanas
* widget acciones rápidas opcional

---

## Fase 3 Android — Notificación persistente de monitorización

### Objetivo

Equivalente práctico a Live Activity.

## Notification Surface

Cuando se monitoriza una estación, mostrar una notificación ongoing con:

* nombre estación
* bicis
* huecos
* estado
* tiempo restante de monitorización
* acción “Detener”
* acción “Abrir mapa”

## Estados

* monitorizando
* sin bicis
* sin huecos
* alternativa sugerida
* finalizada

## Tareas técnicas

* Crear canal de notificaciones específico
* Implementar foreground-like monitoring solo si es necesario
* Si no es necesario, usar actualización periódica discreta con notificación ongoing
* Añadir acciones:

  * stop
  * open station
  * open alternative

## Entregables

* plantilla notificación ongoing
* flujo start/stop
* mensaje de alternativa

---

## Fase 4 Android — Shortcuts y accesos rápidos

### Objetivo

Dar acceso rápido desde launcher o sistema.

## Implementar

* shortcut dinámica:

  * estaciones cercanas
  * favorita
  * monitorizar favorita
* deep links internos

## Entregables

* dynamic shortcuts
* iconos y labels
* navegación interna resuelta

---

## Fase 5 Android — Wear OS o extensibilidad futura

### Solo si la base lo permite

No forzar esta fase si no existe infraestructura.

## Posibles acciones

* tile de estación favorita
* complication con bicis/huecos
* acceso rápido a estaciones cercanas

## Entregables

* evaluación técnica
* propuesta solo si coste es razonable

---

# Diseño de datos compartidos entre superficies

## Shared station snapshot

Crear una representación compacta lista para widgets:

* `id`
* `nameShort`
* `nameFull`
* `bikesAvailable`
* `docksAvailable`
* `statusTextShort`
* `statusLevel`
* `lastUpdatedEpoch`
* `distanceMeters`
* `isFavorite`
* `alternativeStationId`
* `alternativeStationName`
* `alternativeDistanceMeters`

## Shared surface state

* `hasLocationPermission`
* `hasNotificationPermission`
* `hasFavoriteStation`
* `isDataFresh`
* `lastSyncEpoch`
* `cityName`

---

# Deep links requeridos

El agente debe añadir o revisar deep links para que widgets, notificaciones y shortcuts abran pantallas concretas:

* `biciradar://home`
* `biciradar://map`
* `biciradar://station/{id}`
* `biciradar://favorites`
* `biciradar://monitor/{id}`
* `biciradar://city/{id}`

---

# Estados de error y fallback

El agente debe contemplar obligatoriamente:

* sin permiso de localización
* sin permiso de notificaciones
* sin favorita configurada
* datos stale
* sin conexión
* estación eliminada o no disponible
* ciudad sin datos actuales

Cada superficie debe tener mensaje corto de fallback.

Ejemplos:

* “Configura una estación favorita”
* “Abre la app para actualizar”
* “Sin permiso de ubicación”
* “Datos no disponibles”

---

# Telemetría local y depuración

Como la app no usa analytics, el agente no debe añadir tracking.
Sí puede añadir logs de depuración internos solo para desarrollo.

## Permitido

* logs de render de widget
* logs de refresh
* logs de actualización de Live Activity
* logs de errores de extensión

## No permitido

* tracking de uso
* eventos analíticos remotos
* perfilado de usuario

---

# Prioridades de implementación

## MVP recomendado

### iOS

1. Favorite Station Home Widget
2. Lock Screen bikes widget
3. Live Activity de monitorización
4. Shortcuts básicos

### Android

1. Favorite Station Widget
2. Notificación ongoing de monitorización
3. Shortcut dinámica a favorita

## Fase 2

### iOS

* Nearby Stations Widget
* Lock Screen docks widget
* alternativa en Live Activity

### Android

* Nearby Stations Widget
* quick actions widget

## Fase 3

* Commute widget
* wearables extra
* configuraciones avanzadas

---

# Criterios de aceptación

## Widgets

* Cargan desde caché local en menos de un tiempo razonable
* No requieren abrir la app para ver datos recientes
* Abren la pantalla correcta al pulsar
* Soportan estado vacío y error

## Live Activity / ongoing notification

* Se inicia desde la acción de monitorización
* Refleja el estado actual de estación
* Se actualiza durante el periodo de monitorización
* Finaliza correctamente
* Sugiere alternativa cuando toca

## Shortcuts

* Ejecutan navegación o acción real
* No abren pantallas inconsistentes
* Usan lenguaje claro y mantenible

---

# Restricciones para el agente

* No introducir nuevas dependencias pesadas salvo necesidad real
* Reutilizar la lógica de monitorización existente
* Reutilizar la caché local existente
* No duplicar lógica de negocio en widgets
* Centralizar umbrales y reglas de disponibilidad
* Mantener consistencia visual con la app actual
* No añadir analytics
* No enviar la ubicación fuera del dispositivo

---

# Entrega esperada por plataforma

## iOS

* Widget extension
* Live Activity / ActivityKit implementation
* App Intents
* deep links
* tests básicos de mapping y state rendering
* documentación breve de configuración

## Android

* AppWidget implementation
* monitoring notification
* deep links / shortcuts
* tests básicos de mapping y rendering state
* documentación breve de configuración

---

# Prompt breve para darle directamente al agente

Puedes pasarle esto tal cual:

> Implementa en BiciRadar superficies nativas por plataforma para acceso rápido al estado de estaciones:
>
> **iOS**
>
> * Home Screen Widget de estación favorita
> * Home Screen Widget de estaciones cercanas
> * Lock Screen Widgets para bicis y huecos
> * Live Activity para monitorización de estación con Dynamic Island
> * App Intents / Siri Shortcuts para abrir favorita, ver cercanas y monitorizar favorita
>
> **Android**
>
> * Widget de estación favorita
> * Widget de estaciones cercanas
> * Notificación persistente de monitorización como equivalente a Live Activity
> * Shortcuts dinámicos a favorita, cercanas y monitorización
>
> Reutiliza la caché local existente y la lógica actual de monitorización. No añadas analytics ni tracking. No envíes ubicación fuera del dispositivo. Implementa deep links a home, map, station, favorites y monitor. Cada superficie debe soportar estados vacíos, error, falta de permisos y datos stale. Prioriza un MVP con favorita + monitorización antes de expandir a cercanas y commute.
>
> Entrega código por fases, empezando por modelo compartido, snapshots reutilizables y luego implementación nativa por plataforma.
