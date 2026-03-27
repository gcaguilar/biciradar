@testable import BiciRadar
import XCTest

final class AppleSurfaceStoreTests: XCTestCase {
    func testDecodesSavedPlacesFromSnapshotBundle() throws {
        let json = """
        {
          "generatedAtEpoch": 1000,
          "favoriteStation": {
            "id": "fav",
            "nameShort": "Favorita",
            "nameFull": "Favorita completa",
            "cityId": "zaragoza",
            "latitude": 41.65,
            "longitude": -0.88,
            "bikesAvailable": 4,
            "docksAvailable": 7,
            "statusTextShort": "Disponible",
            "statusLevel": "Good",
            "lastUpdatedEpoch": 900,
            "distanceMeters": 120,
            "isFavorite": true
          },
          "homeStation": {
            "id": "home",
            "nameShort": "Casa",
            "nameFull": "Casa completa",
            "cityId": "zaragoza",
            "latitude": 41.66,
            "longitude": -0.87,
            "bikesAvailable": 2,
            "docksAvailable": 9,
            "statusTextShort": "Pocas",
            "statusLevel": "Low",
            "lastUpdatedEpoch": 900,
            "distanceMeters": 80,
            "isFavorite": true
          },
          "workStation": {
            "id": "work",
            "nameShort": "Trabajo",
            "nameFull": "Trabajo completo",
            "cityId": "zaragoza",
            "latitude": 41.64,
            "longitude": -0.89,
            "bikesAvailable": 0,
            "docksAvailable": 5,
            "statusTextShort": "Sin bicis",
            "statusLevel": "Empty",
            "lastUpdatedEpoch": 900,
            "distanceMeters": 150,
            "isFavorite": false
          },
          "nearbyStations": [],
          "state": {
            "hasLocationPermission": true,
            "hasNotificationPermission": true,
            "hasFavoriteStation": true,
            "isDataFresh": true,
            "lastSyncEpoch": 900,
            "cityId": "zaragoza",
            "cityName": "Zaragoza",
            "userLatitude": 41.65,
            "userLongitude": -0.88
          }
        }
        """

        let bundle = try JSONDecoder().decode(AppleSurfaceSnapshotBundle.self, from: Data(json.utf8))

        XCTAssertEqual(bundle.homeStation?.id, "home")
        XCTAssertEqual(bundle.workStation?.id, "work")
        XCTAssertEqual(bundle.homeStation?.statusLevel, .low)
        XCTAssertEqual(bundle.workStation?.statusTextShort, "Sin bicis")
        XCTAssertEqual(bundle.station(for: .favorite)?.id, "fav")
        XCTAssertEqual(bundle.station(for: .home)?.id, "home")
        XCTAssertEqual(bundle.station(for: .work)?.id, "work")
    }

    func testDecodesLegacyBundleWithoutSavedPlaces() throws {
        let json = """
        {
          "generatedAtEpoch": 1000,
          "favoriteStation": null,
          "nearbyStations": [],
          "state": {
            "hasLocationPermission": true,
            "hasNotificationPermission": true,
            "hasFavoriteStation": false,
            "isDataFresh": false,
            "lastSyncEpoch": 900,
            "cityId": "zaragoza",
            "cityName": "Zaragoza",
            "userLatitude": null,
            "userLongitude": null
          }
        }
        """

        let bundle = try JSONDecoder().decode(AppleSurfaceSnapshotBundle.self, from: Data(json.utf8))

        XCTAssertNil(bundle.homeStation)
        XCTAssertNil(bundle.workStation)
        XCTAssertFalse(bundle.state.isDataFresh)
        XCTAssertNil(bundle.station(for: .home))
    }
}
