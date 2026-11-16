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
        Group {
            switch screenshotSurface() {
            case .dashboard:
                WatchDashboardView()
            case .stationDetail:
                WatchScreenshotStationDetailView()
            case .monitoring:
                WatchScreenshotMonitoringView()
            }
        }
        // One accent for every control (buttons, links, progress) instead of per-view tints.
        .tint(Color.biziDarkPrimary)
    }

    private func screenshotSurface() -> WatchScreenshotSurface {
        guard defaults.bool(forKey: watchScreenshotModeKey) else {
            return .dashboard
        }
        return WatchScreenshotSurface(rawValue: defaults.string(forKey: watchScreenshotSurfaceKey) ?? "") ?? .dashboard
    }
}

// MARK: - Dashboard

private let watchSpring = Animation.spring(response: 0.34, dampingFraction: 0.84)

/// Navigation/background tint shared by the dashboard and the detail screens.
private extension ShapeStyle where Self == AnyGradient {
    static var biziWatchBackground: AnyGradient { Color.biziDarkPrimary.opacity(0.3).gradient }
}

struct WatchDashboardView: View {
    @StateObject private var model = WatchDashboardModel()
    @ObservedObject private var syncBridge = WatchFavoritesSyncBridge.shared
    @Environment(\.scenePhase) private var scenePhase

