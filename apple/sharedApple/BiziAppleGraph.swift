import BiziMobileUi
import Foundation
import WidgetKit

struct BiziStationSnapshot: Identifiable, Hashable {
    let id: String
    let name: String
    let address: String
    let bikesAvailable: Int
    let slotsFree: Int
    let distanceMeters: Int
}

// MARK: - BiziAppleGraph

/// Actor que gestiona el grafo de dependencias KMP para iOS.
///
/// **Antes** este actor accedía al grafo KMP mediante reflexión dinámica
/// (`NSClassFromString`, `NSSelectorFromString`, `unsafeBitCast`), lo que
/// convertía cambios de API en fallos en runtime y dejaba la integración sin
/// contrato compilable.
///
/// **Ahora** el grafo se crea una sola vez a través de `MobileGraphFactory` y
/// todas las operaciones se delegan a `BiziAppleFacade` — una facade KMP con
/// tipos estáticos. Cualquier cambio de API rompe la compilación de Xcode,
/// no el runtime.
actor BiziAppleGraph {
    static let shared = BiziAppleGraph()

    private let bindings = IOSPlatformBindings(
        appConfiguration: AppConfiguration.companion.createDefault(),
        remoteConfigBridge: FirebaseBootstrap.remoteConfigBridge,
        crashlyticsBridge: FirebaseBootstrap.crashlyticsBridge
    )

    /// Grafo KMP creado una sola vez. Se inicializa de forma lazy al primer acceso.
    private lazy var graph: any SharedGraph = {
        let graph = MobileGraphFactory.shared.create(platformBindings: bindings)
        bindings.onGraphCreated(graph: graph)
        return graph
    }()

    /// Facade tipada. Se construye la primera vez que se llama a `ensureFacade()`.
    private var _facade: BiziAppleFacade?

    // MARK: - Public API

    func refreshData(forceRefresh: Bool = false) async throws {
        try await refreshIfNeeded(forceRefresh: forceRefresh)
    }

    func currentSelectedCity() async throws -> City {
        let facade = try await ensureFacade()
        return facade.currentSelectedCity()
    }

    func setSelectedCity(_ city: City) async throws {
        let facade = try await ensureFacade()
        try await facade.setSelectedCity(city: city)
    }

    func nearestStation() async throws -> BiziStationSnapshot? {
        try await refreshIfNeeded()
        let station = try await ensureFacade().nearestStation()
        return station.map(snapshot(from:))
    }

    func nearestStationWithBikes() async throws -> BiziStationSnapshot? {
        try await refreshIfNeeded()
        let station = try await ensureFacade().nearestStationWithBikes()
        return station.map(snapshot(from:))
    }

    func nearestStationWithSlots() async throws -> BiziStationSnapshot? {
        try await refreshIfNeeded()
        let station = try await ensureFacade().nearestStationWithSlots()
        return station.map(snapshot(from:))
    }

    func favoriteStations() async throws -> [BiziStationSnapshot] {
        try await refreshIfNeeded()
        let stations = try await ensureFacade().favoriteStations()
        return stations.map(snapshot(from:))
    }

    func syncFavoritesFromPeer() async throws {
        let facade = try await ensureFacade()
        try await facade.syncFavoritesFromPeer()
    }

    func suggestedStations(limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshIfNeeded()
        let stations = try await ensureFacade().suggestedStations(limit: Int32(limit))
        return stations.map(snapshot(from:))
    }

    func nearbyStations(limit: Int = 5) async throws -> [BiziStationSnapshot] {
        try await refreshIfNeeded()
        let stations = try await ensureFacade().nearbyStations(limit: Int32(limit))
        return stations.map(snapshot(from:))
    }

    func stationSuggestions(matching query: String, limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshIfNeeded()
        let stations = try await ensureFacade().stationSuggestions(query: query, limit: Int32(limit))
        return stations.map(snapshot(from:))
    }

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
        try await refreshIfNeeded()
        let station = try await ensureFacade().stationMatchingQuery(query: query)
        return station.map(snapshot(from:))
    }

    func station(stationId: String) async throws -> BiziStationSnapshot? {
        try await refreshIfNeeded()
        let facade = try await ensureFacade()
        let station = facade.stationById(stationId: stationId)
        return station.map(snapshot(from:))
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        try await refreshIfNeeded()
        return try await ensureFacade().assistantResponse(action: action)
    }

    func routeToStation(named query: String?) async throws -> BiziStationSnapshot? {
        let facade = try await ensureFacade()
        let station = try await facade.routeToStation(query: query)
        return station.map(snapshot(from:))
    }

    func deliverSavedPlaceAlertNotificationsIfNeeded() async throws {
        let facade = try await ensureFacade()
        let triggers = try await facade.evaluateSavedPlaceAlerts()
        guard !triggers.isEmpty else { return }
        let hasPermission = try await bindings.localNotifier.hasPermission().boolValue
        guard hasPermission else { return }
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

    @discardableResult
    func refreshWidgetData(reloadTimelines: Bool = true) async throws -> Bool {
        let success = try await graph.refreshWidgetDataUseCase.execute()
        guard success.boolValue else { return false }
        if reloadTimelines {
            await MainActor.run {
                WidgetCenter.shared.reloadAllTimelines()
            }
        }
        return true
    }

    // MARK: - Private helpers

    private func ensureFacade() async throws -> BiziAppleFacade {
        if let existing = _facade { return existing }
        let newFacade = BiziAppleFacade.companion.create(graph: graph)
        try await newFacade.bootstrap()
        _facade = newFacade
        return newFacade
    }

    /// Refresca datos de estaciones (ignora el snapshot devuelto por la facade).
    private func refreshIfNeeded(forceRefresh: Bool = false) async throws {
        let facade = try await ensureFacade()
        _ = try await facade.refreshData(forceRefresh: forceRefresh)
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

// MARK: - Errors

enum AppleGraphError: LocalizedError {
    case emptyAssistantResponse
    case missingGraphBinding(String)

    var errorDescription: String? {
        switch self {
        case .emptyAssistantResponse:
            return "No se recibió respuesta del asistente."
        case let .missingGraphBinding(selector):
            return "No se pudo invocar el selector \(selector) del grafo iOS."
        }
    }
}
