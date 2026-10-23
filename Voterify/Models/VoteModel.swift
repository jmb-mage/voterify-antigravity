import Foundation

/// Status flag for a VoteModel.
enum VoteStatus: String, Codable, CaseIterable, Identifiable {
    case pending = "Pending"
    case recorded = "Recorded"
    case verified = "Verified"
    case rejected = "Rejected"

    var id: String { rawValue }
}

/// Vote model: election id, candidate id, candidate name, voter id, date, status flag.
struct VoteModel: Identifiable, Codable, Hashable {
    var id: String
    var electionId: String
    var candidateId: String
    var candidateName: String
    /// Anonymous Voter GUID (`VoterModel.id` / `publicUniqueName`).
    var voterId: String
    var date: Date
    var statusFlag: VoteStatus
    /// Composite blockchain key constructed from the vote and the unique voter ID.
    var blockchainKey: String

    init(
        id: String = "vote-\(UUID().uuidString.prefix(12).lowercased())",
        electionId: String,
        candidateId: String,
        candidateName: String,
        voterId: String,
        date: Date = Date(),
        statusFlag: VoteStatus = .verified,
        blockchainKey: String? = nil
    ) {
        self.id = id
        self.electionId = electionId
        self.candidateId = candidateId
        self.candidateName = candidateName
        self.voterId = voterId
        self.date = date
        self.statusFlag = statusFlag
        self.blockchainKey = blockchainKey ?? Self.makeBlockchainKey(
            voterId: voterId,
            electionId: electionId,
            candidateId: candidateId
        )
    }

    /// Generates the deterministic blockchain key combining the unique anonymous voter ID and the vote selection.
    static func makeBlockchainKey(voterId: String, electionId: String, candidateId: String) -> String {
        "\(voterId)::\(electionId)::\(candidateId)"
    }
}
