# Plan de Primera Release iOS a App Store

## Objetivo

- Publicar la primera version iOS de BiciRadar en la App Store.
- El flujo debe ser: GitHub Actions -> build firmada -> subida a App Store Connect -> envio a revision -> publicacion automatica tras aprobacion de Apple.
- No usar TestFlight como canal operativo de distribucion para esta primera release.

## Estado actual del repositorio

- Ya existe build y test de iOS en `.github/workflows/build.yml`.
- El repo ya puede generar una IPA firmada cuando estan configurados los secretos Apple.
- La firma se apoya en `scripts/install_apple_signing_assets.sh`.
- La exportacion de la IPA se apoya en `scripts/export_ios_ipa.sh`.
- El bundle id principal actual es `com.gcaguilar.biciradar.ios` en `apple/project.yml`.
- El repo no tiene todavia un workflow dedicado a publicar en App Store Connect.
- El repo no tiene todavia configuracion de `fastlane`.
- La documentacion actual de release esta mas orientada a builds internas que a publicacion productiva en la store.

## Decisiones para la primera release

- Usar un workflow dedicado, separado del workflow general de build.
- Disparar la primera release solo por `workflow_dispatch` para reducir riesgo.
- Usar `fastlane upload_to_app_store` para automatizar subida, envio a revision y publicacion automatica.
- Mantener metadata, pricing, disponibilidad, privacy labels y capturas de App Store Connect gestionadas manualmente fuera del repo en esta primera release.
- Reutilizar los scripts y secretos de firma ya existentes en el repositorio.

## Alcance

### En alcance

- Crear workflow de publicacion iOS a App Store.
- Crear configuracion minima de `fastlane`.
- Exportar IPA con metodo `app-store`.
- Subir la IPA a App Store Connect.
- Enviar automaticamente la version a revision.
- Publicar automaticamente cuando Apple apruebe la release.

### Fuera de alcance en esta primera iteracion

- Gestionar metadata completa desde `fastlane/metadata`.
- Gestionar capturas desde el repositorio.
- Automatizar cambios de descripcion, keywords, categorias o pricing desde CI.
- Automatizar una estrategia de phased release.

## Prerrequisitos fuera del repo

### App Store Connect

- La ficha de la app debe existir para `com.gcaguilar.biciradar.ios`.
- Deben estar aceptados los agreements de Apple.
- Deben estar completos impuestos, banca y datos legales del equipo.
- Deben estar configurados nombre, descripcion, subtitulo, categoria y URLs necesarias.
- Deben estar cargadas las capturas requeridas para iPhone y, si aplica, iPad.
- Debe estar completado el age rating.
- Deben estar completadas las privacy labels.
- Debe estar definida la informacion de App Review.
- Debe estar resuelta la declaracion de export compliance.
- Debe estar definido el precio y la disponibilidad por pais.

### Apple Developer

- Debe existir certificado `Apple Distribution`.
- Debe existir provisioning profile de App Store para `com.gcaguilar.biciradar.ios`.
- Debe estar confirmado el Team ID correcto.
- Debe verificarse si la app de watch va incluida correctamente en la release iOS o requiere ajustes previos.

## Secretos y variables requeridos en GitHub

- En un repo publico, conviene guardar los secretos del workflow en un `Environment` protegido, por ejemplo `app-store`.

### Secretos Apple ya reutilizables

- `APPLE_TEAM_ID`
- `APPLE_SIGNING_CERTIFICATE_P12_BASE64`
- `APPLE_SIGNING_CERTIFICATE_PASSWORD`
- `APPLE_PROVISIONING_PROFILE_BASE64`
- `APPLE_KEYCHAIN_PASSWORD` opcional

### Secretos de App Store Connect a anadir

- `APP_STORE_CONNECT_ISSUER_ID`
- `APP_STORE_CONNECT_KEY_ID`
- `APP_STORE_CONNECT_API_KEY_P8`

### Secretos de build iOS a mantener

- `GOOGLE_MAPS_KEY`
- `GOOGLE_SERVICE_INFO_PLIST_IOS` opcional pero recomendado si la release usa Firebase en iOS

### Variables de repositorio

- `APPLE_EXPORT_METHOD=app-store`
- `APPLE_SIGNING_CERTIFICATE_TYPE=Apple Distribution`
- `APPLE_BUNDLE_ID=com.gcaguilar.biciradar.ios`

### Secretos de submission a definir

- `APP_REVIEW_CONTACT_FIRST_NAME`
- `APP_REVIEW_CONTACT_LAST_NAME`
- `APP_REVIEW_CONTACT_EMAIL`
- `APP_REVIEW_CONTACT_PHONE`
- `APP_REVIEW_NOTES` opcional

### Variables de submission a definir

- `APP_USES_ENCRYPTION`

## Cambios a implementar en el repositorio

- Crear `.github/workflows/publish-ios-store.yml`.
- Crear `Gemfile` para fijar `fastlane`.
- Crear `fastlane/Fastfile`.
- Crear opcionalmente `fastlane/Appfile` si hace falta fijar app identifier o team.
- Anadir un script local para imprimir valores de CI listos para copiar y pegar en GitHub.

## Diseno del workflow de release

