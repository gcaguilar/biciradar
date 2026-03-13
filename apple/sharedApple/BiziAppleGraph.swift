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
            stationsApiUrl: "https://www.zaragoza.es/sede/servicio/urbanismo-infraestructuras/estacion-bicicleta.json?rows=300",
            stationsFallbackApiUrl: "https://api.citybik.es/v2/networks/bizi-zaragoza",
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
            try await bootstrapSettings()
            try await bootstrapFavorites()
            hasBootstrapped = true
        }
        try await refreshStations()
    }

    func nearestStation() async throws -> BiziStationSnapshot? {
        try await refreshData()
        let snapshots = stationsState().stations.map(snapshot(from:))
        return selectNearestSnapshot(from: snapshots, radiusMeters: currentSearchRadiusMeters())
    }

    func nearestStationWithBikes() async throws -> BiziStationSnapshot? {
        try await refreshData()
        let snapshots = stationsState().stations.map(snapshot(from:))
        return selectNearestSnapshot(from: snapshots, radiusMeters: currentSearchRadiusMeters()) { station in
            station.bikesAvailable > 0
        }
    }

    func nearestStationWithSlots() async throws -> BiziStationSnapshot? {
        try await refreshData()
        let snapshots = stationsState().stations.map(snapshot(from:))
        return selectNearestSnapshot(from: snapshots, radiusMeters: currentSearchRadiusMeters()) { station in
            station.slotsFree > 0
        }
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
        let snapshots = stationsState().stations.map(snapshot(from:))
        guard let query else { return snapshots.first }
        let normalizedQuery = normalizeStationQuery(query)
        guard !normalizedQuery.isEmpty else { return snapshots.first }
        if let pinnedStationId = pinnedStationId(for: normalizedQuery),
           let pinnedStation = try await station(stationId: pinnedStationId) {
            return pinnedStation
        }
        let numericQuery = query.filter(\.isNumber)
        return snapshots.first(where: { station in
            station.matches(normalizedQuery: normalizedQuery, numericQuery: numericQuery)
        })
    }

    func station(stationId: String) async throws -> BiziStationSnapshot? {
        try await refreshData()
        return graph.stationsRepository.stationById(stationId: stationId).map(snapshot(from:))
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
                favoriteIds: favoriteIds,
                searchRadiusMeters: Int32(currentSearchRadiusMeters())
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

    private func bootstrapSettings() async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            graph.settingsRepository.bootstrap { error in
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
            graph.stationsRepository.loadIfNeeded { error in
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

    private func currentSearchRadiusMeters() -> Int {
        Int(graph.settingsRepository.currentSearchRadiusMeters())
    }

    private func pinnedStationId(for normalizedQuery: String) -> String? {
        switch normalizedQuery {
        case "casa", "mi casa", "home":
            return graph.favoritesRepository.currentHomeStationId()
        case "trabajo", "mi trabajo", "work", "oficina", "mi oficina":
            return graph.favoritesRepository.currentWorkStationId()
        default:
            return nil
        }
    }
}

private func normalizeStationQuery(_ value: String) -> String {
    value
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
}

private extension BiziStationSnapshot {
    func matches(normalizedQuery: String, numericQuery: String) -> Bool {
        let normalizedName = name.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
        let normalizedAddress = address.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
        let normalizedId = id.folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
        let stationNumericId = id.filter(\.isNumber)
        return normalizedName.contains(normalizedQuery) ||
            normalizedAddress.contains(normalizedQuery) ||
            normalizedId == normalizedQuery ||
            normalizedId.contains(normalizedQuery) ||
            (!numericQuery.isEmpty && stationNumericId == numericQuery)
    }
}

private func selectNearestSnapshot(
    from snapshots: [BiziStationSnapshot],
    radiusMeters: Int,
    predicate: (BiziStationSnapshot) -> Bool = { _ in true }
) -> BiziStationSnapshot? {
    snapshots.first(where: { $0.distanceMeters <= radiusMeters && predicate($0) }) ??
        snapshots.first(where: predicate)
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
