import SwiftUI

@main
struct BiziZaragozaApp: App {
    var body: some Scene {
        WindowGroup {
            NavigationStack {
                VStack(alignment: .leading, spacing: 16) {
                    Text("Bizi Zaragoza")
                        .font(.largeTitle.bold())
                        .foregroundStyle(.red)

                    Text("Shell SwiftUI para integrar la UI Compose compartida desde `BiziMobileUi.MainViewController()` y coordinar Siri, rutas y sync con Apple Watch.")
                        .foregroundStyle(.secondary)

                    NavigationLink("Abrir App Intents") {
                        IOSAssistantShortcutsView()
                    }
                    .buttonStyle(.borderedProminent)

                    Spacer()
                }
                .padding(24)
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
