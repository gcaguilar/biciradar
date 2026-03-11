import BiziMobileUi
import Foundation

@MainActor
final class AppleLaunchRequestStore: ObservableObject {
    static let shared = AppleLaunchRequestStore()

    private enum LaunchAction {
        static let favoriteStations = "favorite_stations"
        static let legacyFavorites = "favorites"
        static let nearestStation = "nearest_station"
        static let openAssistant = "open_assistant"
        static let stationStatus = "station_status"
        static let routeToStation = "route_to_station"
        static let showStation = "show_station"
    }

    private let defaults: UserDefaults
    private let actionKey = "bizizaragoza.pendingAction"
    private let stationIdKey = "bizizaragoza.pendingStationId"

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func save(_ request: any MobileLaunchRequest) {
        switch request {
        case is MobileLaunchRequestFavorites:
            defaults.set(LaunchAction.favoriteStations, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestNearestStation:
            defaults.set(LaunchAction.nearestStation, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestOpenAssistant:
            defaults.set(LaunchAction.openAssistant, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestStationStatus:
            defaults.set(LaunchAction.stationStatus, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case let request as MobileLaunchRequestRouteToStation:
            defaults.set(LaunchAction.routeToStation, forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        case let request as MobileLaunchRequestShowStation:
            defaults.set(LaunchAction.showStation, forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        default:
            defaults.removeObject(forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        }
    }

    func takePendingRequest() -> (any MobileLaunchRequest)? {
        defer {
            defaults.removeObject(forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        }

        let action = defaults.string(forKey: actionKey)
        let stationId = defaults.string(forKey: stationIdKey)

        switch action {
        case LaunchAction.favoriteStations, LaunchAction.legacyFavorites:
            return MobileLaunchRequestFavorites.shared
        case LaunchAction.nearestStation:
            return MobileLaunchRequestNearestStation.shared
        case LaunchAction.openAssistant:
            return MobileLaunchRequestOpenAssistant.shared
        case LaunchAction.stationStatus:
            return MobileLaunchRequestStationStatus.shared
        case LaunchAction.routeToStation:
            guard let stationId else { return nil }
            return MobileLaunchRequestRouteToStation(stationId: stationId)
        case LaunchAction.showStation:
            guard let stationId else { return nil }
            return MobileLaunchRequestShowStation(stationId: stationId)
        default:
            return nil
        }
    }
}
