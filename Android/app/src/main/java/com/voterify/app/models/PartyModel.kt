package com.voterify.app.models

import java.util.UUID

/**
 * Party model representing a political or civic party.
 */
data class PartyModel(
    val id: String = "party-${UUID.randomUUID().toString().take(8).lowercase()}",
    val name: String
)
