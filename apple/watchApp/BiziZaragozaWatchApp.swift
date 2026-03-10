import SwiftUI

@main
struct BiziZaragozaWatchApp: App {
    var body: some Scene {
        WindowGroup {
            WatchDashboardView()
        }
    }
}

struct WatchDashboardView: View {
    var body: some View {
        List {
            Section("Cercanas") {
                Label("Plaza España · 150 m", systemImage: "bicycle")
                Label("Plaza Aragón · 300 m", systemImage: "bicycle")
            }

            Section("Acciones") {
                Label("Ver favoritas", systemImage: "heart.circle")
                Label("Sincronizar favoritas", systemImage: "arrow.triangle.2.circlepath.circle")
                Label("Abrir ruta en el iPhone", systemImage: "map.circle")
            }
        }
    }
}
