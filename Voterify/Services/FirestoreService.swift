import Foundation

/// Connects to Google Cloud Firestore using the configuration from `FirestoreConfig` (`.xcconfig`),
/// with local disk caching so all collections persist across launches and work offline in the Simulator.
final class FirestoreService {
    let config: FirestoreConfig
    private let session: URLSession
    private let encoder: JSONEncoder
    private let decoder: JSONDecoder

    init(config: FirestoreConfig = .current, session: URLSession = .shared) {
        self.config = config
        self.session = session

        let enc = JSONEncoder()
        enc.dateEncodingStrategy = .iso8601
        enc.outputFormatting = [.prettyPrinted, .sortedKeys]
        self.encoder = enc

        let dec = JSONDecoder()
        dec.dateDecodingStrategy = .iso8601
        self.decoder = dec
    }

    // MARK: - Local Cache Directory

    private var cacheDirectory: URL {
        let urls = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)
        let dir = urls[0].appendingPathComponent("FirestoreLocalCache", isDirectory: true)
        if !FileManager.default.fileExists(atPath: dir.path) {
            try? FileManager.default.createDirectory(at: dir, withIntermediateDirectories: true)
        }
        return dir
    }

    private func localFileURL(for collection: String) -> URL {
        cacheDirectory.appendingPathComponent("\(collection).json")
    }

    // MARK: - Collection Persistence & Remote Firestore Sync

    func loadCollection<T: Codable>(_ collection: String, as type: [T].Type) -> [T]? {
        let url = localFileURL(for: collection)
        guard let data = try? Data(contentsOf: url) else { return nil }
        return try? decoder.decode(type, from: data)
    }

    func saveCollection<T: Codable>(_ items: [T], to collection: String) {
        let url = localFileURL(for: collection)
        if let data = try? encoder.encode(items) {
            try? data.write(to: url, options: .atomic)
        }
    }

    /// Pushes a single document to the configured Google Firestore REST endpoint (non-blocking).
    func syncDocumentToFirestore<T: Codable & Identifiable>(_ document: T, collection: String) async -> Bool where T.ID == String {
        guard let endpoint = config.collectionURL(for: collection, documentId: document.id),
              let payloadData = try? encoder.encode(document) else {
            return false
        }

        var request = URLRequest(url: endpoint)
        request.httpMethod = "PATCH"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.timeoutInterval = 4.0

        // Wrap payload in Firestore REST document fields format
        let wrapper: [String: Any] = [
            "fields": [
                "jsonPayload": [
                    "stringValue": String(data: payloadData, encoding: .utf8) ?? "{}"
                ]
            ]
        ]
        request.httpBody = try? JSONSerialization.data(withJSONObject: wrapper)

        do {
            let (_, response) = try await session.data(for: request)
            if let httpResponse = response as? HTTPURLResponse {
                return (200...299).contains(httpResponse.statusCode)
            }
            return false
        } catch {
            return false
        }
    }
}
