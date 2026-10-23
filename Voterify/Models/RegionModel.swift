import Foundation

/// Region model representing an electoral geographical district or jurisdiction.
struct RegionModel: Identifiable, Codable, Hashable {
    var id: String
    var name: String

    init(id: String = "region-\(UUID().uuidString.prefix(8).lowercased())", name: String) {
        self.id = id
        self.name = name
    }
}
