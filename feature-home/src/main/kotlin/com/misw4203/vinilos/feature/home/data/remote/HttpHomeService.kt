package com.misw4203.vinilos.feature.home.data.remote

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import com.misw4203.vinilos.feature.home.data.remote.dto.TrackDto
import org.json.JSONArray
import org.json.JSONObject
import android.content.ContentResolver
import android.util.Log
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

    override suspend fun createAlbum(album: AlbumDto): Boolean {
        val connection = openConnection("/albums")
        return try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val payload = JSONObject().apply {
                put("name", album.name ?: album.title)
                put("cover", album.cover ?: album.image)
                put("releaseDate", album.releaseDate)
                put("description", album.description)
                put("genre", album.genre)
                put("recordLabel", album.recordLabel)
            }.toString()

            connection.outputStream.use { os ->
                os.write(payload.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val responseCode = connection.responseCode
            val isSuccess = responseCode in HTTP_SUCCESS_RANGE

            if (!isSuccess) {
                val errorBody = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                Log.e("HttpHomeService", "Create album error: $errorBody")
            }

            isSuccess
        } catch (e: Exception) {
            Log.e("HttpHomeService", "Create album exception: ${e.message}", e)
            false
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun uploadCover(contentResolver: ContentResolver, uriString: String): String? {
        // The backend Postman collections (collections/Album Tests.postman_collection.json)
        // do not expose an endpoint for uploading raw files (no "/albums/upload").
        // To keep the app working with the provided API, avoid attempting to POST
        // the binary and instead return the picked URI string so the UI can display
        // the image locally (Coil supports content:// URIs). The createAlbum call
        // will continue to send the cover string to the backend; if your backend
        // later exposes an upload endpoint, restore the multipart upload here.
        return try {
            // Return the provided URI so the UI can preview it directly.
            uriString
        } catch (_: Exception) {
            null
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

internal fun JSONArray?.toTrackDtos(): List<TrackDto> {
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

internal fun JSONArray?.toPerformerDtos(): List<PerformerDto> {
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

internal fun JSONArray?.toCommentDtos(): List<CommentDto> {
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
