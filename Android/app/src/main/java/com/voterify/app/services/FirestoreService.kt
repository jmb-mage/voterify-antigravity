package com.voterify.app.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Connects to Google Cloud Firestore REST endpoints configured in `FirestoreConfig`.
 */
class FirestoreService(
    val config: FirestoreConfig = FirestoreConfig.current
) {
    /**
     * Pushes a document payload asynchronously to the configured Google Firestore REST endpoint.
     */
    suspend fun syncDocumentToFirestore(
        documentId: String,
        jsonPayload: String,
        collection: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val endpoint = URL(config.collectionUrl(collection, documentId))
            val connection = (endpoint.openConnection() as HttpURLConnection).apply {
                requestMethod = "PATCH"
                connectTimeout = 4000
                readTimeout = 4000
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            }

            val body = JSONObject().apply {
                put(
                    "fields",
                    JSONObject().apply {
                        put(
                            "jsonPayload",
                            JSONObject().apply {
                                put("stringValue", jsonPayload)
                            }
                        )
                    }
                )
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(body.toString())
            }

            val code = connection.responseCode
            connection.disconnect()
            code in 200..299
        } catch (_: Exception) {
            false
        }
    }
}
