package com.misw4203.vinilos.feature.home.data.remote

import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface ArtistsService {
    suspend fun getMusicians(): List<PerformerDto>
}

class HttpArtistsService(
    private val baseUrl: String,
) : ArtistsService {

    override suspend fun getMusicians(): List<PerformerDto> {
        val connection = openConnection("/musicians")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            if (connection.responseCode !in HTTP_SUCCESS_RANGE) return emptyList()

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parsePerformers(payload)
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parsePerformers(payload: String): List<PerformerDto> {
        val jsonArray = JSONArray(payload)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.safeOptJSONObject(index) ?: continue
                add(parsePerformer(json))
            }
        }
    }

    private fun parsePerformer(json: JSONObject): PerformerDto {
        return PerformerDto(
            id = json.optLongOrNull("id") ?: json.optLongOrNull("new_id_a"),
            name = json.optStringOrNull("name"),
            image = json.optStringOrNull("image"),
            description = json.optStringOrNull("description"),
            birthDate = json.optStringOrNull("birthDate"),
        )
    }

    private fun openConnection(path: String): HttpURLConnection {
        return (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
        val HTTP_SUCCESS_RANGE = 200..299
    }
}

private fun JSONArray.safeOptJSONObject(index: Int): JSONObject? =
    runCatching { getJSONObject(index) }.getOrNull()

private fun JSONObject.optStringOrNull(name: String): String? =
    optString(name).takeIf { it.isNotBlank() }

private fun JSONObject.optLongOrNull(name: String): Long? =
    if (has(name) && !isNull(name)) optLong(name) else null

