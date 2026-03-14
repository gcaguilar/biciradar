import BiziMobileUi
import SwiftUI
import UIKit

@main
struct BiziZaragozaApp: App {
    @Environment(\.scenePhase) private var scenePhase

    /// Single long-lived wrapper — the Compose tree is never torn down on navigation.
    private let composeWrapper: BiziMainViewControllerWrapper = {
        let factory: (any StationMapViewFactory)? = GoogleMapsBootstrap.isSdkLinked()
            ? GoogleMapsStationMapFactory()
            : nil
        return BiziMobileViewControllerKt.MainViewControllerWrapper(
            launchRequest: nil,
            stationMapViewFactory: factory
        )
    }()

    init() {
        AppleLaunchRequestStore.shared.seedFromLaunchEnvironment()
        FavoritesSyncBridge.shared.activate()
        FirebaseBootstrap.configureIfAvailable()
        GoogleMapsBootstrap.configureIfAvailable()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView(wrapper: composeWrapper)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(uiColor: .systemBackground))
                .ignoresSafeArea()
                .onAppear(perform: applyPendingLaunchRequest)
                .onChange(of: scenePhase) { newPhase in
                    if newPhase == .active {
                        applyPendingLaunchRequest()
                    }
                }
        }
    }

    private func applyPendingLaunchRequest() {
        guard let request = AppleLaunchRequestStore.shared.takePendingRequest() else { return }
        composeWrapper.updateLaunchRequest(request: request)
    }
}

struct IOSAssistantShortcutsView: View {
    var body: some View {
        List {
            Label("Estación más cercana", systemImage: "location.circle")
            Label("Mis favoritas", systemImage: "heart.circle")
            Label("Estado de estación", systemImage: "info.circle")
            Label("Ruta a estación", systemImage: "map.circle")
        }
        .navigationTitle("Atajos")
    }
}
