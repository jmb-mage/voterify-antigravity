import Foundation

/// Anonymous representation of the UserModel.
/// Contains a public unique name generated using a GUID (`publicUniqueName`) and
/// a `privateUserId` used privately to match back to the `UserModel` without exposing identity on the public blockchain.
struct VoterModel: Identifiable, Codable, Hashable {
    /// Unique anonymous Voter GUID used in votes and the public blockchain key.
    var id: String
    /// Public unique name generated using a GUID (e.g., "VOTER-550E8400-E29B-41D4-A716-446655440000").
    var publicUniqueName: String
    /// Private ID used internally to match to `UserModel.id`. Never published in `VoteModel` or `BlockchainBlockModel`.
    var privateUserId: String
    var createdAt: Date

    init(
        id: String = UUID().uuidString.uppercased(),
        publicUniqueName: String? = nil,
        privateUserId: String,
        createdAt: Date = Date()
    ) {
        let normalizedGuid = id.uppercased()
        self.id = normalizedGuid
        self.publicUniqueName = publicUniqueName ?? "VOTER-\(normalizedGuid)"
        self.privateUserId = privateUserId
        self.createdAt = createdAt
    }
}
