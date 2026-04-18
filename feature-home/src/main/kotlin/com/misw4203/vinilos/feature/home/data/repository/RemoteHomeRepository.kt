package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.mapper.toHomeItem
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class RemoteHomeRepository(
    private val baseUrl: String,
) : HomeRepository {

    override fun observeItems(): Flow<List<HomeItem>> = flow {
        emit(fetchAlbums())
    }.flowOn(Dispatchers.IO)

    private fun fetchAlbums(): List<HomeItem> {
        val connection = (URL(baseUrl.trimEnd('/') + "/albums").openConnection() as HttpURLConnection)
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

    private fun parseAlbums(payload: String): List<AlbumDto> {
        val jsonArray = JSONArray(payload)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.optJSONObject(index) ?: continue
                add(
                    AlbumDto(
                        id = json.optLongOrNull("id"),
                        name = json.optStringOrNull("name"),
                        title = json.optStringOrNull("title"),
                        artist = json.optStringOrNull("artist"),
                        recordLabel = json.optStringOrNull("recordLabel"),
                        genre = json.optStringOrNull("genre"),
                        releaseDate = json.optStringOrNull("releaseDate"),
                    ),
                )
            }
        }
    }
}

private fun JSONArray.optJSONObject(index: Int): org.json.JSONObject? =
    runCatching { getJSONObject(index) }.getOrNull()

private fun org.json.JSONObject.optStringOrNull(name: String): String? =
    optString(name).takeIf { it.isNotBlank() }

private fun org.json.JSONObject.optLongOrNull(name: String): Long? =
    if (has(name) && !isNull(name)) optLong(name) else null
