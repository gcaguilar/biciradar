# Adaptive layout

BiciRadar adapts its Compose UI to the width of the window and, on foldables, to the
device posture. All of it lives in `shared/mobile-ui` (`commonMain`), so Android and the
Compose-hosted parts of iOS use the same rules. iOS keeps its native tab chrome on Compact
windows; from Medium width on (iPad, iPhone Duo unfolded) the same Compose rail and
list-detail layouts that Android uses take over.

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
- The iOS native bar (`UITabBar`, Liquid Glass) is **Compact-only chrome**: a bottom bar is
  not a wide-window chrome, so from Medium width on it is removed from the SwiftUI hierarchy
  and the Compose rail takes over. `BiziMobileApp` computes that decision
  (`nativeTabBarVisible`) and reports it through `onNativeChromeVisibilityChanged`, which is
  the single source of truth for both layers — SwiftUI never measures the window itself, so
  the native bar and the Compose rail can never both appear (or both disappear).
- On iOS the rail gets its insets from `NavigationRailDefaults.windowInsets` (vertical +
  start), so it stays clear of the Dynamic Island and of a landscape sensor housing.

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

- `Compact`: tapping a station pushes `Screen.StationDetail` as before. Folding while the
  detail pane is showing opens that same station full screen instead of dropping the user
  back to the list (the selection is consumed, so back returns to the list).
- `Medium`/`Expanded`: the station detail opens in a secondary pane (Material 3
  `ListDetailPaneScaffold`, two panes from Medium width). The list stays visible, system
  back returns to the list, and the detail top bar hides its back arrow.
- The scaffold directive is built with
  `calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth`, which also keeps panes clear of
  separating hinges and splits vertically in tabletop posture.
- The selected station is remembered **above** the compact/wide branch. Folding an iPhone
  Duo (or resizing an iPad) recomposes the host with the other layout, and anything
  remembered inside a branch would be dropped: the detail pane used to come back empty.
  Now the last selection is replayed through `initialDestinationHistory`.

## iPhone Duo (and other iOS size changes)

The iPhone Duo is a book-style foldable with two iOS 27 displays:

| Display | Resolution | Points (scale 3x) | `BiziWindowLayout` |
| --- | --- | --- | --- |
| Inner (unfolded) | 1878x2670 @ 430 ppi, ~4:3 | 626x890 | `Medium` (rail + list-detail) |
| Outer (folded) | 1398x2034 @ 460 ppi | 466x678 | `Compact` (native tab bar) |

Both displays have a Dynamic Island, so the Live Activity already renders there with no
extra code. What the foldable changes is the *window*, and everything derives from
`LocalBiziWindowInfo`:

- Unfolding crosses the 600dp breakpoint, so navigation chrome, grids, sheets and the
  list-detail switch layouts live; the selected station survives it.
- Maps re-frame on resize instead of keeping the folded shape's framing: the Google Maps
  factory adjusts zoom by the viewport's short side and re-applies the camera when the
  bounds change; the MapKit fallback pins the requested radius to the short side of the
  region instead of using a square one.
- Widgets: `NearbyStationsWidget` supports `systemExtraLarge` (iPad / unfolded phone) with a
  two-column layout, and the station card's metric digits scale with the widget family.
- The Dynamic Island Live Activity uses `.leading`, `.trailing`, `.center` (station name),
  `.bottom` (status + alternative), plus `compactLeading`/`compactTrailing` and a numeric
  `minimal`.

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
./gradlew :androidApp:compilePlaystoreDebugKotlin
./gradlew ktlintCheckAll
```

On a device or emulator, exercise: rotation, split-screen resize, fold/unfold on a foldable
AVD, tabletop posture, and opening a station from both Cerca and Favoritos in compact and
wide windows. On iOS, do the same on an iPad or an iPhone Duo: folding/unfolding while a
station is open in the list-detail pane is the case that used to lose the selection, and
unfolding is what must bring up the navigation rail and hide the native bottom tab bar.
