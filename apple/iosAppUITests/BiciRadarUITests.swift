import XCTest

final class BiciRadarUITests: XCTestCase {
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testNearbyLoadsByDefault() {
        let app = makeApp()
        app.launch()

        XCTAssertTrue(app.staticTexts["Cerca"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Estaciones cercanas"].waitForExistence(timeout: 20))
    }

    func testPendingFavoritesLaunchOpensFavoritesScreen() {
        let app = makeApp(pendingAction: "favorite_stations")
        app.launch()

        XCTAssertTrue(app.staticTexts["Favoritos"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Buscar estación para fijarla o filtrar favoritas"].waitForExistence(timeout: 20))
    }

    func testPendingAssistantLaunchOpensShortcutsScreen() {
        let app = makeApp(pendingAction: "open_assistant")
        app.launch()

        XCTAssertTrue(app.staticTexts["Atajos"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Cómo invocarlos"].waitForExistence(timeout: 20))
    }

    func testUnknownPendingStationLaunchDoesNotBlockDashboard() {
        let app = makeApp(
            pendingAction: "show_station",
            pendingStationId: "ui-test-missing-station"
        )
        app.launch()

        XCTAssertTrue(app.staticTexts["Estaciones cercanas"].waitForExistence(timeout: 20))
    }

    private func makeApp(
        pendingAction: String? = nil,
        pendingStationId: String? = nil
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["BIZI_UI_TEST_MODE"] = "1"
        if let pendingAction {
            app.launchEnvironment["BIZI_UI_TEST_PENDING_ACTION"] = pendingAction
        }
        if let pendingStationId {
            app.launchEnvironment["BIZI_UI_TEST_PENDING_STATION_ID"] = pendingStationId
        }
        return app
    }
}
