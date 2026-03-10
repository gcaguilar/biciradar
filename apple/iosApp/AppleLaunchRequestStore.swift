import BiziMobileUi
import Foundation

@MainActor
final class AppleLaunchRequestStore: ObservableObject {
    static let shared = AppleLaunchRequestStore()

    private let defaults = UserDefaults.standard
    private let actionKey = "bizizaragoza.pendingAction"
    private let stationIdKey = "bizizaragoza.pendingStationId"

    func save(_ request: any MobileLaunchRequest) {
        switch request {
        case is MobileLaunchRequestFavorites:
            defaults.set("favorites", forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestNearestStation:
            defaults.set("nearest_station", forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestOpenAssistant:
            defaults.set("open_assistant", forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case is MobileLaunchRequestStationStatus:
            defaults.set("station_status", forKey: actionKey)
            defaults.removeObject(forKey: stationIdKey)
        case let request as MobileLaunchRequestRouteToStation:
            defaults.set("route_to_station", forKey: actionKey)
            defaults.set(request.stationId, forKey: stationIdKey)
        case let request as MobileLaunchRequestShowStation:
            defaults.set("show_station", forKey: actionKey)
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
        case "favorites":
            return MobileLaunchRequestFavorites.shared
        case "nearest_station":
            return MobileLaunchRequestNearestStation.shared
        case "open_assistant":
            return MobileLaunchRequestOpenAssistant.shared
        case "station_status":
            return MobileLaunchRequestStationStatus.shared
        case "route_to_station":
            return MobileLaunchRequestRouteToStation(stationId: stationId)
        case "show_station":
            guard let stationId else { return nil }
            return MobileLaunchRequestShowStation(stationId: stationId)
        default:
            return nil
        }
    }
}
