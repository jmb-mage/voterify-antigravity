package com.voterify.app.models

import java.time.Instant
import java.util.UUID

/**
 * Anonymous representation of the UserModel.
 * Contains a public unique name generated using a GUID (`publicUniqueName`) and
 * a `privateUserId` used privately to match back to the `UserModel` without exposing identity on the public blockchain.
 */
data class VoterModel(
    val id: String = UUID.randomUUID().toString().uppercase(),
    val publicUniqueName: String = "VOTER-$id",
    val privateUserId: String,
    val createdAt: String = Instant.now().toString()
)
