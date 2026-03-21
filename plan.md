# Localization Migration Plan

## Current state

- Shared KMP now has typed keys started in `SharedString`.
- Apple CI is temporarily unblocked with Spanish fallback in `appleMain` shared localization.
- Android metadata strings remain in platform `strings.xml`.
- Apple native strings still use `.strings` files and project localization wiring needs more cleanup.

## Next steps

1. Continue migrating `shared/mobile-ui` away from `localizedText("literal")` to `SharedString` keys or Compose resources.
2. Expand typed keys in `shared/core/src/commonMain/kotlin/com/gcaguilar/biciradar/core/Localization.kt` by screen/feature.
3. Update `BiziMobileApp.kt` in chunks:
   - map screen
   - favorites screen
   - settings screen
   - station detail
   - shortcuts/help screens
4. Keep `DefaultAssistantIntentResolver` and tests aligned with typed keys only.
5. Introduce real Compose string resources for shared UI once the key inventory is stable.
6. Revisit Apple native localization:
   - verify `apple/project.yml`
   - regenerate project if needed
   - keep `.lproj` files or migrate to `.xcstrings`
7. After migration chunks, run:
   - `./gradlew --no-daemon :shared:core:jvmTest`
   - `./gradlew --no-daemon :androidApp:assembleDebug`
8. Then re-run GitHub Actions and fix remaining Apple-native build issues.

## Important constraints

- Do not touch unrelated local changes like `androidApp/src/androidMain/AndroidManifest.xml` unless required.
- Keep commits atomic by migration area.
- Prefer stabilizing CI first, then improving localization architecture.
