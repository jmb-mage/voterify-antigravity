package com.voterify.app.services

import com.voterify.app.models.BlockchainBlockModel
import com.voterify.app.models.VoteModel
import com.voterify.app.models.VoteStatus
import java.time.Instant

data class BlockchainVerificationReport(
    val isValid: Boolean,
    val totalBlocks: Int,
    val invalidBlockIndices: List<Int>,
    val verifiedAt: String = Instant.now().toString()
)

/**
 * Manages the public cryptographic blockchain ledger where every vote is stored
 * using the vote and the unique anonymous voter ID in the blockchain key.
 */
class BlockchainService {
    companion object {
        val GENESIS_PREVIOUS_HASH: String = "0".repeat(64)
    }

    fun makeGenesisBlock(): BlockchainBlockModel {
        val genesisTimestamp = "2026-10-01T00:00:00Z"
        val genesisVote = VoteModel(
            id = "genesis-vote",
            electionId = "GENESIS",
            candidateId = "GENESIS",
            candidateName = "Genesis Ledger Block",
            voterId = "VOTER-00000000-0000-0000-0000-000000000000",
            date = genesisTimestamp,
            statusFlag = VoteStatus.VERIFIED,
            blockchainKey = "VOTER-00000000-0000-0000-0000-000000000000::GENESIS::GENESIS"
        )
        return mineBlock(
            index = 0,
            timestamp = genesisTimestamp,
            vote = genesisVote,
            previousHash = GENESIS_PREVIOUS_HASH
        )
    }

    fun appendVoteBlock(
        vote: VoteModel,
        existingChain: List<BlockchainBlockModel>
    ): BlockchainBlockModel {
        val previousBlock = existingChain.lastOrNull() ?: makeGenesisBlock()
        val nextIndex = previousBlock.index + 1
        return mineBlock(
            index = nextIndex,
            timestamp = vote.date,
            vote = vote,
            previousHash = previousBlock.blockHash
        )
    }

    private fun mineBlock(
        index: Int,
        timestamp: String,
        vote: VoteModel,
        previousHash: String
    ): BlockchainBlockModel {
        var nonce = 0
        var candidate = BlockchainBlockModel.create(
            index = index,
            timestamp = timestamp,
            vote = vote,
            previousHash = previousHash,
            nonce = nonce
        )
        while (!candidate.blockHash.startsWith("00") && nonce < 50_000) {
            nonce += 1
            candidate = BlockchainBlockModel.create(
                id = candidate.id,
                index = index,
                timestamp = timestamp,
                vote = vote,
                previousHash = previousHash,
                nonce = nonce
            )
        }
        return candidate
    }

    fun verifyChain(chain: List<BlockchainBlockModel>): BlockchainVerificationReport {
        if (chain.isEmpty()) {
            return BlockchainVerificationReport(
                isValid = true,
                totalBlocks = 0,
                invalidBlockIndices = emptyList()
            )
        }

        val sorted = chain.sortedBy { it.index }
        val invalidIndices = mutableListOf<Int>()

        for (i in sorted.indices) {
            val block = sorted[i]
            val expectedKey = VoteModel.makeBlockchainKey(
                voterId = block.vote.voterId,
                electionId = block.vote.electionId,
                candidateId = block.vote.candidateId
            )
            val keyMatches = (block.blockKey == block.vote.blockchainKey) && (block.blockKey == expectedKey)
            val previousHashMatches = if (i == 0) {
                block.previousHash == GENESIS_PREVIOUS_HASH
            } else {
                block.previousHash == sorted[i - 1].blockHash
            }

            if (!block.isHashValid || !keyMatches || !previousHashMatches) {
                invalidIndices.add(block.index)
            }
        }

        return BlockchainVerificationReport(
            isValid = invalidIndices.isEmpty(),
            totalBlocks = sorted.size,
            invalidBlockIndices = invalidIndices
        )
    }
}
