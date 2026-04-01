import AppIntents
import SwiftUI
import WidgetKit

#if canImport(ActivityKit)
import ActivityKit
#endif

private struct SurfaceEntry: TimelineEntry {
    let date: Date
    let bundle: AppleSurfaceSnapshotBundle?

    var favoriteStation: AppleSurfaceStationSnapshot? { bundle?.favoriteStation }
    var homeStation: AppleSurfaceStationSnapshot? { bundle?.homeStation }
    var workStation: AppleSurfaceStationSnapshot? { bundle?.workStation }
    var nearbyStations: [AppleSurfaceStationSnapshot] { bundle?.nearbyStations ?? [] }
    var state: AppleSurfaceState? { bundle?.state }
}

private struct SurfaceTimelineProvider: TimelineProvider {
    func placeholder(in context: Context) -> SurfaceEntry {
        SurfaceEntry(date: .now, bundle: sampleBundle)
    }

    func getSnapshot(in context: Context, completion: @escaping (SurfaceEntry) -> Void) {
        completion(SurfaceEntry(date: .now, bundle: BiziSurfaceStore.readSnapshotBundle() ?? sampleBundle))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<SurfaceEntry>) -> Void) {
        let entry = SurfaceEntry(date: .now, bundle: BiziSurfaceStore.readSnapshotBundle())
        let refreshDate = Calendar.current.date(byAdding: .minute, value: 15, to: .now) ?? .now.addingTimeInterval(900)
        completion(Timeline(entries: [entry], policy: .after(refreshDate)))
    }
}

@main
struct BiciRadarWidgets: WidgetBundle {
    var body: some Widget {
        FavoriteStationWidget()
        NearbyStationsWidget()
        CommuteStationsWidget()
        if #available(iOS 17.0, *) {
            ConfigurableStationWidget()
        }
        BikesLockScreenWidget()
        DocksLockScreenWidget()
        StationStatusLockScreenWidget()
        if #available(iOSApplicationExtension 16.1, *) {
            BiziMonitoringLiveActivityWidget()
        }
    }
}

struct FavoriteStationWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarFavoriteStation", provider: SurfaceTimelineProvider()) { entry in
            FavoriteStationWidgetView(entry: entry)
        }
        .configurationDisplayName("Estación favorita")
        .description("Consulta bicis, huecos y la última actualización sin abrir la app.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

struct NearbyStationsWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarNearbyStations", provider: SurfaceTimelineProvider()) { entry in
            NearbyStationsWidgetView(entry: entry)
        }
        .configurationDisplayName("Estaciones cercanas")
        .description("Muestra las estaciones más próximas desde el último snapshot disponible.")
        .supportedFamilies([.systemMedium, .systemLarge])
    }
}

struct CommuteStationsWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarCommuteStations", provider: SurfaceTimelineProvider()) { entry in
            CommuteStationsWidgetView(entry: entry)
        }
        .configurationDisplayName("Casa y trabajo")
        .description("Acceso rápido a tus estaciones guardadas para casa y trabajo.")
        .supportedFamilies([.systemMedium])
    }
}

struct BikesLockScreenWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarLockBikes", provider: SurfaceTimelineProvider()) { entry in
            BikesAccessoryView(entry: entry)
        }
        .configurationDisplayName("Bicis favorita")
        .description("Bicis disponibles de tu estación favorita.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

struct DocksLockScreenWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarLockDocks", provider: SurfaceTimelineProvider()) { entry in
            DocksAccessoryView(entry: entry)
        }
        .configurationDisplayName("Huecos favorita")
        .description("Huecos libres de tu estación favorita.")
        .supportedFamilies([.accessoryInline, .accessoryCircular, .accessoryRectangular])
    }
}

struct StationStatusLockScreenWidget: Widget {
    var body: some WidgetConfiguration {
        StaticConfiguration(kind: "BiciRadarLockStatus", provider: SurfaceTimelineProvider()) { entry in
            StationStatusAccessoryView(entry: entry)
        }
        .configurationDisplayName("Estado favorita")
        .description("Estado compacto de tu estación favorita.")
        .supportedFamilies([.accessoryInline, .accessoryRectangular])
    }
}

private struct FavoriteStationWidgetView: View {
    let entry: SurfaceEntry

