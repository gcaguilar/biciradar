import BiziMobileUi
import Foundation
import WatchConnectivity

@MainActor
final class FavoritesSyncBridge: NSObject, ObservableObject, @preconcurrency WCSessionDelegate {
    static let shared = FavoritesSyncBridge()
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
    private let defaults: UserDefaults
    private let routeRequestStore: AppleLaunchRequestStore
    private let lastRouteRequestAtKey = "bizizaragoza.lastRouteRequestAt"

    init(
        defaults: UserDefaults = BiziSharedStorage.sharedDefaults,
        routeRequestStore: AppleLaunchRequestStore? = nil
    ) {
        self.defaults = defaults
        self.routeRequestStore = routeRequestStore ?? .shared
        self.favoriteIds = Set(defaults.stringArray(forKey: Self.favoritesCacheKey) ?? [])
        self.homeStationId = defaults.string(forKey: Self.homeStationCacheKey)
        self.workStationId = defaults.string(forKey: Self.workStationCacheKey)
        super.init()
    }

    func activate() {
        guard WCSession.isSupported() else { return }
        let session = WCSession.default
        session.delegate = self
        session.activate()
        apply(context: session.receivedApplicationContext)
        syncWatchContextFromAppGroup()
    }

    func pushFavorites(_ favoriteIds: Set<String>) {
        applyFavoriteIds(favoriteIds)
        pushCurrentContext()
    }

    func syncMonitoringFromSurfaceSnapshot() {
        let monitoringSession = BiziSurfaceStore.activeMonitoringSession().map(WatchConnectivityMonitoringSession.init)
        cacheMonitoringSession(monitoringSession)
        pushCurrentContext()
    }

    func syncWatchContextFromAppGroup(favoritesURL: URL? = BiziSharedStorage.favoritesSnapshotURL()) {
        if let snapshot = loadFavoritesSnapshot(from: favoritesURL) {
            apply(snapshot: snapshot)
        }
        pushCurrentContext()
    }

    func session(
        _ session: WCSession,
        activationDidCompleteWith activationState: WCSessionActivationState,
        error: Error?
    ) {
        let context = session.receivedApplicationContext
        Task { @MainActor in
            apply(context: context)
            if activationState == .activated {
                syncWatchContextFromAppGroup()
            }
        }
    }

    func sessionDidBecomeInactive(_ session: WCSession) {}

    func sessionDidDeactivate(_ session: WCSession) {
        session.activate()
    }

    func session(_ session: WCSession, didReceiveApplicationContext applicationContext: [String: Any]) {
        Task { @MainActor in
            apply(context: applicationContext)
        }
    }

