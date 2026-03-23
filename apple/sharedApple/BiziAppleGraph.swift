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
        appConfiguration: AppConfiguration.companion.createDefault()
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

    func suggestedStations(limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let state = stationsState()
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

    func stationSuggestions(matching query: String, limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let snapshots = stationsState().stations.map(snapshot(from:))
        let normalizedQuery = normalizeStationSearchText(query)
        guard !normalizedQuery.isEmpty else {
            return try await suggestedStations(limit: limit)
        }
        let pinnedStationId = pinnedStationId(for: normalizedQuery)
        let numericQuery = query.filter(\.isNumber)
        return Array(
            snapshots
                .compactMap { snapshot -> (Int, BiziStationSnapshot)? in
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

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
        guard let query else {
            return try await suggestedStations(limit: 1).first
        }
        let matches = try await stationSuggestions(matching: query, limit: 1)
        return matches.first
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

private func normalizeStationSearchText(_ value: String?) -> String {
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
        .filter { !$0.isEmpty && !appleStationStopwords.contains($0) }
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

private extension BiziStationSnapshot {
    func searchScore(
        normalizedQuery: String,
        numericQuery: String,
        pinnedStationId: String?,
    ) -> Int? {
        if id == pinnedStationId {
            return 1000
        }

        let normalizedName = normalizeStationSearchText(name)
        let normalizedAddress = normalizeStationSearchText(address)
        let normalizedId = normalizeStationSearchText(id)
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

private let appleStationStopwords: Set<String> = [
    "de",
    "del",
    "la",
    "las",
    "el",
    "los",
]
