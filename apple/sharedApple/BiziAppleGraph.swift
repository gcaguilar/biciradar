import BiziMobileUi
import Foundation

struct BiziStationSnapshot: Identifiable, Hashable {
    let id: String
    let name: String
    let address: String
    let bikesAvailable: Int
    let slotsFree: Int
    let distanceMeters: Int
}

actor BiziAppleGraph {
    static let shared = BiziAppleGraph()

    private let bindings = IOSPlatformBindings(
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

    func refreshData() async throws {
        if !hasBootstrapped {
            try await bootstrapFavorites()
            hasBootstrapped = true
        }
        try await refreshStations()
    }

    func nearestStation() async throws -> BiziStationSnapshot? {
        try await refreshData()
        return stationsState().stations.first.map(snapshot(from:))
    }

    func favoriteStations() async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let state = stationsState()
        return state.stations
            .filter { graph.favoritesRepository.isFavorite(stationId: $0.id) }
            .map(snapshot(from:))
    }

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
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

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        try await refreshData()
        let state = stationsState()
        let favoriteIds = Set(
            state.stations
                .filter { graph.favoritesRepository.isFavorite(stationId: $0.id) }
                .map(\.id)
        )
        return try await withCheckedThrowingContinuation { continuation in
            graph.assistantIntentResolver.resolve(
                action: action,
                stationsState: state,
                favoriteIds: favoriteIds
            ) { resolution, error in
                if let error {
                    continuation.resume(throwing: error)
                } else if let resolution {
                    continuation.resume(returning: resolution)
                } else {
                    continuation.resume(throwing: AppleGraphError.emptyAssistantResponse)
                }
            }
        }
    }

    func routeToStation(named query: String?) async throws -> BiziStationSnapshot? {
        guard let station = try await station(matching: query) else {
            return nil
        }
        try await refreshData()
        if let target = graph.stationsRepository.stationById(stationId: station.id) {
            graph.routeLauncher.launch(station: target)
        }
        return station
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

    private func snapshot(from station: Station) -> BiziStationSnapshot {
        BiziStationSnapshot(
            id: station.id,
            name: station.name,
            address: station.address,
            bikesAvailable: Int(station.bikesAvailable),
            slotsFree: Int(station.slotsFree),
            distanceMeters: Int(station.distanceMeters)
        )
    }
}

enum AppleGraphError: LocalizedError {
    case emptyAssistantResponse

    var errorDescription: String? {
        switch self {
        case .emptyAssistantResponse:
            return "No se recibió respuesta del asistente."
        }
    }
}
