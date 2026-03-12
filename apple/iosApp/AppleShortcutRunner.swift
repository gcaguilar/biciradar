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
            fallbackMessage: "Abriendo Bizi Zaragoza para buscar una estación cercana."
        )
    }

    func nearestStationWithBikesDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: MobileLaunchRequestNearestStationWithBikes.shared,
            action: AssistantActionNearestStationWithBikes.shared,
            fallbackMessage: "Abriendo Bizi Zaragoza para buscar una estación cercana con bicis disponibles."
        )
    }

    func nearestStationWithSlotsDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: MobileLaunchRequestNearestStationWithSlots.shared,
            action: AssistantActionNearestStationWithSlots.shared,
            fallbackMessage: "Abriendo Bizi Zaragoza para buscar una estación cercana con huecos libres."
        )
    }

    func favoriteStationsDialog() async -> String {
        await saveLaunchRequest(MobileLaunchRequestFavorites.shared)
        do {
            let favorites = try await graph.favoriteStations()
            guard !favorites.isEmpty else {
                return "Abriendo Bizi Zaragoza. Todavía no tienes estaciones favoritas guardadas."
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return "Abriendo tus favoritas. Tienes \(favorites.count) en total: \(summary)."
        } catch {
            return "Abriendo Bizi Zaragoza para mostrar tus favoritas."
        }
    }

    func stationStatusDialog(stationName: String?) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in
                "\(station.name) tiene \(station.bikesAvailable) bicis disponibles y \(station.slotsFree) huecos libres."
            },
            missingMessage: "No he encontrado esa estación en Bizi Zaragoza.",
            errorMessage: "No he podido consultar el estado de esa estación."
        )
    }

    func stationBikeCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in "\(station.name) tiene \(station.bikesAvailable) bicis disponibles." },
            missingMessage: "No he encontrado esa estación en Bizi Zaragoza.",
            errorMessage: "No he podido consultar las bicis de esa estación."
        )
    }

    func stationSlotCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in "\(station.name) tiene \(station.slotsFree) huecos libres." },
            missingMessage: "No he encontrado esa estación en Bizi Zaragoza.",
            errorMessage: "No he podido consultar los huecos de esa estación."
        )
    }

    func routeToStationDialog(stationName: String) async -> String {
        do {
            guard let station = try await graph.station(matching: stationName) else {
                return "No he encontrado esa estación en Zaragoza Bizi."
            }
            await saveLaunchRequest(MobileLaunchRequestRouteToStation(stationId: station.id))
            return "Abriendo una ruta hacia \(station.name)."
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
}
