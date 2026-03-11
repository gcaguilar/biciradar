# Release Checklist

## Android y Wear OS

- Definir `GOOGLE_MAPS_API_KEY` si quieres mapa Android con tiles reales.
- Definir `BIZI_GEMINI_PROXY_BASE_URL` solo si vas a publicar también el proxy opcional de Gemini.
- Configurar firma release en `androidApp` y `wearApp`.
- Generar builds:

```bash
./gradlew :androidApp:assembleRelease :wearApp:assembleRelease
```

## iOS y watchOS

- Abrir [apple/BiziZaragoza.xcodeproj](/Users/guillermo.castella/bizi/apple/BiziZaragoza.xcodeproj).
- Configurar `Team`, firma y provisioning de `BiziZaragoza`, `BiziZaragozaWatch` y sus tests.
- Si quieres Gemini en Apple, sobreescribir `geminiProxyBaseUrl` en los bridges Swift/KMP antes de archivar.
- Generar archivo:

```bash
xcodebuild -project apple/BiziZaragoza.xcodeproj -scheme BiziZaragoza -configuration Release archive
xcodebuild -project apple/BiziZaragoza.xcodeproj -scheme BiziZaragozaWatch -configuration Release archive
```

## Backend opcional

- Exportar `GEMINI_API_KEY`.
- Arrancar o desplegar `backend/gemini-proxy`.
- Verificar `GET /health`.

## QA mínima

- Ubicación real en Android, iPhone, Wear OS y Apple Watch.
- Favoritos sincronizados entre móvil y reloj.
- Siri/App Shortcuts en Apple.
- Android shortcuts y búsqueda de estación por nombre o número.
- Ruta nativa a una estación.
- Validar fallback del radio configurable cuando no haya estaciones dentro del umbral.
