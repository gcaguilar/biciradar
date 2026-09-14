@testable import BiciRadar
import XCTest

final class BiziHomeScreenQuickActionsTests: XCTestCase {
    func testAlwaysExposesNearbyAndFavorites() {
        let actions = BiziHomeScreenQuickActions.actions(from: nil)

        XCTAssertEqual(actions.map(\.type), ["surface_nearby", "surface_favorites"])
        XCTAssertEqual(actions.first?.deepLink, "biciradar://home")
    }

    func testAddsFavoriteStationAndMonitoring() {
        let actions = BiziHomeScreenQuickActions.actions(
            from: bundle(favorite: station(id: "station-42", name: "Plaza España"))
        )

        XCTAssertEqual(
            actions.map(\.type),
            ["surface_nearby", "surface_favorite_station", "surface_monitor_favorite", "surface_favorites"]
        )
        XCTAssertTrue(actions.contains { $0.deepLink == "biciradar://station/station-42" })
        XCTAssertTrue(actions.contains { $0.deepLink == "biciradar://monitor/station-42" })
    }

    func testAddsHomeAndWorkSavedPlaces() {
        let actions = BiziHomeScreenQuickActions.actions(
            from: bundle(
                home: station(id: "station-home", name: "Puerta del Carmen"),
                work: station(id: "station-work", name: "Plaza Aragón")
            )
        )

        XCTAssertEqual(
            actions.map(\.type),
            ["surface_nearby", "surface_home_station", "surface_work_station", "surface_favorites"]
        )
        XCTAssertTrue(actions.contains { $0.deepLink == "biciradar://station/station-home" })
        XCTAssertTrue(actions.contains { $0.deepLink == "biciradar://station/station-work" })
    }

    func testTrimsToIOSLimit() {
        let actions = BiziHomeScreenQuickActions.actions(
            from: bundle(
                favorite: station(id: "station-favorite", name: "Plaza España"),
                home: station(id: "station-home", name: "Puerta del Carmen"),
                work: station(id: "station-work", name: "Plaza Aragón")
            )
        )

        XCTAssertEqual(actions.count, BiziHomeScreenQuickActions.maxCount)
        XCTAssertEqual(
            actions.map(\.type),
            ["surface_nearby", "surface_home_station", "surface_work_station", "surface_favorite_station"]
        )
        XCTAssertFalse(actions.contains { $0.type == "surface_favorites" })
    }

    func testAvoidsDuplicateStationEntriesWhenFavoriteMatchesSavedPlace() {
        let actions = BiziHomeScreenQuickActions.actions(
            from: bundle(
                favorite: station(id: "station-home", name: "Puerta del Carmen"),
                home: station(id: "station-home", name: "Puerta del Carmen"),
                work: station(id: "station-work", name: "Plaza Aragón")
            )
        )

        XCTAssertEqual(
            actions.map(\.type),
            ["surface_nearby", "surface_home_station", "surface_work_station", "surface_monitor_favorite"]
        )
        XCTAssertEqual(actions.filter { $0.deepLink == "biciradar://station/station-home" }.count, 1)
    }
}

private func bundle(
    favorite: AppleSurfaceStationSnapshot? = nil,
    home: AppleSurfaceStationSnapshot? = nil,
    work: AppleSurfaceStationSnapshot? = nil
) -> AppleSurfaceSnapshotBundle {
    AppleSurfaceSnapshotBundle(
        generatedAtEpoch: 1,
        favoriteStation: favorite,
        homeStation: home,
        workStation: work,
        nearbyStations: [],
        monitoringSession: nil,
        state: AppleSurfaceState(
            hasLocationPermission: true,
            hasNotificationPermission: true,
            hasFavoriteStation: favorite != nil,
            isDataFresh: true,
            lastSyncEpoch: 1,
            cityId: "zaragoza",
            cityName: "Zaragoza",
            userLatitude: nil,
            userLongitude: nil
        )
    )
}

private func station(id: String, name: String) -> AppleSurfaceStationSnapshot {
    AppleSurfaceStationSnapshot(
        id: id,
        nameShort: name,
        nameFull: name,
        cityId: "zaragoza",
        latitude: 41.65,
        longitude: -0.88,
        bikesAvailable: 6,
        docksAvailable: 5,
        statusTextShort: "Disponible",
        statusLevel: .good,
        lastUpdatedEpoch: 1,
        distanceMeters: nil,
        isFavorite: true,
        alternativeStationId: nil,
        alternativeStationName: nil,
        alternativeDistanceMeters: nil
    )
}
