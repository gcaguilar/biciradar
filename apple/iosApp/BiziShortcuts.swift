import AppIntents
import BiziMobileUi

struct NearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación más cercana"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        await MainActor.run {
            AppleLaunchRequestStore.shared.save(MobileLaunchRequestNearestStation.shared)
        }
        do {
            let resolution = try await BiziAppleGraph.shared.assistantResponse(
                for: AssistantActionNearestStation.shared
            )
            guard let stationId = resolution.highlightedStationId else {
                return .result(dialog: "Abriendo Bizi Zaragoza para buscar una estación cercana.")
            }
            guard let station = try await BiziAppleGraph.shared.station(stationId: stationId) else {
                return .result(dialog: IntentDialog(stringLiteral: resolution.spokenResponse))
            }
            await MainActor.run {
                AppleLaunchRequestStore.shared.save(
                    MobileLaunchRequestShowStation(stationId: station.id)
                )
            }
            return .result(dialog: IntentDialog(stringLiteral: resolution.spokenResponse))
        } catch {
            return .result(dialog: "Abriendo Bizi Zaragoza para buscar una estación cercana.")
        }
    }
}

struct NearestStationWithBikesIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana con bicis"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        await MainActor.run {
            AppleLaunchRequestStore.shared.save(MobileLaunchRequestNearestStationWithBikes.shared)
        }
        do {
            let resolution = try await BiziAppleGraph.shared.assistantResponse(
                for: AssistantActionNearestStationWithBikes.shared
            )
            guard let stationId = resolution.highlightedStationId else {
                return .result(dialog: "Abriendo Bizi Zaragoza para buscar una estación cercana con bicis disponibles.")
            }
            guard let station = try await BiziAppleGraph.shared.station(stationId: stationId) else {
                return .result(dialog: IntentDialog(stringLiteral: resolution.spokenResponse))
            }
            await MainActor.run {
                AppleLaunchRequestStore.shared.save(
                    MobileLaunchRequestShowStation(stationId: station.id)
                )
            }
            return .result(dialog: IntentDialog(stringLiteral: resolution.spokenResponse))
        } catch {
            return .result(dialog: "Abriendo Bizi Zaragoza para buscar una estación cercana con bicis disponibles.")
        }
    }
}

struct NearestStationWithSlotsIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana con huecos"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        await MainActor.run {
            AppleLaunchRequestStore.shared.save(MobileLaunchRequestNearestStationWithSlots.shared)
        }
        do {
            let resolution = try await BiziAppleGraph.shared.assistantResponse(
                for: AssistantActionNearestStationWithSlots.shared
            )
            guard let stationId = resolution.highlightedStationId else {
                return .result(dialog: "Abriendo Bizi Zaragoza para buscar una estación cercana con huecos libres.")
            }
            guard let station = try await BiziAppleGraph.shared.station(stationId: stationId) else {
                return .result(dialog: IntentDialog(stringLiteral: resolution.spokenResponse))
            }
            await MainActor.run {
                AppleLaunchRequestStore.shared.save(
                    MobileLaunchRequestShowStation(stationId: station.id)
                )
            }
            return .result(dialog: IntentDialog(stringLiteral: resolution.spokenResponse))
        } catch {
            return .result(dialog: "Abriendo Bizi Zaragoza para buscar una estación cercana con huecos libres.")
        }
    }
}

struct FavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Mis favoritas"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        await MainActor.run {
            AppleLaunchRequestStore.shared.save(MobileLaunchRequestFavorites.shared)
        }
        do {
            let favorites = try await BiziAppleGraph.shared.favoriteStations()
            guard !favorites.isEmpty else {
                return .result(dialog: "Abriendo Bizi Zaragoza. Todavía no tienes estaciones favoritas guardadas.")
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return .result(
                dialog: "Abriendo tus favoritas. Tienes \(favorites.count) en total: \(summary)."
            )
        } catch {
            return .result(dialog: "Abriendo Bizi Zaragoza para mostrar tus favoritas.")
        }
    }
}

struct StationStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de estación"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre de estación")
    var stationName: String?

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziAppleGraph.shared.station(matching: stationName) else {
                return .result(dialog: "No he encontrado esa estación en Bizi Zaragoza.")
            }
            return .result(
                dialog: "\(station.name) tiene \(station.bikesAvailable) bicis disponibles y \(station.slotsFree) huecos libres."
            )
        } catch {
            return .result(dialog: "No he podido consultar el estado de esa estación.")
        }
    }
}

struct StationBikeCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Bicis en estación"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre o número de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziAppleGraph.shared.station(matching: stationName) else {
                return .result(dialog: "No he encontrado esa estación en Bizi Zaragoza.")
            }
            return .result(
                dialog: "\(station.name) tiene \(station.bikesAvailable) bicis disponibles."
            )
        } catch {
            return .result(dialog: "No he podido consultar las bicis de esa estación.")
        }
    }
}

struct StationSlotCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Huecos en estación"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre o número de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziAppleGraph.shared.station(matching: stationName) else {
                return .result(dialog: "No he encontrado esa estación en Bizi Zaragoza.")
            }
            return .result(
                dialog: "\(station.name) tiene \(station.slotsFree) huecos libres."
            )
        } catch {
            return .result(dialog: "No he podido consultar los huecos de esa estación.")
        }
    }
}

struct RouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a estación"
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Nombre de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziAppleGraph.shared.station(matching: stationName) else {
                return .result(dialog: "No he encontrado esa estación en Zaragoza Bizi.")
            }
            await MainActor.run {
                AppleLaunchRequestStore.shared.save(
                    MobileLaunchRequestRouteToStation(stationId: station.id)
                )
            }
            return .result(dialog: "Abriendo una ruta hacia \(station.name).")
        } catch {
            return .result(dialog: "No he podido preparar esa ruta ahora mismo.")
        }
    }
}

struct BiziAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: NearestStationIntent(),
            phrases: [
                "Muéstrame la estación más cercana en \(.applicationName)"
            ],
            shortTitle: "Estación cercana",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: NearestStationWithBikesIntent(),
            phrases: [
                "Muéstrame la estación más cercana con bicis en \(.applicationName)"
            ],
            shortTitle: "Con bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: NearestStationWithSlotsIntent(),
            phrases: [
                "Muéstrame la estación más cercana con huecos en \(.applicationName)"
            ],
            shortTitle: "Con huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: FavoriteStationsIntent(),
            phrases: [
                "Abre mis favoritas en \(.applicationName)"
            ],
            shortTitle: "Favoritas",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: StationStatusIntent(),
            phrases: [
                "Enséñame el estado de una estación en \(.applicationName)"
            ],
            shortTitle: "Estado",
            systemImageName: "info.circle"
        )
        AppShortcut(
            intent: StationBikeCountIntent(),
            phrases: [
                "Enséñame cuántas bicis tiene una estación en \(.applicationName)"
            ],
            shortTitle: "Bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: StationSlotCountIntent(),
            phrases: [
                "Enséñame cuántos huecos tiene una estación en \(.applicationName)"
            ],
            shortTitle: "Huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: RouteToStationIntent(),
            phrases: [
                "Llévame a una estación con \(.applicationName)"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
    }
}
