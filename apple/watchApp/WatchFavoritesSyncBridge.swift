import Foundation
import WatchConnectivity

@MainActor
final class WatchFavoritesSyncBridge: NSObject, ObservableObject, @preconcurrency WCSessionDelegate {
    static let shared = WatchFavoritesSyncBridge()

    @Published private(set) var favoriteIds: Set<String> = []

    private override init() {
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

    private func apply(context: [String: Any]) {
        let ids = (context["favorite_ids"] as? [String]) ?? []
        favoriteIds = Set(ids)
    }
}
