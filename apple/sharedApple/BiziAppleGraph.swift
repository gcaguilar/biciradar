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
        appConfiguration: AppConfiguration.companion.createDefault(),
        remoteConfigBridge: FirebaseBootstrap.remoteConfigBridge
    )

    private lazy var graphObject: NSObject = {
        let factoryClass = NSClassFromString("BMUMobileGraphFactory") as? NSObject.Type
        let factory = factoryClass?.perform(NSSelectorFromString("shared"))?.takeUnretainedValue()
        let graph = factory?.perform(
            NSSelectorFromString("createPlatformBindings:"),
            with: bindings
        )?.takeUnretainedValue()

        guard let graphObject = graph as? NSObject else {
            preconditionFailure("No se pudo crear MobileGraphFactory desde BiziMobileUi.")
        }

        if let typedGraph = graphObject as? any SharedGraph {
            bindings.onGraphCreated(graph: typedGraph)
        }
        return graphObject
    }()
    private var hasBootstrapped = false

    // MARK: - Public API

    func refreshData(forceRefresh: Bool = false) async throws {
        try await ensureBootstrapped()
        let useCase = graphProperty("refreshStationDataIfNeeded")
        let _: SurfaceSnapshotBundle? = try await invokeAsyncObject(
            on: useCase,
            selector: "executeForceRefresh:completionHandler:",
            boolArg: forceRefresh
        )
    }

    func currentSelectedCity() async throws -> City {
        try await ensureBootstrapped()
        let useCase = graphProperty("getCurrentCity")
        return try invokeSyncObject(on: useCase, selector: "execute")
    }

    func setSelectedCity(_ city: City) async throws {
        try await ensureBootstrapped()
        let useCase = graphProperty("updateSelectedCity")
        try await invokeAsyncError(
            on: useCase,
            selector: "executeCity:completionHandler:",
            arg: city
        )
    }

    func nearestStation() async throws -> BiziStationSnapshot? {
        try await refreshData()
        let useCase = graphProperty("findNearestStation")
        let station: Station? = try await invokeAsyncObject(
            on: useCase,
            selector: "executeWithCompletionHandler:"
        )
        return station.map(snapshot(from:))
    }

    func nearestStationWithBikes() async throws -> BiziStationSnapshot? {
        try await refreshData()
        let useCase = graphProperty("findNearestStationWithBikes")
        let station: Station? = try await invokeAsyncObject(
            on: useCase,
            selector: "executeWithCompletionHandler:"
        )
        return station.map(snapshot(from:))
    }

    func nearestStationWithSlots() async throws -> BiziStationSnapshot? {
        try await refreshData()
        let useCase = graphProperty("findNearestStationWithSlots")
        let station: Station? = try await invokeAsyncObject(
            on: useCase,
            selector: "executeWithCompletionHandler:"
        )
        return station.map(snapshot(from:))
    }

    func favoriteStations() async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let useCase = graphProperty("getFavoriteStationList")
        let stations: [Station] = try await invokeAsyncObject(
            on: useCase,
            selector: "executeWithCompletionHandler:"
        ) ?? []
        return stations.map(snapshot(from:))
    }

    func suggestedStations(limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let useCase = graphProperty("getSuggestedStations")
        let stations: [Station] = try await invokeAsyncObject(
            on: useCase,
            selector: "executeLimit:completionHandler:",
            int32Arg: Int32(limit)
        ) ?? []
        return stations.map(snapshot(from:))
    }

    func stationSuggestions(matching query: String, limit: Int = 8) async throws -> [BiziStationSnapshot] {
        try await refreshData()
        let useCase = graphProperty("filterStationsByQuery")
        let stations: [Station] = try await invokeAsyncObject(
            on: useCase,
            selector: "executeQuery:completionHandler:",
            arg: query as NSString
        ) ?? []
        if stations.isEmpty {
            return try await suggestedStations(limit: limit)
        }
        return Array(stations.prefix(limit).map(snapshot(from:)))
    }

    func station(matching query: String?) async throws -> BiziStationSnapshot? {
        try await refreshData()
        let useCase = graphProperty("findStationMatchingQuery")
        let station: Station? = try await invokeAsyncObject(
            on: useCase,
            selector: "executeQuery:completionHandler:",
            arg: query as NSString?
        )
        return station.map(snapshot(from:))
    }

    func station(stationId: String) async throws -> BiziStationSnapshot? {
        try await refreshData()
        let useCase = graphProperty("findStationById")
        let station: Station? = try invokeSyncObject(
            on: useCase,
            selector: "executeStationId:",
            arg: stationId as NSString
        )
        return station.map(snapshot(from:))
    }

    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution {
        try await refreshData()
        let useCase = graphProperty("resolveAssistantIntent")
        let resolution: AssistantResolution? = try await invokeAsyncObject(
            on: useCase,
            selector: "executeAction:completionHandler:",
            arg: action as AnyObject
        )
        if let resolution {
            return resolution
        }
        throw AppleGraphError.emptyAssistantResponse
    }

    func routeToStation(named query: String?) async throws -> BiziStationSnapshot? {
        guard let station = try await station(matching: query) else { return nil }
        let useCase = graphProperty("findStationById")
        let target: Station? = try invokeSyncObject(
            on: useCase,
            selector: "executeStationId:",
            arg: station.id as NSString
        )
        if let target,
           let graph = graphObject as? any SharedGraph {
            graph.routeLauncher.launch(station: target)
        }
        return station
    }

    func deliverSavedPlaceAlertNotificationsIfNeeded() async throws {
        try await ensureBootstrapped()
        let useCase = graphProperty("evaluateSavedPlaceAlerts")
        let triggers: [SavedPlaceAlertTrigger] = try await invokeAsyncObject(
            on: useCase,
            selector: "executeNowEpoch:completionHandler:",
            int64Arg: Int64(Date().timeIntervalSince1970 * 1000)
        ) ?? []
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
        let useCase = graphProperty("bootstrapSession")
        try await invokeAsyncError(
            on: useCase,
            selector: "executeWithCompletionHandler:"
        )
        hasBootstrapped = true
    }

    private func graphProperty(_ name: String) -> NSObject {
        guard let value = graphObject.perform(NSSelectorFromString(name))?.takeUnretainedValue() as? NSObject else {
            preconditionFailure("No se pudo resolver la propiedad '\(name)' del grafo iOS.")
        }
        return value
    }

    private func invokeSyncObject<T>(
        on target: NSObject,
        selector: String
    ) throws -> T {
        let sel = NSSelectorFromString(selector)
        guard let value = target.perform(sel)?.takeUnretainedValue() as? T else {
            throw AppleGraphError.missingGraphBinding(selector)
        }
        return value
    }

    private func invokeSyncObject<T>(
        on target: NSObject,
        selector: String,
        arg: AnyObject?
    ) throws -> T? {
        let sel = NSSelectorFromString(selector)
        return target.perform(sel, with: arg)?.takeUnretainedValue() as? T
    }

    private func invokeAsyncError(
        on target: NSObject,
        selector: String
    ) async throws {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, @escaping (NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            method(target, sel) { error in
                if let error { continuation.resume(throwing: error) } else { continuation.resume() }
            }
        }
    }

    private func invokeAsyncError(
        on target: NSObject,
        selector: String,
        arg: AnyObject
    ) async throws {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, AnyObject, @escaping (NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            method(target, sel, arg) { error in
                if let error { continuation.resume(throwing: error) } else { continuation.resume() }
            }
        }
    }

    private func invokeAsyncObject<T>(
        on target: NSObject,
        selector: String
    ) async throws -> T? {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, @escaping (AnyObject?, NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T?, Error>) in
            method(target, sel) { value, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: value as? T)
                }
            }
        }
    }

    private func invokeAsyncObject<T>(
        on target: NSObject,
        selector: String,
        arg: AnyObject?
    ) async throws -> T? {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, AnyObject?, @escaping (AnyObject?, NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T?, Error>) in
            method(target, sel, arg) { value, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: value as? T)
                }
            }
        }
    }

    private func invokeAsyncObject<T>(
        on target: NSObject,
        selector: String,
        boolArg: Bool
    ) async throws -> T? {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, Bool, @escaping (AnyObject?, NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T?, Error>) in
            method(target, sel, boolArg) { value, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: value as? T)
                }
            }
        }
    }

    private func invokeAsyncObject<T>(
        on target: NSObject,
        selector: String,
        int32Arg: Int32
    ) async throws -> T? {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, Int32, @escaping (AnyObject?, NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T?, Error>) in
            method(target, sel, int32Arg) { value, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: value as? T)
                }
            }
        }
    }

    private func invokeAsyncObject<T>(
        on target: NSObject,
        selector: String,
        int64Arg: Int64
    ) async throws -> T? {
        let sel = NSSelectorFromString(selector)
        typealias Method = @convention(c) (AnyObject, Selector, Int64, @escaping (AnyObject?, NSError?) -> Void) -> Void
        let method = unsafeBitCast(target.method(for: sel), to: Method.self)
        return try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<T?, Error>) in
            method(target, sel, int64Arg) { value, error in
                if let error {
                    continuation.resume(throwing: error)
                } else {
                    continuation.resume(returning: value as? T)
                }
            }
        }
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
