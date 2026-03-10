import AppIntents
import BiziMobileUi

struct NearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación más cercana"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziAppleGraph.shared.nearestStation() else {
                return .result(dialog: "No he encontrado estaciones de Bizi ahora mismo.")
            }
            return .result(
                dialog: "La estación más cercana es \(station.name), a \(station.distanceMeters) metros, con \(station.bikesAvailable) bicis y \(station.slotsFree) huecos."
            )
        } catch {
            return .result(dialog: "No he podido consultar Bizi Zaragoza ahora mismo.")
        }
    }
}

struct FavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Mis favoritas"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        do {
            let favorites = try await BiziAppleGraph.shared.favoriteStations()
            guard !favorites.isEmpty else {
                return .result(dialog: "Todavía no tienes estaciones favoritas guardadas.")
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return .result(
                dialog: "Tus favoritas son \(summary). Tienes \(favorites.count) en total."
            )
        } catch {
            return .result(dialog: "No he podido leer tus favoritas ahora mismo.")
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
            intent: RouteToStationIntent(),
            phrases: [
                "Llévame a una estación con \(.applicationName)"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
    }
}
