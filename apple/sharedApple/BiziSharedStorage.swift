import Foundation

enum BiziSharedStorage {
    static let appGroupIdentifier = "group.com.gcaguilar.biciradar"
    static let snapshotsDirectoryName = "bizi"
    static let snapshotFileName = "surface_snapshot.json"

    static var sharedDefaults: UserDefaults {
        UserDefaults(suiteName: appGroupIdentifier) ?? .standard
    }

    static func appGroupContainerURL() -> URL? {
        FileManager.default.containerURL(forSecurityApplicationGroupIdentifier: appGroupIdentifier)
    }

    static func snapshotsDirectoryURL() -> URL? {
        appGroupContainerURL()?.appendingPathComponent(snapshotsDirectoryName, isDirectory: true)
    }

    static func surfaceSnapshotURL() -> URL? {
        snapshotsDirectoryURL()?.appendingPathComponent(snapshotFileName)
    }
}
