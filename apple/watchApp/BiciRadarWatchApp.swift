import AppIntents
import SwiftUI

// MARK: - Paleta BiciRadar

private extension Color {
    // Tema claro
    static let biziPrimary = Color(hex: 0x1D74BD)
    static let biziSecondary = Color(hex: 0x64C23A)
    static let biziTertiary = Color(hex: 0x0D1B2A)
    static let biziNeutral = Color(hex: 0x64779D)
    // Tema oscuro (watch siempre oscuro)
    static let biziDarkPrimary = Color(hex: 0x1070CA)
    static let biziDarkSecondary = Color(hex: 0x64C832)
    static let biziDarkTertiary = Color(hex: 0xA05ABA)
    static let biziDarkNeutral = Color(hex: 0x0F172A)
    // Semánticos
    static let biziError = Color(hex: 0xCF6679)
    static let biziWarning = Color(hex: 0xF28000)

    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255
        let g = Double((hex >> 8) & 0xFF) / 255
        let b = Double(hex & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

// MARK: - App

@main
struct BiciRadarWatchApp: App {
    init() {
        WatchFavoritesSyncBridge.shared.activate()
        Task {
            WatchBiziAppShortcuts.updateAppShortcutParameters()
        }
    }

    var body: some Scene {
        WindowGroup {
            WatchRootView()
        }
    }
}

private enum WatchScreenshotSurface: String {
    case dashboard
    case stationDetail = "station_detail"
    case monitoring
}

private let watchScreenshotModeKey = "bizizaragoza.watch.screenshot_mode"
private let watchScreenshotSurfaceKey = "bizizaragoza.watch.screenshot_surface"

private struct WatchRootView: View {
    private let defaults = UserDefaults.standard

    var body: some View {
        switch screenshotSurface() {
        case .dashboard:
            WatchDashboardView()
        case .stationDetail:
            WatchScreenshotStationDetailView()
        case .monitoring:
            WatchScreenshotMonitoringView()
        }
    }

    private func screenshotSurface() -> WatchScreenshotSurface {
        guard defaults.bool(forKey: watchScreenshotModeKey) else {
            return .dashboard
        }
        return WatchScreenshotSurface(rawValue: defaults.string(forKey: watchScreenshotSurfaceKey) ?? "") ?? .dashboard
    }
}

// MARK: - Dashboard

struct WatchDashboardView: View {
    @StateObject private var model = WatchDashboardModel()
    @ObservedObject private var syncBridge = WatchFavoritesSyncBridge.shared
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        NavigationStack {
            List {
                // Header
                Section {
                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 6) {
                            Circle()
                                .fill(Color.biziDarkPrimary)
                                .frame(width: 8, height: 8)
                            Text("BiciRadar")
                                .font(.headline)
                                .foregroundStyle(Color.biziDarkPrimary)
                        }
                        Text("Estaciones cerca y favoritas.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                        HStack(spacing: 6) {
                            WatchCountBadge(
                                label: "Cerca",
                                count: model.nearbyStations.count,
                                color: .biziDarkPrimary
                            )
                            WatchCountBadge(
                                label: "Fav",
                                count: syncBridge.favoriteIds.count,
                                color: .biziDarkSecondary
                            )
                        }
                    }
                    .padding(.vertical, 4)
                    .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.nearbyStations.count)
                    .animation(.spring(response: 0.36, dampingFraction: 0.82), value: syncBridge.favoriteIds.count)
                }

                if let errorMessage = model.errorMessage {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Label(errorMessage, systemImage: "exclamationmark.triangle.fill")
                                .font(.footnote)
                                .foregroundStyle(Color.biziError)
                                .transition(.opacity)
                            Button {
                                refreshDashboard(forceRefresh: true)
                            } label: {
                                Label("Reintentar", systemImage: "arrow.clockwise.circle")
                                    .foregroundStyle(Color.biziDarkPrimary)
                            }
                            .buttonStyle(.bordered)
                        }
                    }
                }

                if let monitoringSession = syncBridge.monitoringSession, monitoringSession.isActive {
                    Section("Monitorización") {
                        WatchMonitoringCard(session: monitoringSession)
                    }
                }

                Section("Cerca de ti") {
                    if model.nearbyStations.isEmpty, model.isLoading {
                        HStack {
                            ProgressView()
                                .tint(Color.biziDarkPrimary)
                            Text("Cargando…")
                                .font(.footnote)
                                .foregroundStyle(.secondary)
                        }
                    } else {
                        ForEach(model.nearbyStations) { station in
                            NavigationLink {
                                WatchStationDetailView(station: station)
                            } label: {
                                StationRow(station: station)
                            }
                        }
                    }
                }

                Section("Sincronizadas") {
                    if model.favoriteStations.isEmpty {
                        Text(syncBridge.favoriteIds.isEmpty
                             ? "Esperando favoritas del iPhone."
                             : "Todavía no he podido cargar tus favoritas.")
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(model.favoriteStations) { station in
                            NavigationLink {
                                WatchStationDetailView(station: station)
                            } label: {
                                StationRow(station: station)
                            }
                        }
                    }
                }

                Section {
                    Button {
                        refreshDashboard(forceRefresh: true)
                    } label: {
                        Label("Actualizar", systemImage: "arrow.clockwise")
                            .foregroundStyle(Color.biziDarkPrimary)
                    }
                }
            }
            .navigationTitle("BiciRadar")
            .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.nearbyStations.count)
            .animation(.spring(response: 0.36, dampingFraction: 0.82), value: model.favoriteStations.count)
            .task {
                await model.refresh(
                    favoriteIds: syncBridge.favoriteIds,
                    homeStationId: syncBridge.homeStationId,
                    workStationId: syncBridge.workStationId,
                    forceRefresh: true
                )
            }
            .onChange(of: scenePhase) { phase in
                guard phase == .active else { return }
                refreshDashboard(forceRefresh: true)
            }
            .onChange(of: syncBridge.favoriteIds) { favoriteIds in
                Task {
                    await model.refresh(
                        favoriteIds: favoriteIds,
                        homeStationId: syncBridge.homeStationId,
                        workStationId: syncBridge.workStationId,
                        forceRefresh: true
                    )
                }
            }
            .onChange(of: syncBridge.homeStationId) { _ in
                Task {
                    await model.refresh(
                        favoriteIds: syncBridge.favoriteIds,
                        homeStationId: syncBridge.homeStationId,
                        workStationId: syncBridge.workStationId,
                        forceRefresh: true
                    )
                }
            }
            .onChange(of: syncBridge.workStationId) { _ in
                Task {
                    await model.refresh(
                        favoriteIds: syncBridge.favoriteIds,
                        homeStationId: syncBridge.homeStationId,
                        workStationId: syncBridge.workStationId,
                        forceRefresh: true
                    )
                }
            }
        }
    }

    private func refreshDashboard(forceRefresh: Bool = false) {
        Task {
            await model.refresh(
                favoriteIds: syncBridge.favoriteIds,
                homeStationId: syncBridge.homeStationId,
                workStationId: syncBridge.workStationId,
                forceRefresh: forceRefresh
            )
        }
    }
}

