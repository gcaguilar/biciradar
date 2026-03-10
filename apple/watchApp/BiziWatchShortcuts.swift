import AppIntents

struct WatchNearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana en reloj"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziWatchGraph.shared.nearestStation() else {
                return .result(dialog: "No he encontrado estaciones de Bizi cerca de ti ahora mismo.")
            }
            return .result(
                dialog: "\(station.name) está a \(station.distanceMeters) metros, con \(station.bikesAvailable) bicis y \(station.slotsFree) huecos."
            )
        } catch {
            return .result(dialog: "No he podido consultar Bizi Zaragoza en el reloj.")
        }
    }
}

struct WatchFavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Favoritas en reloj"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let favoriteIds = await MainActor.run { WatchFavoritesSyncBridge.shared.favoriteIds }
        do {
            let favorites = try await BiziWatchGraph.shared.favoriteStations(favoriteIds: favoriteIds)
            guard !favorites.isEmpty else {
                return .result(dialog: "Todavía no tengo favoritas sincronizadas desde el iPhone.")
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return .result(dialog: "Tus favoritas en el reloj son \(summary).")
        } catch {
            return .result(dialog: "No he podido consultar tus favoritas en el reloj.")
        }
    }
}

struct WatchRouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta desde reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        do {
            guard let station = try await BiziWatchGraph.shared.station(matching: stationName) else {
                return .result(dialog: "No he encontrado esa estación para enviar la ruta al iPhone.")
            }
            let requested = await MainActor.run {
                WatchFavoritesSyncBridge.shared.requestRoute(to: station.id)
            }
            if requested {
                return .result(dialog: "He pedido al iPhone que abra la ruta a \(station.name).")
            }
            return .result(dialog: "No he podido contactar con el iPhone ahora mismo.")
        } catch {
            return .result(dialog: "No he podido preparar esa ruta desde el reloj.")
        }
    }
}

struct WatchBiziAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: WatchNearestStationIntent(),
            phrases: [
                "Muéstrame la estación más cercana en el reloj con \(.applicationName)"
            ],
            shortTitle: "Cercana",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: WatchFavoriteStationsIntent(),
            phrases: [
                "Enséñame mis favoritas en el reloj con \(.applicationName)"
            ],
            shortTitle: "Favoritas",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: WatchRouteToStationIntent(),
            phrases: [
                "Abre una ruta en mi iPhone con \(.applicationName)"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
    }
}
