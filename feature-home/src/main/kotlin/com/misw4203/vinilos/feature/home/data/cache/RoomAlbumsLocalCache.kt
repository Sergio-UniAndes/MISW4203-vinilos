package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto

class RoomAlbumsLocalCache(
    private val dao: AlbumDao,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val now: () -> Long = System::currentTimeMillis,
) : AlbumsLocalCache {

    override suspend fun read(): List<AlbumDto>? {
        val rows = dao.getAll()
        if (rows.isEmpty()) return null
        val oldest = rows.minOf { it.cachedAtMillis }
        if (now() - oldest > ttlMillis) return null
        return rows.map { it.toDto() }
    }

    override suspend fun write(albums: List<AlbumDto>) {
        if (albums.isEmpty()) return
        val timestamp = now()
        val entities = albums.mapNotNull { it.toEntity(timestamp) }
        if (entities.isEmpty()) return
        dao.replaceAll(entities)
    }

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 5 * 60 * 1000L
    }
}