// MARK: - Station Row

private struct StationRow: View {
    let station: WatchStationSnapshot

    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text(station.name)
                .font(.headline)
                .lineLimit(2)
            Text(station.address)
                .font(.footnote)
                .foregroundStyle(.secondary)
                .lineLimit(1)
            Text(station.statusText)
                .font(.caption2.weight(.semibold))
                .foregroundStyle(watchStatusColor(station.statusLevel))
            HStack(spacing: 5) {
                WatchInfoBadge(
                    label: "Dist",
                    value: watchDistanceText(station.distanceMeters),
                    tint: .biziNeutral
                )
                WatchInfoBadge(
                    label: "B",
                    value: "\(station.bikesAvailable)",
                    tint: station.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                )
                WatchInfoBadge(
                    label: "H",
                    value: "\(station.slotsFree)",
                    tint: station.slotsFree > 0 ? .biziDarkSecondary : .biziError
                )
            }
        }
        .padding(.vertical, 2)
        .animation(.spring(response: 0.32, dampingFraction: 0.85), value: station.bikesAvailable)
        .animation(.spring(response: 0.32, dampingFraction: 0.85), value: station.slotsFree)
    }
}

// MARK: - Detail View

private struct WatchStationDetailView: View {
    let station: WatchStationSnapshot
    @State private var routeStatus: String?
    @State private var localRouteStatus: String?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 10) {
                Text(station.name)
                    .font(.headline)
                    .foregroundStyle(Color.biziDarkPrimary)
                Text(station.address)
                    .font(.footnote)
                    .foregroundStyle(.secondary)

                // Stats row
                HStack(spacing: 6) {
                    WatchStatPill(
                        systemImage: "location.fill",
                        value: watchDistanceText(station.distanceMeters),
                        tint: .biziNeutral
                    )
                    WatchStatPill(
                        systemImage: "bicycle",
                        value: "\(station.bikesAvailable)",
                        tint: station.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                    )
                    WatchStatPill(
                        systemImage: "parkingsign",
                        value: "\(station.slotsFree)",
                        tint: station.slotsFree > 0 ? .biziDarkSecondary : .biziError
                    )
                }
                Text(station.statusText)
                    .font(.footnote.weight(.semibold))
                    .foregroundStyle(watchStatusColor(station.statusLevel))

                Button {
                    Task {
                        do {
                            let launchedStation = try await BiziWatchGraph.shared.openRoute(to: station.id)
                            localRouteStatus = launchedStation == nil
                                ? "No he encontrado esa estación."
                                : "Abriendo la ruta en el reloj."
                        } catch {
                            localRouteStatus = "No se pudo abrir la ruta."
                        }
                    }
                } label: {
                    Label("Ruta aquí", systemImage: "figure.walk")
                }
                .buttonStyle(.bordered)
                .tint(Color.biziDarkPrimary)

                Button {
                    let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: station.id)
                    routeStatus = requested
                        ? "He enviado la ruta al iPhone."
                        : "No he podido contactar con el iPhone."
                } label: {
                    Label("Ruta en iPhone", systemImage: "iphone")
                }
                .buttonStyle(.borderedProminent)
                .tint(Color.biziDarkSecondary)

                if let localRouteStatus {
                    StatusMessage(text: localRouteStatus)
                }
                if let routeStatus {
                    StatusMessage(text: routeStatus)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .animation(.spring(response: 0.32, dampingFraction: 0.84), value: localRouteStatus)
            .animation(.spring(response: 0.32, dampingFraction: 0.84), value: routeStatus)
        }
        .navigationTitle("Detalle")
    }
}