    var body: some View {
        let destination = entry.favoriteStation.flatMap { URL(string: "biciradar://station/\($0.id)") }
            ?? URL(string: "biciradar://favorites")
        return StationSummaryWidgetCard(
            station: entry.favoriteStation,
            label: nil,
            fallbackTitle: "BiciRadar",
            fallbackMessage: fallbackMessage(for: entry, kind: .favorite)
        )
        .widgetURL(destination)
    }
}

@available(iOS 17.0, *)
private struct ConfigurableStationEntry: TimelineEntry {
    let date: Date
    let bundle: AppleSurfaceSnapshotBundle?
    let slot: AppleSurfaceSnapshotSlot

    var station: AppleSurfaceStationSnapshot? { bundle?.station(for: slot) }
    var state: AppleSurfaceState? { bundle?.state }
}

@available(iOS 17.0, *)
private enum ConfigurableStationChoice: String, AppEnum {
    case favorite
    case home
    case work

    static var typeDisplayRepresentation = TypeDisplayRepresentation(name: "Estación")
    static var caseDisplayRepresentations: [ConfigurableStationChoice: DisplayRepresentation] = [
        .favorite: DisplayRepresentation(title: "Favorita"),
        .home: DisplayRepresentation(title: "Casa"),
        .work: DisplayRepresentation(title: "Trabajo")
    ]

    var slot: AppleSurfaceSnapshotSlot {
        AppleSurfaceSnapshotSlot(rawValue: rawValue) ?? .favorite
    }
}

@available(iOS 17.0, *)
private struct ConfigurableStationIntent: WidgetConfigurationIntent {
    static var title: LocalizedStringResource = "Estación rápida"
    static var description = IntentDescription("Elige si el widget debe mostrar tu favorita, casa o trabajo.")

    @Parameter(title: "Estación")
    var station: ConfigurableStationChoice?

    init() {
        station = .favorite
    }

    init(station: ConfigurableStationChoice) {
        self.station = station
    }

    static var parameterSummary: some ParameterSummary {
        Summary("Mostrar \(\.$station)")
    }
}

@available(iOS 17.0, *)
private struct ConfigurableStationTimelineProvider: AppIntentTimelineProvider {
    typealias Intent = ConfigurableStationIntent
    typealias Entry = ConfigurableStationEntry

    func recommendations() -> [AppIntentRecommendation<ConfigurableStationIntent>] {
        AppleSurfaceSnapshotSlot.allCases.map { slot in
            let description: String
            switch slot {
            case .favorite:
                description = "Favorita rápida"
            case .home:
                description = "Casa rápida"
            case .work:
                description = "Trabajo rápido"
            }
            return AppIntentRecommendation(
                intent: ConfigurableStationIntent(
                    station: ConfigurableStationChoice(rawValue: slot.rawValue) ?? .favorite
                ),
                description: description
            )
        }
    }

    func placeholder(in context: Context) -> ConfigurableStationEntry {
        ConfigurableStationEntry(date: .now, bundle: sampleBundle, slot: .favorite)
    }

    func snapshot(for configuration: ConfigurableStationIntent, in context: Context) async -> ConfigurableStationEntry {
        ConfigurableStationEntry(
            date: .now,
            bundle: BiziSurfaceStore.readSnapshotBundle() ?? sampleBundle,
            slot: configuration.station?.slot ?? .favorite
        )
    }

    func timeline(for configuration: ConfigurableStationIntent, in context: Context) async -> Timeline<ConfigurableStationEntry> {
        let entry = ConfigurableStationEntry(
            date: .now,
            bundle: BiziSurfaceStore.readSnapshotBundle(),
            slot: configuration.station?.slot ?? .favorite
        )
        let refreshDate = Calendar.current.date(byAdding: .minute, value: 15, to: .now) ?? .now.addingTimeInterval(900)
        return Timeline(entries: [entry], policy: .after(refreshDate))
    }
}

