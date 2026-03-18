import BiziMobileUi
import Foundation

protocol AppleGraphClient {
    func favoriteStations() async throws -> [BiziStationSnapshot]
    func station(matching query: String?) async throws -> BiziStationSnapshot?
    func station(stationId: String) async throws -> BiziStationSnapshot?
    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution
}

extension BiziAppleGraph: AppleGraphClient {}

struct AppleShortcutRunner {
    let graph: any AppleGraphClient
    let saveLaunchRequest: (MobileLaunchRequest) async -> Void

    init(
        graph: any AppleGraphClient = BiziAppleGraph.shared,
        saveLaunchRequest: @escaping (MobileLaunchRequest) async -> Void = { request in
            await MainActor.run {
                AppleLaunchRequestStore.shared.save(request)
            }
        }
    ) {
        self.graph = graph
        self.saveLaunchRequest = saveLaunchRequest
    }

    func nearestStationDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: MobileLaunchRequestNearestStation.shared,
            action: AssistantActionNearestStation.shared,
            fallbackMessage: "Abre Bici Radar para buscar una estación cercana."
        )
    }

    func nearestStationWithBikesDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: MobileLaunchRequestNearestStationWithBikes.shared,
            action: AssistantActionNearestStationWithBikes.shared,
            fallbackMessage: "Abre Bici Radar para buscar una estación cercana con bicis disponibles."
        )
    }

    func nearestStationWithSlotsDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: MobileLaunchRequestNearestStationWithSlots.shared,
            action: AssistantActionNearestStationWithSlots.shared,
            fallbackMessage: "Abre Bici Radar para buscar una estación cercana con huecos libres."
        )
    }

    func favoriteStationsDialog() async -> String {
        await saveLaunchRequest(MobileLaunchRequestFavorites.shared)
        do {
            let favorites = try await graph.favoriteStations()
            guard !favorites.isEmpty else {
                return "Abre Bici Radar. Todavía no tienes estaciones favoritas guardadas."
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return "Tus favoritas en Bici Radar. Tienes \(favorites.count) en total: \(summary)."
        } catch {
            return "Abre Bici Radar para mostrar tus favoritas."
        }
    }

    func stationStatusDialog(stationName: String?) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in
                "\(station.name) tiene \(station.bikesAvailable) bicis disponibles y \(station.slotsFree) huecos libres."
            },
            missingMessage: "No he encontrado esa estación en Bici Radar.",
            errorMessage: "No he podido consultar el estado de esa estación."
        )
    }

    func stationStatusDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in
                "\(station.name) tiene \(station.bikesAvailable) bicis disponibles y \(station.slotsFree) huecos libres."
            },
            missingMessage: "No he encontrado esa estación en Bici Radar.",
            errorMessage: "No he podido consultar el estado de esa estación."
        )
    }

    func stationBikeCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in "\(station.name) tiene \(station.bikesAvailable) bicis disponibles." },
            missingMessage: "No he encontrado esa estación en Bici Radar.",
            errorMessage: "No he podido consultar las bicis de esa estación."
        )
    }

    func stationBikeCountDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in "\(station.name) tiene \(station.bikesAvailable) bicis disponibles." },
            missingMessage: "No he encontrado esa estación en Bici Radar.",
            errorMessage: "No he podido consultar las bicis de esa estación."
        )
    }

    func stationSlotCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in "\(station.name) tiene \(station.slotsFree) huecos libres." },
            missingMessage: "No he encontrado esa estación en Bici Radar.",
            errorMessage: "No he podido consultar los huecos de esa estación."
        )
    }

    func stationSlotCountDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in "\(station.name) tiene \(station.slotsFree) huecos libres." },
            missingMessage: "No he encontrado esa estación en Bici Radar.",
            errorMessage: "No he podido consultar los huecos de esa estación."
        )
    }

    func savedPlaceStatusDialog(savedPlace: String) async -> String {
        await stationStatusDialog(stationName: savedPlace)
    }

    func savedPlaceBikeCountDialog(savedPlace: String) async -> String {
        await stationBikeCountDialog(stationName: savedPlace)
    }

    func savedPlaceSlotCountDialog(savedPlace: String) async -> String {
        await stationSlotCountDialog(stationName: savedPlace)
    }

    func savedPlaceRouteDialog(savedPlace: String) async -> String {
        await routeToStationDialog(stationName: savedPlace)
    }

    func routeToStationDialog(stationName: String) async -> String {
        do {
            guard let station = try await graph.station(matching: stationName) else {
                return "No he encontrado esa estación en Bici Radar."
            }
            await saveLaunchRequest(MobileLaunchRequestRouteToStation(stationId: station.id))
            return "Preparando ruta hacia \(station.name) en Bici Radar."
        } catch {
            return "No he podido preparar esa ruta ahora mismo."
        }
    }

    func routeToStationDialog(stationId: String) async -> String {
        do {
            guard let station = try await graph.station(stationId: stationId) else {
                return "No he encontrado esa estación en Bici Radar."
            }
            await saveLaunchRequest(MobileLaunchRequestRouteToStation(stationId: station.id))
            return "Preparando ruta hacia \(station.name) en Bici Radar."
        } catch {
            return "No he podido preparar esa ruta ahora mismo."
        }
    }

    private func nearestStationDialog(
        fallbackRequest: MobileLaunchRequest,
        action: any AssistantAction,
        fallbackMessage: String
    ) async -> String {
        await saveLaunchRequest(fallbackRequest)
        do {
            let resolution = try await graph.assistantResponse(for: action)
            guard let stationId = resolution.highlightedStationId else {
                return fallbackMessage
            }
            guard let station = try await graph.station(stationId: stationId) else {
                return resolution.spokenResponse
            }
            await saveLaunchRequest(MobileLaunchRequestShowStation(stationId: station.id))
            return resolution.spokenResponse
        } catch {
            return fallbackMessage
        }
    }

    private func stationDetailDialog(
        stationName: String?,
        value: (BiziStationSnapshot) -> String,
        missingMessage: String,
        errorMessage: String
    ) async -> String {
        do {
            guard let station = try await graph.station(matching: stationName) else {
                return missingMessage
            }
            return value(station)
        } catch {
            return errorMessage
        }
    }

    private func stationDetailDialog(
        stationId: String,
        value: (BiziStationSnapshot) -> String,
        missingMessage: String,
        errorMessage: String
    ) async -> String {
        do {
            guard let station = try await graph.station(stationId: stationId) else {
                return missingMessage
            }
            return value(station)
        } catch {
            return errorMessage
        }
    }
}
