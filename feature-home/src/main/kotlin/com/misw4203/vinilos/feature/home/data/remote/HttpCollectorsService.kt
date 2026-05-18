package com.misw4203.vinilos.feature.home.data.remote

import android.util.Log
import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

interface CollectorsService {
    suspend fun getCollectors(): List<CollectorDto>
}

class HttpCollectorsService(
    private val baseUrl: String,
) : CollectorsService {

    override suspend fun getCollectors(): List<CollectorDto> {
        val connection = openConnection("/collectors")
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT_MILLIS
            connection.readTimeout = TIMEOUT_MILLIS

            val responseCode = connection.responseCode
            if (responseCode !in HTTP_SUCCESS_RANGE) {
                Log.w(TAG, "GET /collectors returned $responseCode")
                return emptyList()
            }

            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            parseCollectors(payload)
        } catch (e: Exception) {
            Log.w(TAG, "GET /collectors failed: ${e.message}", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseCollectors(payload: String): List<CollectorDto> {
        val jsonArray = JSONArray(payload)
        return buildList {
            for (index in 0 until jsonArray.length()) {
                val json = jsonArray.safeOptJSONObject(index) ?: continue
                add(parseCollector(json))
            }
        }
    }

    private fun parseCollector(json: JSONObject): CollectorDto {
        val albumCount = json.optJSONArray("collectorAlbums")?.length() ?: 0
        return CollectorDto(
            id = json.optLongOrNull("id"),
            name = json.optStringOrNull("name"),
            telephone = json.optStringOrNull("telephone"),
            email = json.optStringOrNull("email"),
            albumCount = albumCount,
        )
    }

    private fun openConnection(path: String): HttpURLConnection =
        URL(baseUrl.trimEnd('/') + path).openConnection() as HttpURLConnection

    private companion object {
        const val TIMEOUT_MILLIS = 10_000
        const val TAG = "HttpCollectorsService"
        val HTTP_SUCCESS_RANGE = 200..299
    }
}
