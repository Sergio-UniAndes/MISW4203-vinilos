package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.mapper.toHomeItem
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.CommentDto
import com.misw4203.vinilos.feature.home.data.remote.dto.PerformerDto
import com.misw4203.vinilos.feature.home.data.remote.dto.TrackDto
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class RemoteHomeRepository(
    private val baseUrl: String,
) : HomeRepository {

    override fun observeItems(): Flow<List<HomeItem>> = flow {
        emit(fetchAlbums())
    }.flowOn(Dispatchers.IO)

    override fun observeItem(id: String): Flow<HomeItem?> = flow {
        emit(fetchAlbum(id))
    }.flowOn(Dispatchers.IO)

    private fun fetchAlbums(): List<HomeItem> {
        val connection = openConnection("/albums")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) return emptyList()

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val albums = parseAlbums(payload)
            albums.mapIndexed { index, dto -> dto.toHomeItem(index) }
        } catch (_: Exception) {
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun fetchAlbum(id: String): HomeItem? {
        val connection = openConnection("/albums/$id")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) return null

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val dto = parseAlbum(JSONObject(payload))
            dto.toHomeItem(0)
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
}

private fun JSONArray.safeOptJSONObject(index: Int): org.json.JSONObject? =
    runCatching { getJSONObject(index) }.getOrNull()

private fun org.json.JSONObject.optStringOrNull(name: String): String? =
    optString(name).takeIf { it.isNotBlank() }

private fun org.json.JSONObject.optLongOrNull(name: String): Long? =
    if (has(name) && !isNull(name)) optLong(name) else null

private fun org.json.JSONObject.optIntOrNull(name: String): Int? =
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

