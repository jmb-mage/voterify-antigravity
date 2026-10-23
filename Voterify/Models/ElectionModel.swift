import Foundation

/// Election Model: name, id, date start, date end, region id, active flag, and a list of Candidates.
struct ElectionModel: Identifiable, Codable, Hashable {
    var id: String
    var name: String
    var dateStart: Date
    var dateEnd: Date
    var regionId: String
    var isActive: Bool
    var candidates: [CandidateModel]

    init(
        id: String = "election-\(UUID().uuidString.prefix(8).lowercased())",
        name: String,
        dateStart: Date = Date(),
        dateEnd: Date = Calendar.current.date(byAdding: .day, value: 30, to: Date()) ?? Date(),
        regionId: String,
        isActive: Bool = true,
        candidates: [CandidateModel] = []
    ) {
        self.id = id
        self.name = name
        self.dateStart = dateStart
        self.dateEnd = dateEnd
        self.regionId = regionId
        self.isActive = isActive
        self.candidates = candidates
    }

    var isCurrentlyOpen: Bool {
        let now = Date()
        return isActive && now >= dateStart && now <= dateEnd
    }
}
