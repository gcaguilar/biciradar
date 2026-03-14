import Foundation

/// Persists the last-known bikes-available count per station so that background refreshes
/// can detect which favorite stations newly have bikes after being empty.
struct StationAvailabilityCache {
    private static let defaultsKey = "bizizaragoza.stationAvailabilityCache"
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    /// Returns the previously cached bike count for a station, or `nil` if unseen before.
    func cachedBikeCount(for stationId: String) -> Int? {
        let dict = defaults.dictionary(forKey: Self.defaultsKey) as? [String: Int] ?? [:]
        return dict[stationId]
    }

    /// Persists current bike counts for the given snapshots.
    func persist(_ snapshots: [BiziStationSnapshot]) {
        var dict = defaults.dictionary(forKey: Self.defaultsKey) as? [String: Int] ?? [:]
        for snapshot in snapshots {
            dict[snapshot.id] = snapshot.bikesAvailable
        }
        defaults.set(dict, forKey: Self.defaultsKey)
    }

    /// Returns the subset of `candidates` that were previously at 0 bikes and now have bikes.
    func newlyAvailableStations(from candidates: [BiziStationSnapshot]) -> [BiziStationSnapshot] {
        candidates.filter { snapshot in
            let previous = cachedBikeCount(for: snapshot.id)
            // If we've never seen this station, don't notify (avoid notification storm on first run).
            guard let previous else { return false }
            return previous == 0 && snapshot.bikesAvailable > 0
        }
    }
}
