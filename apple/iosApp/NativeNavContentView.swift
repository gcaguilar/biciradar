import BiziMobileUi
import SwiftUI
import UIKit

// MARK: - Native navigation chrome (Liquid Glass on iOS 26+)
//
// iOS renders the system tab bar; iOS 26 gives it the Liquid Glass material for free,
// because it is the stock `UITabBar` and not something we draw ourselves. There is no
// version branch anywhere in this file. Android is untouched: it keeps `BiziBottomBar` /
// `MobileNavigationRail` from `shared/mobile-ui` exactly as before.
//
// Architecture: exactly ONE Compose instance (`BiziMainViewControllerWrapper`) fills the
// screen and keeps owning ALL navigation, just like on Android. The native bar floats over
// it and only reports tab taps back into Compose. Two dead ends we already went down, kept
// here so nobody re-walks them:
//
//  1. One `MainViewControllerWrapper` per tab. That meant five independent `BiziMobileApp`
//     instances, each replaying the startup splash, the onboarding gate and the feedback
//     nudge — and since the tab's root `Screen` was never actually forwarded into Kotlin,
//     all five booted at `Screen.Nearby`. Switching tabs only moved the highlight while the
//     same "Cerca" screen re-appeared behind a splash.
//  2. A SwiftUI `TabView` as chrome, with `Color.clear` tab contents and Compose behind it.
//     A TabView insists on owning the whole screen and paints an opaque background behind
//     its content area, so Compose was completely covered — blank screen.
//
// The cost of using a bare `UITabBar` instead of a `TabView` is cosmetic: no floating pill
// shape and no `tabBarMinimizeBehavior` (hide-on-scroll). The glass material is there.

enum BiziTab: Hashable, CaseIterable {
    case nearby, map, favorites, trip, profile

    /// Top-level Compose destination this tab maps to.
    var screen: Screen {
        switch self {
        case .nearby: return Screen.Nearby()
        case .map: return Screen.Map()
        case .favorites: return Screen.Favorites()
        case .trip: return Screen.Trip(prefilledQuery: nil)
        case .profile: return Screen.Profile()
        }
    }

    var title: String {
        switch self {
        case .nearby: return "Cerca"
        case .map: return "Mapa"
        case .favorites: return "Favoritas"
        case .trip: return "Viaje"
        case .profile: return "Perfil"
        }
    }

    var systemImage: String {
        switch self {
        case .nearby: return "bicycle"
        case .map: return "map"
        case .favorites: return "heart"
        case .trip: return "arrow.triangle.turn.up.right.diamond"
        case .profile: return "slider.horizontal.3"
        }
    }

    static func from(screen: Screen) -> BiziTab? {
        switch screen {
        case is Screen.Nearby: return .nearby
        case is Screen.Map: return .map
        case is Screen.Favorites: return .favorites
        case is Screen.Trip: return .trip
        case is Screen.Profile: return .profile
        default: return nil
        }
    }
}

/// Owns the single Compose instance and keeps the native tab bar's selection in sync with
/// whatever Compose's NavController is currently showing.
@Observable
final class NativeShellModel {
    var selectedTab: BiziTab = .nearby

    /// Not `let` only because the `onActivate` closure needs `self`; assigned once in `init`.
    private(set) var wrapper: BiziMainViewControllerWrapper!

    init() {
        let factory: (any StationMapViewFactory)? = GoogleMapsBootstrap.isSdkLinked()
            ? GoogleMapsStationMapFactory()
            : nil
        wrapper = BiziMobileViewControllerKt.MainViewControllerWrapper(
            // Deep links arrive through `BiciRadarApp.applyPendingLaunchRequest()` →
            // `wrapper.updateLaunchRequest(...)`, same as before the native shell.
            launchRequest: nil,
            stationMapViewFactory: factory,
            remoteConfigBridge: FirebaseBootstrap.remoteConfigBridge,
            // Pushes stay inside Compose (same as Android) — nothing to forward.
            onNavigate: nil,
            // Compose tells us when the top-level destination changes on its own (deep
            // link, "ver en el mapa" from a detail screen, back to root, …) so the tab
            // bar highlight follows. Kotlin may call this off the main thread.
            onActivate: { [weak self] screen in
                guard let tab = BiziTab.from(screen: screen) else { return }
                DispatchQueue.main.async { self?.selectedTab = tab }
            },
            // Mismo grafo y MISMAS `IOSPlatformBindings` que usan los widgets/atajos/watch
            // sync (`BiziAppleGraph`) — ver `BiziSharedGraph` en BiziAppleGraph.swift. Antes
            // se dejaban ambos en `nil`, lo que hacía que Compose creara SU PROPIA copia de
            // FavoritesRepository/StationsRepository y SU PROPIO `IOSPlatformBindings`
            // (con su propio `IOSRouteLauncher` late-wired), desincronizando la app.
            graph: BiziSharedGraph.graph,
            platformBindings: BiziSharedGraph.platformBindings
        )
    }

