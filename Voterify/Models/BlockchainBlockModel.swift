import CryptoKit
import Foundation

/// Immutable block in the public election blockchain database.
/// Uses the vote and the unique anonymous voter ID in `blockKey`, and links to the previous block via SHA-256.
struct BlockchainBlockModel: Identifiable, Codable, Hashable {
    var id: String
    var index: Int
    var timestamp: Date
    /// Composite blockchain key combining the unique anonymous voter ID and the vote payload.
    var blockKey: String
    var electionId: String
    var voterId: String
    var vote: VoteModel
    var previousHash: String
    var nonce: Int
    var blockHash: String

    init(
        id: String = UUID().uuidString,
        index: Int,
        timestamp: Date = Date(),
        vote: VoteModel,
        previousHash: String,
        nonce: Int = 0
    ) {
        self.id = id
        self.index = index
        self.timestamp = timestamp
        self.blockKey = vote.blockchainKey
        self.electionId = vote.electionId
        self.voterId = vote.voterId
        self.vote = vote
        self.previousHash = previousHash
        self.nonce = nonce
        self.blockHash = Self.computeSHA256(
            index: index,
            timestamp: timestamp,
            blockKey: vote.blockchainKey,
            vote: vote,
            previousHash: previousHash,
            nonce: nonce
        )
    }

    /// Computes a deterministic SHA-256 hash over the block contents, including `blockKey` (voter ID + vote).
    static func computeSHA256(
        index: Int,
        timestamp: Date,
        blockKey: String,
        vote: VoteModel,
        previousHash: String,
        nonce: Int
    ) -> String {
        let isoTimestamp = Int64(timestamp.timeIntervalSince1970 * 1000)
        let payload = [
            "idx:\(index)",
            "ts:\(isoTimestamp)",
            "key:\(blockKey)",
            "election:\(vote.electionId)",
            "candidate:\(vote.candidateId)",
            "voter:\(vote.voterId)",
            "status:\(vote.statusFlag.rawValue)",
            "prev:\(previousHash)",
            "nonce:\(nonce)"
        ].joined(separator: "|")

        let digest = SHA256.hash(data: Data(payload.utf8))
        return digest.map { String(format: "%02x", $0) }.joined()
    }

    /// Verifies that the stored `blockHash` matches the recomputed SHA-256 digest of the block.
    var isHashValid: Bool {
        let expected = Self.computeSHA256(
            index: index,
            timestamp: timestamp,
            blockKey: blockKey,
            vote: vote,
            previousHash: previousHash,
            nonce: nonce
        )
        return expected == blockHash
    }
}
