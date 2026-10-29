import Core
import MobileUi
import Foundation

protocol AppleGraphClient {
    func favoriteStations() async throws -> [BiziStationSnapshot]
    func station(matching query: String?) async throws -> BiziStationSnapshot?
    func station(stationId: String) async throws -> BiziStationSnapshot?
    func currentSelectedCity() async throws -> City
    func setSelectedCity(_ city: City) async throws
    func assistantResponse(for action: any AssistantAction) async throws -> AssistantResolution
}

extension BiziAppleGraph: AppleGraphClient {}

struct AppleShortcutRunner {
    let graph: any AppleGraphClient
    let saveLaunchRequest: (navigation.MobileLaunchRequest) async -> Void

    init(
        graph: any AppleGraphClient = BiziAppleGraph.shared,
        saveLaunchRequest: @escaping (navigation.MobileLaunchRequest) async -> Void = { request in
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
            fallbackRequest: navigation.MobileLaunchRequest.NearestStation.shared,
            action: AssistantAction.NearestStation.shared,
            fallbackMessage: "Abre Bici Radar para buscar una estación cercana."
        )
    }

    func nearestStationWithBikesDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: navigation.MobileLaunchRequest.NearestStationWithBikes.shared,
            action: AssistantAction.NearestStationWithBikes.shared,
            fallbackMessage: "Abre Bici Radar para buscar una estación cercana con bicis disponibles."
        )
    }

    func nearestStationWithSlotsDialog() async -> String {
        await nearestStationDialog(
            fallbackRequest: navigation.MobileLaunchRequest.NearestStationWithSlots.shared,
            action: AssistantAction.NearestStationWithSlots.shared,
            fallbackMessage: "Abre Bici Radar para buscar una estación cercana con huecos libres."
        )
    }

    func favoriteStationsDialog() async -> String {
        await saveLaunchRequest(navigation.MobileLaunchRequest.Favorites.shared)
        do {
            let favorites = try await graph.favoriteStations()
            guard !favorites.isEmpty else {
                return BiziDialogMessage.noFavoritesConfigured
            }
            let summary = favorites
                .prefix(3)
                .map(\.name)
                .joined(separator: ", ")
            return "Tus favoritas en Bici Radar. Tienes \(favorites.count) en total: \(summary)."
        } catch {
            return BiziDialogMessage.favoritesError
        }
    }

    func openFavoriteStationDialog() async -> String {
        do {
            if let favorite = BiziSurfaceStore.favoriteStation() {
                await saveLaunchRequest(navigation.MobileLaunchRequest.ShowStation(stationId: favorite.id))
                return "Abriendo tu estación favorita en Bici Radar."
            }

            guard let favorite = (try await graph.favoriteStations()).first else {
                return "Abre Bici Radar. Todavía no tienes una estación favorita configurada."
            }
            await saveLaunchRequest(navigation.MobileLaunchRequest.ShowStation(stationId: favorite.id))
            return "Abriendo tu estación favorita en Bici Radar."
        } catch {
            return "Abre Bici Radar para mostrar tu estación favorita."
        }
    }

    func openNearbyStationsDialog() async -> String {
        await saveLaunchRequest(navigation.MobileLaunchRequest.Home.shared)
        return "Abriendo estaciones cercanas en Bici Radar."
    }

    func monitorFavoriteStationDialog() async -> String {
        do {
            if let favorite = BiziSurfaceStore.favoriteStation() {
                await saveLaunchRequest(navigation.MobileLaunchRequest.MonitorStation(stationId: favorite.id))
                return "Preparando la monitorización de tu estación favorita en Bici Radar."
            }

            guard let favorite = (try await graph.favoriteStations()).first else {
                return "Abre Bici Radar. Necesitas una estación favorita para monitorizarla."
            }
            await saveLaunchRequest(navigation.MobileLaunchRequest.MonitorStation(stationId: favorite.id))
            return "Preparando la monitorización de tu estación favorita en Bici Radar."
        } catch {
            return BiziDialogMessage.monitorError
        }
    }

    func changeCityDialog(cityId: String) async -> String {
        guard let city = City.Companion.shared.fromId(id: cityId) else {
            return BiziDialogMessage.cityNotFound
        }
        do {
            try await graph.setSelectedCity(city)
            await saveLaunchRequest(navigation.MobileLaunchRequest.SelectCity(cityId: city.id))
            return "Cambiando a \(city.displayName) en Bici Radar."
        } catch {
            return BiziDialogMessage.cityChangeError
        }
    }

    func stationStatusDialog(stationName: String?) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in stationStatusText(name: station.name, bikesAvailable: station.bikesAvailable, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFound,
            errorMessage: BiziDialogMessage.stationStateError
        )
    }

    func stationStatusDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in stationStatusText(name: station.name, bikesAvailable: station.bikesAvailable, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFound,
            errorMessage: BiziDialogMessage.stationStateError
        )
    }

    func stationBikeCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in stationBikeCountText(name: station.name, bikesAvailable: station.bikesAvailable) },
            missingMessage: BiziDialogMessage.stationNotFound,
            errorMessage: BiziDialogMessage.stationBikeError
        )
    }

    func stationBikeCountDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in stationBikeCountText(name: station.name, bikesAvailable: station.bikesAvailable) },
            missingMessage: BiziDialogMessage.stationNotFound,
            errorMessage: BiziDialogMessage.stationBikeError
        )
    }

    func stationSlotCountDialog(stationName: String) async -> String {
        await stationDetailDialog(
            stationName: stationName,
            value: { station in stationSlotCountText(name: station.name, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFound,
            errorMessage: BiziDialogMessage.stationSlotError
        )
    }

    func stationSlotCountDialog(stationId: String) async -> String {
        await stationDetailDialog(
            stationId: stationId,
            value: { station in stationSlotCountText(name: station.name, slotsFree: station.slotsFree) },
            missingMessage: BiziDialogMessage.stationNotFound,
            errorMessage: BiziDialogMessage.stationSlotError
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
                return BiziDialogMessage.routeNotFound
            }
            await saveLaunchRequest(navigation.MobileLaunchRequest.RouteToStation(stationId: station.id))
            return "Preparando ruta hacia \(station.name) en Bici Radar."
        } catch {
            return BiziDialogMessage.routeError
        }
    }

    func routeToStationDialog(stationId: String) async -> String {
        do {
            guard let station = try await graph.station(stationId: stationId) else {
                return BiziDialogMessage.routeNotFound
            }
            await saveLaunchRequest(navigation.MobileLaunchRequest.RouteToStation(stationId: station.id))
            return "Preparando ruta hacia \(station.name) en Bici Radar."
        } catch {
            return BiziDialogMessage.routeError
        }
    }

    private func nearestStationDialog(
        fallbackRequest: navigation.MobileLaunchRequest,
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
            await saveLaunchRequest(navigation.MobileLaunchRequest.ShowStation(stationId: station.id))
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
