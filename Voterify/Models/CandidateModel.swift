import Foundation

/// Candidate model: name, id, election id, party id, active flag.
struct CandidateModel: Identifiable, Codable, Hashable {
    var id: String
    var name: String
    var electionId: String
    var partyId: String
    var isActive: Bool

    init(
        id: String = "cand-\(UUID().uuidString.prefix(8).lowercased())",
        name: String,
        electionId: String,
        partyId: String,
        isActive: Bool = true
    ) {
        self.id = id
        self.name = name
        self.electionId = electionId
        self.partyId = partyId
        self.isActive = isActive
    }
}
