import Core
import MobileUi
import Foundation

/// Builds the shared `AppConfiguration` from Info.plist build settings (populated
/// from `Config/LocalSecrets.xcconfig`) with a Debug-only environment override
/// that makes local smoke testing easy from the Xcode scheme.
enum AppleAppConfiguration {
    static func make() -> AppConfiguration {
        AppConfiguration.Companion.shared.configured(
            backendBaseUrl: value("BiziBackendBaseUrl", env: "BICI_BACKEND_URL"),
            auth0Domain: value("BiziAuth0Domain", env: "BICI_AUTH0_DOMAIN"),
            auth0ClientId: value("BiziAuth0ClientId", env: "BICI_AUTH0_CLIENT_ID"),
            auth0Audience: value("BiziAuth0Audience", env: "BICI_AUTH0_AUDIENCE"),
            auth0Scheme: value("BiziAuth0Scheme", env: "BICI_AUTH0_SCHEME")
        )
    }

    private static func value(_ infoKey: String, env: String) -> String {
        #if DEBUG
        if let override = ProcessInfo.processInfo.environment[env]?
            .trimmingCharacters(in: .whitespacesAndNewlines), !override.isEmpty {
            return override
        }
        #endif
        let raw = Bundle.main.object(forInfoDictionaryKey: infoKey) as? String ?? ""
        return raw.trimmingCharacters(in: .whitespacesAndNewlines)
    }
}
