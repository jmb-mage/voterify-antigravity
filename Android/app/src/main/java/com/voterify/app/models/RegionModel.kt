package com.voterify.app.models

import java.util.UUID

/**
 * Region model representing an electoral geographical district or jurisdiction.
 */
data class RegionModel(
    val id: String = "region-${UUID.randomUUID().toString().take(8).lowercase()}",
    val name: String
)