    /// Called when the user taps a native tab: updates the bar immediately and asks
    /// Compose to run the very same NavController transaction `BiziBottomBar` uses.
    func select(_ tab: BiziTab) {
        selectedTab = tab
        wrapper.selectTab(tab: tab.screen)
    }
}

/// The one and only Compose host. Ignores safe areas because `BiziMobileApp` applies its
/// own window insets (that was already true before the native shell; letting SwiftUI
/// consume the top inset is what pushed the "Cerca" heading up into the status bar).
struct ComposeShellView: UIViewControllerRepresentable {
    let wrapper: BiziMainViewControllerWrapper

    func makeUIViewController(context: Context) -> UIViewController {
        wrapper.viewController
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

/// Single, lazily created shell shared by the view and by `BiciRadarApp` (which needs the
/// same wrapper to deliver deep links, foreground refreshes and the final background
/// check). `static let` is lazy in Swift, so the Compose view controller is not built until
/// the first SwiftUI body runs — i.e. after `BiciRadarApp.init()` has configured Firebase
/// and Google Maps.
@MainActor
enum BiziComposeShell {
    static let model = NativeShellModel()
}

@MainActor
struct NativeNavContentView: View {
    private var model: NativeShellModel { BiziComposeShell.model }

    var body: some View {
        ZStack(alignment: .bottom) {
            ComposeShellView(wrapper: model.wrapper)
                .ignoresSafeArea()

            // A bare `UITabBar` rather than a SwiftUI `TabView`: a TabView insists on
            // owning the whole screen and paints an opaque background behind its content
            // area, which covered the Compose layer underneath entirely (blank screen).
            // A standalone bar has no content area at all — it is only the chrome, laid
            // over Compose, and iOS 26 still renders it with the Liquid Glass material
            // because it is the stock system bar.
            NativeTabBar(
                tabs: BiziTab.allCases,
                selected: model.selectedTab,
                onSelect: { model.select($0) }
            )
            .frame(height: NativeTabBar.height)
        }
        .tint(Color(.accent))
    }
}

/// The system tab bar, hosted directly so it can float over the full-screen Compose view.
/// No `#available` checks: `UITabBar` is ancient, and iOS 26 upgrades its material to
/// Liquid Glass on its own. Older iOS just shows the familiar translucent blur.
struct NativeTabBar: UIViewRepresentable {
    /// `UITabBar`'s standard height, excluding the bottom safe-area inset. Kept in sync
    /// with `NativeTabBarHeight` in `BiziNavigationShell.kt`, which reserves room for it
    /// so bottom-anchored Compose UI never ends up underneath the bar.
    static let height: CGFloat = 49

    let tabs: [BiziTab]
    let selected: BiziTab
    let onSelect: (BiziTab) -> Void

    func makeUIView(context: Context) -> UITabBar {
        let bar = UITabBar()
        bar.delegate = context.coordinator
        bar.items = tabs.enumerated().map { index, tab in
            UITabBarItem(
                title: tab.title,
                image: UIImage(systemName: tab.systemImage),
                tag: index
            )
        }
        bar.selectedItem = bar.items?.first
        return bar
    }

    func updateUIView(_ bar: UITabBar, context: Context) {
        context.coordinator.tabs = tabs
        context.coordinator.onSelect = onSelect
        guard
            let items = bar.items,
            let index = tabs.firstIndex(of: selected),
            index < items.count
        else { return }
        // Only assign when it actually changed: reassigning the same item re-triggers the
        // bar's selection animation on every SwiftUI update.
        if bar.selectedItem !== items[index] {
            bar.selectedItem = items[index]
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(tabs: tabs, onSelect: onSelect)
    }

    final class Coordinator: NSObject, UITabBarDelegate {
        var tabs: [BiziTab]
        var onSelect: (BiziTab) -> Void

        init(tabs: [BiziTab], onSelect: @escaping (BiziTab) -> Void) {
            self.tabs = tabs
            self.onSelect = onSelect
        }

        func tabBar(_ tabBar: UITabBar, didSelect item: UITabBarItem) {
            guard item.tag >= 0, item.tag < tabs.count else { return }
            onSelect(tabs[item.tag])
        }
    }
}
