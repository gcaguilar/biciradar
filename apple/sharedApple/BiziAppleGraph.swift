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

    private lazy var graph: any SharedGraph = {
        let g = MobileGraphFactory.shared.create(platformBindings: bindings)
        bindings.onGraphCreated(graph: g)
        return g
    }()
    private var hasBootstrapped = false

    // MARK: - Public API

    func refreshData(forceRefresh: Bool = false) async throws {
        try await ensureBootstrapped()
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            graph.refreshStationDataIfNeeded.execute(forceRefresh: forceRefresh) { _, error in
                if let error { continuation.resume(throwing: error) } else { continuation.resume() }
            }
        }
    }

    func currentSelectedCity() async throws -> City {
        try await ensureBootstrapped()
        return graph.getCurrentCity.execute()
    }

    func setSelectedCity(_ city: City) async throws {
        try await ensureBootstrapped()
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            graph.updateSelectedCity.execute(city: city) { error in
                if let error { continuation.resume(throwing: error) } else { continuation.resume() }
            }
        }
    }

    func nearestStation() async throws -> BiziStationSnapshot? {
        try await refreshData()
        return try await graph.findNearestStation.execute().map(snapshot(from:))
    }

    func nearestStationWithBikes() async throws -> BiziStationSnapshot? {
        try await refreshData()
        return try await graph.findNearestStationWithBikes.execute().map(snapshot(from:))
    }

    func nearestStationWithSlots() async throws -> BiziStationSnapshot? {
        try await refreshData()
        return try await graph.findNearestStationWithSlots.execute().map(snapshot(from:))
    }

    func favoriteStations() async throws -> [BiziStationSnapshot] {
        try await refreshData()
        return try await graph.getFavoriteStationList.execute().map(snapshot(from:))
    }

    func suggestedStations(limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshData()
        return try await graph.getSuggestedStations.execute(limit: Int32(limit)).map(snapshot(from:))
    }

    func stationSuggestions(matching query: String, limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let stations = try await graph.filterStationsByQuery.execute(query: query)
        if stations.isEmpty {
            return try await suggestedStations(limit: limit)
        }
        return Array(stations.prefix(limit).map(snapshot(from:)))
    }

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
        try await refreshData()
        return try await graph.findStationMatchingQuery.execute(query: query).map(snapshot(from:))
    }

    func station(stationId: String) async throws -> BiziStationSnapshot? {
        try await refreshData()
        return graph.findStationById.execute(stationId: stationId).map(snapshot(from:))
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        try await refreshData()
        return try await withCheckedThrowingContinuation { continuation in
            graph.resolveAssistantIntent.execute(action: action) { resolution, error in
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
        guard let station = try await station(matching: query) else { return nil }
        if let target = graph.findStationById.execute(stationId: station.id) {
            graph.routeLauncher.launch(station: target)
        }
        return station
    }

    /// Evaluates saved-place alert rules and fires platform notifications where needed.
    func deliverSavedPlaceAlertNotificationsIfNeeded() async throws {
        try await ensureBootstrapped()
        let triggers = try await graph.evaluateSavedPlaceAlerts.execute(
            nowEpoch: Int64(Date().timeIntervalSince1970 * 1000)
        )
        guard !triggers.isEmpty else { return }
        let hasNotificationPermission = try await bindings.localNotifier.hasPermission().boolValue
        guard hasNotificationPermission else { return }
        for trigger in triggers {
            await MainActor.run {
                BiziNotificationService.shared.postSavedPlaceAlert(
                    title: trigger.notificationTitle(),
                    body: trigger.notificationBody(),
                    ruleId: trigger.ruleId
                )
            }
        }
    }

    // MARK: - Private helpers

    private func ensureBootstrapped() async throws {
        guard !hasBootstrapped else { return }
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            graph.bootstrapSession.execute { error in
                if let error { continuation.resume(throwing: error) } else { continuation.resume() }
            }
        }
        hasBootstrapped = true
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
