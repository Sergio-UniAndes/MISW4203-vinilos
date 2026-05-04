package com.misw4203.vinilos.feature.home.data.remote

import android.util.Log
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface ArtistsService {
    suspend fun getMusicians(): List<MusicianDto>
    suspend fun getMusician(id: Long): MusicianDto?
}

class HttpArtistsService(
    private val baseUrl: String,
) : ArtistsService {

    override suspend fun getMusicians(): List<MusicianDto> {
        val connection = openConnection("/musicians")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val responseCode = connection.responseCode
            if (responseCode !in HTTP_SUCCESS_RANGE) {
                Log.w(TAG, "GET /musicians returned $responseCode")
                return emptyList()
            }

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseMusicians(payload)
        } catch (e: Exception) {
            Log.w(TAG, "GET /musicians failed: ${e.message}", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    override suspend fun getMusician(id: Long): MusicianDto? {
        val connection = openConnection("/musicians/$id")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val responseCode = connection.responseCode
            if (responseCode !in HTTP_SUCCESS_RANGE) {
                Log.w(TAG, "GET /musicians/$id returned $responseCode")
                return null
            }

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseMusician(JSONObject(payload))
        } catch (e: Exception) {
            Log.w(TAG, "GET /musicians/$id failed: ${e.message}", e)
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun parseMusicians(payload: String): List<MusicianDto> {
        val jsonArray = JSONArray(payload)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.safeOptJSONObject(index) ?: continue
                add(parseMusician(json))
            }
        }
    }

    private fun parseMusician(json: JSONObject): MusicianDto {
        return MusicianDto(
            id = json.optLongOrNull("id"),
            name = json.optStringOrNull("name"),
            image = json.optStringOrNull("image"),
            description = json.optStringOrNull("description"),
            birthDate = json.optStringOrNull("birthDate"),
            albums = json.optJSONArray("albums").toAlbumDtos(),
        )
    }

    private fun openConnection(path: String): HttpURLConnection {
        return (URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
        const val TAG = "HttpArtistsService"
        val HTTP_SUCCESS_RANGE = 200..299
    }
}

internal fun JSONArray?.toAlbumDtos(): List<AlbumDto> {
    if (this == null) return emptyList()
    return buildList {
        for (index in 0 until length()) {
            val item = safeOptJSONObject(index) ?: continue
            add(
                AlbumDto(
                    id = item.optLongOrNull("id"),
                    name = item.optStringOrNull("name"),
                    cover = item.optStringOrNull("cover"),
                    description = item.optStringOrNull("description"),
                    genre = item.optStringOrNull("genre"),
                    releaseDate = item.optStringOrNull("releaseDate"),
                    recordLabel = item.optStringOrNull("recordLabel"),
                ),
            )
        }
    }
}
