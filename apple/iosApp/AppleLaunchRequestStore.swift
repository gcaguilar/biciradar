import Core
import MobileUi
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
        static let savedPlaceAlerts = "saved_place_alerts"
    }

    private let defaults: UserDefaults
    private let actionKey = "bizizaragoza.pendingAction"
    private let stationIdKey = "bizizaragoza.pendingStationId"

    init(defaults: UserDefaults = BiziSharedStorage.sharedDefaults) {
        self.defaults = defaults
    }

    func save(_ request: any navigation.MobileLaunchRequest) {
        switch request {
        case is navigation.MobileLaunchRequest.Home:
            defaults.set(LaunchAction.home, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.Map:
            defaults.set(LaunchAction.map, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.Favorites:
            defaults.set(LaunchAction.favoriteStations, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.NearestStation:
            defaults.set(LaunchAction.nearestStation, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.NearestStationWithBikes:
            defaults.set(LaunchAction.nearestStationWithBikes, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.NearestStationWithSlots:
            defaults.set(LaunchAction.nearestStationWithSlots, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.OpenAssistant:
            defaults.set(LaunchAction.openAssistant, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.StationStatus:
            defaults.set(LaunchAction.stationStatus, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case let request as navigation.MobileLaunchRequest.MonitorStation:
            defaults.set(LaunchAction.monitorStation, forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        case let request as navigation.MobileLaunchRequest.SelectCity:
            defaults.set(LaunchAction.selectCity, forKey: actionKey)
            defaults.set(request.cityId, forKey: stationIdKey)
        case let request as navigation.MobileLaunchRequest.RouteToStation:
            defaults.set(LaunchAction.routeToStation, forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        case let request as navigation.MobileLaunchRequest.ShowStation:
            defaults.set(LaunchAction.showStation, forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        case is navigation.MobileLaunchRequest.SavedPlaceAlerts:
            defaults.set(LaunchAction.savedPlaceAlerts, forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        default:
            defaults.removeObject(forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        }
    }

    func takePendingRequest() -> (any navigation.MobileLaunchRequest)? {
        defer {
            defaults.removeObject(forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        }

        let action = defaults.string(forKey: actionKey)
        let stationId = defaults.string(forKey: stationIdKey)

        switch action {
        case LaunchAction.home:
            return navigation.MobileLaunchRequest.Home.shared
        case LaunchAction.map:
            return navigation.MobileLaunchRequest.Map.shared
        case LaunchAction.favoriteStations, LaunchAction.legacyFavorites:
            return navigation.MobileLaunchRequest.Favorites.shared
        case LaunchAction.nearestStation:
            return navigation.MobileLaunchRequest.NearestStation.shared
        case LaunchAction.nearestStationWithBikes:
            return navigation.MobileLaunchRequest.NearestStationWithBikes.shared
        case LaunchAction.nearestStationWithSlots:
            return navigation.MobileLaunchRequest.NearestStationWithSlots.shared
        case LaunchAction.openAssistant:
            return navigation.MobileLaunchRequest.OpenAssistant.shared
        case LaunchAction.stationStatus:
            return navigation.MobileLaunchRequest.StationStatus.shared
        case LaunchAction.monitorStation:
            guard let stationId else { return nil }
            return navigation.MobileLaunchRequest.MonitorStation(stationId: stationId)
        case LaunchAction.selectCity:
            guard let stationId else { return nil }
            return navigation.MobileLaunchRequest.SelectCity(cityId: stationId)
        case LaunchAction.routeToStation:
            guard let stationId else { return nil }
            return navigation.MobileLaunchRequest.RouteToStation(stationId: stationId)
        case LaunchAction.showStation:
            guard let stationId else { return nil }
            return navigation.MobileLaunchRequest.ShowStation(stationId: stationId)
        case LaunchAction.savedPlaceAlerts:
            return navigation.MobileLaunchRequest.SavedPlaceAlerts.shared
        default:
            return nil
        }
    }
}
