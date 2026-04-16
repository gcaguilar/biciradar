import BiziMobileUi
import Foundation

enum AppleDeepLinkParser {
    static func isGarminPairingRequest(_ url: URL) -> Bool {
        guard url.scheme?.lowercased() == "biciradar" else { return false }
        return url.host?.lowercased() == "garmin" && url.path.lowercased() == "/pair"
    }

    static func parse(_ url: URL) -> (any MobileLaunchRequest)? {
        guard url.scheme?.lowercased() == "biciradar" else { return nil }
        let host = url.host?.lowercased()
        let pathComponent = url.pathComponents.dropFirst().first?.trimmingCharacters(in: CharacterSet(charactersIn: "/"))
        let components = URLComponents(url: url, resolvingAgainstBaseURL: false)
        let action = components?.queryItems?.first(where: { $0.name == "action" })?.value?.lowercased()

        switch host {
        case "home", "nearby":
            return MobileLaunchRequestHome.shared
        case "map":
            return MobileLaunchRequestMap.shared
        case "favorites":
            return MobileLaunchRequestFavorites.shared
        case "alerts":
            return MobileLaunchRequestSavedPlaceAlerts.shared
        case "station":
            guard let stationId = pathComponent, !stationId.isEmpty else { return nil }
            if action == "route_to_station" {
                return MobileLaunchRequestRouteToStation(stationId: stationId)
            }
            return MobileLaunchRequestShowStation(stationId: stationId)
        case "monitor":
            guard let stationId = pathComponent, !stationId.isEmpty else { return nil }
            return MobileLaunchRequestMonitorStation(stationId: stationId)
        case "city":
            guard let cityId = pathComponent, !cityId.isEmpty else { return nil }
            return MobileLaunchRequestSelectCity(cityId: cityId)
        default:
            return nil
        }
    }
}
