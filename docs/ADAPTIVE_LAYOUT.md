# Adaptive layout

BiciRadar adapts its Compose UI to the width of the window and, on foldables, to the
device posture. All of it lives in `shared/mobile-ui` (`commonMain`), so Android and the
Compose-hosted parts of iOS use the same rules. iOS keeps its native tab chrome; only the
Compose content inside the tabs follows these rules.

## Window information

`layout/BiziWindowInfo.kt` is the single source of truth:

- `BiziWindowLayout`: `Compact`, `Medium` (>= 600dp wide) and `Expanded` (>= 840dp wide).
  A window shorter than 480dp stays `Compact`, so landscape phones keep the bottom bar.
- `BiziHinge`: separating hinge reported by the device, in dp. Vertical hinges report their
  X extent; horizontal hinges their Y extent.
- `BiziWindowInfo`: layout class, `isTabletop`, and the separating hinge (if any).
- `LocalBiziWindowInfo`: the CompositionLocal every screen reads.
- `rememberBiziWindowInfo()`: built from `currentWindowAdaptiveInfo()` and provided once by
  `theme/ThemeProvider.kt`.

Do not query `WindowSizeClass` or `WindowAdaptiveInfo` from screens. Read
`LocalBiziWindowInfo` or use the helpers below so breakpoints stay consistent.

```kotlin
val windowInfo = LocalBiziWindowInfo.current
if (windowInfo.isWide) { /* rail layout */ }
```

## Navigation shell

`components/BiziNavigationShell.kt`:

- `Compact`: bottom bar (`BiziBottomBar`).
- `Medium`/`Expanded`: navigation rail (`MobileNavigationRail`) plus content.
- On foldables in book posture, the separating vertical hinge becomes a gutter between the
  rail and the content so the seam never cuts through either.
- The iOS native-chrome path (`useNativeChrome = true`) is unchanged.

## Content width

- `Modifier.responsivePageWidth()`: fills the screen up to 760dp (Medium) or 920dp
  (Expanded) and centers the content.
- `Modifier.responsiveContentWidth()`: same cap but only constrains width, for overlays that
  should not take the full height.

`theme/ThemeProvider.kt` re-exports `pageBackgroundColor` and `responsivePageWidth` for
callers that still import them from `theme`; both delegate to the implementations in
`BiziTheme.kt`.

## Adaptive lists

The Cerca (`NearbyScreen`) and Favoritos (`FavoritesScreen`) station lists use
`LazyVerticalGrid` with `GridCells.Adaptive(BiziStationGridMinCellWidth)` (280dp):

- phones stay at one column, so the layout is unchanged there;
- foldables and wide windows get two or more columns automatically;
- header, banners and empty states span the full line (`GridItemSpan(maxLineSpan)`).

## List-detail on wide windows

`navigation/StationListDetailHost.kt` wraps the Cerca and Favoritos destinations:

- `Compact`: tapping a station pushes `Screen.StationDetail` as before.
- `Medium`/`Expanded`: the station detail opens in a secondary pane (Material 3
  `ListDetailPaneScaffold`, two panes from Medium width). The list stays visible, system
  back returns to the list, and the detail top bar hides its back arrow.
- The scaffold directive is built with
  `calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth`, which also keeps panes clear of
  separating hinges and splits vertically in tabletop posture.

Deep links (`biciradar://station/{id}`), widgets, shortcuts, notifications and the station
search overlays keep opening the full-screen `Screen.StationDetail` route; those entry points
either have no list to return to or are themselves full-screen overlays.

## Sheets and dialogs

- `components/BiziBottomSheet.kt` is the only entry point for modal bottom sheets. It uses
  560dp max width on wide windows (640dp Material default otherwise) and stays anchored to
  the window bottom, which on tabletop foldables means the lower half.
- `AlertDialog` usages rely on the Material defaults (280dp..560dp wide, centered), which are
  already correct on wide windows.

## Validating changes

```bash
./gradlew :shared:mobile-ui:jvmTest
./gradlew :shared:mobile-ui:compileAndroidMain
./gradlew :androidApp:compilePlaystoreDebugKotlin :androidApp:compileFdroidDebugKotlin
./gradlew ktlintCheckAll
```

On a device or emulator, exercise: rotation, split-screen resize, fold/unfold on a foldable
AVD, tabletop posture, and opening a station from both Cerca and Favoritos in compact and
wide windows.
