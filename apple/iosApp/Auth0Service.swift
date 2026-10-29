import Auth0
import Core
import KotlinRuntime
import MobileUi
import Foundation

/// Auth0.swift-backed implementation of the Kotlin `Auth0Gateway`.
///
/// The shared `Auth0AuthProvider` owns the observable state; this type only runs
/// the OIDC ceremony and owns the Keychain-backed `CredentialsManager`.
final class Auth0Service: KotlinBase, platform.Auth0Gateway {
    private let clientId: String
    private let domain: String
    private let audience: String
    private let redirectURL: URL
    private let credentialsManager: CredentialsManager

    init(configuration: AppConfiguration) {
        self.clientId = configuration.auth0ClientId
        self.domain = configuration.auth0Domain
        self.audience = configuration.auth0Audience
        let bundleId = Bundle.main.bundleIdentifier ?? "com.gcaguilar.biciradar.ios"
        let scheme = configuration.auth0Scheme.isEmpty ? "biciradar" : configuration.auth0Scheme
        self.redirectURL = URL(string: "\(scheme)://\(domain)/ios/\(bundleId)/callback")!
        self.credentialsManager = CredentialsManager(
            authentication: Auth0.authentication(clientId: clientId, domain: domain)
        )
        super.init()
    }

    func restoreToken() async throws -> String? {
        guard credentialsManager.canRenew() || credentialsManager.hasValid(minTTL: 60) else { return nil }
        return try? await credentialsManager.credentials().accessToken
    }

    func login() async throws -> String? {
        do {
            let credentials = try await Auth0
                .webAuth(clientId: clientId, domain: domain)
                .audience(audience)
                .scope("openid profile email offline_access")
                .redirectURL(redirectURL)
                .start()
            try credentialsManager.store(credentials: credentials)
            return credentials.accessToken
        } catch {
            return nil
        }
    }

    func logout() async throws {
        _ = try? await Auth0
            .webAuth(clientId: clientId, domain: domain)
            .redirectURL(redirectURL)
            .logout()
        try? credentialsManager.clear()
    }

    func accessToken() async throws -> String? {
        guard credentialsManager.hasValid(minTTL: 60) else {
            try? credentialsManager.clear()
            return nil
        }
        return try? await credentialsManager.credentials().accessToken
    }
}
