import Foundation
import WatchConnectivity

@MainActor
final class WatchFavoritesSyncBridge: NSObject, ObservableObject, @preconcurrency WCSessionDelegate {
    static let shared = WatchFavoritesSyncBridge()
    static let favoritesCacheKey = "bizizaragoza.watch.favorite_ids"
    static let homeStationCacheKey = "bizizaragoza.watch.home_station_id"
    static let workStationCacheKey = "bizizaragoza.watch.work_station_id"
    static let monitoringSessionCacheKey = "bizizaragoza.watch.monitoring_session"
    static let monitoringSessionContextKey = "monitoring_session"
    static let snapshotV2CacheKey = "bizizaragoza.watch.favorite_categories_v2"
    static let snapshotV2ContextKey = "favorite_categories_v2"

    @Published private(set) var favoriteIds: Set<String> = []
    @Published private(set) var homeStationId: String?
    @Published private(set) var workStationId: String?
    @Published private(set) var monitoringSession: WatchConnectivityMonitoringSession?
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        self.favoriteIds = Set(defaults.stringArray(forKey: Self.favoritesCacheKey) ?? [])
        self.homeStationId = defaults.string(forKey: Self.homeStationCacheKey)
        self.workStationId = defaults.string(forKey: Self.workStationCacheKey)
        self.monitoringSession = defaults.data(forKey: Self.monitoringSessionCacheKey)
            .flatMap { try? JSONDecoder().decode(WatchConnectivityMonitoringSession.self, from: $0) }
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
        apply(context: session.receivedApplicationContext)
    }

    /// Sends a one-shot route request. It uses `sendMessage` instead of the persistent
    /// application context so a request made while the iPhone is unreachable can't be replayed
    /// (and open Maps) later; in that case the caller reports the failure.
    func requestRoute(to stationId: String) -> Bool {
        let session = WCSession.default
        guard session.activationState == .activated, session.isReachable else { return false }
        session.sendMessage(
            [
                "route_station_id": stationId,
                "route_requested_at": Date().timeIntervalSince1970,
            ],
            replyHandler: nil,
            errorHandler: nil
        )
        return true
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        let context = session.receivedApplicationContext
        Task { @MainActor in
            self.apply(context: context)
        }
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        Task { @MainActor in
            self.apply(context: applicationContext)
        }
    }

    #if os(iOS)
    func sessionDidBecomeInactive(_ session: WCSession) {}
    func sessionDidDeactivate(_ session: WCSession) {
        session.activate()
    }
    #endif

    func apply(context: [String: Any]) {
        if let ids = context["favorite_ids"] as? [String] {
            favoriteIds = Set(ids)
            defaults.set(ids, forKey: Self.favoritesCacheKey)
        }
        if let snapshot = context[Self.snapshotV2ContextKey] as? String,
           let data = snapshot.data(using: .utf8),
           let decoded = try? JSONDecoder().decode(BiziSyncSnapshotV2.self, from: data) {
            favoriteIds = decoded.favoriteIds
            homeStationId = decoded.homeStationId
            workStationId = decoded.workStationId
            defaults.set(Array(decoded.favoriteIds), forKey: Self.favoritesCacheKey)
            defaults.set(decoded.homeStationId, forKey: Self.homeStationCacheKey)
            defaults.set(decoded.workStationId, forKey: Self.workStationCacheKey)
            defaults.set(snapshot, forKey: Self.snapshotV2CacheKey)
        }
        if let homeStationId = context["home_station_id"] as? String {
            let trimmed = homeStationId.trimmingCharacters(in: .whitespacesAndNewlines)
            if trimmed.isEmpty {
                self.homeStationId = nil
                defaults.removeObject(forKey: Self.homeStationCacheKey)
            } else {
                self.homeStationId = trimmed
                defaults.set(trimmed, forKey: Self.homeStationCacheKey)
            }
        }
        if let workStationId = context["work_station_id"] as? String {
            let trimmed = workStationId.trimmingCharacters(in: .whitespacesAndNewlines)
            if trimmed.isEmpty {
                self.workStationId = nil
                defaults.removeObject(forKey: Self.workStationCacheKey)
            } else {
                self.workStationId = trimmed
                defaults.set(trimmed, forKey: Self.workStationCacheKey)
            }
        }
        if let monitoringData = context[Self.monitoringSessionContextKey] as? Data,
           let monitoringSession = try? JSONDecoder().decode(WatchConnectivityMonitoringSession.self, from: monitoringData) {
            self.monitoringSession = monitoringSession
            defaults.set(monitoringData, forKey: Self.monitoringSessionCacheKey)
        } else if context.keys.contains(Self.monitoringSessionContextKey) || !context.isEmpty {
            monitoringSession = nil
            defaults.removeObject(forKey: Self.monitoringSessionCacheKey)
        }
    }
}
