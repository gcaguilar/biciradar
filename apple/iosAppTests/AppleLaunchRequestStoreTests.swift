@testable import BiciRadar
import Core
import MobileUi
import XCTest

@MainActor
final class AppleLaunchRequestStoreTests: XCTestCase {
    private var defaults: UserDefaults!
    private var store: AppleLaunchRequestStore!
    private var suiteName: String!

    override func setUp() {
        super.setUp()
        suiteName = "com.gcaguilar.biciradar.tests.launch.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suiteName)
        defaults.removePersistentDomain(forName: suiteName)
        store = AppleLaunchRequestStore(defaults: defaults)
    }

    override func tearDown() {
        defaults.removePersistentDomain(forName: suiteName)
        defaults = nil
        store = nil
        suiteName = nil
        super.tearDown()
    }

    func testFavoritesRequestRoundTripsAndClears() {
        store.save(navigation.MobileLaunchRequest.Favorites.shared)

        XCTAssertEqual(defaults.string(forKey: "bizizaragoza.pendingAction"), "favorite_stations")
        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.Favorites)
        XCTAssertNil(store.takePendingRequest())
    }

    func testHomeAndMapRequestsRoundTrip() {
        store.save(navigation.MobileLaunchRequest.Home.shared)
        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.Home)

        store.save(navigation.MobileLaunchRequest.Map.shared)
        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.Map)
    }

    func testLegacyFavoritesActionStillLoads() {
        defaults.set("favorites", forKey: "bizizaragoza.pendingAction")

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.Favorites)
    }

    func testNearestStationRequestRoundTrips() {
        store.save(navigation.MobileLaunchRequest.NearestStation.shared)

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.NearestStation)
    }

    func testNearestStationWithBikesRequestRoundTrips() {
        store.save(navigation.MobileLaunchRequest.NearestStationWithBikes.shared)

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.NearestStationWithBikes)
    }

    func testNearestStationWithSlotsRequestRoundTrips() {
        store.save(navigation.MobileLaunchRequest.NearestStationWithSlots.shared)

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.NearestStationWithSlots)
    }

    func testOpenAssistantRequestRoundTrips() {
        store.save(navigation.MobileLaunchRequest.OpenAssistant.shared)

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.OpenAssistant)
    }

    func testStationStatusRequestRoundTrips() {
        store.save(navigation.MobileLaunchRequest.StationStatus.shared)

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.StationStatus)
    }

    func testRouteRequestRequiresStationIdentifier() {
        defaults.set("route_to_station", forKey: "bizizaragoza.pendingAction")

        XCTAssertNil(store.takePendingRequest())
    }

    func testRouteRequestPreservesStationIdentifier() {
        store.save(navigation.MobileLaunchRequest.RouteToStation(stationId: "station-42"))

        let request = store.takePendingRequest() as? navigation.MobileLaunchRequest.RouteToStation
        XCTAssertEqual(request?.stationId, "station-42")
        XCTAssertNil(store.takePendingRequest())
    }

    func testShowStationRequestPreservesStationIdentifier() {
        store.save(navigation.MobileLaunchRequest.ShowStation(stationId: "station-9"))

        let request = store.takePendingRequest() as? navigation.MobileLaunchRequest.ShowStation
        XCTAssertEqual(request?.stationId, "station-9")
        XCTAssertNil(store.takePendingRequest())
    }

    func testMonitorStationRequestPreservesStationIdentifier() {
        store.save(navigation.MobileLaunchRequest.MonitorStation(stationId: "station-9"))

        let request = store.takePendingRequest() as? navigation.MobileLaunchRequest.MonitorStation
        XCTAssertEqual(request?.stationId, "station-9")
        XCTAssertNil(store.takePendingRequest())
    }

    func testSelectCityRequestPreservesCityIdentifier() {
        store.save(navigation.MobileLaunchRequest.SelectCity(cityId: "zaragoza"))

        let request = store.takePendingRequest() as? navigation.MobileLaunchRequest.SelectCity
        XCTAssertEqual(request?.cityId, "zaragoza")
        XCTAssertNil(store.takePendingRequest())
    }

    func testSavedPlaceAlertsRequestRoundTrip() {
        store.save(navigation.MobileLaunchRequest.SavedPlaceAlerts.shared)

        XCTAssertTrue(store.takePendingRequest() is navigation.MobileLaunchRequest.SavedPlaceAlerts)
        XCTAssertNil(store.takePendingRequest())
    }

}