private struct WatchScreenshotStationDetailView: View {
    @State private var station = sampleWatchStation

    var body: some View {
        NavigationStack {
            WatchStationDetailView(station: station)
        }
        .task {
            let resolvedStationWithBikes = try? await BiziWatchGraph.shared.nearestStationWithBikes()
            let resolvedNearestStation = try? await BiziWatchGraph.shared.nearestStation()
            if let resolvedStation = resolvedStationWithBikes ?? resolvedNearestStation {
                station = resolvedStation
            }
        }
    }
}

private struct WatchScreenshotMonitoringView: View {
    var body: some View {
        NavigationStack {
            List {
                Section("Monitorización") {
                    WatchMonitoringCard(session: sampleWatchMonitoringSession)
                }
                Section("Cerca de ti") {
                    StationRow(station: sampleWatchStation)
                }
            }
            .navigationTitle("BiciRadar")
        }
    }
}

private func watchStatusColor(_ statusLevel: WatchStationStatusLevel) -> Color {
    switch statusLevel {
    case .good:
        return .biziDarkSecondary
    case .low:
        return .biziWarning
    case .empty, .full:
        return .biziError
    }
}

private struct WatchMonitoringCard: View {
    let session: WatchConnectivityMonitoringSession
    @State private var routeStatus: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(session.stationName)
                .font(.headline)
                .lineLimit(2)
            Text(session.statusText)
                .font(.caption2.weight(.semibold))
                .foregroundStyle(monitoringColor(session.color))
            HStack(spacing: 5) {
                WatchInfoBadge(
                    label: "B",
                    value: "\(session.bikesAvailable)",
                    tint: session.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                )
                WatchInfoBadge(
                    label: "H",
                    value: "\(session.docksAvailable)",
                    tint: session.docksAvailable > 0 ? .biziDarkSecondary : .biziError
                )
                WatchInfoBadge(
                    label: "T",
                    value: session.remainingMinutesText,
                    tint: .biziNeutral
                )
            }
            if let alternativeStationName = session.alternativeStationName, !alternativeStationName.isEmpty {
                Text(alternativeText(session))
                    .font(.footnote)
                    .foregroundStyle(.secondary)
                if let alternativeStationId = session.alternativeStationId {
                    Button {
                        let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: alternativeStationId)
                        routeStatus = requested
                            ? "He enviado la alternativa al iPhone."
                            : "No he podido contactar con el iPhone."
                    } label: {
                        Label("Ruta alternativa", systemImage: "arrow.triangle.turn.up.right.circle")
                    }
                    .buttonStyle(.bordered)
                    .tint(Color.biziDarkSecondary)
                }
            }
            Button {
                let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: session.stationId)
                routeStatus = requested
                    ? "He enviado la ruta al iPhone."
                    : "No he podido contactar con el iPhone."
            } label: {
                Label("Ruta en iPhone", systemImage: "iphone")
            }
            .buttonStyle(.borderedProminent)
            .tint(Color.biziDarkPrimary)

            if let routeStatus {
                StatusMessage(text: routeStatus)
            }
        }
        .padding(.vertical, 2)
        .animation(.spring(response: 0.32, dampingFraction: 0.84), value: session.statusText)
        .animation(.spring(response: 0.32, dampingFraction: 0.84), value: routeStatus)
    }
}

