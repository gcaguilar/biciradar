@preconcurrency import ConnectIQ
import Foundation
import UIKit

@MainActor
final class GarminConnectManager: NSObject {
    static let shared = GarminConnectManager()

    private let connectIQ: ConnectIQ = ConnectIQ.sharedInstance()!
    private let appUUID = UUID(uuidString: "9b02e1cf-d60a-42d8-ad70-8c8ef1c4bdfa")!
    private let storeUUID = UUID(uuidString: "9b02e1cf-d60a-42d8-ad70-8c8ef1c4bdfa")!
    private let defaults = UserDefaults.standard
    private let devicesKey = "garmin.authorized.devices.v1"
    private var devicesById: [UUID: IQDevice] = [:]
    private var appsByDeviceId: [UUID: IQApp] = [:]
    private var readyDeviceIds: Set<UUID> = []
    private var latestPayload: [String: Any]?

    func start() {
        connectIQ.initialize(withUrlScheme: "biciradar", uiOverrideDelegate: self, stateRestorationIdentifier: "biciradar-garmin")
        restoreDevices()
        Task {
            await publishLatestStations()
        }
    }

    func beginPairing() {
        connectIQ.showDeviceSelection()
    }

    @discardableResult
    func handleOpenURL(_ url: URL) -> Bool {
        let devices = connectIQ.parseDeviceSelectionResponse(from: url) as? [IQDevice] ?? []
        guard !devices.isEmpty else { return false }
        replaceAuthorizedDevices(devices)
        Task {
            await publishLatestStations()
        }
        return true
    }

    func sceneDidBecomeActive() {
        Task {
            await publishLatestStations()
        }
    }

    private func restoreDevices() {
        guard let data = defaults.data(forKey: devicesKey) else { return }
        let decoded = (try? NSKeyedUnarchiver.unarchivedArrayOfObjects(ofClass: IQDevice.self, from: data)) ?? []
        replaceAuthorizedDevices(decoded, persist: false)
    }

    private func replaceAuthorizedDevices(_ devices: [IQDevice], persist: Bool = true) {
        connectIQ.unregister(forAllDeviceEvents: self)
        connectIQ.unregister(forAllAppMessages: self)
        devicesById.removeAll()
        appsByDeviceId.removeAll()
        readyDeviceIds.removeAll()

        for device in devices {
            devicesById[device.uuid] = device
            connectIQ.register(forDeviceEvents: device, delegate: self)
        }

        if persist, let data = try? NSKeyedArchiver.archivedData(withRootObject: devices, requiringSecureCoding: true) {
            defaults.set(data, forKey: devicesKey)
        }
    }

    private func app(for device: IQDevice) -> IQApp? {
        if let existing = appsByDeviceId[device.uuid] { return existing }
        guard let app = IQApp(uuid: appUUID, store: storeUUID, device: device) else {
            NSLog("[GarminConnect][ERROR] Failed to create IQApp for device: \(device.uuid.uuidString)")
            return nil
        }
        appsByDeviceId[device.uuid] = app
        return app
    }

    private func publishLatestStations(forceRefresh: Bool = false) async {
        do {
            if forceRefresh {
                try await BiziAppleGraph.shared.refreshData(forceRefresh: true)
            }
            let stations = try await BiziAppleGraph.shared.nearbyStations(limit: 5)
            latestPayload = GarminPayloadBuilder.build(stations: stations)
            sendLatestPayloadToReadyDevices()
        } catch {
            NSLog("[GarminConnect][ERROR] Failed to publish stations: \(error.localizedDescription)")
        }
    }

    private func sendLatestPayloadToReadyDevices() {
        guard let payload = latestPayload else { return }
        for deviceId in readyDeviceIds {
            guard let app = appsByDeviceId[deviceId] else { continue }
            connectIQ.sendMessage(
                payload as NSDictionary,
                to: app,
                progress: { _, _ in },
                completion: { result in
                    if result != .success {
                        NSLog("[GarminConnect][WARN] sendMessage failed: \(result.rawValue)")
                    }
                }
            )
        }
    }
}

extension GarminConnectManager: IQDeviceEventDelegate {
    nonisolated func deviceStatusChanged(_ device: IQDevice, status: IQDeviceStatus) {
        Task { @MainActor [weak self] in
            guard let self else { return }
            if status == .connected {
                guard let app = self.app(for: device) else { return }
                guard let appStatus = await self.connectIQ.appStatus(app), appStatus.isInstalled else { return }
                self.connectIQ.register(forAppMessages: app, delegate: self)
                if self.readyDeviceIds.contains(device.uuid) {
                    self.sendLatestPayloadToReadyDevices()
                }
            } else {
                self.readyDeviceIds.remove(device.uuid)
            }
        }
    }

    nonisolated func deviceCharacteristicsDiscovered(_ device: IQDevice) {
        Task { @MainActor [weak self] in
            guard let self else { return }
            self.readyDeviceIds.insert(device.uuid)
            _ = self.app(for: device)
            self.sendLatestPayloadToReadyDevices()
        }
    }
}

extension GarminConnectManager: IQAppMessageDelegate {
    nonisolated func receivedMessage(_ message: Any!, from app: IQApp!) {
        guard let payload = message as? [String: Any], let type = payload["type"] as? String else {
            return
        }
        Task { @MainActor [weak self] in
            guard let self else { return }
            if type == "refresh_request" {
                await self.publishLatestStations(forceRefresh: true)
                return
            }

            if type == "open_route",
               let stationId = payload["stationId"] as? String,
               !stationId.isEmpty {
                AppleLaunchRequestStore.shared.save(MobileLaunchRequestRouteToStation(stationId: stationId))
                if let url = URL(string: "biciradar://station/\(stationId)?action=route_to_station") {
                    UIApplication.shared.open(url, options: [:], completionHandler: nil)
                }
            }
        }
    }
}

extension GarminConnectManager: IQUIOverrideDelegate {
    nonisolated func needsToInstallConnectMobile() {
        Task { @MainActor [weak self] in
            guard let self else { return }
            let alert = UIAlertController(
                title: "Garmin Connect requerido",
                message: "Instala Garmin Connect para autorizar tus dispositivos Connect IQ.",
                preferredStyle: .alert
            )
            alert.addAction(UIAlertAction(title: "Cancelar", style: .cancel))
            alert.addAction(UIAlertAction(title: "Abrir App Store", style: .default) { _ in
                self.connectIQ.showAppStoreForConnectMobile()
            })
            UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap(\.windows)
                .first(where: \.isKeyWindow)?
                .rootViewController?
                .present(alert, animated: true)
        }
    }
}

private enum GarminPayloadBuilder {
    static func build(stations: [BiziStationSnapshot]) -> [String: Any] {
        [
            "nearest": stations.first.map(stationPayload) ?? NSNull(),
            "backup": Array(stations.dropFirst().prefix(4).map(stationPayload)),
            "timestamp": Int(Date().timeIntervalSince1970),
        ]
    }

    private static func stationPayload(_ station: BiziStationSnapshot) -> [String: Any] {
        [
            "id": station.id,
            "name": truncated(station.name),
            "bikes": station.bikesAvailable,
            "distance": station.distanceMeters,
            "ebikes": 0,
        ]
    }

    private static func truncated(_ value: String) -> String {
        guard value.count > 20 else { return value }
        return String(value.prefix(17)) + "..."
    }
}