@available(iOS 17.0, *)
struct ConfigurableStationWidget: Widget {
    var body: some WidgetConfiguration {
        AppIntentConfiguration(
            kind: "BiciRadarConfigurableStation",
            intent: ConfigurableStationIntent.self,
            provider: ConfigurableStationTimelineProvider()
        ) { entry in
            ConfigurableStationWidgetView(entry: entry)
        }
        .configurationDisplayName("Estación rápida")
        .description("Elige si quieres ver la favorita, casa o trabajo.")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

@available(iOS 17.0, *)
private struct ConfigurableStationWidgetView: View {
    let entry: ConfigurableStationEntry

    var body: some View {
        let destination = entry.station.flatMap { URL(string: "biciradar://station/\($0.id)") }
            ?? URL(string: "biciradar://favorites")
        return StationSummaryWidgetCard(
            station: entry.station,
            label: entry.slot.widgetTitle,
            fallbackTitle: entry.slot.widgetTitle,
            fallbackMessage: entry.slot.widgetFallbackMessage(state: entry.state)
        )
        .widgetURL(destination)
    }
}

private struct NearbyStationsWidgetView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let visibleStations = family == .systemLarge ? entry.nearbyStations.prefix(5) : entry.nearbyStations.prefix(3)
        let content = ZStack {
            LinearGradient(colors: [Color(red: 0.95, green: 0.97, blue: 1.0), Color.white], startPoint: .topLeading, endPoint: .bottomTrailing)
            if visibleStations.isEmpty {
                fallbackView(title: "Cercanas", message: fallbackMessage(for: entry, kind: .nearby))
                    .padding(16)
            } else {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Cerca de ti")
                        .font(.headline)
                        .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                    ForEach(Array(visibleStations)) { station in
                        HStack(spacing: 8) {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(station.nameShort)
                                    .font(.caption.weight(.semibold))
                                    .lineLimit(1)
                                Text(station.distanceMeters.map(formatWidgetDistance) ?? station.statusTextShort)
                                    .font(.caption2)
                                    .foregroundStyle(.secondary)
                            }
                            Spacer()
                            Text("\(station.bikesAvailable) / \(station.docksAvailable)")
                                .font(.caption.weight(.bold))
                                .foregroundStyle(statusColor(station.statusLevel))
                        }
                    }
                    Spacer(minLength: 0)
                    if family == .systemLarge {
                        Text(BiziSurfaceStore.relativeUpdateText(lastUpdatedEpoch: entry.state?.lastSyncEpoch))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(16)
            }
        }
        return content.widgetURL(URL(string: "biciradar://home"))
    }
}

