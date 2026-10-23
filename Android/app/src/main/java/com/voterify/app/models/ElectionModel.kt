package com.voterify.app.models

import java.util.UUID

/**
 * Election Model: name, id, date start, date end, region id, active flag, and a list of Candidates.
 */
data class ElectionModel(
    val id: String = "election-${UUID.randomUUID().toString().take(8).lowercase()}",
    val name: String,
    val dateStart: String = "2026-10-01",
    val dateEnd: String = "2026-11-30",
    val regionId: String,
    val isActive: Boolean = true,
    val candidates: List<CandidateModel> = emptyList()
)
