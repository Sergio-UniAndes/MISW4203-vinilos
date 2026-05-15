package com.misw4203.vinilos.feature.home.data.remote

import android.util.Log
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface CommentsService {
    suspend fun getComments(albumId: String): List<CommentDto>
    suspend fun postComment(
        albumId: String,
        description: String,
        rating: Int,
        collectorId: Long,
    ): CommentDto?
}

class HttpCommentsService(
    private val baseUrl: String,
) : CommentsService {

    override suspend fun getComments(albumId: String): List<CommentDto> {
        val connection = openConnection("/albums/$albumId/comments")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val responseCode = connection.responseCode
            if (responseCode !in HTTP_SUCCESS_RANGE) {
                Log.w(TAG, "GET /albums/$albumId/comments returned $responseCode")
                return emptyList()
            }

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseComments(payload)
        } catch (e: Exception) {
            Log.w(TAG, "GET /albums/$albumId/comments failed: ${e.message}", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun postComment(
        albumId: String,
        description: String,
        rating: Int,
        collectorId: Long,
    ): CommentDto? {
        val connection = openConnection("/albums/$albumId/comments")
        return try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val payload = JSONObject().apply {
                put("description", description)
                put("rating", rating)
                put("collector", JSONObject().apply { put("id", collectorId) })
            }.toString()

            connection.outputStream.use { os ->
                os.write(payload.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode !in HTTP_SUCCESS_RANGE) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                Log.e(TAG, "POST /albums/$albumId/comments error $responseCode: $errorBody")
                return null
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            parseComment(JSONObject(responseBody))
        } catch (e: Exception) {
            Log.e(TAG, "POST /albums/$albumId/comments exception: ${e.message}", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun parseComments(payload: String): List<CommentDto> {
        val jsonArray = JSONArray(payload)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.safeOptJSONObject(index) ?: continue
                add(parseComment(json))
            }
        }
    }

    private fun parseComment(json: JSONObject): CommentDto = CommentDto(
        id = json.optLongOrNull("id"),
        description = json.optStringOrNull("description"),
        rating = json.optIntOrNull("rating"),
    )

    private fun openConnection(path: String): HttpURLConnection =
        (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection)

    private companion object {
        const val TAG = "HttpCommentsService"
        const val TIMEOUT_MILLIS = 10_000
        val HTTP_SUCCESS_RANGE = 200..299
    }
}
