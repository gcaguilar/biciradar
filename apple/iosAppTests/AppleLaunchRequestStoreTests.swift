@testable import BiciRadar
import BiziMobileUi
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
        store.save(MobileLaunchRequestFavorites.shared)

        XCTAssertEqual(defaults.string(forKey: "bizizaragoza.pendingAction"), "favorite_stations")
        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestFavorites)
        XCTAssertNil(store.takePendingRequest())
    }

    func testHomeAndMapRequestsRoundTrip() {
        store.save(MobileLaunchRequestHome.shared)
        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestHome)

        store.save(MobileLaunchRequestMap.shared)
        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestMap)
    }

    func testLegacyFavoritesActionStillLoads() {
        defaults.set("favorites", forKey: "bizizaragoza.pendingAction")

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestFavorites)
    }

    func testNearestStationRequestRoundTrips() {
        store.save(MobileLaunchRequestNearestStation.shared)

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestNearestStation)
    }

    func testNearestStationWithBikesRequestRoundTrips() {
        store.save(MobileLaunchRequestNearestStationWithBikes.shared)

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestNearestStationWithBikes)
    }

    func testNearestStationWithSlotsRequestRoundTrips() {
        store.save(MobileLaunchRequestNearestStationWithSlots.shared)

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestNearestStationWithSlots)
    }

    func testOpenAssistantRequestRoundTrips() {
        store.save(MobileLaunchRequestOpenAssistant.shared)

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestOpenAssistant)
    }

    func testStationStatusRequestRoundTrips() {
        store.save(MobileLaunchRequestStationStatus.shared)

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestStationStatus)
    }

    func testRouteRequestRequiresStationIdentifier() {
        defaults.set("route_to_station", forKey: "bizizaragoza.pendingAction")

        XCTAssertNil(store.takePendingRequest())
    }

    func testRouteRequestPreservesStationIdentifier() {
        store.save(MobileLaunchRequestRouteToStation(stationId: "station-42"))

        let request = store.takePendingRequest() as? MobileLaunchRequestRouteToStation
        XCTAssertEqual(request?.stationId, "station-42")
        XCTAssertNil(store.takePendingRequest())
    }

    func testShowStationRequestPreservesStationIdentifier() {
        store.save(MobileLaunchRequestShowStation(stationId: "station-9"))

        let request = store.takePendingRequest() as? MobileLaunchRequestShowStation
        XCTAssertEqual(request?.stationId, "station-9")
        XCTAssertNil(store.takePendingRequest())
    }

    func testMonitorStationRequestPreservesStationIdentifier() {
        store.save(MobileLaunchRequestMonitorStation(stationId: "station-9"))

        let request = store.takePendingRequest() as? MobileLaunchRequestMonitorStation
        XCTAssertEqual(request?.stationId, "station-9")
        XCTAssertNil(store.takePendingRequest())
    }

    func testSelectCityRequestPreservesCityIdentifier() {
        store.save(MobileLaunchRequestSelectCity(cityId: "zaragoza"))

        let request = store.takePendingRequest() as? MobileLaunchRequestSelectCity
        XCTAssertEqual(request?.cityId, "zaragoza")
        XCTAssertNil(store.takePendingRequest())
    }

    func testSavedPlaceAlertsRequestRoundTrip() {
        store.save(MobileLaunchRequestSavedPlaceAlerts.shared)

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestSavedPlaceAlerts)
        XCTAssertNil(store.takePendingRequest())
    }

}
