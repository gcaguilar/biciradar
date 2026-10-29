@testable import BiciRadar
import Core
import MobileUi
import XCTest

final class AppleDeepLinkParserTests: XCTestCase {
    func testParsesTopLevelDeepLinks() {
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://home")!) is navigation.MobileLaunchRequest.Home)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://nearby")!) is navigation.MobileLaunchRequest.Home)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://map")!) is navigation.MobileLaunchRequest.Map)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://favorites")!) is navigation.MobileLaunchRequest.Favorites)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://alerts")!) is navigation.MobileLaunchRequest.SavedPlaceAlerts)
    }

    func testParsesStationAndMonitoringDeepLinks() {
        let stationRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://station/station-42")!) as? navigation.MobileLaunchRequest.ShowStation
        let routeRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://station/station-42?action=route_to_station")!) as? navigation.MobileLaunchRequest.RouteToStation
        let monitorRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://monitor/station-42")!) as? navigation.MobileLaunchRequest.MonitorStation
        let cityRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://city/zaragoza")!) as? navigation.MobileLaunchRequest.SelectCity

        XCTAssertEqual(stationRequest?.stationId, "station-42")
        XCTAssertEqual(routeRequest?.stationId, "station-42")
        XCTAssertEqual(monitorRequest?.stationId, "station-42")
        XCTAssertEqual(cityRequest?.cityId, "zaragoza")
    }

    func testRejectsUnknownOrIncompleteDeepLinks() {
        XCTAssertNil(AppleDeepLinkParser.parse(URL(string: "https://biciradar.app/station/42")!))
        XCTAssertNil(AppleDeepLinkParser.parse(URL(string: "biciradar://station")!))
        XCTAssertNil(AppleDeepLinkParser.parse(URL(string: "biciradar://unknown")!))
    }
}
