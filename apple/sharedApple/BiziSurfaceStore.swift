import Foundation

enum BiziSurfaceStore {
    static func readSnapshotBundle() -> AppleSurfaceSnapshotBundle? {
        guard let url = BiziSharedStorage.surfaceSnapshotURL() else { return nil }
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(AppleSurfaceSnapshotBundle.self, from: data)
    }

    static func favoriteStation() -> AppleSurfaceStationSnapshot? {
        readSnapshotBundle()?.favoriteStation
    }

    static func activeMonitoringSession() -> AppleSurfaceMonitoringSession? {
        guard let session = readSnapshotBundle()?.monitoringSession, session.isActive else { return nil }
        return session
    }

    static func relativeUpdateText(lastUpdatedEpoch: Int64?) -> String {
        guard let lastUpdatedEpoch else { return "Datos no disponibles" }
        let lastUpdated = Date(timeIntervalSince1970: TimeInterval(lastUpdatedEpoch) / 1000)
        let formatter = RelativeDateTimeFormatter()
        formatter.unitsStyle = .short
        return formatter.localizedString(for: lastUpdated, relativeTo: Date())
    }
}

struct AppleSurfaceSnapshotBundle: Decodable {
    let generatedAtEpoch: Int64
    let favoriteStation: AppleSurfaceStationSnapshot?
    let nearbyStations: [AppleSurfaceStationSnapshot]
    let monitoringSession: AppleSurfaceMonitoringSession?
    let state: AppleSurfaceState
}

struct AppleSurfaceStationSnapshot: Decodable, Hashable, Identifiable {
    let id: String
    let nameShort: String
    let nameFull: String
    let cityId: String
    let latitude: Double
    let longitude: Double
    let bikesAvailable: Int
    let docksAvailable: Int
    let statusTextShort: String
    let statusLevel: AppleSurfaceStatusLevel
    let lastUpdatedEpoch: Int64
    let distanceMeters: Int?
    let isFavorite: Bool
    let alternativeStationId: String?
    let alternativeStationName: String?
    let alternativeDistanceMeters: Int?
}

struct AppleSurfaceMonitoringSession: Decodable, Hashable {
    let stationId: String
    let stationName: String
    let cityId: String
    let kind: AppleSurfaceMonitoringKind
    let status: AppleSurfaceMonitoringStatus
    let bikesAvailable: Int
    let docksAvailable: Int
    let statusLevel: AppleSurfaceStatusLevel
    let startedAtEpoch: Int64
    let expiresAtEpoch: Int64
    let lastUpdatedEpoch: Int64
    let isActive: Bool
    let alternativeStationId: String?
    let alternativeStationName: String?
    let alternativeDistanceMeters: Int?

    var remainingSeconds: Int {
        max(Int((expiresAtEpoch - Int64(Date().timeIntervalSince1970 * 1000)) / 1000), 0)
    }
}

struct AppleSurfaceState: Decodable, Hashable {
    let hasLocationPermission: Bool
    let hasNotificationPermission: Bool
    let hasFavoriteStation: Bool
    let isDataFresh: Bool
    let lastSyncEpoch: Int64?
    let cityId: String
    let cityName: String
    let userLatitude: Double?
    let userLongitude: Double?
}

enum AppleSurfaceStatusLevel: String, Decodable {
    case good = "Good"
    case low = "Low"
    case empty = "Empty"
    case full = "Full"
    case unavailable = "Unavailable"
}

enum AppleSurfaceMonitoringKind: String, Decodable {
    case bikes = "Bikes"
    case docks = "Docks"
}

enum AppleSurfaceMonitoringStatus: String, Decodable {
    case monitoring = "Monitoring"
    case changedToEmpty = "ChangedToEmpty"
    case changedToFull = "ChangedToFull"
    case alternativeAvailable = "AlternativeAvailable"
    case ended = "Ended"
    case expired = "Expired"
}
