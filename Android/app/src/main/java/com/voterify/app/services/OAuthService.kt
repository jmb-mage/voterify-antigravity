package com.voterify.app.services

import com.voterify.app.models.OAuthProvider
import com.voterify.app.models.UserModel
import com.voterify.app.models.VoterModel
import java.util.UUID
import kotlin.math.abs

data class AuthenticatedSession(
    val user: UserModel,
    val voter: VoterModel,
    val oauthClientIdUsed: String
)

/**
 * Handles OAuth login across Apple, Google, Facebook, and Twitter/X providers
 * using the client IDs configured in the Gradle build configuration files.
 */
class OAuthService(
    private val config: FirestoreConfig = FirestoreConfig.current
) {
    fun clientId(provider: OAuthProvider): String = when (provider) {
        OAuthProvider.APPLE -> config.oauth.appleServiceId
        OAuthProvider.GOOGLE -> config.oauth.googleClientId
        OAuthProvider.FACEBOOK -> config.oauth.facebookAppId
        OAuthProvider.TWITTER -> config.oauth.twitterClientId
    }

    fun signIn(
        provider: OAuthProvider,
        customDisplayName: String?,
        existingUsers: List<UserModel>,
        existingVoters: List<VoterModel>,
        asAdminPreset: Boolean
    ): AuthenticatedSession {
        val trimmedName = customDisplayName?.trim().orEmpty()
        val user: UserModel = if (asAdminPreset) {
            val adminUser = existingUsers.firstOrNull { it.id == "user-admin-001" }
            UserModel(
                id = adminUser?.id ?: "user-admin-001",
                name = trimmedName.ifEmpty { adminUser?.name ?: "Alex Morgan (Admin)" },
                email = adminUser?.email ?: "admin@voterify.org",
                oauthProvider = provider,
                oauthSubjectId = "${provider.rawValue}-admin-001"
            )
        } else {
            val resolvedName = trimmedName.ifEmpty { defaultUserName(provider) }
            val deterministicId = "user-${provider.rawValue}-${abs(resolvedName.lowercase().hashCode() % 100_000)}"
            existingUsers.firstOrNull { it.id == deterministicId } ?: UserModel(
                id = deterministicId,
                name = resolvedName,
                email = "${resolvedName.lowercase().replace(" ", ".")}@${provider.rawValue}.oauth.org",
                oauthProvider = provider,
                oauthSubjectId = "${provider.rawValue}-sub-${UUID.randomUUID().toString().take(8)}"
            )
        }

        val voter: VoterModel = existingVoters.firstOrNull { it.privateUserId == user.id } ?: run {
            val newGuid = UUID.randomUUID().toString().uppercase()
            VoterModel(
                id = newGuid,
                publicUniqueName = "VOTER-$newGuid",
                privateUserId = user.id
            )
        }

        return AuthenticatedSession(
            user = user,
            voter = voter,
            oauthClientIdUsed = clientId(provider)
        )
    }

    private fun defaultUserName(provider: OAuthProvider): String = when (provider) {
        OAuthProvider.APPLE -> "Jordan Taylor (Apple ID)"
        OAuthProvider.GOOGLE -> "Taylor Morgan (Google)"
        OAuthProvider.FACEBOOK -> "Casey Rivera (Facebook)"
        OAuthProvider.TWITTER -> "Riley Quinn (@rquinn)"
    }
}
