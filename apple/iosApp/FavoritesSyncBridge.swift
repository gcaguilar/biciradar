import Foundation
import WatchConnectivity

@MainActor
final class FavoritesSyncBridge: NSObject, ObservableObject, WCSessionDelegate {
    static let shared = FavoritesSyncBridge()

    @Published private(set) var favoriteIds: Set<String> = []

    private override init() {
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
    }

    func pushFavorites(_ favoriteIds: Set<String>) {
        self.favoriteIds = favoriteIds
        guard WCSession.default.activationState == .activated else { return }
        try? WCSession.default.updateApplicationContext([
            "favorite_ids": Array(favoriteIds)
        ])
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {}

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        session.activate()
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        let ids = (applicationContext["favorite_ids"] as? [String]) ?? []
        Task { @MainActor in
            favoriteIds = Set(ids)
        }
    }
}