private func formatWidgetDistance(_ meters: Int) -> String {
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

private struct CommuteStationsWidgetView: View {
    let entry: SurfaceEntry

    var body: some View {
        let content = ZStack {
            LinearGradient(
                colors: [
                    Color(red: 0.95, green: 0.98, blue: 0.95),
                    Color(red: 0.97, green: 0.98, blue: 1.0)
                ],
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            if entry.homeStation == nil, entry.workStation == nil {
                fallbackView(title: "Casa y trabajo", message: commuteFallbackMessage(for: entry))
                    .padding(16)
            } else {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Casa y trabajo")
                        .font(.headline)
                        .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                    HStack(spacing: 10) {
                        savedPlaceColumn(
                            label: "Casa",
                            station: entry.homeStation,
                            accent: Color(red: 0.21, green: 0.58, blue: 0.27)
                        )
                        savedPlaceColumn(
                            label: "Trabajo",
                            station: entry.workStation,
                            accent: Color(red: 0.11, green: 0.45, blue: 0.74)
                        )
                    }
                }
                .padding(16)
            }
        }
        return content.widgetURL(URL(string: "biciradar://favorites"))
    }

    @ViewBuilder
    private func savedPlaceColumn(
        label: String,
        station: AppleSurfaceStationSnapshot?,
        accent: Color
    ) -> some View {
        let content = VStack(alignment: .leading, spacing: 6) {
            Text(label.uppercased())
                .font(.caption2.weight(.bold))
                .foregroundStyle(accent)
            if let station {
                Text(station.nameShort)
                    .font(.callout.weight(.semibold))
                    .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                    .lineLimit(2)
                Text("\(station.bikesAvailable) bicis · \(station.docksAvailable) huecos")
                    .font(.caption2.weight(.semibold))
                    .foregroundStyle(statusColor(station.statusLevel))
                Text(station.statusTextShort)
                    .font(.caption2)
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            } else {
                Text("Sin configurar")
                    .font(.callout.weight(.semibold))
                    .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                Text("Elige la estación")
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
        .padding(12)
        .background(Color.white.opacity(0.85), in: RoundedRectangle(cornerRadius: 18, style: .continuous))

        if let station, let url = URL(string: "biciradar://station/\(station.id)") {
            Link(destination: url) {
                content
            }
            .buttonStyle(.plain)
        } else if let url = URL(string: "biciradar://favorites") {
            Link(destination: url) {
                content
            }
            .buttonStyle(.plain)
        }
    }
}

private struct BikesAccessoryView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let destination = entry.favoriteStation.flatMap { URL(string: "biciradar://station/\($0.id)") }
            ?? URL(string: "biciradar://favorites")
        let bikes = entry.favoriteStation?.bikesAvailable
        let content: AnyView
        switch family {
        case .accessoryInline:
            content = AnyView(Text(bikes.map { "Bicis \($0)" } ?? "Sin favorita"))
        case .accessoryCircular:
            content = AnyView(
                ZStack {
                    AccessoryWidgetBackground()
                    Text(bikes.map(String.init) ?? "--")
                        .font(.system(.title3, design: .rounded).bold())
                }
            )
        default:
            content = AnyView(
                VStack(alignment: .leading) {
                    Text("Bicis")
                    Text(bikes.map(String.init) ?? "--")
                        .font(.title2.bold())
                }
            )
        }
        return content.widgetURL(destination)
    }
}

private struct DocksAccessoryView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let destination = entry.favoriteStation.flatMap { URL(string: "biciradar://station/\($0.id)") }
            ?? URL(string: "biciradar://favorites")
        let docks = entry.favoriteStation?.docksAvailable
        let content: AnyView
        switch family {
        case .accessoryInline:
            content = AnyView(Text(docks.map { "Huecos \($0)" } ?? "Sin favorita"))
        case .accessoryCircular:
            content = AnyView(
                ZStack {
                    AccessoryWidgetBackground()
                    Text(docks.map(String.init) ?? "--")
                        .font(.system(.title3, design: .rounded).bold())
                }
            )
        default:
            content = AnyView(
                VStack(alignment: .leading) {
                    Text("Huecos")
                    Text(docks.map(String.init) ?? "--")
                        .font(.title2.bold())
                }
            )
        }
        return content.widgetURL(destination)
    }
}

private struct StationStatusAccessoryView: View {
    let entry: SurfaceEntry
    @Environment(\.widgetFamily) private var family

    var body: some View {
        let destination = entry.favoriteStation.flatMap { URL(string: "biciradar://station/\($0.id)") }
            ?? URL(string: "biciradar://favorites")
        let status = entry.favoriteStation?.statusTextShort ?? "Sin favorita"
        let content: AnyView
        switch family {
        case .accessoryInline:
            content = AnyView(Text(status))
        default:
            content = AnyView(
                VStack(alignment: .leading) {
                    Text(entry.favoriteStation?.nameShort ?? "BiciRadar")
                        .lineLimit(1)
                    Text(status)
                        .foregroundStyle(entry.favoriteStation.map { statusColor($0.statusLevel) } ?? .secondary)
                }
            )
        }
        return content.widgetURL(destination)
    }
}

