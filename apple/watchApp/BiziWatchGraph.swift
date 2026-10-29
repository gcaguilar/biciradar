import Core
import Foundation

actor BiziWatchGraph {
    static let shared = BiziWatchGraph()

    private let bindings = platform.WatchOSPlatformBindings(
        appConfiguration: AppConfiguration.Companion.shared.createDefault()
    )

    private lazy var graph: any SharedGraph = di.CoreGraph.Companion.shared.create(
        platformBindings: bindings
    )
    private var hasBootstrapped = false

    // MARK: - Public API

    func nearbyStations(limit: Int = 5) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        return try await graph.getNearbyStationList.execute(limit: Int32(limit)).map(snapshot(from:))
    }

    func nearestStation() async throws -> WatchStationSnapshot? {
        try await refreshData()
        return try await graph.findNearestStation.execute().map(snapshot(from:))
    }

    func nearestStationWithBikes() async throws -> WatchStationSnapshot? {
        try await refreshData()
        return try await graph.findNearestStationWithBikes.execute().map(snapshot(from:))
    }

    func nearestStationWithSlots() async throws -> WatchStationSnapshot? {
        try await refreshData()
        return try await graph.findNearestStationWithSlots.execute().map(snapshot(from:))
    }

    func favoriteStations(favoriteIds: Set<String>) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        return try await graph.getFavoriteStationList.execute()
            .filter { favoriteIds.contains($0.id) }
            .map(snapshot(from:))
    }

    func suggestedStations(limit: Int = 8) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        return try await graph.getSuggestedStations.execute(limit: Int32(limit)).map(snapshot(from:))
    }

    func stationSuggestions(matching query: String, limit: Int = 8) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        let stations = try await graph.filterStationsByQuery.execute(query: query)
        if stations.isEmpty {
            return try await suggestedStations(limit: limit)
        }
        return Array(stations.prefix(limit).map(snapshot(from:)))
    }

    func station(stationId: String) async throws -> WatchStationSnapshot? {
        try await refreshData()
        return graph.findStationById.execute(stationId: stationId).map(snapshot(from:))
    }

    func station(matching query: String?) async throws -> WatchStationSnapshot? {
        try await refreshData()
        return try await graph.findStationMatchingQuery.execute(query: query).map(snapshot(from:))
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        try await refreshData()
        return try await graph.resolveAssistantIntent.execute(action: action)
    }

    func openRoute(to stationId: String) async throws -> WatchStationSnapshot? {
        try await refreshData()
        guard let station = graph.findStationById.execute(stationId: stationId) else { return nil }
        graph.routeLauncher.launch(station: station)
        return snapshot(from: station)
    }

    func refreshData(forceRefresh: Bool = false) async throws {
        try await ensureBootstrapped()
        _ = try await graph.refreshStationDataIfNeeded.execute(forceRefresh: forceRefresh)
    }

    // MARK: - Private helpers

    private func ensureBootstrapped() async throws {
        guard !hasBootstrapped else { return }
        try await graph.bootstrapSession.execute()
        hasBootstrapped = true
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
