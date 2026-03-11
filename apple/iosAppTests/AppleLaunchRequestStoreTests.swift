import BiziMobileUi
import XCTest

@MainActor
final class AppleLaunchRequestStoreTests: XCTestCase {
    private var defaults: UserDefaults!
    private var store: AppleLaunchRequestStore!
    private var suiteName: String!

    override func setUp() {
        super.setUp()
        suiteName = "com.gcaguilar.bizizaragoza.tests.launch.\(UUID().uuidString)"
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

        XCTAssertTrue(store.takePendingRequest() is MobileLaunchRequestFavorites)
        XCTAssertNil(store.takePendingRequest())
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
}
