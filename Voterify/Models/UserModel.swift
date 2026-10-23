import Foundation

/// OAuth authentication providers supported for Voterify login.
enum OAuthProvider: String, Codable, CaseIterable, Identifiable {
    case apple = "apple"
    case google = "google"
    case facebook = "facebook"
    case twitter = "twitter"

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .apple: return "Apple"
        case .google: return "Google"
        case .facebook: return "Facebook"
        case .twitter: return "Twitter / X"
        }
    }

    var systemIconName: String {
        switch self {
        case .apple: return "apple.logo"
        case .google: return "globe"
        case .facebook: return "person.2.circle.fill"
        case .twitter: return "at.circle.fill"
        }
    }
}

/// Login user model based on OAuth providers (Apple, Google, Facebook, Twitter), ID, and Name.
struct UserModel: Identifiable, Codable, Hashable {
    var id: String
    var name: String
    var email: String?
    var oauthProvider: OAuthProvider
    var oauthSubjectId: String?
    var createdAt: Date

    init(
        id: String = UUID().uuidString,
        name: String,
        email: String? = nil,
        oauthProvider: OAuthProvider,
        oauthSubjectId: String? = nil,
        createdAt: Date = Date()
    ) {
        self.id = id
        self.name = name
        self.email = email
        self.oauthProvider = oauthProvider
        self.oauthSubjectId = oauthSubjectId
        self.createdAt = createdAt
    }
}
