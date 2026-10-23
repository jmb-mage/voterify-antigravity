package com.voterify.app.models

import java.time.Instant
import java.util.UUID

/**
 * Status flag for a VoteModel.
 */
enum class VoteStatus(val rawValue: String) {
    PENDING("Pending"),
    RECORDED("Recorded"),
    VERIFIED("Verified"),
    REJECTED("Rejected");

    companion object {
        fun fromRaw(raw: String): VoteStatus =
            entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: VERIFIED
    }
}

/**
 * Vote model: election id, candidate id, candidate name, voter id, date, status flag.
 */
data class VoteModel(
    val id: String = "vote-${UUID.randomUUID().toString().take(12).lowercase()}",
    val electionId: String,
    val candidateId: String,
    val candidateName: String,
    val voterId: String,
    val date: String = Instant.now().toString(),
    val statusFlag: VoteStatus = VoteStatus.VERIFIED,
    val blockchainKey: String = makeBlockchainKey(voterId, electionId, candidateId)
) {
    companion object {
        /**
         * Generates the deterministic blockchain key combining the unique anonymous voter ID and the vote selection.
         */
        fun makeBlockchainKey(voterId: String, electionId: String, candidateId: String): String =
            "$voterId::$electionId::$candidateId"
    }
}
