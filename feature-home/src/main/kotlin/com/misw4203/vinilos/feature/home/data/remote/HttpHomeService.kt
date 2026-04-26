package com.misw4203.vinilos.feature.home.data.remote

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import com.misw4203.vinilos.feature.home.data.remote.dto.TrackDto
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class HttpHomeService(
    private val baseUrl: String,
) : HomeService {

    override suspend fun getAlbums(): List<AlbumDto> {
        val connection = openConnection("/albums")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            if (connection.responseCode !in HTTP_SUCCESS_RANGE) return emptyList()

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseAlbums(payload)
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun getAlbum(id: String): AlbumDto? {
        val connection = openConnection("/albums/$id")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            if (connection.responseCode !in HTTP_SUCCESS_RANGE) return null

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseAlbum(JSONObject(payload))
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun parseAlbums(payload: String): List<AlbumDto> {
        val jsonArray = JSONArray(payload)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.safeOptJSONObject(index) ?: continue
                add(parseAlbum(json))
            }
        }
    }

    private fun parseAlbum(json: JSONObject): AlbumDto {
        return AlbumDto(
            id = json.optLongOrNull("id") ?: json.optLongOrNull("new_id_a"),
            name = json.optStringOrNull("name"),
            title = json.optStringOrNull("title"),
            artist = json.optStringOrNull("artist"),
            recordLabel = json.optStringOrNull("recordLabel"),
            label = json.optStringOrNull("label"),
            format = json.optStringOrNull("format"),
            cover = json.optStringOrNull("cover"),
            image = json.optStringOrNull("image"),
            description = json.optStringOrNull("description"),
            genre = json.optStringOrNull("genre"),
            releaseDate = json.optStringOrNull("releaseDate"),
            tracks = json.optJSONArray("tracks").toTrackDtos(),
            performers = json.optJSONArray("performers").toPerformerDtos(),
            comments = json.optJSONArray("comments").toCommentDtos(),
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

private fun JSONObject.optIntOrNull(name: String): Int? =
    if (has(name) && !isNull(name)) optInt(name) else null

private fun JSONArray?.toTrackDtos(): List<TrackDto> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = safeOptJSONObject(index) ?: continue
            add(
                TrackDto(
                    id = item.optLongOrNull("id"),
                    name = item.optStringOrNull("name"),
                    duration = item.optStringOrNull("duration"),
                ),
            )
        }
    }
}

private fun JSONArray?.toPerformerDtos(): List<PerformerDto> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = safeOptJSONObject(index) ?: continue
            add(
                PerformerDto(
                    id = item.optLongOrNull("id"),
                    name = item.optStringOrNull("name"),
                    image = item.optStringOrNull("image"),
                    description = item.optStringOrNull("description"),
                    birthDate = item.optStringOrNull("birthDate"),
                ),
            )
        }
    }
}

private fun JSONArray?.toCommentDtos(): List<CommentDto> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = safeOptJSONObject(index) ?: continue
            add(
                CommentDto(
                    id = item.optLongOrNull("id"),
                    description = item.optStringOrNull("description"),
                    rating = item.optIntOrNull("rating"),
                ),
            )
        }
    }
}
