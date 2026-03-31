import BiziSharedCore
import Foundation

actor BiziWatchGraph {
    static let shared = BiziWatchGraph()

    private let bindings = WatchOSPlatformBindings(
        appConfiguration: AppConfiguration.companion.createDefault()
    )

    private lazy var graph: any SharedGraph = SharedGraphCompanion.shared.create(
        platformBindings: bindings
    )
    private var hasBootstrapped = false

    func nearbyStations(limit: Int = 5) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        return Array(try stationsState().stations.prefix(limit).map(snapshot(from:)))
    }

    func nearestStation() async throws -> WatchStationSnapshot? {
        try await refreshData()
        let snapshots = try stationsState().stations.map(snapshot(from:))
        return selectNearestWatchSnapshot(from: snapshots, radiusMeters: currentSearchRadiusMeters())
    }

    func nearestStationWithBikes() async throws -> WatchStationSnapshot? {
        try await refreshData()
        let snapshots = try stationsState().stations.map(snapshot(from:))
        return selectNearestWatchSnapshot(from: snapshots, radiusMeters: currentSearchRadiusMeters()) { station in
            station.bikesAvailable > 0
        }
    }

    func nearestStationWithSlots() async throws -> WatchStationSnapshot? {
        try await refreshData()
        let snapshots = try stationsState().stations.map(snapshot(from:))
        return selectNearestWatchSnapshot(from: snapshots, radiusMeters: currentSearchRadiusMeters()) { station in
            station.slotsFree > 0
        }
    }

    func favoriteStations(favoriteIds: Set<String>) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        guard !favoriteIds.isEmpty else { return [] }
        return try stationsState().stations
            .filter { favoriteIds.contains($0.id) }
            .map(snapshot(from:))
    }

    func suggestedStations(limit: Int = 8) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        let state = try stationsState()
        let favoriteIds = Set(
            state.stations
                .filter { graph.favoritesRepository.isFavorite(stationId: $0.id) }
                .map(\.id)
        )
        let homeStationId = graph.favoritesRepository.currentHomeStationId()
        let workStationId = graph.favoritesRepository.currentWorkStationId()
        let rankedStations = state.stations.sorted { lhs, rhs in
            let lhsPriority = lhs.suggestionPriority(
                favoriteIds: favoriteIds,
                homeStationId: homeStationId,
                workStationId: workStationId
            )
            let rhsPriority = rhs.suggestionPriority(
                favoriteIds: favoriteIds,
                homeStationId: homeStationId,
                workStationId: workStationId
            )
            if lhsPriority != rhsPriority {
                return lhsPriority > rhsPriority
            }
            if lhs.distanceMeters != rhs.distanceMeters {
                return lhs.distanceMeters < rhs.distanceMeters
            }
            return lhs.name.localizedCaseInsensitiveCompare(rhs.name) == .orderedAscending
        }
        return Array(rankedStations.prefix(limit).map(snapshot(from:)))
    }

    func stationSuggestions(matching query: String, limit: Int = 8) async throws -> [WatchStationSnapshot] {
        try await refreshData()
        let snapshots = try stationsState().stations.map(snapshot(from:))
        let normalizedQuery = normalizeWatchStationSearchText(query)
        guard !normalizedQuery.isEmpty else {
            return try await suggestedStations(limit: limit)
        }
        let pinnedStationId = pinnedStationId(for: normalizedQuery)
        let numericQuery = query.filter(\.isNumber)
        return Array(
            snapshots
                .compactMap { snapshot -> (Int, WatchStationSnapshot)? in
                    snapshot.searchScore(
                        normalizedQuery: normalizedQuery,
                        numericQuery: numericQuery,
                        pinnedStationId: pinnedStationId
                    ).map { ($0, snapshot) }
                }
                .sorted { lhs, rhs in
                    if lhs.0 != rhs.0 {
                        return lhs.0 > rhs.0
                    }
                    if lhs.1.distanceMeters != rhs.1.distanceMeters {
                        return lhs.1.distanceMeters < rhs.1.distanceMeters
                    }
                    return lhs.1.name.localizedCaseInsensitiveCompare(rhs.1.name) == .orderedAscending
                }
                .map(\.1)
                .prefix(limit)
        )
    }

    func station(stationId: String) async throws -> WatchStationSnapshot? {
        try await refreshData()
        return graph.stationsRepository.stationById(stationId: stationId).map(snapshot(from:))
    }

    func station(matching query: String?) async throws -> WatchStationSnapshot? {
        guard let query else {
            return try await suggestedStations(limit: 1).first
        }
        let matches = try await stationSuggestions(matching: query, limit: 1)
        return matches.first
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        try await refreshData()
        let state = try stationsState()
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
                    continuation.resume(throwing: WatchGraphError.emptyAssistantResponse)
                }
            }
        }
    }

    func openRoute(to stationId: String) async throws -> WatchStationSnapshot? {
        try await refreshData()
        guard let station = graph.stationsRepository.stationById(stationId: stationId) else {
            return nil
        }
        graph.routeLauncher.launch(station: station)
        return snapshot(from: station)
    }

    func refreshData(forceRefresh: Bool = false) async throws {
        if !hasBootstrapped {
            try await bootstrapSettings()
            try await bootstrapFavorites()
            hasBootstrapped = true
        }
        try await refreshStations(forceRefresh: forceRefresh)
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

    private func refreshStations(forceRefresh: Bool = false) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            let completion: (Error?) -> Void = { error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume()
                }
            }
            if forceRefresh {
                graph.stationsRepository.forceRefresh { error in completion(error) }
            } else {
                graph.stationsRepository.loadIfNeeded { error in completion(error) }
            }
        }
    }

    private func stationsState() throws -> StationsState {
        guard let state = graph.stationsRepository.state.value as? StationsState else {
            throw WatchGraphError.invalidStationsState
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

private func selectNearestWatchSnapshot(
    from snapshots: [WatchStationSnapshot],
    radiusMeters: Int,
    predicate: (WatchStationSnapshot) -> Bool = { _ in true }
) -> WatchStationSnapshot? {
    snapshots.first(where: { $0.distanceMeters <= radiusMeters && predicate($0) }) ??
        snapshots.first(where: predicate)
}

enum WatchGraphError: LocalizedError {
    case emptyAssistantResponse
    case invalidStationsState

    var errorDescription: String? {
        switch self {
        case .emptyAssistantResponse:
            return "No se recibió respuesta del asistente del reloj."
        case .invalidStationsState:
            return "No se pudo leer el estado de estaciones del repositorio."
        }
    }
}

private func normalizeWatchStationSearchText(_ value: String?) -> String {
    var normalized = (value ?? "")
        .trimmingCharacters(in: CharacterSet.whitespacesAndNewlines)
        .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: Locale.current)
        .lowercased()
    normalized = normalized.replacingOccurrences(of: "\\bc/\\s*", with: " calle ", options: NSString.CompareOptions.regularExpression)
    normalized = normalized.replacingOccurrences(of: "\\bpza\\.?\\b", with: " plaza ", options: NSString.CompareOptions.regularExpression)
    normalized = normalized.replacingOccurrences(of: "\\bavda\\.?\\b", with: " avenida ", options: NSString.CompareOptions.regularExpression)
    normalized = normalized.replacingOccurrences(of: "\\bav\\.?\\b", with: " avenida ", options: NSString.CompareOptions.regularExpression)
    normalized = normalized.replacingOccurrences(of: "[^a-z0-9 ]", with: " ", options: NSString.CompareOptions.regularExpression)
    let tokens = normalized
        .replacingOccurrences(of: "\\s+", with: " ", options: NSString.CompareOptions.regularExpression)
        .split(separator: " ")
        .map(String.init)
        .filter { !$0.isEmpty && !watchStationStopwords.contains($0) }
    return tokens.joined(separator: " ")
}

private extension Station {
    func suggestionPriority(
        favoriteIds: Set<String>,
        homeStationId: String?,
        workStationId: String?,
    ) -> Int {
        if id == homeStationId { return 400 }
        if id == workStationId { return 380 }
        if favoriteIds.contains(id) { return 320 }
        return 100
    }
}

extension WatchStationSnapshot {
    func searchScore(
        normalizedQuery: String,
        numericQuery: String,
        pinnedStationId: String?,
    ) -> Int? {
        if id == pinnedStationId {
            return 1000
        }

        let normalizedName = normalizeWatchStationSearchText(name)
        let normalizedAddress = normalizeWatchStationSearchText(address)
        let normalizedId = normalizeWatchStationSearchText(id)
        let stationNumericId = id.filter(\.isNumber)
        let queryTokens = Set(normalizedQuery.split(separator: " ").map(String.init))
        let nameTokens = Set(normalizedName.split(separator: " ").map(String.init))
        let addressTokens = Set(normalizedAddress.split(separator: " ").map(String.init))

        if normalizedId == normalizedQuery || (!numericQuery.isEmpty && stationNumericId == numericQuery) {
            return 950
        }
        if normalizedName == normalizedQuery {
            return 900
        }
        if normalizedAddress == normalizedQuery {
            return 860
        }
        if normalizedName.hasPrefix(normalizedQuery) {
            return 820
        }
        if normalizedAddress.hasPrefix(normalizedQuery) {
            return 780
        }
        if !queryTokens.isEmpty && queryTokens.isSubset(of: nameTokens) {
            return 740
        }
        if !queryTokens.isEmpty && queryTokens.isSubset(of: addressTokens) {
            return 700
        }
        if normalizedName.contains(normalizedQuery) {
            return 660
        }
        if normalizedAddress.contains(normalizedQuery) {
            return 620
        }
        if normalizedId.contains(normalizedQuery) || (!numericQuery.isEmpty && stationNumericId.contains(numericQuery)) {
            return 580
        }
        return nil
    }
}

private let watchStationStopwords: Set<String> = [
    "de",
    "del",
    "la",
    "las",
    "el",
    "los",
]
