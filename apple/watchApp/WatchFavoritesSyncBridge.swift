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

    func requestRoute(to stationId: String) -> Bool {
        guard WCSession.default.activationState == .activated else { return false }
        var context = WCSession.default.receivedApplicationContext
        context["favorite_ids"] = Array(favoriteIds)
        context["home_station_id"] = homeStationId
        context["work_station_id"] = workStationId
        let snapshot = WatchFavoritesSnapshotV2(
            categories: [],
            stationCategory: [:],
            favoriteIds: favoriteIds,
            homeStationId: homeStationId,
            workStationId: workStationId
        )
        if let encoded = try? JSONEncoder().encode(snapshot),
           let string = String(data: encoded, encoding: .utf8) {
            context[Self.snapshotV2ContextKey] = string
            defaults.set(string, forKey: Self.snapshotV2CacheKey)
        }
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
           let decoded = try? JSONDecoder().decode(WatchFavoritesSnapshotV2.self, from: data) {
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

struct WatchConnectivityMonitoringSession: Codable, Hashable {
    let stationId: String
    let stationName: String
    let bikesAvailable: Int
    let docksAvailable: Int
    let statusText: String
    let statusLevel: String
    let expiresAtEpoch: Int64
    let lastUpdatedEpoch: Int64
    let isActive: Bool
    let alternativeStationId: String?
    let alternativeStationName: String?
    let alternativeDistanceMeters: Int?

    var remainingMinutesText: String {
        let remainingSeconds = max(Int((expiresAtEpoch - Int64(Date().timeIntervalSince1970 * 1000)) / 1000), 0)
        if remainingSeconds < 60 {
            return "Ahora"
        }
        let minutes = remainingSeconds / 60
        return minutes == 1 ? "1 min" : "\(minutes) min"
    }

    var color: WatchMonitoringAccent {
        switch statusLevel {
        case "Empty", "Full":
            return .error
        case "Low":
            return .warning
        default:
            return .good
        }
    }
}

enum WatchMonitoringAccent {
    case good
    case warning
    case error
}

private struct WatchFavoritesSnapshotV2: Codable {
    let categories: [WatchFavoriteCategory]?
    let stationCategory: [String: String]?
    let favoriteIds: Set<String>
    let homeStationId: String?
    let workStationId: String?
}

private struct WatchFavoriteCategory: Codable {
    let id: String
    let label: String
    let isSystem: Bool
}