#if canImport(ActivityKit)
@available(iOSApplicationExtension 16.1, *)
struct BiziMonitoringLiveActivityWidget: Widget {
    var body: some WidgetConfiguration {
        ActivityConfiguration(for: BiziMonitoringActivityAttributes.self) { context in
            VStack(alignment: .leading, spacing: 8) {
                Text(context.attributes.stationName)
                    .font(.headline)
                HStack(spacing: 12) {
                    Label("\(context.state.bikesAvailable)", systemImage: "bicycle")
                    Label("\(context.state.docksAvailable)", systemImage: "parkingsign")
                }
                Text(context.state.statusText)
                    .font(.caption)
                    .foregroundStyle(.secondary)
                if let alternativeSummary = context.state.alternativeSummaryText {
                    if let alternativeURL = context.state.alternativeStationURL {
                        Link(destination: alternativeURL) {
                            Text(alternativeSummary)
                                .font(.caption2)
                        }
                    } else {
                        Text(alternativeSummary)
                            .font(.caption2)
                    }
                }
                Text(Date(timeIntervalSince1970: TimeInterval(context.state.expiresAtEpoch) / 1000), style: .timer)
                    .font(.caption2.monospacedDigit())
            }
            .padding(16)
            .activityBackgroundTint(Color(red: 0.95, green: 0.97, blue: 1.0))
            .activitySystemActionForegroundColor(Color(red: 0.11, green: 0.45, blue: 0.74))
            .widgetURL(URL(string: "biciradar://monitor/\(context.attributes.stationId)"))
        } dynamicIsland: { context in
            DynamicIsland {
                DynamicIslandExpandedRegion(.leading) {
                    Text("\(context.state.bikesAvailable) bicis")
                }
                DynamicIslandExpandedRegion(.trailing) {
                    Text("\(context.state.docksAvailable) huecos")
                }
                DynamicIslandExpandedRegion(.bottom) {
                    VStack(alignment: .leading) {
                        Text(context.attributes.stationName)
                            .font(.headline)
                        Text(context.state.statusText)
                            .font(.caption)
                        if let alternativeSummary = context.state.alternativeSummaryText {
                            if let alternativeURL = context.state.alternativeStationURL {
                                Link(destination: alternativeURL) {
                                    Text(alternativeSummary)
                                        .font(.caption2)
                                }
                            } else {
                                Text(alternativeSummary)
                                    .font(.caption2)
                            }
                        }
                    }
                }
            } compactLeading: {
                Label("\(context.state.bikesAvailable)", systemImage: "bicycle")
            } compactTrailing: {
                Label("\(context.state.docksAvailable)", systemImage: "parkingsign")
            } minimal: {
                Text(String(context.state.statusText.prefix(1)))
            }
            .widgetURL(URL(string: "biciradar://monitor/\(context.attributes.stationId)"))
        }
    }
}
#endif

private enum SurfaceFallbackKind {
    case favorite
    case nearby
}

private struct StationSummaryWidgetCard: View {
    let station: AppleSurfaceStationSnapshot?
    let label: String?
    let fallbackTitle: String
    let fallbackMessage: String

    @Environment(\.widgetFamily) private var family

    var body: some View {
        ZStack {
            LinearGradient(colors: [Color(red: 0.98, green: 0.96, blue: 0.94), Color.white], startPoint: .topLeading, endPoint: .bottomTrailing)
            if let station {
                VStack(alignment: .leading, spacing: 10) {
                    if let label {
                        Text(label.uppercased())
                            .font(.caption2.weight(.bold))
                            .foregroundStyle(.secondary)
                    }
                    Text(station.nameShort)
                        .font(family == .systemSmall ? .headline : .title3.weight(.semibold))
                        .foregroundStyle(Color(red: 0.05, green: 0.11, blue: 0.16))
                        .lineLimit(2)
                    HStack(spacing: 12) {
                        metricBlock(label: "Bicis", value: station.bikesAvailable, tint: Color(red: 0.11, green: 0.45, blue: 0.74))
                        metricBlock(label: "Huecos", value: station.docksAvailable, tint: Color(red: 0.21, green: 0.58, blue: 0.27))
                    }
                    Spacer(minLength: 0)
                    HStack {
                        Text(station.statusTextShort)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(statusColor(station.statusLevel))
                        Spacer()
                        Text(BiziSurfaceStore.relativeUpdateText(lastUpdatedEpoch: station.lastUpdatedEpoch))
                            .font(.caption2)
                            .foregroundStyle(.secondary)
                    }
                }
                .padding(16)
            } else {
                fallbackView(title: fallbackTitle, message: fallbackMessage)
                    .padding(16)
            }
        }
    }

