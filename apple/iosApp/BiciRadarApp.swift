import AppIntents
import BiziMobileUi
import ConnectIQ
import Foundation
import OSLog
import SwiftUI
import UIKit
import WidgetKit

@main
struct BiciRadarApp: App {
    @Environment(\.scenePhase) private var scenePhase

    /// Single long-lived wrapper — the Compose tree is never torn down on navigation.
    private let composeWrapper: BiziMainViewControllerWrapper = {
        let factory: (any StationMapViewFactory)? = GoogleMapsBootstrap.isSdkLinked()
            ? GoogleMapsStationMapFactory()
            : nil
        return BiziMobileViewControllerKt.MainViewControllerWrapper(
            launchRequest: nil,
            stationMapViewFactory: factory,
            remoteConfigBridge: FirebaseBootstrap.remoteConfigBridge
        )
    }()

    init() {
        FavoritesSyncBridge.shared.activate()
        FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
        FirebaseBootstrap.configureIfAvailable()
        GoogleMapsBootstrap.configureIfAvailable()
        GarminConnectManager.shared.start()
        BiziBackgroundTaskHandler.registerTasks()
        Task {
            BiziAppShortcuts.updateAppShortcutParameters()
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
                    SurfaceMonitoringActivityController.shared.startRefreshing()
                    FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
                    AppleSurfaceRefreshCoordinator.shared.refreshNow(
                        reason: "initial app appear",
                        forceDataRefresh: false
                    )
                }
                .onOpenURL { url in
                    if GarminConnectManager.shared.handleOpenURL(url) {
                        return
                    }
                    if AppleDeepLinkParser.isGarminPairingRequest(url) {
                        GarminConnectManager.shared.beginPairing()
                        return
                    }
                    guard let request = AppleDeepLinkParser.parse(url) else { return }
                    AppleLaunchRequestStore.shared.save(request)
                    applyPendingLaunchRequest()
                }
                .onChange(of: scenePhase) { newPhase in
                    switch newPhase {
                    case .active:
                        applyPendingLaunchRequest()
                        GarminConnectManager.shared.sceneDidBecomeActive()
                        FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
                        SurfaceMonitoringActivityController.shared.startRefreshing()
                        composeWrapper.requestRefresh()
                        AppleSurfaceRefreshCoordinator.shared.refreshNow(
                            reason: "scene active",
                            forceDataRefresh: true
                        )
                    case .background:
                        BiziBackgroundTaskHandler.scheduleAppRefresh()
                        handleBackgroundTransitionForMonitoring()
                        FavoritesSyncBridge.shared.syncWatchContextFromAppGroup()
                        FavoritesSyncBridge.shared.syncMonitoringFromSurfaceSnapshot()
                        SurfaceMonitoringActivityController.shared.stopRefreshing()
                        AppleSurfaceRefreshCoordinator.shared.scheduleRefresh(
                            reason: "scene background",
                            forceDataRefresh: false,
                            syncMonitoring: true,
                            delayNanoseconds: 250_000_000
                        )
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

@MainActor
final class AppleSurfaceRefreshCoordinator {
    static let shared = AppleSurfaceRefreshCoordinator()

    private let logger = Logger(subsystem: "com.gcaguilar.biciradar.ios", category: "AppleSurfaceRefresh")
    private var scheduledTask: Task<Void, Never>?
    private var pendingForceDataRefresh = false
    private var pendingSyncMonitoring = false

    func refreshNow(
        reason: String,
        forceDataRefresh: Bool = false,
        syncMonitoring: Bool = true
    ) {
        enqueue(
            reason: reason,
            forceDataRefresh: forceDataRefresh,
            syncMonitoring: syncMonitoring,
            delayNanoseconds: 0
        )
    }

    func scheduleRefresh(
        reason: String,
        forceDataRefresh: Bool = false,
        syncMonitoring: Bool = true,
        delayNanoseconds: UInt64 = 750_000_000
    ) {
        enqueue(
            reason: reason,
            forceDataRefresh: forceDataRefresh,
            syncMonitoring: syncMonitoring,
            delayNanoseconds: delayNanoseconds
        )
    }

    private func enqueue(
        reason: String,
        forceDataRefresh: Bool,
        syncMonitoring: Bool,
        delayNanoseconds: UInt64
    ) {
        pendingForceDataRefresh = pendingForceDataRefresh || forceDataRefresh
        pendingSyncMonitoring = pendingSyncMonitoring || syncMonitoring
        scheduledTask?.cancel()
        scheduledTask = Task { [weak self] in
            if delayNanoseconds > 0 {
                try? await Task.sleep(nanoseconds: delayNanoseconds)
            }
            guard !Task.isCancelled else { return }
            await self?.performRefresh(reason: reason)
        }
    }

    private func performRefresh(reason: String) async {
        let forceDataRefresh = pendingForceDataRefresh
        let syncMonitoring = pendingSyncMonitoring
        pendingForceDataRefresh = false
        pendingSyncMonitoring = false
        scheduledTask = nil

        do {
            if forceDataRefresh {
                try await BiziAppleGraph.shared.refreshData(forceRefresh: true)
            }
            _ = try await BiziAppleGraph.shared.refreshWidgetData(reloadTimelines: false)

            WidgetTimelineReloadScheduler.shared.scheduleReloads()

            if syncMonitoring {
                FavoritesSyncBridge.shared.syncMonitoringFromSurfaceSnapshot()
                SurfaceMonitoringActivityController.shared.syncNow()
            }
        } catch {
            logger.error("Surface refresh failed (\(reason, privacy: .public)): \(error.localizedDescription, privacy: .public)")
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
