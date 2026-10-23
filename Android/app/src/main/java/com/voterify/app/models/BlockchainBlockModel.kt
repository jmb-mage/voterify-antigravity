package com.voterify.app.models

import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

/**
 * Immutable block in the public election blockchain database.
 * Uses the vote and the unique anonymous voter ID in `blockKey`, and links to the previous block via SHA-256.
 */
data class BlockchainBlockModel(
    val id: String = UUID.randomUUID().toString(),
    val index: Int,
    val timestamp: String = Instant.now().toString(),
    val blockKey: String,
    val electionId: String,
    val voterId: String,
    val vote: VoteModel,
    val previousHash: String,
    val nonce: Int,
    val blockHash: String
) {
    val isHashValid: Boolean
        get() = computeSHA256(
            index = index,
            timestamp = timestamp,
            blockKey = blockKey,
            vote = vote,
            previousHash = previousHash,
            nonce = nonce
        ) == blockHash

    companion object {
        fun create(
            id: String = UUID.randomUUID().toString(),
            index: Int,
            timestamp: String = Instant.now().toString(),
            vote: VoteModel,
            previousHash: String,
            nonce: Int = 0
        ): BlockchainBlockModel {
            val hash = computeSHA256(
                index = index,
                timestamp = timestamp,
                blockKey = vote.blockchainKey,
                vote = vote,
                previousHash = previousHash,
                nonce = nonce
            )
            return BlockchainBlockModel(
                id = id,
                index = index,
                timestamp = timestamp,
                blockKey = vote.blockchainKey,
                electionId = vote.electionId,
                voterId = vote.voterId,
                vote = vote,
                previousHash = previousHash,
                nonce = nonce,
                blockHash = hash
            )
        }

        fun computeSHA256(
            index: Int,
            timestamp: String,
            blockKey: String,
            vote: VoteModel,
            previousHash: String,
            nonce: Int
        ): String {
            val payload = listOf(
                "idx:$index",
                "ts:$timestamp",
                "key:$blockKey",
                "election:${vote.electionId}",
                "candidate:${vote.candidateId}",
                "voter:${vote.voterId}",
                "status:${vote.statusFlag.rawValue}",
                "prev:$previousHash",
                "nonce:$nonce"
            ).joinToString("|")

            val digest = MessageDigest.getInstance("SHA-256")
                .digest(payload.toByteArray(Charsets.UTF_8))
            return digest.joinToString("") { "%02x".format(it) }
        }
    }
}
