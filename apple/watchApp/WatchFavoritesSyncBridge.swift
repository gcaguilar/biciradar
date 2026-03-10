import Foundation
import WatchConnectivity

@MainActor
final class WatchFavoritesSyncBridge: NSObject, ObservableObject, WCSessionDelegate {
    static let shared = WatchFavoritesSyncBridge()

    @Published private(set) var favoriteIds: [String] = []

    private override init() {
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {}

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        let ids = (applicationContext["favorite_ids"] as? [String]) ?? []
        Task { @MainActor in
            favoriteIds = ids
        }
    }
}
