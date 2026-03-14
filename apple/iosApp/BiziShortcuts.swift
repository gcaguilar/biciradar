import AppIntents
import BiziMobileUi

enum SavedPlaceShortcut: String, AppEnum {
    case home
    case work

    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Estación guardada")
    static var caseDisplayRepresentations: [SavedPlaceShortcut: DisplayRepresentation] = [
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

struct NearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación más cercana"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().nearestStationDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct NearestStationWithBikesIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana con bicis"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().nearestStationWithBikesDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct NearestStationWithSlotsIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana con huecos"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().nearestStationWithSlotsDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct FavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Mis favoritas"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().favoriteStationsDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct StationStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de estación"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre de estación")
    var stationName: String?

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().stationStatusDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct StationBikeCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Bicis en estación"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre o número de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().stationBikeCountDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct StationSlotCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Huecos en estación"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Nombre o número de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().stationSlotCountDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct RouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a estación"
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Nombre de estación")
    var stationName: String

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().routeToStationDialog(stationName: stationName)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct SavedPlaceStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de casa o trabajo"
    static var openAppWhenRun: Bool = false

    @Parameter(title: "Estación guardada")
    var savedPlace: SavedPlaceShortcut

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().savedPlaceStatusDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct SavedPlaceRouteIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a casa o trabajo"
    static var openAppWhenRun: Bool = true

    @Parameter(title: "Estación guardada")
    var savedPlace: SavedPlaceShortcut

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().savedPlaceRouteDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct BiziAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: NearestStationIntent(),
            phrases: [
                "Muéstrame la estación más cercana en \(.applicationName)",
                "Abre \(.applicationName) y muéstrame la estación más cercana"
            ],
            shortTitle: "Estación cercana",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: NearestStationWithBikesIntent(),
            phrases: [
                "Muéstrame la estación más cercana con bicis en \(.applicationName)",
                "Abre \(.applicationName) y muéstrame una estación con bicis disponibles"
            ],
            shortTitle: "Con bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: NearestStationWithSlotsIntent(),
            phrases: [
                "Muéstrame la estación más cercana con huecos en \(.applicationName)",
                "Abre \(.applicationName) y muéstrame una estación con huecos libres"
            ],
            shortTitle: "Con huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: FavoriteStationsIntent(),
            phrases: [
                "Muéstrame mis favoritas en \(.applicationName)",
                "Abre \(.applicationName) y muéstrame mis estaciones favoritas"
            ],
            shortTitle: "Favoritas",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: StationStatusIntent(),
            phrases: [
                "Consulta el estado de una estación en \(.applicationName)",
                "¿Cuántas bicis y huecos tiene una estación en \(.applicationName)?"
            ],
            shortTitle: "Estado",
            systemImageName: "info.circle"
        )
        AppShortcut(
            intent: StationBikeCountIntent(),
            phrases: [
                "¿Cuántas bicis tiene una estación en \(.applicationName)?",
                "Bicis disponibles en una estación en \(.applicationName)"
            ],
            shortTitle: "Bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: StationSlotCountIntent(),
            phrases: [
                "¿Cuántos huecos tiene una estación en \(.applicationName)?",
                "Huecos libres en una estación en \(.applicationName)"
            ],
            shortTitle: "Huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: RouteToStationIntent(),
            phrases: [
                "Llévame a una estación con \(.applicationName)",
                "Abre \(.applicationName) y navega a una estación de bizi"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
        AppShortcut(
            intent: SavedPlaceStatusIntent(),
            phrases: [
                "Estado de \(\.$savedPlace) en \(.applicationName)",
                "¿Hay bicis cerca de \(\.$savedPlace) en \(.applicationName)?"
            ],
            shortTitle: "Casa/Trabajo",
            systemImageName: "house.circle"
        )
        AppShortcut(
            intent: SavedPlaceRouteIntent(),
            phrases: [
                "Llévame a \(\.$savedPlace) con \(.applicationName)",
                "Abre \(.applicationName) y navega a \(\.$savedPlace)"
            ],
            shortTitle: "Ruta casa",
            systemImageName: "house.circle"
        )
    }
}