private func alternativeText(_ session: WatchConnectivityMonitoringSession) -> String {
    guard let alternativeStationName = session.alternativeStationName, !alternativeStationName.isEmpty else {
        return "Alternativa no disponible"
    }
    if let alternativeDistanceMeters = session.alternativeDistanceMeters {
        return "Alternativa: \(alternativeStationName) (\(watchDistanceText(alternativeDistanceMeters)))"
    }
    return "Alternativa: \(alternativeStationName)"
}

private func watchDistanceText(_ meters: Int) -> String {
    if meters >= 1000 {
        let km = Double(meters) / 1000.0
        let rounded = (km * 10).rounded(.towardZero) / 10
        if rounded.truncatingRemainder(dividingBy: 1) == 0 {
            return "\(Int(rounded)) km"
        }
        return "\(rounded) km"
    }
    return "\(meters) m"
}

private func monitoringColor(_ accent: WatchMonitoringAccent) -> Color {
    switch accent {
    case .good:
        return .biziDarkSecondary
    case .warning:
        return .biziWarning
    case .error:
        return .biziError
    }
}

// MARK: - Reusable Components

private struct WatchInfoBadge: View {
    let label: String
    let value: String
    var tint: Color = .biziNeutral

    var body: some View {
        VStack(spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(tint.opacity(0.7))
            Text(value)
                .font(.footnote.weight(.semibold))
                .foregroundStyle(tint)
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 5)
        .background(tint.opacity(0.15), in: RoundedRectangle(cornerRadius: 10, style: .continuous))
        .animation(.spring(response: 0.3, dampingFraction: 0.82), value: value)
    }
}

private struct WatchCountBadge: View {
    let label: String
    let count: Int
    let color: Color

    var body: some View {
        HStack(spacing: 3) {
            Text("\(count)")
                .font(.footnote.weight(.bold))
                .foregroundStyle(color)
            Text(label)
                .font(.caption2)
                .foregroundStyle(color.opacity(0.7))
        }
        .padding(.horizontal, 7)
        .padding(.vertical, 4)
        .background(color.opacity(0.15), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}

private struct WatchStatPill: View {
    let systemImage: String
    let value: String
    let tint: Color

    var body: some View {
        HStack(spacing: 3) {
            Image(systemName: systemImage)
                .font(.caption2)
            Text(value)
                .font(.caption.weight(.semibold))
        }
        .foregroundStyle(tint)
        .padding(.horizontal, 6)
        .padding(.vertical, 4)
        .background(tint.opacity(0.15), in: RoundedRectangle(cornerRadius: 8, style: .continuous))
    }
}

private struct StatusMessage: View {
    let text: String

    var body: some View {
        Text(text)
            .font(.footnote)
            .foregroundStyle(.secondary)
            .transition(.opacity.combined(with: .move(edge: .bottom)))
    }
}

private let sampleWatchStation = WatchStationSnapshot(
    id: "screenshot-station",
    name: "19-Asalto. Servet",
    address: "C/ Asalto - C/ Miguel Servet",
    bikesAvailable: 10,
    slotsFree: 12,
    distanceMeters: 48
)

private let sampleWatchMonitoringSession = WatchConnectivityMonitoringSession(
    stationId: "screenshot-station",
    stationName: "19-Asalto. Servet",
    bikesAvailable: 10,
    docksAvailable: 12,
    statusText: "Disponible",
    statusLevel: "Good",
    expiresAtEpoch: Int64(Date().timeIntervalSince1970 * 1000) + 20 * 60 * 1000,
    lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
    isActive: true,
    alternativeStationId: "screenshot-alt",
    alternativeStationName: "96-Pomarón",
    alternativeDistanceMeters: 211
)
