package com.voterify.app.models

import java.util.UUID

/**
 * Candidate model: name, id, election id, party id, active flag.
 */
data class CandidateModel(
    val id: String = "cand-${UUID.randomUUID().toString().take(8).lowercase()}",
    val name: String,
    val electionId: String,
    val partyId: String,
    val isActive: Boolean = true
)
