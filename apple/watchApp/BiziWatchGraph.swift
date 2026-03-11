import BiziSharedCore
import Foundation

struct WatchStationSnapshot: Identifiable, Hashable {
    let id: String
    let name: String
    let address: String
    let bikesAvailable: Int
    let slotsFree: Int
    let distanceMeters: Int
}

actor BiziWatchGraph {
    static let shared = BiziWatchGraph()

    private let bindings = WatchOSPlatformBindings(
        appConfiguration: AppConfiguration(
            cityBikesNetworkUrl: "https://api.citybik.es/v2/networks/bizi-zaragoza",
            geminiProxyBaseUrl: "http://127.0.0.1:8080",
            defaultLatitude: 41.6488,
            defaultLongitude: -0.8891
        )
    )

    private lazy var graph: any SharedGraph = SharedGraphCompanion.shared.create(
        platformBindings: bindings
    )
    private var hasBootstrapped = false

    func nearbyStations(limit: Int = 5) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        return Array(stationsState().stations.prefix(limit).map(snapshot(from:)))
    }

    func nearestStation() async throws -> WatchStationSnapshot? {
        return try await nearbyStations(limit: 1).first
    }

    func favoriteStations(favoriteIds: Set<String>) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        guard !favoriteIds.isEmpty else { return [] }
        return stationsState().stations
            .filter { favoriteIds.contains($0.id) }
            .map(snapshot(from:))
    }

    func station(stationId: String) async throws -> WatchStationSnapshot? {
        try await refreshData()
        return graph.stationsRepository.stationById(stationId: stationId).map(snapshot(from:))
    }

    func station(matching query: String?) async throws -> WatchStationSnapshot? {
        try await refreshData()
        let trimmedQuery = query?.trimmingCharacters(in: .whitespacesAndNewlines)
        let state = stationsState()
        if let trimmedQuery, !trimmedQuery.isEmpty {
            let normalized = trimmedQuery.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
            return state.stations
                .map(snapshot(from:))
                .first(where: { station in
                    station.name.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current).contains(normalized) ||
                    station.address.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current).contains(normalized)
                })
        }
        return state.stations.first.map(snapshot(from:))
    }

    func openRoute(to stationId: String) async throws -> WatchStationSnapshot? {
        try await refreshData()
        guard let station = graph.stationsRepository.stationById(stationId: stationId) else {
            return nil
        }
        graph.routeLauncher.launch(station: station)
        return snapshot(from: station)
    }

    private func refreshData() async throws {
        if !hasBootstrapped {
            try await bootstrapFavorites()
            hasBootstrapped = true
        }
        try await refreshStations()
    }

    private func bootstrapFavorites() async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            graph.favoritesRepository.bootstrap { error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume()
                }
            }
        }
    }

    private func refreshStations() async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            graph.stationsRepository.refresh { error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume()
                }
            }
        }
    }

    private func stationsState() -> StationsState {
        guard let state = graph.stationsRepository.state.value as? StationsState else {
            return StationsState(stations: [], isLoading: false, errorMessage: nil, userLocation: nil)
        }
        return state
    }

    private func snapshot(from station: Station) -> WatchStationSnapshot {
        WatchStationSnapshot(
            id: station.id,
            name: station.name,
            address: station.address,
            bikesAvailable: Int(station.bikesAvailable),
            slotsFree: Int(station.slotsFree),
            distanceMeters: Int(station.distanceMeters)
        )
    }
}
