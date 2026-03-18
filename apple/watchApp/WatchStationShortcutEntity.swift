import AppIntents

struct WatchStationShortcutEntity: AppEntity, Identifiable, Hashable {
    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Estación")
    static var defaultQuery = WatchStationShortcutEntityQuery()

    let id: String
    let name: String
    let address: String
    let bikesAvailable: Int
    let slotsFree: Int

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(
            title: .init(stringLiteral: name),
            subtitle: .init(stringLiteral: address)
        )
    }
}

struct WatchStationShortcutEntityQuery: EntityStringQuery {
    func entities(for identifiers: [String]) async throws -> [WatchStationShortcutEntity] {
        var entities: [WatchStationShortcutEntity] = []
        for identifier in identifiers {
            if let station = try await BiziWatchGraph.shared.station(stationId: identifier) {
                entities.append(.init(from: station))
            }
        }
        return entities
    }

    func entities(matching string: String) async throws -> [WatchStationShortcutEntity] {
        try await BiziWatchGraph.shared
            .stationSuggestions(matching: string, limit: 8)
            .map(WatchStationShortcutEntity.init(from:))
    }

    func suggestedEntities() async throws -> [WatchStationShortcutEntity] {
        try await BiziWatchGraph.shared
            .suggestedStations(limit: 8)
            .map(WatchStationShortcutEntity.init(from:))
    }
}

private extension WatchStationShortcutEntity {
    init(from station: WatchStationSnapshot) {
        self.init(
            id: station.id,
            name: station.name,
            address: station.address,
            bikesAvailable: station.bikesAvailable,
            slotsFree: station.slotsFree
        )
    }
}
