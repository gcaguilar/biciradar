import AppIntents

struct NearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación más cercana"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        .result(dialog: "Abriendo Bizi Zaragoza para mostrar la estación más cercana.")
    }
}

struct FavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Mis favoritas"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        .result(dialog: "Abriendo tus estaciones favoritas.")
    }
}

struct StationStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de estación"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        .result(dialog: "Abriendo Bizi Zaragoza para mostrar el estado de la estación más cercana.")
    }
}

struct RouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a estación"
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Nombre de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        .result(dialog: "Abriendo una ruta hacia \(stationName).")
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
