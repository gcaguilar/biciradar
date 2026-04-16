@testable import BiciRadar
import BiziMobileUi
import XCTest

final class AppleDeepLinkParserTests: XCTestCase {
    func testParsesTopLevelDeepLinks() {
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://home")!) is MobileLaunchRequestHome)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://nearby")!) is MobileLaunchRequestHome)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://map")!) is MobileLaunchRequestMap)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://favorites")!) is MobileLaunchRequestFavorites)
        XCTAssertTrue(AppleDeepLinkParser.parse(URL(string: "biciradar://alerts")!) is MobileLaunchRequestSavedPlaceAlerts)
    }

    func testParsesStationAndMonitoringDeepLinks() {
        let stationRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://station/station-42")!) as? MobileLaunchRequestShowStation
        let routeRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://station/station-42?action=route_to_station")!) as? MobileLaunchRequestRouteToStation
        let monitorRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://monitor/station-42")!) as? MobileLaunchRequestMonitorStation
        let cityRequest = AppleDeepLinkParser.parse(URL(string: "biciradar://city/zaragoza")!) as? MobileLaunchRequestSelectCity

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
