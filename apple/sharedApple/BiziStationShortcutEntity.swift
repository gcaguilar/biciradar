import AppIntents

struct BiziStationShortcutEntity: AppEntity, Identifiable, Hashable {
    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Estación")
    static var defaultQuery = BiziStationShortcutEntityQuery()

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

struct BiziStationShortcutEntityQuery: EntityStringQuery {
    func entities(for identifiers: [String]) async throws -> [BiziStationShortcutEntity] {
        var entities: [BiziStationShortcutEntity] = []
        for identifier in identifiers {
            if let station = try await BiziAppleGraph.shared.station(stationId: identifier) {
                entities.append(.init(from: station))
            }
        }
        return entities
    }

    func entities(matching string: String) async throws -> [BiziStationShortcutEntity] {
        try await BiziAppleGraph.shared
            .stationSuggestions(matching: string, limit: 8)
            .map(BiziStationShortcutEntity.init(from:))
    }

    func suggestedEntities() async throws -> [BiziStationShortcutEntity] {
        try await BiziAppleGraph.shared
            .suggestedStations(limit: 8)
            .map(BiziStationShortcutEntity.init(from:))
    }
}

private extension BiziStationShortcutEntity {
    init(from station: BiziStationSnapshot) {
        self.init(
            id: station.id,
            name: station.name,
            address: station.address,
            bikesAvailable: station.bikesAvailable,
            slotsFree: station.slotsFree
        )
    }
}
