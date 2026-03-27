import Foundation

struct WatchStationSnapshot: Identifiable, Hashable, Codable {
    let id: String
    let name: String
    let address: String
    let bikesAvailable: Int
    let slotsFree: Int
    let distanceMeters: Int
}

enum WatchStationStatusLevel: Hashable {
    case good
    case low
    case empty
    case full
}

extension WatchStationSnapshot {
    var statusLevel: WatchStationStatusLevel {
        if bikesAvailable == 0 { return .empty }
        if slotsFree == 0 { return .full }
        if bikesAvailable <= 3 || slotsFree <= 3 { return .low }
        return .good
    }

    var statusText: String {
        switch statusLevel {
        case .good:
            return "Disponible"
        case .low:
            return "Pocas"
        case .empty:
            return "Sin bicis"
        case .full:
            return "Sin huecos"
        }
    }
}

protocol WatchSurfaceSnapshotStoring {
    func read() -> WatchSurfaceSnapshotBundle?
    func write(_ snapshot: WatchSurfaceSnapshotBundle)
}

struct WatchSurfaceSnapshotBundle: Codable, Equatable {
    let generatedAtEpoch: Int64
    let nearbyStations: [WatchStationSnapshot]
    let favoriteStations: [WatchStationSnapshot]
}

struct WatchSurfaceSnapshotStore: WatchSurfaceSnapshotStoring {
    static let shared = WatchSurfaceSnapshotStore()

    private let fileManager: FileManager
    private let appGroupIdentifier: String
    private let snapshotsDirectoryName: String
    private let snapshotFileName: String

    init(
        fileManager: FileManager = .default,
        appGroupIdentifier: String = "group.com.gcaguilar.biciradar",
        snapshotsDirectoryName: String = "bizi",
        snapshotFileName: String = "watch_surface_snapshot.json"
    ) {
        self.fileManager = fileManager
        self.appGroupIdentifier = appGroupIdentifier
        self.snapshotsDirectoryName = snapshotsDirectoryName
        self.snapshotFileName = snapshotFileName
    }

    func read() -> WatchSurfaceSnapshotBundle? {
        guard let url = snapshotURL() else { return nil }
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? JSONDecoder().decode(WatchSurfaceSnapshotBundle.self, from: data)
    }

    func write(_ snapshot: WatchSurfaceSnapshotBundle) {
        guard let directoryURL = snapshotsDirectoryURL() else { return }
        let snapshotURL = directoryURL.appendingPathComponent(snapshotFileName)
        do {
            try fileManager.createDirectory(at: directoryURL, withIntermediateDirectories: true)
            let data = try JSONEncoder().encode(snapshot)
            try data.write(to: snapshotURL, options: .atomic)
        } catch {
            #if DEBUG
            print("WatchSurfaceSnapshotStore write failed: \(error.localizedDescription)")
            #endif
        }
    }

    private func snapshotURL() -> URL? {
        snapshotsDirectoryURL()?.appendingPathComponent(snapshotFileName)
    }

    private func snapshotsDirectoryURL() -> URL? {
        fileManager
            .containerURL(forSecurityApplicationGroupIdentifier: appGroupIdentifier)?
            .appendingPathComponent(snapshotsDirectoryName, isDirectory: true)
    }
}
