import Foundation

/// Verification report for the public election blockchain database.
struct BlockchainVerificationReport: Equatable {
    let isValid: Bool
    let totalBlocks: Int
    let invalidBlockIndices: [Int]
    let verifiedAt: Date
}

/// Manages the public cryptographic blockchain ledger where every vote is stored
/// using the vote and the unique anonymous voter ID in the blockchain key.
final class BlockchainService {
    static let genesisPreviousHash = String(repeating: "0", count: 64)

    /// Creates the initial Genesis Block for the public election ledger.
    func makeGenesisBlock() -> BlockchainBlockModel {
        let genesisTimestamp = Date(timeIntervalSince1970: 1_760_000_000)
        let genesisVote = VoteModel(
            id: "genesis-vote",
            electionId: "GENESIS",
            candidateId: "GENESIS",
            candidateName: "Genesis Ledger Block",
            voterId: "VOTER-00000000-0000-0000-0000-000000000000",
            date: genesisTimestamp,
            statusFlag: .verified,
            blockchainKey: "VOTER-00000000-0000-0000-0000-000000000000::GENESIS::GENESIS"
        )
        return mineBlock(
            index: 0,
            timestamp: genesisTimestamp,
            vote: genesisVote,
            previousHash: Self.genesisPreviousHash
        )
    }

    /// Appends a new vote block onto the blockchain using a lightweight SHA-256 proof-of-work prefix ("00").
    func appendVoteBlock(vote: VoteModel, existingChain: [BlockchainBlockModel]) -> BlockchainBlockModel {
        let previousBlock = existingChain.last ?? makeGenesisBlock()
        let nextIndex = previousBlock.index + 1
        return mineBlock(
            index: nextIndex,
            timestamp: vote.date,
            vote: vote,
            previousHash: previousBlock.blockHash
        )
    }

    /// Mines a block until its SHA-256 hash begins with "00" so tampering is immediately detectable.
    private func mineBlock(
        index: Int,
        timestamp: Date,
        vote: VoteModel,
        previousHash: String
    ) -> BlockchainBlockModel {
        var nonce = 0
        var candidate = BlockchainBlockModel(
            index: index,
            timestamp: timestamp,
            vote: vote,
            previousHash: previousHash,
            nonce: nonce
        )
        while !candidate.blockHash.hasPrefix("00") && nonce < 50_000 {
            nonce += 1
            candidate = BlockchainBlockModel(
                id: candidate.id,
                index: index,
                timestamp: timestamp,
                vote: vote,
                previousHash: previousHash,
                nonce: nonce
            )
        }
        return candidate
    }

    /// Verifies the cryptographic integrity of the entire public blockchain ledger.
    func verifyChain(_ chain: [BlockchainBlockModel]) -> BlockchainVerificationReport {
        guard !chain.isEmpty else {
            return BlockchainVerificationReport(
                isValid: true,
                totalBlocks: 0,
                invalidBlockIndices: [],
                verifiedAt: Date()
            )
        }

        var invalidIndices: [Int] = []
        let sorted = chain.sorted { $0.index < $1.index }

        for i in sorted.indices {
            let block = sorted[i]
            // 1. Verify internal SHA-256 hash matches payload + blockKey (voterId + vote)
            let expectedKey = VoteModel.makeBlockchainKey(
                voterId: block.vote.voterId,
                electionId: block.vote.electionId,
                candidateId: block.vote.candidateId
            )
            let keyMatches = (block.blockKey == block.vote.blockchainKey) && (block.blockKey == expectedKey)

            // 2. Verify previousHash link
            let previousHashMatches: Bool
            if i == 0 {
                previousHashMatches = (block.previousHash == Self.genesisPreviousHash)
            } else {
                previousHashMatches = (block.previousHash == sorted[i - 1].blockHash)
            }

            if !block.isHashValid || !keyMatches || !previousHashMatches {
                invalidIndices.append(block.index)
            }
        }

        return BlockchainVerificationReport(
            isValid: invalidIndices.isEmpty,
            totalBlocks: sorted.count,
            invalidBlockIndices: invalidIndices,
            verifiedAt: Date()
        )
    }
}