    var body: some View {
        NavigationStack {
            List {
                if let errorMessage = model.errorMessage {
                    Section {
                        VStack(alignment: .leading, spacing: 8) {
                            Label(errorMessage, systemImage: "exclamationmark.triangle.fill")
                                .font(.footnote)
                                .foregroundStyle(Color.biziError)
                            Button {
                                refreshDashboard(forceRefresh: true)
                            } label: {
                                Label("Reintentar", systemImage: "arrow.clockwise.circle")
                            }
                            .buttonStyle(.borderedProminent)
                        }
                        .transition(.opacity)
                    }
                }

                if let monitoringSession = syncBridge.monitoringSession, monitoringSession.isActive {
                    Section("Monitorización") {
                        WatchMonitoringCard(session: monitoringSession)
                    }
                }

                Section("Cerca de ti") {
                    if model.nearbyStations.isEmpty, model.isLoading {
                        HStack(spacing: 8) {
                            ProgressView()
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
            }
            .navigationTitle("BiciRadar")
            .containerBackground(.biziWatchBackground, for: .navigation)
            .toolbar {
                ToolbarItem(placement: .bottomBar) {
                    Button {
                        refreshDashboard(forceRefresh: true)
                    } label: {
                        Label("Actualizar", systemImage: "arrow.clockwise")
                    }
                    .disabled(model.isLoading)
                }
            }
            .animation(watchSpring, value: model.nearbyStations.count)
            .animation(watchSpring, value: model.favoriteStations.count)
            .animation(watchSpring, value: model.errorMessage)
            .task {
                await model.refresh(
                    favoriteIds: syncBridge.favoriteIds,
                    homeStationId: syncBridge.homeStationId,
                    workStationId: syncBridge.workStationId,
                    forceRefresh: true
                )
            }
            .onChange(of: scenePhase) { _, phase in
                guard phase == .active else { return }
                refreshDashboard(forceRefresh: true)
            }
            .onChange(of: syncBridge.favoriteIds) { refreshDashboard(forceRefresh: true) }
            .onChange(of: syncBridge.homeStationId) { refreshDashboard(forceRefresh: true) }
            .onChange(of: syncBridge.workStationId) { refreshDashboard(forceRefresh: true) }
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
        VStack(alignment: .leading, spacing: 4) {
            Text(station.name)
                .font(.headline)
                .lineLimit(2)
            Text(station.statusText)
                .font(.caption2.weight(.semibold))
                .foregroundStyle(watchStatusColor(station.statusLevel))
            HStack(spacing: 4) {
                WatchMetricPill(
                    systemImage: "bicycle",
                    value: "\(station.bikesAvailable)",
                    label: "Bicis",
                    tint: station.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                )
                WatchMetricPill(
                    systemImage: "parkingsign",
                    value: "\(station.slotsFree)",
                    label: "Huecos",
                    tint: station.slotsFree > 0 ? .biziDarkSecondary : .biziError
                )
                WatchMetricPill(
                    systemImage: "location.fill",
                    value: watchDistanceText(station.distanceMeters),
                    label: "Dist.",
                    tint: .biziNeutral
                )
            }
        }
        .padding(.vertical, 2)
        .accessibilityElement(children: .combine)
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
                VStack(alignment: .leading, spacing: 2) {
                    Text(station.name)
                        .font(.headline)
                    Text(station.address)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                }
                .accessibilityElement(children: .combine)

                // The two numbers people open the app for: big, rounded, monospaced.
                HStack(spacing: 6) {
                    WatchAvailabilityTile(
                        systemImage: "bicycle",
                        value: station.bikesAvailable,
                        label: "Bicis",
                        tint: station.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                    )
                    WatchAvailabilityTile(
                        systemImage: "parkingsign",
                        value: station.slotsFree,
                        label: "Huecos",
                        tint: station.slotsFree > 0 ? .biziDarkSecondary : .biziError
                    )
                }

                HStack(spacing: 6) {
                    Text(station.statusText)
                        .font(.footnote.weight(.semibold))
                        .foregroundStyle(watchStatusColor(station.statusLevel))
                    Spacer(minLength: 0)
                    Label(watchDistanceText(station.distanceMeters), systemImage: "location.fill")
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .accessibilityLabel(Text("Dist."))
                        .accessibilityValue(Text(watchDistanceText(station.distanceMeters)))
                }

                Button {
                    Task {
                        do {
                            let launchedStation = try await BiziWatchGraph.shared.openRoute(to: station.id)
                            localRouteStatus = launchedStation == nil
                                ? String(localized: "No he encontrado esa estación.")
                                : String(localized: "Abriendo la ruta en el reloj.")
                        } catch {
                            localRouteStatus = String(localized: "No se pudo abrir la ruta.")
                        }
                    }
                } label: {
                    Label("Ruta aquí", systemImage: "figure.walk")
                }
                .buttonStyle(.bordered)

                Button {
                    let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: station.id)
                    routeStatus = requested
                        ? String(localized: "He enviado la ruta al iPhone.")
                        : String(localized: "No he podido contactar con el iPhone.")
                } label: {
                    Label("Ruta en iPhone", systemImage: "iphone")
                }
                .buttonStyle(.borderedProminent)

                if let localRouteStatus {
                    StatusMessage(text: localRouteStatus)
                }
                if let routeStatus {
                    StatusMessage(text: routeStatus)
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .animation(watchSpring, value: localRouteStatus)
            .animation(watchSpring, value: routeStatus)
        }
        .navigationTitle("Detalle")
        .containerBackground(.biziWatchBackground, for: .navigation)
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
            HStack(spacing: 4) {
                WatchMetricPill(
                    systemImage: "bicycle",
                    value: "\(session.bikesAvailable)",
                    label: "Bicis",
                    tint: session.bikesAvailable > 0 ? .biziDarkPrimary : .biziError
                )
                WatchMetricPill(
                    systemImage: "parkingsign",
                    value: "\(session.docksAvailable)",
                    label: "Huecos",
                    tint: session.docksAvailable > 0 ? .biziDarkSecondary : .biziError
                )
                WatchMetricPill(
                    systemImage: "timer",
                    value: session.remainingMinutesText,
                    label: "Tiempo restante",
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
                            ? String(localized: "He enviado la alternativa al iPhone.")
                            : String(localized: "No he podido contactar con el iPhone.")
                    } label: {
                        Label("Ruta alternativa", systemImage: "arrow.triangle.turn.up.right.circle")
                    }
                    .buttonStyle(.bordered)
                }
            }
            Button {
                let requested = WatchFavoritesSyncBridge.shared.requestRoute(to: session.stationId)
                routeStatus = requested
                    ? String(localized: "He enviado la ruta al iPhone.")
                    : String(localized: "No he podido contactar con el iPhone.")
            } label: {
                Label("Ruta en iPhone", systemImage: "iphone")
            }
            .buttonStyle(.borderedProminent)

            if let routeStatus {
                StatusMessage(text: routeStatus)
            }
        }
        .padding(.vertical, 2)
        .animation(watchSpring, value: session.statusText)
        .animation(watchSpring, value: routeStatus)
    }
}

private func alternativeText(_ session: WatchConnectivityMonitoringSession) -> String {
    guard let alternativeStationName = session.alternativeStationName, !alternativeStationName.isEmpty else {
        return String(localized: "Alternativa no disponible")
    }
    if let alternativeDistanceMeters = session.alternativeDistanceMeters {
        return String(localized: "Alternativa: \(alternativeStationName) (\(watchDistanceText(alternativeDistanceMeters)))")
    }
    return String(localized: "Alternativa: \(alternativeStationName)")
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

/// Compact icon + value capsule. Icons replace the old "B" / "H" / "T" letters, which
/// meant nothing to VoiceOver or to people reading the watch in another language.
private struct WatchMetricPill: View {
    let systemImage: String
    let value: String
    let label: LocalizedStringKey
    let tint: Color

    var body: some View {
        HStack(spacing: 3) {
            Image(systemName: systemImage)
                .font(.caption2)
            Text(value)
                .font(.caption.weight(.semibold))
                .monospacedDigit()
                .contentTransition(.numericText())
        }
        // Brand tints on their own 18% fill are ~4:1; lighten the glyphs, not the fill.
        .foregroundStyle(tint.mix(with: .white, by: 0.25))
        .padding(.horizontal, 7)
        .padding(.vertical, 4)
        .background(tint.opacity(0.18), in: Capsule())
        .animation(watchSpring, value: value)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityValue(Text(value))
    }
}

/// Large availability figure used on the station detail screen.
private struct WatchAvailabilityTile: View {
    let systemImage: String
    let value: Int
    let label: LocalizedStringKey
    let tint: Color

    var body: some View {
        VStack(spacing: 1) {
            Image(systemName: systemImage)
                .font(.caption2)
            Text("\(value)")
                .font(.system(.title2, design: .rounded).weight(.bold))
                .monospacedDigit()
                .contentTransition(.numericText(value: Double(value)))
            Text(label)
                .font(.caption2)
        }
        .foregroundStyle(tint.mix(with: .white, by: 0.25))
        .frame(maxWidth: .infinity)
        .padding(.vertical, 8)
        .background(tint.opacity(0.18), in: RoundedRectangle(cornerRadius: 16, style: .continuous))
        .animation(watchSpring, value: value)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityValue(Text("\(value)"))
    }
}

private struct StatusMessage: View {
    let text: String

    var body: some View {
        // `text` is already localized (String(localized:)); keep it verbatim.
        Text(verbatim: text)
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
