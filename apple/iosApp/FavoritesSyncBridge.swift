import BiziMobileUi
import Foundation
import WatchConnectivity

@MainActor
final class FavoritesSyncBridge: NSObject, ObservableObject, @preconcurrency WCSessionDelegate {
    static let shared = FavoritesSyncBridge()
    static let favoritesCacheKey = "bizizaragoza.watch.favorite_ids"

    @Published private(set) var favoriteIds: Set<String> = []
    private let defaults: UserDefaults
    private let routeRequestStore: AppleLaunchRequestStore
    private let lastRouteRequestAtKey = "bizizaragoza.lastRouteRequestAt"

    init(
        defaults: UserDefaults = .standard,
        routeRequestStore: AppleLaunchRequestStore = .shared
    ) {
        self.defaults = defaults
        self.routeRequestStore = routeRequestStore
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
        apply(context: session.receivedApplicationContext)
    }

    func pushFavorites(_ favoriteIds: Set<String>) {
        self.favoriteIds = favoriteIds
        defaults.set(Array(favoriteIds), forKey: Self.favoritesCacheKey)
        guard WCSession.default.activationState == .activated else { return }
        var context = WCSession.default.receivedApplicationContext
        context["favorite_ids"] = Array(favoriteIds)
        context.removeValue(forKey: "route_station_id")
        context.removeValue(forKey: "route_requested_at")
        try? WCSession.default.updateApplicationContext(context)
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        apply(context: session.receivedApplicationContext)
        if activationState == .activated, !favoriteIds.isEmpty {
            pushFavorites(favoriteIds)
        }
    }

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        session.activate()
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        apply(context: applicationContext)
    }

    func apply(context: [String: Any]) {
        let ids = (context["favorite_ids"] as? [String]) ?? []
        favoriteIds = Set(ids)
        defaults.set(ids, forKey: Self.favoritesCacheKey)
        let routeRequestedAt = (context["route_requested_at"] as? Double) ?? 0
        if let routeStationId = context["route_station_id"] as? String,
           !routeStationId.isEmpty,
           routeRequestedAt > defaults.double(forKey: lastRouteRequestAtKey) {
            defaults.set(routeRequestedAt, forKey: lastRouteRequestAtKey)
            routeRequestStore.save(
                MobileLaunchRequestRouteToStation(stationId: routeStationId)
            )
        }
    }
}
