import BiziSharedCore
import Foundation

protocol WatchGraphClient {
    func nearbyStations(limit: Int) async throws -> [WatchStationSnapshot]
    func favoriteStations(favoriteIds: Set<String>) async throws -> [WatchStationSnapshot]
    func station(matching query: String?) async throws -> WatchStationSnapshot?
    func station(stationId: String) async throws -> WatchStationSnapshot?
    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution
    func openRoute(to stationId: String) async throws -> WatchStationSnapshot?
}

extension BiziWatchGraph: WatchGraphClient {}

struct WatchShortcutRunner {
    let graph: any WatchGraphClient
    let favoriteIdsProvider: () async -> Set<String>
    let routeRequester: (String) async -> Bool

    init(
        graph: any WatchGraphClient = BiziWatchGraph.shared,
        favoriteIdsProvider: @escaping () async -> Set<String> = {
            await MainActor.run { WatchFavoritesSyncBridge.shared.favoriteIds }
        },
        routeRequester: @escaping (String) async -> Bool = { stationId in
            await MainActor.run { WatchFavoritesSyncBridge.shared.requestRoute(to: stationId) }
        }
    ) {
        self.graph = graph
        self.favoriteIdsProvider = favoriteIdsProvider
        self.routeRequester = routeRequester
    }

    func nearestStationDialog() async -> String {
        await assistantDialog(
            action: AssistantActionNearestStation.shared,
            emptyMessage: "No he encontrado estaciones de Bizi cerca de ti ahora mismo.",
            errorMessage: "No he podido consultar Bici Radar en el reloj."
        )
    }

    func nearestStationWithBikesDialog() async -> String {
        await assistantDialog(
            action: AssistantActionNearestStationWithBikes.shared,
            emptyMessage: "No he encontrado estaciones cercanas con bicis disponibles ahora mismo.",
            errorMessage: "No he podido consultar estaciones con bicis disponibles en el reloj."
        )
    }

    func nearestStationWithSlotsDialog() async -> String {
        await assistantDialog(
            action: AssistantActionNearestStationWithSlots.shared,
            emptyMessage: "No he encontrado estaciones cercanas con huecos libres ahora mismo.",
            errorMessage: "No he podido consultar estaciones con huecos libres en el reloj."
        )
    }

    func favoriteStationsDialog() async -> String {
        let favoriteIds = await favoriteIdsProvider()
        do {
            let favorites = try await graph.favoriteStations(favoriteIds: favoriteIds)
            guard !favorites.isEmpty else {
                return BiziDialogMessage.noFavoritesWatch
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return "Tus favoritas en el reloj son \(summary)."
        } catch {
            return BiziDialogMessage.favoritesErrorWatch
        }
    }

    func routeToStationDialog(stationName: String) async -> String {
        do {
            guard let station = try await graph.station(matching: stationName) else {
                return BiziDialogMessage.routeNotFoundWatch
            }
            let requested = await routeRequester(station.id)
            return requested
                ? "He pedido al iPhone que abra la ruta a \(station.name)."
                : BiziDialogMessage.routeNoContact
        } catch {
            return BiziDialogMessage.routeErrorWatch
        }
    }

    func routeToStationDialog(stationId: String) async -> String {
        do {
            guard let station = try await graph.station(stationId: stationId) else {
                return BiziDialogMessage.routeNotFoundWatch
            }
            let requested = await routeRequester(station.id)
            return requested
                ? "He pedido al iPhone que abra la ruta a \(station.name)."
                : BiziDialogMessage.routeNoContact
        } catch {
            return BiziDialogMessage.routeErrorWatch
        }
    }

    func stationStatusDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in stationStatusText(name: station.name, bikesAvailable: station.bikesAvailable, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationStateErrorWatch
        )
    }

    func stationStatusDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in stationStatusText(name: station.name, bikesAvailable: station.bikesAvailable, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationStateErrorWatch
        )
    }

    func stationBikeCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in stationBikeCountText(name: station.name, bikesAvailable: station.bikesAvailable) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationBikeErrorWatch
        )
    }

    func stationBikeCountDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in stationBikeCountText(name: station.name, bikesAvailable: station.bikesAvailable) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationBikeErrorWatch
        )
    }

    func stationSlotCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in stationSlotCountText(name: station.name, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationSlotErrorWatch
        )
    }

    func stationSlotCountDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in stationSlotCountText(name: station.name, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationSlotErrorWatch
        )
    }

    func savedPlaceStatusDialog(savedPlace: String) async -> String {
        await stationDetailDialog(
            stationName: savedPlace,
            value: { station in stationStatusText(name: station.name, bikesAvailable: station.bikesAvailable, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFoundWatch,
            errorMessage: BiziDialogMessage.stationStateErrorWatch
        )
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

    private func assistantDialog(
        action: any AssistantAction,
        emptyMessage: String,
        errorMessage: String
    ) async -> String {
        do {
            let resolution = try await graph.assistantResponse(for: action)
            return resolution.highlightedStationId == nil ? emptyMessage : resolution.spokenResponse
        } catch {
            return errorMessage
        }
    }

    private func stationDetailDialog(
        stationName: String,
        value: (WatchStationSnapshot) -> String,
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
        value: (WatchStationSnapshot) -> String,
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
