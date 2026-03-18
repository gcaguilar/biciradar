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

struct WatchStationStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de estación en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿Qué estación quieres consultar?")
    )
    var station: WatchStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Cómo está \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().stationStatusDialog(stationId: station.id)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchRouteToStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta desde reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿A qué estación quieres ir?")
    )
    var station: WatchStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Llévame a \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().routeToStationDialog(stationId: station.id)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchSavedPlaceStatusIntent: AppIntent {
    static var title: LocalizedStringResource = "Estado de casa o trabajo en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres consultar casa o trabajo?")
    )
    var savedPlace: WatchSavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Cómo está \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().savedPlaceStatusDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchSavedPlaceBikeCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Bicis en casa o trabajo en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres consultar casa o trabajo?")
    )
    var savedPlace: WatchSavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Cuántas bicis hay en \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().savedPlaceBikeCountDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchSavedPlaceSlotCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Huecos en casa o trabajo en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres consultar casa o trabajo?")
    )
    var savedPlace: WatchSavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Cuántos huecos hay en \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().savedPlaceSlotCountDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchSavedPlaceRouteIntent: AppIntent {
    static var title: LocalizedStringResource = "Ruta a casa o trabajo en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación guardada",
        requestValueDialog: IntentDialog("¿Quieres ir a casa o a trabajo?")
    )
    var savedPlace: WatchSavedPlaceShortcut

    static var parameterSummary: some ParameterSummary {
        Summary("Llévame a \(\.$savedPlace)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().savedPlaceRouteDialog(savedPlace: savedPlace.spokenQuery)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchStationBikeCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Bicis en estación en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿De qué estación quieres saber las bicis?")
    )
    var station: WatchStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Cuántas bicis hay en \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().stationBikeCountDialog(stationId: station.id)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchStationSlotCountIntent: AppIntent {
    static var title: LocalizedStringResource = "Huecos en estación en reloj"
    static var openAppWhenRun: Bool = false

    @Parameter(
        title: "Estación",
        requestValueDialog: IntentDialog("¿De qué estación quieres saber los huecos?")
    )
    var station: WatchStationShortcutEntity

    static var parameterSummary: some ParameterSummary {
        Summary("Cuántos huecos hay en \(\.$station)")
    }

    func perform() async throws -> some IntentResult {
        let dialog = await WatchShortcutRunner().stationSlotCountDialog(stationId: station.id)
        return .result(dialog: IntentDialog(stringLiteral: dialog))
    }
}

struct WatchBiziAppShortcuts: AppShortcutsProvider {
    static var appShortcuts: [AppShortcut] {
        AppShortcut(
            intent: WatchNearestStationIntent(),
            phrases: [
                "Cuál es la estación más cercana con \(.applicationName)",
                "Qué estación tengo más cerca con \(.applicationName)",
                "Dónde tengo una estación cerca con \(.applicationName)"
            ],
            shortTitle: "Cercana",
            systemImageName: "location.circle"
        )
        AppShortcut(
            intent: WatchNearestStationWithBikesIntent(),
            phrases: [
                "Dónde hay bicis cerca con \(.applicationName)",
                "Cuál es la estación más cercana con bicis con \(.applicationName)",
                "Quiero coger una bici con \(.applicationName)"
            ],
            shortTitle: "Con bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: WatchNearestStationWithSlotsIntent(),
            phrases: [
                "Dónde puedo dejar la bici con \(.applicationName)",
                "Cuál es la estación más cercana con huecos con \(.applicationName)",
                "Quiero dejar la bici con \(.applicationName)"
            ],
            shortTitle: "Con huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: WatchFavoriteStationsIntent(),
            phrases: [
                "Abre mis favoritas con \(.applicationName)",
                "Enséñame mis favoritas con \(.applicationName)",
                "Mis favoritas con \(.applicationName)"
            ],
            shortTitle: "Favoritas",
            systemImageName: "heart.circle"
        )
        AppShortcut(
            intent: WatchStationStatusIntent(),
            phrases: [
                "Cómo está \(\.$station) con \(.applicationName)",
                "Cuál es el estado de \(\.$station) en \(.applicationName)",
                "Qué tal está \(\.$station) con \(.applicationName)"
            ],
            shortTitle: "Estado",
            systemImageName: "info.circle"
        )
        AppShortcut(
            intent: WatchStationBikeCountIntent(),
            phrases: [
                "Cuántas bicis hay en \(\.$station) con \(.applicationName)",
                "Bicis disponibles en \(\.$station) en \(.applicationName)",
                "Cuántas bicis tiene \(\.$station) con \(.applicationName)"
            ],
            shortTitle: "Bicis",
            systemImageName: "bicycle.circle"
        )
        AppShortcut(
            intent: WatchStationSlotCountIntent(),
            phrases: [
                "Cuántos huecos hay en \(\.$station) con \(.applicationName)",
                "Huecos libres en \(\.$station) en \(.applicationName)",
                "Cuántos anclajes libres hay en \(\.$station) con \(.applicationName)"
            ],
            shortTitle: "Huecos",
            systemImageName: "parkingsign.circle"
        )
        AppShortcut(
            intent: WatchRouteToStationIntent(),
            phrases: [
                "Llévame a \(\.$station) con \(.applicationName)",
                "Cómo llego a \(\.$station) con \(.applicationName)",
                "Navega a \(\.$station) con \(.applicationName)"
            ],
            shortTitle: "Ruta",
            systemImageName: "map.circle"
        )
        AppShortcut(
            intent: WatchSavedPlaceStatusIntent(),
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
            intent: WatchSavedPlaceRouteIntent(),
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
