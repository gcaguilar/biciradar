import AppIntents

struct WatchNearestStationIntent: AppIntent {
    static var title: LocalizedStringResource = "Estación cercana en reloj"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        .result(dialog: "Abriendo la estación más cercana en el reloj.")
    }
}

struct WatchFavoriteStationsIntent: AppIntent {
    static var title: LocalizedStringResource = "Favoritas en reloj"
    static var openAppWhenRun: Bool = true

    func perform() async throws -> some IntentResult {
        .result(dialog: "Abriendo tus favoritas sincronizadas en el reloj.")
    }
}
