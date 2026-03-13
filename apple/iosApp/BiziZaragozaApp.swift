import BiziMobileUi
import SwiftUI
import UIKit

@main
struct BiziZaragozaApp: App {
    @Environment(\.scenePhase) private var scenePhase
    @State private var launchRequest: (any MobileLaunchRequest)?
    @State private var launchToken: Int = 0

    init() {
        AppleLaunchRequestStore.shared.seedFromLaunchEnvironment()
        FavoritesSyncBridge.shared.activate()
        FirebaseBootstrap.configureIfAvailable()
        GoogleMapsBootstrap.configureIfAvailable()
    }

    var body: some Scene {
        WindowGroup {
            ComposeRootView(launchRequest: launchRequest)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(uiColor: .systemBackground))
                .ignoresSafeArea()
                .id(launchToken)
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
        launchRequest = request
        launchToken += 1
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
