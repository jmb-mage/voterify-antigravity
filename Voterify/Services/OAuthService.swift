import AuthenticationServices
import Foundation

/// Authentication result pairing the OAuth-authenticated `UserModel` with its anonymous `VoterModel`.
struct AuthenticatedSession {
    let user: UserModel
    let voter: VoterModel
    let oauthClientIdUsed: String
}

/// Handles OAuth login across Apple, Google, Facebook, and Twitter/X providers
/// using the client IDs configured in the Xcode `.xcconfig` files.
final class OAuthService {
    private let config: FirestoreConfig

    init(config: FirestoreConfig = .current) {
        self.config = config
    }

    /// Returns the `.xcconfig`-defined OAuth Client/Service ID for the requested provider.
    func clientId(for provider: OAuthProvider) -> String {
        switch provider {
        case .apple:
            return config.oauth.appleServiceId
        case .google:
            return config.oauth.googleClientId
        case .facebook:
            return config.oauth.facebookAppId
        case .twitter:
            return config.oauth.twitterClientId
        }
    }

    /// Authenticates with the selected OAuth provider and provisions or resolves the anonymous `VoterModel`.
    func signIn(
        provider: OAuthProvider,
        customDisplayName: String?,
        existingUsers: [UserModel],
        existingVoters: [VoterModel],
        asAdminPreset: Bool
    ) -> AuthenticatedSession {
        let user: UserModel
        if asAdminPreset, let adminUser = existingUsers.first(where: { $0.id == "user-admin-001" }) {
            user = UserModel(
                id: adminUser.id,
                name: customDisplayName?.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty == false
                    ? customDisplayName!
                    : adminUser.name,
                email: adminUser.email,
                oauthProvider: provider,
                oauthSubjectId: "\(provider.rawValue)-admin-001"
            )
        } else {
            let trimmedName = customDisplayName?.trimmingCharacters(in: .whitespacesAndNewlines) ?? ""
            let resolvedName = trimmedName.isEmpty ? defaultUserName(for: provider) : trimmedName
            let deterministicId = "user-\(provider.rawValue)-\(abs(resolvedName.lowercased().hashValue % 100_000))"

            if let match = existingUsers.first(where: { $0.id == deterministicId }) {
                user = match
            } else {
                user = UserModel(
                    id: deterministicId,
                    name: resolvedName,
                    email: "\(resolvedName.lowercased().replacingOccurrences(of: " ", with: "."))@\(provider.rawValue).oauth.org",
                    oauthProvider: provider,
                    oauthSubjectId: "\(provider.rawValue)-sub-\(UUID().uuidString.prefix(8))"
                )
            }
        }

        // Resolve or generate the anonymous VoterModel with a public unique GUID name and privateUserId link
        let voter: VoterModel
        if let existingVoter = existingVoters.first(where: { $0.privateUserId == user.id }) {
            voter = existingVoter
        } else {
            let newGuid = UUID().uuidString.uppercased()
            voter = VoterModel(
                id: newGuid,
                publicUniqueName: "VOTER-\(newGuid)",
                privateUserId: user.id
            )
        }

        return AuthenticatedSession(
            user: user,
            voter: voter,
            oauthClientIdUsed: clientId(for: provider)
        )
    }

    private func defaultUserName(for provider: OAuthProvider) -> String {
        switch provider {
        case .apple: return "Jordan Taylor (Apple ID)"
        case .google: return "Taylor Morgan (Google)"
        case .facebook: return "Casey Rivera (Facebook)"
        case .twitter: return "Riley Quinn (@rquinn)"
        }
    }
}