    func apply(context: [String: Any]) {
        if let ids = context["favorite_ids"] as? [String] {
            applyFavoriteIds(Set(ids))
        }
        if let snapshot = context[Self.snapshotV2ContextKey] as? String,
           let data = snapshot.data(using: .utf8),
           let decoded = try? JSONDecoder().decode(BiziSyncSnapshotV2.self, from: data) {
            apply(snapshot: decoded)
            defaults.set(snapshot, forKey: Self.snapshotV2CacheKey)
        }
        if let homeStationId = stringValue(from: context, key: "home_station_id") {
            applyHomeStationId(homeStationId)
        } else if context.keys.contains("home_station_id") {
            applyHomeStationId(nil)
        }
        if let workStationId = stringValue(from: context, key: "work_station_id") {
            applyWorkStationId(workStationId)
        } else if context.keys.contains("work_station_id") {
            applyWorkStationId(nil)
        }
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

    private func pushCurrentContext() {
        guard WCSession.isSupported(), WCSession.default.activationState == .activated else { return }
        var context: [String: Any] = [
            "favorite_ids": Array(currentFavoriteIds())
        ]
        context["home_station_id"] = currentHomeStationId() ?? ""
        context["work_station_id"] = currentWorkStationId() ?? ""
        let snapshot = BiziSyncSnapshotV2(
            categories: [],
            stationCategory: [:],
            favoriteIds: currentFavoriteIds(),
            homeStationId: currentHomeStationId(),
            workStationId: currentWorkStationId()
        )
        if let encoded = try? JSONEncoder().encode(snapshot),
           let string = String(data: encoded, encoding: .utf8) {
            context[Self.snapshotV2ContextKey] = string
            defaults.set(string, forKey: Self.snapshotV2CacheKey)
        }
        if let monitoringSessionData = defaults.data(forKey: Self.monitoringSessionCacheKey) {
            context[Self.monitoringSessionContextKey] = monitoringSessionData
        }
        try? WCSession.default.updateApplicationContext(context)
    }

    private func cacheMonitoringSession(_ monitoringSession: WatchConnectivityMonitoringSession?) {
        guard let monitoringSession else {
            defaults.removeObject(forKey: Self.monitoringSessionCacheKey)
            return
        }
        if let encoded = try? JSONEncoder().encode(monitoringSession) {
            defaults.set(encoded, forKey: Self.monitoringSessionCacheKey)
        }
    }

    private func currentFavoriteIds() -> Set<String> {
        Set(defaults.stringArray(forKey: Self.favoritesCacheKey) ?? favoriteIds.map { $0 })
    }

    private func currentHomeStationId() -> String? {
        defaults.string(forKey: Self.homeStationCacheKey) ?? homeStationId
    }

    private func currentWorkStationId() -> String? {
        defaults.string(forKey: Self.workStationCacheKey) ?? workStationId
    }

    private func loadFavoritesSnapshot(from url: URL?) -> BiziSyncSnapshotV2? {
        guard let url else { return nil }
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(BiziSyncSnapshotV2.self, from: data)
    }

    private func apply(snapshot: BiziSyncSnapshotV2) {
        applyFavoriteIds(snapshot.favoriteIds)
        applyHomeStationId(snapshot.homeStationId)
        applyWorkStationId(snapshot.workStationId)
    }

    private func applyFavoriteIds(_ ids: Set<String>) {
        favoriteIds = ids
        defaults.set(Array(ids), forKey: Self.favoritesCacheKey)
    }

    private func applyHomeStationId(_ value: String?) {
        let resolved = normalizedOptionalString(value)
        homeStationId = resolved
        if let resolved {
            defaults.set(resolved, forKey: Self.homeStationCacheKey)
        } else {
            defaults.removeObject(forKey: Self.homeStationCacheKey)
        }
    }

    private func applyWorkStationId(_ value: String?) {
        let resolved = normalizedOptionalString(value)
        workStationId = resolved
        if let resolved {
            defaults.set(resolved, forKey: Self.workStationCacheKey)
        } else {
            defaults.removeObject(forKey: Self.workStationCacheKey)
        }
    }

    private func stringValue(from context: [String: Any], key: String) -> String? {
        guard let value = context[key] as? String else { return nil }
        return normalizedOptionalString(value)
    }

    private func normalizedOptionalString(_ value: String?) -> String? {
        let trimmed = value?.trimmingCharacters(in: .whitespacesAndNewlines)
        guard let trimmed, !trimmed.isEmpty else { return nil }
        return trimmed
    }
}

// MARK: - iOS-side initialiser for WatchConnectivityMonitoringSession
// AppleSurfaceMonitoringSession is an iOS-only type from BiziMobileUi,
// so this mapping lives here rather than in the shared BiziSyncModels file.

extension WatchConnectivityMonitoringSession {
    init(session: AppleSurfaceMonitoringSession) {
        stationId = session.stationId
        stationName = session.stationName
        bikesAvailable = session.bikesAvailable
        docksAvailable = session.docksAvailable
        statusText = switch session.status {
        case .monitoring:
            "Monitorizando"
        case .changedToEmpty:
            "Sin bicis"
        case .changedToFull:
            "Sin huecos"
        case .alternativeAvailable:
            "Alternativa"
        case .ended:
            "Finalizada"
        case .expired:
            "Expirada"
        }
        statusLevel = session.statusLevel.rawValue
        expiresAtEpoch = session.expiresAtEpoch
        lastUpdatedEpoch = session.lastUpdatedEpoch
        isActive = session.isActive
        alternativeStationId = session.alternativeStationId
        alternativeStationName = session.alternativeStationName
        alternativeDistanceMeters = session.alternativeDistanceMeters
    }
}
