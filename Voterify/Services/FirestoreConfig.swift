import Foundation

/// Reads Google Firestore and OAuth connection settings injected from Xcode `.xcconfig` files (`Config/Base.xcconfig`, `Debug.xcconfig`, `Release.xcconfig`) via `Info.plist`.
struct FirestoreConfig {
    let environment: String
    let projectId: String
    let databaseId: String
    let host: String
    let useSSL: Bool
    let useEmulator: Bool
    let apiKey: String
    let apiVersion: String
    let defaultRegion: String

    struct Collections {
        let users: String
        let voters: String
        let elections: String
        let candidates: String
        let regions: String
        let parties: String
        let permissions: String
        let votes: String
        let blockchain: String
    }

    struct OAuthSettings {
        let redirectScheme: String
        let appleServiceId: String
        let googleClientId: String
        let facebookAppId: String
        let twitterClientId: String
    }

    let collections: Collections
    let oauth: OAuthSettings

    /// Loads configuration from `Bundle.main.infoDictionary` (populated from Xcode `.xcconfig` build settings).
    static let current: FirestoreConfig = {
        let info = Bundle.main.infoDictionary ?? [:]
        let fsDict = info["FirestoreConfiguration"] as? [String: Any] ?? [:]
        let colDict = fsDict["Collections"] as? [String: String] ?? [:]
        let oauthDict = info["OAuthConfiguration"] as? [String: String] ?? [:]

        func clean(_ value: Any?, fallback: String) -> String {
            guard let str = value as? String, !str.isEmpty, !str.hasPrefix("$(") else {
                return fallback
            }
            return str
        }

        let useSSLString = clean(fsDict["UseSSL"], fallback: "YES").uppercased()
        let useEmulatorString = clean(fsDict["UseEmulator"], fallback: "NO").uppercased()

        return FirestoreConfig(
            environment: clean(fsDict["Environment"], fallback: "development"),
            projectId: clean(fsDict["ProjectID"], fallback: "voterify-dev"),
            databaseId: clean(fsDict["DatabaseID"], fallback: "(default)"),
            host: clean(fsDict["Host"], fallback: "firestore.googleapis.com"),
            useSSL: useSSLString == "YES" || useSSLString == "TRUE",
            useEmulator: useEmulatorString == "YES" || useEmulatorString == "TRUE",
            apiKey: clean(fsDict["APIKey"], fallback: "AIzaSyDevelopmentVoterifyFirestoreKey00000"),
            apiVersion: clean(fsDict["APIVersion"], fallback: "v1"),
            defaultRegion: clean(fsDict["DefaultRegion"], fallback: "nam5"),
            collections: Collections(
                users: clean(colDict["Users"], fallback: "users"),
                voters: clean(colDict["Voters"], fallback: "voters"),
                elections: clean(colDict["Elections"], fallback: "elections"),
                candidates: clean(colDict["Candidates"], fallback: "candidates"),
                regions: clean(colDict["Regions"], fallback: "regions"),
                parties: clean(colDict["Parties"], fallback: "parties"),
                permissions: clean(colDict["Permissions"], fallback: "permissions"),
                votes: clean(colDict["Votes"], fallback: "votes"),
                blockchain: clean(colDict["Blockchain"], fallback: "blockchain_ledger")
            ),
            oauth: OAuthSettings(
                redirectScheme: clean(oauthDict["RedirectScheme"], fallback: "voterify"),
                appleServiceId: clean(oauthDict["AppleServiceID"], fallback: "com.voterify.app.auth"),
                googleClientId: clean(oauthDict["GoogleClientID"], fallback: "100000000000-voterifygoogleclient.apps.googleusercontent.com"),
                facebookAppId: clean(oauthDict["FacebookAppID"], fallback: "100000000000000"),
                twitterClientId: clean(oauthDict["TwitterClientID"], fallback: "voterify_twitter_oauth2_client_id")
            )
        )
    }()

    /// Base REST URL for Google Cloud Firestore documents endpoint derived from `.xcconfig`.
    var baseDocumentsURLString: String {
        let scheme = useSSL ? "https" : "http"
        return "\(scheme)://\(host)/\(apiVersion)/projects/\(projectId)/databases/\(databaseId)/documents"
    }

    /// Returns the full collection endpoint URL for a given collection name.
    func collectionURL(for collectionName: String, documentId: String? = nil) -> URL? {
        var path = "\(baseDocumentsURLString)/\(collectionName)"
        if let documentId = documentId {
            path += "/\(documentId)"
        }
        path += "?key=\(apiKey)"
        return URL(string: path)
    }
}
