package com.voterify.app.services

import com.voterify.app.BuildConfig

/**
 * Reads Google Firestore and OAuth connection settings injected from Gradle configuration files
 * (`config/firestore-debug.properties`, `config/firestore-release.properties`, and `gradle.properties`).
 */
data class FirestoreConfig(
    val environment: String = BuildConfig.FIRESTORE_ENVIRONMENT,
    val projectId: String = BuildConfig.FIRESTORE_PROJECT_ID,
    val databaseId: String = BuildConfig.FIRESTORE_DATABASE_ID,
    val host: String = BuildConfig.FIRESTORE_HOST,
    val useSSL: Boolean = BuildConfig.FIRESTORE_USE_SSL,
    val useEmulator: Boolean = BuildConfig.FIRESTORE_USE_EMULATOR,
    val apiKey: String = BuildConfig.FIRESTORE_API_KEY,
    val apiVersion: String = BuildConfig.FIRESTORE_API_VERSION,
    val defaultRegion: String = BuildConfig.FIRESTORE_DEFAULT_REGION,
    val collections: Collections = Collections(),
    val oauth: OAuthSettings = OAuthSettings()
) {
    data class Collections(
        val users: String = BuildConfig.FIRESTORE_COLLECTION_USERS,
        val voters: String = BuildConfig.FIRESTORE_COLLECTION_VOTERS,
        val elections: String = BuildConfig.FIRESTORE_COLLECTION_ELECTIONS,
        val candidates: String = BuildConfig.FIRESTORE_COLLECTION_CANDIDATES,
        val regions: String = BuildConfig.FIRESTORE_COLLECTION_REGIONS,
        val parties: String = BuildConfig.FIRESTORE_COLLECTION_PARTIES,
        val permissions: String = BuildConfig.FIRESTORE_COLLECTION_PERMISSIONS,
        val votes: String = BuildConfig.FIRESTORE_COLLECTION_VOTES,
        val blockchain: String = BuildConfig.FIRESTORE_COLLECTION_BLOCKCHAIN
    )

    data class OAuthSettings(
        val redirectScheme: String = BuildConfig.OAUTH_REDIRECT_SCHEME,
        val appleServiceId: String = BuildConfig.OAUTH_APPLE_SERVICE_ID,
        val googleClientId: String = BuildConfig.OAUTH_GOOGLE_CLIENT_ID,
        val facebookAppId: String = BuildConfig.OAUTH_FACEBOOK_APP_ID,
        val twitterClientId: String = BuildConfig.OAUTH_TWITTER_CLIENT_ID
    )

    val baseDocumentsUrlString: String
        get() {
            val scheme = if (useSSL) "https" else "http"
            return "$scheme://$host/$apiVersion/projects/$projectId/databases/$databaseId/documents"
        }

    fun collectionUrl(collectionName: String, documentId: String? = null): String {
        val path = if (documentId != null) {
            "$baseDocumentsUrlString/$collectionName/$documentId"
        } else {
            "$baseDocumentsUrlString/$collectionName"
        }
        return "$path?key=$apiKey"
    }

    companion object {
        val current = FirestoreConfig()
    }
}
