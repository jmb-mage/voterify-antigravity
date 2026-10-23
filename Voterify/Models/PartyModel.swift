import Foundation

/// Party model representing a political or civic party.
struct PartyModel: Identifiable, Codable, Hashable {
    var id: String
    var name: String

    init(id: String = "party-\(UUID().uuidString.prefix(8).lowercased())", name: String) {
        self.id = id
        self.name = name
    }
}
