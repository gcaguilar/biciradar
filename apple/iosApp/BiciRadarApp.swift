import AppIntents
import BiziMobileUi
import Foundation
import SwiftUI
import UIKit
import WidgetKit

@main
struct BiciRadarApp: App {
    @Environment(\.scenePhase) private var scenePhase
    private let isUITesting = UITestConfiguration.isEnabled

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
        guard !UITestConfiguration.isEnabled else { return }
        FavoritesSyncBridge.shared.activate()
        FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
        FirebaseBootstrap.configureIfAvailable()
        GoogleMapsBootstrap.configureIfAvailable()
        BiziBackgroundTaskHandler.registerTasks()
        Task {
            try? await BiziAppShortcuts.updateAppShortcutParameters()
        }
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView(wrapper: composeWrapper)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(uiColor: .systemBackground))
                .ignoresSafeArea()
                .onAppear(perform: applyPendingLaunchRequest)
                .onAppear {
                    guard !isUITesting else { return }
                    requestNotificationPermission()
                    SurfaceMonitoringActivityController.shared.startRefreshing()
                    WidgetTimelineReloadScheduler.shared.scheduleReloads()
                }
                .onOpenURL { url in
                    guard let request = AppleDeepLinkParser.parse(url) else { return }
                    AppleLaunchRequestStore.shared.save(request)
                    applyPendingLaunchRequest()
                }
                .onChange(of: scenePhase) { newPhase in
                    switch newPhase {
                    case .active:
                        applyPendingLaunchRequest()
                        FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
                        SurfaceMonitoringActivityController.shared.startRefreshing()
                        composeWrapper.requestRefresh()
                        WidgetTimelineReloadScheduler.shared.scheduleReloads()
                        SurfaceMonitoringActivityController.shared.syncNow()
                    case .background:
                        guard !isUITesting else { break }
                        BiziBackgroundTaskHandler.scheduleAppRefresh()
                        handleBackgroundTransitionForMonitoring()
                        FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
                        FavoritesSyncBridge.shared.syncMonitoringFromSurfaceSnapshot()
                        SurfaceMonitoringActivityController.shared.stopRefreshing()
                        WidgetTimelineReloadScheduler.shared.scheduleReloads()
                    default:
                        SurfaceMonitoringActivityController.shared.stopRefreshing()
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

@MainActor
final class WidgetTimelineReloadScheduler {
    static let shared = WidgetTimelineReloadScheduler()

    private var reloadTask: Task<Void, Never>?

    func scheduleReloads() {
        reloadTask?.cancel()
        reloadTask = Task { @MainActor in
            let delays: [UInt64] = [0, 2, 8, 15].map { UInt64($0) * 1_000_000_000 }
            for delay in delays {
                if delay > 0 {
                    try? await Task.sleep(nanoseconds: delay)
                }
                guard !Task.isCancelled else { return }
                WidgetCenter.shared.reloadAllTimelines()
            }
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

private enum UITestConfiguration {
    static let enabledKey = "BIZI_UI_TEST_MODE"

    static var isEnabled: Bool {
        ProcessInfo.processInfo.environment[enabledKey] == "1"
    }
}