    private func metricBlock(label: String, value: Int, tint: Color) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label)
                .font(.caption2)
                .foregroundStyle(.secondary)
            Text("\(value)")
                .font(.system(size: 28, weight: .bold, design: .rounded))
                .foregroundStyle(tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private func fallbackMessage(for entry: SurfaceEntry, kind: SurfaceFallbackKind) -> String {
    guard let state = entry.state else { return "Abre la app para actualizar" }
    if kind == .nearby, !state.hasLocationPermission {
        return "Sin permiso de ubicación"
    }
    if kind == .favorite, !state.hasFavoriteStation {
        return "Configura una estación favorita"
    }
    if !state.isDataFresh {
        return "Abre la app para actualizar"
    }
    return "Datos no disponibles"
}

private func commuteFallbackMessage(for entry: SurfaceEntry) -> String {
    guard let state = entry.state else { return "Abre la app para actualizar" }
    if !state.isDataFresh {
        return "Abre la app para actualizar"
    }
    return "Elige tus estaciones de casa y trabajo"
}

private func fallbackView(title: String, message: String) -> some View {
    VStack(alignment: .leading, spacing: 8) {
        Text(title)
            .font(.headline)
        Text(message)
            .font(.caption)
            .foregroundStyle(.secondary)
    }
    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
}

#if canImport(ActivityKit)
@available(iOSApplicationExtension 16.1, *)
#endif

private func statusColor(_ level: AppleSurfaceStatusLevel) -> Color {
    switch level {
    case .good:
        return Color(red: 0.21, green: 0.58, blue: 0.27)
    case .low:
        return Color.orange
    case .empty, .full:
        return Color.red
    case .unavailable:
        return Color.secondary
    }
}

private let sampleBundle = AppleSurfaceSnapshotBundle(
    generatedAtEpoch: 0,
    favoriteStation: AppleSurfaceStationSnapshot(
        id: "42",
        nameShort: "Plaza Espana",
        nameFull: "Plaza Espana",
        cityId: "zaragoza",
        latitude: 0,
        longitude: 0,
        bikesAvailable: 8,
        docksAvailable: 5,
        statusTextShort: "Disponible",
        statusLevel: .good,
        lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
        distanceMeters: 120,
        isFavorite: true,
        alternativeStationId: nil,
        alternativeStationName: nil,
        alternativeDistanceMeters: nil
    ),
    homeStation: AppleSurfaceStationSnapshot(
        id: "42",
        nameShort: "Plaza Espana",
        nameFull: "Plaza Espana",
        cityId: "zaragoza",
        latitude: 0,
        longitude: 0,
        bikesAvailable: 8,
        docksAvailable: 5,
        statusTextShort: "Disponible",
        statusLevel: .good,
        lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
        distanceMeters: 120,
        isFavorite: true,
        alternativeStationId: nil,
        alternativeStationName: nil,
        alternativeDistanceMeters: nil
    ),
    workStation: AppleSurfaceStationSnapshot(
        id: "13",
        nameShort: "Paraninfo",
        nameFull: "Paraninfo",
        cityId: "zaragoza",
        latitude: 0,
        longitude: 0,
        bikesAvailable: 3,
        docksAvailable: 7,
        statusTextShort: "Pocas",
        statusLevel: .low,
        lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
        distanceMeters: 260,
        isFavorite: false,
        alternativeStationId: nil,
        alternativeStationName: nil,
        alternativeDistanceMeters: nil
    ),
    nearbyStations: [
        AppleSurfaceStationSnapshot(
            id: "42",
            nameShort: "Plaza Espana",
            nameFull: "Plaza Espana",
            cityId: "zaragoza",
            latitude: 0,
            longitude: 0,
            bikesAvailable: 8,
            docksAvailable: 5,
            statusTextShort: "Disponible",
            statusLevel: .good,
            lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
            distanceMeters: 120,
            isFavorite: true,
            alternativeStationId: nil,
            alternativeStationName: nil,
            alternativeDistanceMeters: nil
        ),
        AppleSurfaceStationSnapshot(
            id: "13",
            nameShort: "Paraninfo",
            nameFull: "Paraninfo",
            cityId: "zaragoza",
            latitude: 0,
            longitude: 0,
            bikesAvailable: 3,
            docksAvailable: 7,
            statusTextShort: "Pocas",
            statusLevel: .low,
            lastUpdatedEpoch: Int64(Date().timeIntervalSince1970 * 1000),
            distanceMeters: 260,
            isFavorite: false,
            alternativeStationId: nil,
            alternativeStationName: nil,
            alternativeDistanceMeters: nil
        )
    ],
    monitoringSession: nil,
    state: AppleSurfaceState(
        hasLocationPermission: true,
        hasNotificationPermission: true,
        hasFavoriteStation: true,
        isDataFresh: true,
        lastSyncEpoch: Int64(Date().timeIntervalSince1970 * 1000),
        cityId: "zaragoza",
        cityName: "Zaragoza",
        userLatitude: nil,
        userLongitude: nil
    )
)
