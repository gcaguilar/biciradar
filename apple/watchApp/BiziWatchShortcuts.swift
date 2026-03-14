import AppIntents
import BiziSharedCore

enum WatchSavedPlaceShortcut: String, AppEnum {
    case home
    case work

    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Estación guardada")
    static var caseDisplayRepresentations: [WatchSavedPlaceShortcut: DisplayRepresentation] = [
        .home: DisplayRepresentation(title: "Casa"),
        .work: DisplayRepresentation(title: "Trabajo")
    ]

    var spokenQuery: String {
        switch self {
        case .home: return "casa"
        case .work: return "trabajo"
        }
    }
}

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

struct WatchSavedPlaceStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de casa o trabajo en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Estación guardada")
    var savedPlace: WatchSavedPlaceShortcut

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().savedPlaceStatusDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchSavedPlaceRouteIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a casa o trabajo en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Estación guardada")
    var savedPlace: WatchSavedPlaceShortcut

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().savedPlaceRouteDialog(savedPlace: savedPlace.spokenQuery)
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
                "Muéstrame la estación más cercana en la aplicación \(.applicationName)",
                "¿Qué estación tengo más cerca en la aplicación \(.applicationName)?"
            ],
            shortTitle: "Cercana",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: WatchNearestStationWithBikesIntent(),
            phrases: [
                "Muéstrame la estación más cercana con bicis en la aplicación \(.applicationName)",
                "¿Dónde hay bicis disponibles en la aplicación \(.applicationName)?"
            ],
            shortTitle: "Con bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: WatchNearestStationWithSlotsIntent(),
            phrases: [
                "Muéstrame la estación más cercana con huecos en la aplicación \(.applicationName)",
                "¿Dónde hay huecos libres en la aplicación \(.applicationName)?"
            ],
            shortTitle: "Con huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: WatchFavoriteStationsIntent(),
            phrases: [
                "Muéstrame mis favoritas en la aplicación \(.applicationName)",
                "¿Cómo están mis estaciones favoritas en la aplicación \(.applicationName)?"
            ],
            shortTitle: "Favoritas",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: WatchStationBikeCountIntent(),
            phrases: [
                "¿Cuántas bicis tiene una estación en la aplicación \(.applicationName)?",
                "Bicis disponibles en una estación en la aplicación \(.applicationName)"
            ],
            shortTitle: "Bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: WatchStationSlotCountIntent(),
            phrases: [
                "¿Cuántos huecos tiene una estación en la aplicación \(.applicationName)?",
                "Huecos libres en una estación en la aplicación \(.applicationName)"
            ],
            shortTitle: "Huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: WatchRouteToStationIntent(),
            phrases: [
                "Navega a una estación en la aplicación \(.applicationName)",
                "Abre una ruta a una estación en la aplicación \(.applicationName)"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
        AppShortcut(
            intent: WatchSavedPlaceStatusIntent(),
            phrases: [
                "Estado de \(\.$savedPlace) en la aplicación \(.applicationName)",
                "¿Hay bicis cerca de \(\.$savedPlace) en la aplicación \(.applicationName)?"
            ],
            shortTitle: "Casa/Trabajo",
            systemImageName: "house.circle"
        )
        AppShortcut(
            intent: WatchSavedPlaceRouteIntent(),
            phrases: [
                "Navega a \(\.$savedPlace) en la aplicación \(.applicationName)",
                "Abre una ruta a \(\.$savedPlace) en la aplicación \(.applicationName)"
            ],
            shortTitle: "Ruta casa",
            systemImageName: "house.circle"
        )
    }
}
