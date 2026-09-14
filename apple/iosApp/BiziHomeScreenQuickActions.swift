import Foundation
import UIKit

// MARK: - Home Screen quick actions (iOS)
//
// iOS exposes two unrelated shortcut surfaces:
//
//  * `AppIntents` / `AppShortcutsProvider` (`BiziShortcuts.swift`) → Spotlight, Siri and the
//    Shortcuts app. The system decides whether (and which) of them reach the Home Screen menu.
//  * `UIApplicationShortcutItem` → the deterministic list shown when the user long-presses the
//    app icon. This is the iOS equivalent of the Android launcher shortcuts published from
//    `AndroidDynamicShortcuts.kt`, and it did not exist before this file.
//
// Items are derived from the same shared surface snapshot Android uses, and taps are routed
// through the existing `biciradar://` deep-link pipeline so navigation stays in one place.

/// A single Home Screen quick action, kept free of UIKit so it can be unit tested.
struct BiziHomeScreenQuickAction: Equatable {
    /// Stable identifier. Mirrors the Android `surface_*` shortcut ids.
    let type: String
    let title: String
    let subtitle: String?
    /// Deep link applied when the action is tapped, reusing `AppleDeepLinkParser`.
    let deepLink: String
    let systemImageName: String
}

enum BiziHomeScreenQuickActions {
    /// iOS keeps at most four app-defined Home Screen quick actions; extra items are ignored.
    static let maxCount = 4

    /// Builds the ordered quick actions for a surface snapshot. Pure, so it is unit tested.
    static func actions(from bundle: AppleSurfaceSnapshotBundle?) -> [BiziHomeScreenQuickAction] {
        var actions: [BiziHomeScreenQuickAction] = []
        var seenStationDeepLinks = Set<String>()

        func appendStation(
            type: String,
            title: String,
            systemImageName: String,
            station: AppleSurfaceStationSnapshot?
        ) {
            guard let station else { return }
            let deepLink = "biciradar://station/\(station.id)"
            // Home/work/favorite can point at the same station; never show it twice.
            guard seenStationDeepLinks.insert(deepLink).inserted else { return }
            actions.append(
                BiziHomeScreenQuickAction(
                    type: type,
                    title: title,
                    subtitle: station.nameFull,
                    deepLink: deepLink,
                    systemImageName: systemImageName
                )
            )
        }

        actions.append(
            BiziHomeScreenQuickAction(
                type: "surface_nearby",
                title: "Cerca",
                subtitle: "Ver estaciones cercanas",
                deepLink: "biciradar://home",
                systemImageName: "location.circle"
            )
        )
        appendStation(
            type: "surface_home_station",
            title: "Casa",
            systemImageName: "house.fill",
            station: bundle?.homeStation
        )
        appendStation(
            type: "surface_work_station",
            title: "Trabajo",
            systemImageName: "briefcase.fill",
            station: bundle?.workStation
        )
        appendStation(
            type: "surface_favorite_station",
            title: "Favorita",
            systemImageName: "heart.fill",
            station: bundle?.favoriteStation
        )

        if let favorite = bundle?.favoriteStation {
            actions.append(
                BiziHomeScreenQuickAction(
                    type: "surface_monitor_favorite",
                    title: "Monitorizar",
                    subtitle: favorite.nameShort,
                    deepLink: "biciradar://monitor/\(favorite.id)",
                    systemImageName: "dot.radiowaves.left.and.right"
                )
            )
        }

        actions.append(
            BiziHomeScreenQuickAction(
                type: "surface_favorites",
                title: "Favoritas",
                subtitle: "Abrir favoritas",
                deepLink: "biciradar://favorites",
                systemImageName: "heart.text.square.fill"
            )
        )

        return Array(actions.prefix(maxCount))
    }
}

// MARK: - Publishing

@MainActor
enum BiziHomeScreenQuickActionPublisher {
    /// Rebuilds the Home Screen menu from the latest shared snapshot. Safe to call repeatedly.
    static func publish() {
        let actions = BiziHomeScreenQuickActions.actions(from: BiziSurfaceStore.readSnapshotBundle())
        UIApplication.shared.shortcutItems = actions.map(makeItem)
    }

    private static func makeItem(_ action: BiziHomeScreenQuickAction) -> UIApplicationShortcutItem {
        UIApplicationShortcutItem(
            type: action.type,
            localizedTitle: action.title,
            localizedSubtitle: action.subtitle,
            icon: UIApplicationShortcutIcon(systemImageName: action.systemImageName),
            userInfo: [BiziHomeScreenQuickActionRouter.deepLinkUserInfoKey: action.deepLink as NSString]
        )
    }
}

// MARK: - Tap routing

extension Notification.Name {
    /// Posted after a quick action is stored, so an already-visible scene can apply it.
    static let biziHomeScreenQuickActionRequested = Notification.Name("biziHomeScreenQuickActionRequested")
}

@MainActor
enum BiziHomeScreenQuickActionRouter {
    static let deepLinkUserInfoKey = "deepLink"

    /// Stores the launch request carried by a tapped quick action. Returns whether it was handled.
    @discardableResult
    static func handle(_ item: UIApplicationShortcutItem) -> Bool {
        guard
            let deepLink = item.userInfo?[deepLinkUserInfoKey] as? String,
            let url = URL(string: deepLink),
            let request = AppleDeepLinkParser.parse(url)
        else {
            return false
        }
        AppleLaunchRequestStore.shared.save(request)
        NotificationCenter.default.post(name: .biziHomeScreenQuickActionRequested, object: nil)
        return true
    }
}
