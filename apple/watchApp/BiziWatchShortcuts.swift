import AppIntents
import BiziSharedCore

struct WatchNearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana en reloj"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().nearestStationDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchNearestStationWithBikesIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana con bicis en reloj"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().nearestStationWithBikesDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchNearestStationWithSlotsIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana con huecos en reloj"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().nearestStationWithSlotsDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchFavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Favoritas en reloj"
    static var openAppWhenRun: Bool = false

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().favoriteStationsDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchRouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta desde reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().routeToStationDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchStationBikeCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Bicis en estación en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre o número de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().stationBikeCountDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchStationSlotCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Huecos en estación en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre o número de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().stationSlotCountDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
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
            intent: WatchNearestStationWithBikesIntent(),
            phrases: [
                "Muéstrame la estación más cercana con bicis en el reloj con \(.applicationName)"
            ],
            shortTitle: "Con bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: WatchNearestStationWithSlotsIntent(),
            phrases: [
                "Muéstrame la estación más cercana con huecos en el reloj con \(.applicationName)"
            ],
            shortTitle: "Con huecos",
            systemImageName: "parkingsign.circle"
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
            intent: WatchStationBikeCountIntent(),
            phrases: [
                "Enséñame cuántas bicis tiene una estación en el reloj con \(.applicationName)"
            ],
            shortTitle: "Bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: WatchStationSlotCountIntent(),
            phrases: [
                "Enséñame cuántos huecos tiene una estación en el reloj con \(.applicationName)"
            ],
            shortTitle: "Huecos",
            systemImageName: "parkingsign.circle"
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
