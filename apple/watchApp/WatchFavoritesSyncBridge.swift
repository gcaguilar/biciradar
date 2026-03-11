import Foundation
import WatchConnectivity

@MainActor
final class WatchFavoritesSyncBridge: NSObject, ObservableObject, @preconcurrency WCSessionDelegate {
    static let shared = WatchFavoritesSyncBridge()
    static let favoritesCacheKey = "bizizaragoza.watch.favorite_ids"

    @Published private(set) var favoriteIds: Set<String> = []
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        self.favoriteIds = Set(defaults.stringArray(forKey: Self.favoritesCacheKey) ?? [])
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
        apply(context: session.receivedApplicationContext)
    }

    func requestRoute(to stationId: String) -> Bool {
        guard WCSession.default.activationState == .activated else { return false }
        var context = WCSession.default.receivedApplicationContext
        context["favorite_ids"] = Array(favoriteIds)
        context["route_station_id"] = stationId
        context["route_requested_at"] = Date().timeIntervalSince1970
        do {
            try WCSession.default.updateApplicationContext(context)
            return true
        } catch {
            return false
        }
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        apply(context: session.receivedApplicationContext)
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        apply(context: applicationContext)
    }

    func apply(context: [String: Any]) {
        if let ids = context["favorite_ids"] as? [String] {
            favoriteIds = Set(ids)
            defaults.set(ids, forKey: Self.favoritesCacheKey)
        }
    }
}