### Trigger

- `workflow_dispatch` manual para la primera release.
- En una segunda iteracion se puede valorar disparo por tag `v*`.

### Pasos del workflow

1. Checkout del repositorio.
2. Configurar Java 21.
3. Configurar Android SDK porque el proyecto KMP lo usa en la build compartida actual.
4. Configurar Gradle.
5. Hacer ejecutables `gradlew` y scripts.
6. Escribir `apple/Config/LocalSecrets.xcconfig` y `apple/iosApp/GoogleService-Info.plist`.
7. Ejecutar tests iOS antes de archivar.
8. Instalar certificado y provisioning profile en el runner macOS.
9. Exportar la IPA con `APPLE_EXPORT_METHOD=app-store`.
10. Guardar la IPA como artifact de respaldo.
11. Configurar Ruby y Bundler.
12. Ejecutar `fastlane` para subir la IPA a App Store Connect.
13. Enviar la version a revision.
14. Dejar activada la publicacion automatica tras aprobacion.

## Comportamiento esperado de fastlane

- Usar autenticacion con App Store Connect API key.
- Reusar la IPA generada por el script de exportacion existente.
- Subir binario sin gestionar metadata ni capturas desde el repo.
- Ejecutar `submit_for_review: true`.
- Ejecutar `automatic_release: true`.
- Ejecutar `force: true` para evitar pasos interactivos.
- Mantener `run_precheck_before_submit: true`.
- Rellenar `submission_information` con los datos reales de compliance.

## Configuracion recomendada de la lane

- `skip_metadata: true`
- `skip_screenshots: true`
- `submit_for_review: true`
- `automatic_release: true`
- `force: true`
- `run_precheck_before_submit: true`
- `app_identifier: ENV["APPLE_BUNDLE_ID"]`
- `ipa: ENV["APPLE_IPA_PATH"]`

## Plan de versionado para la primera release

- Mantener como fuente de verdad `MARKETING_VERSION` y `CURRENT_PROJECT_VERSION` en `apple/project.yml`.
- Hacer el bump de version antes de lanzar la release, no durante el workflow.
- Usar el script existente `scripts/bump_version.sh` solo como herramienta previa al commit de release.
- Verificar que el proyecto generado y `apple/project.yml` no tengan deriva antes de la publicacion.

## Riesgos y checks obligatorios

### Riesgo 1 - Metadata incompleta en App Store Connect

- Si la ficha no esta completa, la subida puede funcionar pero el envio a revision fallara.

### Riesgo 2 - Export compliance incorrecto

- No hay que hardcodear un valor sin validar la realidad de la app y lo configurado en Apple.
- La lane debe leer este dato desde variables o dejarlo claramente parametrizado.

### Riesgo 3 - Watch app no embebida correctamente

- Hay que verificar si `BiciRadarWatch` forma parte real del paquete iOS que se va a publicar.
- Si no esta bien conectada al target principal, resolverlo antes de la release.

### Riesgo 4 - Firma incorrecta para App Store

- El certificado debe ser `Apple Distribution`.
- El provisioning profile debe ser de tipo App Store.
- `APPLE_EXPORT_METHOD` debe ser `app-store`.

### Riesgo 5 - Drift entre proyecto fuente y proyecto generado

- Validar `apple/project.yml` frente a `apple/BiciRadar.xcodeproj` antes de la primera release.
- Si hay diferencias de version o configuracion, regenerar el proyecto o alinear ambos lados antes de publicar.

## Checklist operativo de la primera release

### Antes de ejecutar el workflow

- Confirmar que la ficha de App Store Connect esta completa.
- Confirmar que el bundle id es el correcto.
- Confirmar que el perfil de App Store y el certificado de distribucion son validos.
- Confirmar que la version y build number son los deseados.
- Confirmar que la build de `main` esta estable.
- Confirmar smoke test manual basico en iPhone real.

### Durante el workflow

- Ver que los tests pasan.
- Ver que la IPA se exporta con firma de App Store.
- Ver que `fastlane` sube el binario sin errores.
- Ver que App Store Connect crea o asocia el build correcto.
- Ver que la submission entra en estado de revision.

### Despues del workflow

- Verificar en App Store Connect que la version esta `Waiting For Review` o `In Review`.
- Esperar aprobacion de Apple.
- Verificar que la release pasa a produccion automaticamente.
- Validar instalacion desde la store y smoke test posterior a publicacion.

## Orden recomendado de implementacion

1. Verificar prerrequisitos de App Store Connect y Apple Developer.
2. Validar inclusion de watch app en la release iOS.
3. Anadir `Gemfile` y `fastlane/Fastfile`.
4. Crear workflow `publish-ios-store.yml`.
5. Ejecutar una prueba controlada por `workflow_dispatch`.
6. Corregir fallos de signing, submission o compliance.
7. Repetir la ejecucion final para la primera release productiva.

## Criterio de exito

- El workflow genera una IPA valida de App Store.
- La IPA se sube correctamente a App Store Connect.
- La version se envia a revision desde CI.
- La version se publica automaticamente al ser aprobada.
- La app aparece disponible en la App Store sin intervencion manual adicional en el momento de la aprobacion.
