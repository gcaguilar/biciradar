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
        BiziBackgroundTaskHandler.registerTasks()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView(wrapper: composeWrapper)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(uiColor: .systemBackground))
                .ignoresSafeArea()
                .onAppear(perform: applyPendingLaunchRequest)
                .onAppear { requestNotificationPermission() }
                .onChange(of: scenePhase) { newPhase in
                    switch newPhase {
                    case .active:
                        applyPendingLaunchRequest()
                    case .background:
                        BiziBackgroundTaskHandler.scheduleAppRefresh()
                        handleBackgroundTransitionForMonitoring()
                    default:
                        break
                    }
                }
        }
    }

    private func applyPendingLaunchRequest() {
        guard let request = AppleLaunchRequestStore.shared.takePendingRequest() else { return }
        composeWrapper.updateLaunchRequest(request: request)
    }

    private func requestNotificationPermission() {
        Task {
            await BiziNotificationService.shared.requestAuthorization()
        }
    }

    private func handleBackgroundTransitionForMonitoring() {
        let bgTask = UIApplication.shared.beginBackgroundTask(withName: "BiziTripMonitor") {
            // Expiry handler — time ran out, do nothing further
        }
        guard bgTask != .invalid else { return }

        composeWrapper.doFinalBackgroundCheck {
            UIApplication.shared.endBackgroundTask(bgTask)
        }
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
