package com.voterify.app.models

import java.time.Instant
import java.util.UUID

/**
 * OAuth authentication providers supported for Voterify login.
 */
enum class OAuthProvider(val rawValue: String, val displayName: String) {
    APPLE("apple", "Apple"),
    GOOGLE("google", "Google"),
    FACEBOOK("facebook", "Facebook"),
    TWITTER("twitter", "Twitter / X");

    companion object {
        fun fromRaw(raw: String): OAuthProvider =
            entries.firstOrNull { it.rawValue.equals(raw, ignoreCase = true) } ?: GOOGLE
    }
}

/**
 * Login user model based on OAuth providers (Apple, Google, Facebook, Twitter), ID, and Name.
 */
data class UserModel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val email: String? = null,
    val oauthProvider: OAuthProvider,
    val oauthSubjectId: String? = null,
    val createdAt: String = Instant.now().toString()
)
