import Foundation

// MARK: - Shared WatchConnectivity Sync Models
//
// Estructuras Codable compartidas entre FavoritesSyncBridge (iOS) y
// WatchFavoritesSyncBridge (watchOS). Ambas partes serializan y deserializan
// el mismo payload JSON, por lo que el modelo debe ser idéntico en ambos lados.

// MARK: Snapshot de favoritos

/// Payload V2 de favoritos sincronizado vía WatchConnectivity applicationContext.
struct BiziSyncSnapshotV2: Codable {
    let categories: [BiziSyncCategory]?
    let stationCategory: [String: String]?
    let favoriteIds: Set<String>
    let homeStationId: String?
    let workStationId: String?
}

/// Categoría de favorito tal como se serializa en el snapshot de sincronización.
struct BiziSyncCategory: Codable {
    let id: String
    let label: String
    let isSystem: Bool
}

// MARK: Sesión de monitorización

/// Sesión de monitorización de estación serializada para WatchConnectivity.
/// - iOS serializa este modelo al enviar el contexto al reloj.
/// - watchOS lo deserializa y lo expone como estado publicado.
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
}

// MARK: Display helpers (watchOS UI)

extension WatchConnectivityMonitoringSession {
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
