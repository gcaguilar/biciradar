import Core
import MobileUi
import Foundation

enum AppleDeepLinkParser {
    static func parse(_ url: URL) -> (any navigation.MobileLaunchRequest)? {
        guard url.scheme?.lowercased() == "biciradar" else { return nil }
        let host = url.host?.lowercased()
        let pathComponent = url.pathComponents.dropFirst().first?.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        let action = components?.queryItems?.first(where: { $0.name == "action" })?.value?.lowercased()

        switch host {
        case "home", "nearby":
            return navigation.MobileLaunchRequest.Home.shared
        case "map":
            return navigation.MobileLaunchRequest.Map.shared
        case "favorites":
            return navigation.MobileLaunchRequest.Favorites.shared
        case "alerts":
            return navigation.MobileLaunchRequest.SavedPlaceAlerts.shared
        case "station":
            guard let stationId = pathComponent, !stationId.isEmpty else { return nil }
            if action == "route_to_station" {
                return navigation.MobileLaunchRequest.RouteToStation(stationId: stationId)
            }
            return navigation.MobileLaunchRequest.ShowStation(stationId: stationId)
        case "monitor":
            guard let stationId = pathComponent, !stationId.isEmpty else { return nil }
            return navigation.MobileLaunchRequest.MonitorStation(stationId: stationId)
        case "city":
            guard let cityId = pathComponent, !cityId.isEmpty else { return nil }
            return navigation.MobileLaunchRequest.SelectCity(cityId: cityId)
        default:
            return nil
        }
    }
}
