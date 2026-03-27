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

enum StationAvailabilityShortcut: String, AppEnum {
    case status
    case bikes
    case slots

    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Consulta")
    static var caseDisplayRepresentations: [StationAvailabilityShortcut: DisplayRepresentation] = [
        .status: DisplayRepresentation(title: "Estado"),
        .bikes: DisplayRepresentation(title: "Bicis"),
        .slots: DisplayRepresentation(title: "Huecos")
    ]
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

struct OpenFavoriteStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Abrir favorita"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().openFavoriteStationDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct OpenNearbyStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Ver cercanas"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().openNearbyStationsDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct MonitorFavoriteStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Monitorizar favorita"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().monitorFavoriteStationDialog()
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct ChangeCityIntent: AppIntent {
    static var title: LocalizedStringResource = "Cambiar ciudad"
    static var openAppWhenRun: Bool = true

    @Parameter(
        title: "Ciudad",
        requestValueDialog: IntentDialog("¿Qué ciudad quieres abrir?")
    )
    var city: BiziCityShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Abrir \(\.$city)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await AppleShortcutRunner().changeCityDialog(cityId: city.id)
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

struct StationAvailabilityIntent: AppIntent {
    static var title: LocalizedStringResource = "Disponibilidad de estación"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Consulta",
        requestValueDialog: IntentDialog("¿Quieres ver el estado, las bicis o los huecos?")
    )
    var availabilityType: StationAvailabilityShortcut

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿Qué estación quieres consultar?")
    )
    var station: BiziStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Consultar \(\.$availabilityType) en \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let runner = AppleShortcutRunner()
        let dialog: String
        switch availabilityType {
        case .status:
            dialog = await runner.stationStatusDialog(stationId: station.id)
        case .bikes:
            dialog = await runner.stationBikeCountDialog(stationId: station.id)
        case .slots:
            dialog = await runner.stationSlotCountDialog(stationId: station.id)
        }
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
        // iOS only allows up to 10 surfaced App Shortcuts per app.
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
            intent: OpenFavoriteStationIntent(),
            phrases: [
                "Abre mi estación favorita con \(.applicationName)",
                "Enséñame mi favorita con \(.applicationName)",
                "Abre favorita con \(.applicationName)"
            ],
            shortTitle: "Favorita",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: OpenNearbyStationsIntent(),
            phrases: [
                "Ver estaciones cercanas con \(.applicationName)",
                "Abre estaciones cercanas con \(.applicationName)",
                "Enséñame estaciones cerca con \(.applicationName)"
            ],
            shortTitle: "Cercanas",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: MonitorFavoriteStationIntent(),
            phrases: [
                "Monitoriza mi favorita con \(.applicationName)",
                "Vigila mi estación favorita con \(.applicationName)",
                "Monitorizar favorita con \(.applicationName)"
            ],
            shortTitle: "Monitorizar",
            systemImageName: "dot.radiowaves.left.and.right"
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
                "Cuál es el estado de \(\.$savedPlace) en \(.applicationName)",
                "Cuántas bicis hay en \(\.$savedPlace) con \(.applicationName)",
                "Cuántos huecos hay en \(\.$savedPlace) con \(.applicationName)"
            ],
            shortTitle: "Casa/Trabajo",
            systemImageName: "house.circle"
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
