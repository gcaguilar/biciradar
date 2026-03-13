import Foundation

enum GoogleMapsBootstrap {
    private static let apiKeyInfoPlistKey = "BiziGoogleMapsApiKey"

    static func configureIfAvailable() {
        let apiKey = currentApiKey()
        guard !apiKey.isEmpty else { return }
        guard let servicesClass = NSClassFromString("GMSServices") as AnyObject? else { return }

        let selector = NSSelectorFromString("provideAPIKey:")
        guard servicesClass.responds(to: selector) else { return }
        _ = servicesClass.perform(selector, with: apiKey)
    }

    static func isSdkLinked() -> Bool {
        NSClassFromString("GMSServices") != nil && NSClassFromString("GMSMapView") != nil
    }

    static func currentApiKey(bundle: Bundle = .main) -> String {
        guard let rawValue = bundle.object(forInfoDictionaryKey: apiKeyInfoPlistKey) as? String else {
            return ""
        }
        let trimmedValue = rawValue.trimmingCharacters(in: .whitespacesAndNewlines)
        if trimmedValue.isEmpty || trimmedValue.hasPrefix("$(") {
            return ""
        }
        return trimmedValue
    }
}
