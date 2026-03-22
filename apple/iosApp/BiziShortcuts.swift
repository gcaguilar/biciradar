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

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿Qué estación quieres consultar?")
    )
    var station: BiziStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Cómo está \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().stationStatusDialog(stationId: station.id)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct RouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a estación"
    static var openAppWhenRun: Bool = true

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿A qué estación quieres ir?")
    )
    var station: BiziStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Llévame a \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().routeToStationDialog(stationId: station.id)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct SavedPlaceStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de casa o trabajo"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres consultar casa o trabajo?")
    )
    var savedPlace: SavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Cómo está \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().savedPlaceStatusDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct SavedPlaceBikeCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Bicis en casa o trabajo"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres consultar casa o trabajo?")
    )
    var savedPlace: SavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Cuántas bicis hay en \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().savedPlaceBikeCountDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct SavedPlaceSlotCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Huecos en casa o trabajo"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres consultar casa o trabajo?")
    )
    var savedPlace: SavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Cuántos huecos hay en \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().savedPlaceSlotCountDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct SavedPlaceRouteIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a casa o trabajo"
    static var openAppWhenRun: Bool = true

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres ir a casa o a trabajo?")
    )
    var savedPlace: SavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Llévame a \(\.$savedPlace)")
    }

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
                "Cuál es la estación más cercana con \(.applicationName)",
                "Qué estación tengo más cerca con \(.applicationName)",
                "Dónde tengo una estación cerca con \(.applicationName)"
            ],
            shortTitle: "Estación cercana",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: NearestStationWithBikesIntent(),
            phrases: [
                "Dónde hay bicis cerca con \(.applicationName)",
                "Cuál es la estación más cercana con bicis con \(.applicationName)",
                "Quiero coger una bici con \(.applicationName)"
            ],
            shortTitle: "Con bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: NearestStationWithSlotsIntent(),
            phrases: [
                "Dónde puedo dejar la bici con \(.applicationName)",
                "Cuál es la estación más cercana con huecos con \(.applicationName)",
                "Quiero dejar la bici con \(.applicationName)"
            ],
            shortTitle: "Con huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: FavoriteStationsIntent(),
            phrases: [
                "Abre mis favoritas con \(.applicationName)",
                "Enséñame mis favoritas con \(.applicationName)",
                "Mis favoritas con \(.applicationName)"
            ],
            shortTitle: "Favoritas",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: StationStatusIntent(),
            phrases: [
                "Cómo está \(\.$station) con \(.applicationName)",
                "Cuál es el estado de \(\.$station) en \(.applicationName)",
                "Qué tal está \(\.$station) con \(.applicationName)"
            ],
            shortTitle: "Estado",
            systemImageName: "info.circle"
        )
        AppShortcut(
            intent: RouteToStationIntent(),
            phrases: [
                "Llévame a \(\.$station) con \(.applicationName)",
                "Cómo llego a \(\.$station) con \(.applicationName)",
                "Navega a \(\.$station) con \(.applicationName)"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
        AppShortcut(
            intent: SavedPlaceStatusIntent(),
            phrases: [
                "Cómo está \(\.$savedPlace) con \(.applicationName)",
                "Qué tal está \(\.$savedPlace) con \(.applicationName)",
                "Cuál es el estado de \(\.$savedPlace) en \(.applicationName)"
            ],
            shortTitle: "Casa/Trabajo",
            systemImageName: "house.circle"
        )
        AppShortcut(
            intent: SavedPlaceBikeCountIntent(),
            phrases: [
                "Cuántas bicis hay en \(\.$savedPlace) con \(.applicationName)",
                "Bicis disponibles en \(\.$savedPlace) con \(.applicationName)",
                "Cuántas bicis tiene \(\.$savedPlace) con \(.applicationName)"
            ],
            shortTitle: "Bicis casa",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: SavedPlaceSlotCountIntent(),
            phrases: [
                "Cuántos huecos hay en \(\.$savedPlace) con \(.applicationName)",
                "Huecos libres en \(\.$savedPlace) con \(.applicationName)",
                "Cuántos anclajes libres hay en \(\.$savedPlace) con \(.applicationName)"
            ],
            shortTitle: "Huecos casa",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: SavedPlaceRouteIntent(),
            phrases: [
                "Llévame a \(\.$savedPlace) con \(.applicationName)",
                "Cómo llego a \(\.$savedPlace) con \(.applicationName)",
                "Llévame al \(\.$savedPlace) con \(.applicationName)"
            ],
            shortTitle: "Ruta casa",
            systemImageName: "house.circle"
        )
    }
}
