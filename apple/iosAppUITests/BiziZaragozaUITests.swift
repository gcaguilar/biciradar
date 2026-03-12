import XCTest

final class BiziZaragozaUITests: XCTestCase {
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    func testDashboardLoadsByDefault() {
        let app = makeApp()
        app.launch()

        XCTAssertTrue(app.staticTexts["Bizi Zaragoza"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Buscar estación o dirección"].waitForExistence(timeout: 20))
    }

    func testPendingFavoritesLaunchOpensFavoritesScreen() {
        let app = makeApp(pendingAction: "favorite_stations")
        app.launch()

        XCTAssertTrue(app.staticTexts["Favoritas"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Filtrar favoritas"].waitForExistence(timeout: 20))
    }

    func testPendingAssistantLaunchOpensAssistantScreen() {
        let app = makeApp(pendingAction: "open_assistant")
        app.launch()

        XCTAssertTrue(app.staticTexts["Atajos y asistentes"].waitForExistence(timeout: 20))
        XCTAssertTrue(app.staticTexts["Estación más cercana"].waitForExistence(timeout: 20))
    }

    private func makeApp(
        pendingAction: String? = nil,
        pendingStationId: String? = nil
    ) -> XCUIApplication {
        let app = XCUIApplication()
        if let pendingAction {
            app.launchEnvironment["BIZI_UI_TEST_PENDING_ACTION"] = pendingAction
        }
        if let pendingStationId {
            app.launchEnvironment["BIZI_UI_TEST_PENDING_STATION_ID"] = pendingStationId
        }
        return app
    }
}
