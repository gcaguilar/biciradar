import XCTest

@MainActor
final class WatchFavoritesSyncBridgeTests: XCTestCase {
    private var defaults: UserDefaults!
    private var bridge: WatchFavoritesSyncBridge!
    private var suiteName: String!

    override func setUp() {
        super.setUp()
        suiteName = "com.gcaguilar.biciradar.tests.watchsync.\(UUID().uuidString)"
        defaults = UserDefaults(suiteName: suiteName)
        defaults.removePersistentDomain(forName: suiteName)
        bridge = WatchFavoritesSyncBridge(defaults: defaults)
    }

    override func tearDown() {
        defaults.removePersistentDomain(forName: suiteName)
        bridge = nil
        defaults = nil
        suiteName = nil
        super.tearDown()
    }

    func testApplyStoresFavoriteIdsFromApplicationContext() {
        bridge.apply(context: [
            "favorite_ids": ["station-1", "station-2"],
            "home_station_id": "station-1",
            "work_station_id": "station-2",
        ])

        XCTAssertEqual(bridge.favoriteIds, Set(["station-1", "station-2"]))
        XCTAssertEqual(bridge.homeStationId, "station-1")
        XCTAssertEqual(bridge.workStationId, "station-2")
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: WatchFavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["station-1", "station-2"])
        )
        XCTAssertEqual(defaults.string(forKey: WatchFavoritesSyncBridge.homeStationCacheKey), "station-1")
        XCTAssertEqual(defaults.string(forKey: WatchFavoritesSyncBridge.workStationCacheKey), "station-2")
    }

    func testApplyWithoutFavoriteIdsPreservesCachedFavorites() {
        defaults.set(["station-7"], forKey: WatchFavoritesSyncBridge.favoritesCacheKey)
        defaults.set("station-home", forKey: WatchFavoritesSyncBridge.homeStationCacheKey)
        defaults.set("station-work", forKey: WatchFavoritesSyncBridge.workStationCacheKey)
        bridge = WatchFavoritesSyncBridge(defaults: defaults)

        bridge.apply(context: [:])

        XCTAssertEqual(bridge.favoriteIds, Set(["station-7"]))
        XCTAssertEqual(bridge.homeStationId, "station-home")
        XCTAssertEqual(bridge.workStationId, "station-work")
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: WatchFavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["station-7"])
        )
    }

    func testApplyClearsSavedPlacesWhenApplicationContextSendsEmptyStrings() {
        defaults.set(["station-7"], forKey: WatchFavoritesSyncBridge.favoritesCacheKey)
        defaults.set("station-home", forKey: WatchFavoritesSyncBridge.homeStationCacheKey)
        defaults.set("station-work", forKey: WatchFavoritesSyncBridge.workStationCacheKey)
        bridge = WatchFavoritesSyncBridge(defaults: defaults)

        bridge.apply(context: [
            "favorite_ids": ["station-7"],
            "home_station_id": "",
            "work_station_id": "",
        ])

        XCTAssertEqual(bridge.favoriteIds, Set(["station-7"]))
        XCTAssertNil(bridge.homeStationId)
        XCTAssertNil(bridge.workStationId)
        XCTAssertEqual(
            Set(defaults.stringArray(forKey: WatchFavoritesSyncBridge.favoritesCacheKey) ?? []),
            Set(["station-7"])
        )
        XCTAssertNil(defaults.string(forKey: WatchFavoritesSyncBridge.homeStationCacheKey))
        XCTAssertNil(defaults.string(forKey: WatchFavoritesSyncBridge.workStationCacheKey))
    }

    func testApplyStoresMonitoringSessionFromApplicationContext() throws {
        let monitoringSession = WatchConnectivityMonitoringSession(
            stationId: "station-9",
            stationName: "Plaza España",
            bikesAvailable: 2,
            docksAvailable: 8,
            statusText: "Monitorizando",
            statusLevel: "Good",
            expiresAtEpoch: 9_999_999_999,
            lastUpdatedEpoch: 1_234,
            isActive: true,
            alternativeStationId: "station-10",
            alternativeStationName: "Universidad",
            alternativeDistanceMeters: 240
        )
        let monitoringData = try JSONEncoder().encode(monitoringSession)

        bridge.apply(context: [
            "favorite_ids": ["station-9"],
            WatchFavoritesSyncBridge.monitoringSessionContextKey: monitoringData,
        ])

        XCTAssertEqual(bridge.monitoringSession, monitoringSession)
        XCTAssertEqual(defaults.data(forKey: WatchFavoritesSyncBridge.monitoringSessionCacheKey), monitoringData)
    }

    func testApplyWithoutMonitoringSessionClearsCachedMonitoringWhenContextArrives() throws {
        let monitoringSession = WatchConnectivityMonitoringSession(
            stationId: "station-9",
            stationName: "Plaza España",
            bikesAvailable: 2,
            docksAvailable: 8,
            statusText: "Monitorizando",
            statusLevel: "Good",
            expiresAtEpoch: 9_999_999_999,
            lastUpdatedEpoch: 1_234,
            isActive: true,
            alternativeStationId: nil,
            alternativeStationName: nil,
            alternativeDistanceMeters: nil
        )
        defaults.set(
            try JSONEncoder().encode(monitoringSession),
            forKey: WatchFavoritesSyncBridge.monitoringSessionCacheKey
        )
        bridge = WatchFavoritesSyncBridge(defaults: defaults)

        bridge.apply(context: ["favorite_ids": ["station-1"]])

        XCTAssertNil(bridge.monitoringSession)
        XCTAssertNil(defaults.data(forKey: WatchFavoritesSyncBridge.monitoringSessionCacheKey))
    }
}
