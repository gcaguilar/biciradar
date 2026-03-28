import BiziMobileUi
import Foundation

@MainActor
final class AppleLaunchRequestStore: ObservableObject {
    static let shared = AppleLaunchRequestStore()

    private enum LaunchAction {
        static let home = "home"
        static let map = "map"
        static let favoriteStations = "favorite_stations"
        static let legacyFavorites = "favorites"
        static let nearestStation = "nearest_station"
        static let nearestStationWithBikes = "nearest_station_with_bikes"
        static let nearestStationWithSlots = "nearest_station_with_slots"
        static let openAssistant = "open_assistant"
        static let stationStatus = "station_status"
        static let monitorStation = "monitor_station"
        static let selectCity = "select_city"
        static let routeToStation = "route_to_station"
        static let showStation = "show_station"
    }

    private let defaults: UserDefaults
    private let actionKey = "bizizaragoza.pendingAction"
    private let stationIdKey = "bizizaragoza.pendingStationId"

    init(defaults: UserDefaults = BiziSharedStorage.sharedDefaults) {
        self.defaults = defaults
    }

    func save(_ request: any MobileLaunchRequest) {
        switch request {
        case is MobileLaunchRequestHome:
            defaults.set(LaunchAction.home, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestMap:
            defaults.set(LaunchAction.map, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestFavorites:
            defaults.set(LaunchAction.favoriteStations, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestNearestStation:
            defaults.set(LaunchAction.nearestStation, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestNearestStationWithBikes:
            defaults.set(LaunchAction.nearestStationWithBikes, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestNearestStationWithSlots:
            defaults.set(LaunchAction.nearestStationWithSlots, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestOpenAssistant:
            defaults.set(LaunchAction.openAssistant, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestStationStatus:
            defaults.set(LaunchAction.stationStatus, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case let request as MobileLaunchRequestMonitorStation:
            defaults.set(LaunchAction.monitorStation, forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        case let request as MobileLaunchRequestSelectCity:
            defaults.set(LaunchAction.selectCity, forKey: actionKey)
            defaults.set(request.cityId, forKey: stationIdKey)
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
        case LaunchAction.home:
            return MobileLaunchRequestHome.shared
        case LaunchAction.map:
            return MobileLaunchRequestMap.shared
        case LaunchAction.favoriteStations, LaunchAction.legacyFavorites:
            return MobileLaunchRequestFavorites.shared
        case LaunchAction.nearestStation:
            return MobileLaunchRequestNearestStation.shared
        case LaunchAction.nearestStationWithBikes:
            return MobileLaunchRequestNearestStationWithBikes.shared
        case LaunchAction.nearestStationWithSlots:
            return MobileLaunchRequestNearestStationWithSlots.shared
        case LaunchAction.openAssistant:
            return MobileLaunchRequestOpenAssistant.shared
        case LaunchAction.stationStatus:
            return MobileLaunchRequestStationStatus.shared
        case LaunchAction.monitorStation:
            guard let stationId else { return nil }
            return MobileLaunchRequestMonitorStation(stationId: stationId)
        case LaunchAction.selectCity:
            guard let stationId else { return nil }
            return MobileLaunchRequestSelectCity(cityId: stationId)
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
