import AppIntents
import BiziMobileUi

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

struct BiziCityShortcutEntity: AppEntity, Identifiable, Hashable {
    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Ciudad")
    static var defaultQuery = BiziCityShortcutEntityQuery()

    let id: String
    let name: String

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: .init(stringLiteral: name))
    }
}

struct BiziCityShortcutEntityQuery: EntityStringQuery {
    func entities(for identifiers: [String]) async throws -> [BiziCityShortcutEntity] {
        identifiers
            .compactMap { City.companion.fromId(id: $0) }
            .map(BiziCityShortcutEntity.init(from:))
    }

    func entities(matching string: String) async throws -> [BiziCityShortcutEntity] {
        let normalizedQuery = normalizeShortcutSearchText(string)
        guard !normalizedQuery.isEmpty else {
            return try await suggestedEntities()
        }
        return City.entries
            .filter { city in
                normalizeShortcutSearchText(city.displayName).contains(normalizedQuery) || city.id.contains(normalizedQuery)
            }
            .sorted { lhs, rhs in
                lhs.displayName.localizedCaseInsensitiveCompare(rhs.displayName) == .orderedAscending
            }
            .map(BiziCityShortcutEntity.init(from:))
    }

    func suggestedEntities() async throws -> [BiziCityShortcutEntity] {
        let currentCityId = (try? await BiziAppleGraph.shared.currentSelectedCity())?.id
        return City.entries
            .sorted { lhs, rhs in
                if lhs.id == currentCityId { return true }
                if rhs.id == currentCityId { return false }
                return lhs.displayName.localizedCaseInsensitiveCompare(rhs.displayName) == .orderedAscending
            }
            .map(BiziCityShortcutEntity.init(from:))
    }
}

private extension BiziCityShortcutEntity {
    init(from city: City) {
        self.init(id: city.id, name: city.displayName)
    }
}

private func normalizeShortcutSearchText(_ text: String) -> String {
    text
        .folding(options: [.diacriticInsensitive, .caseInsensitive], locale: .current)
        .trimmingCharacters(in: .whitespacesAndNewlines)
        .lowercased()
}
