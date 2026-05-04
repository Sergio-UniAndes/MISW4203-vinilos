package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto

class RoomArtistsLocalCache(
    private val dao: ArtistDao,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val now: () -> Long = System::currentTimeMillis,
) : ArtistsLocalCache {

    override suspend fun read(): List<MusicianDto>? {
        val rows = dao.getAll()
        if (rows.isEmpty()) return null
        val oldest = rows.minOf { it.cachedAtMillis }
        if (now() - oldest > ttlMillis) return null
        return rows.map { it.toDto() }
    }

    override suspend fun write(artists: List<MusicianDto>) {
        if (artists.isEmpty()) return
        val timestamp = now()
        val entities = artists.mapNotNull { it.toEntity(timestamp) }
        if (entities.isEmpty()) return
        dao.replaceAll(entities)
    }

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 5 * 60 * 1000L
    }
}
