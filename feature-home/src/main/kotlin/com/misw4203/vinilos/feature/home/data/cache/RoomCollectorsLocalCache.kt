package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto

class RoomCollectorsLocalCache(
    private val dao: CollectorDao,
    private val ttlMillis: Long = DEFAULT_TTL_MILLIS,
    private val now: () -> Long = System::currentTimeMillis,
) : CollectorsLocalCache {

    override suspend fun read(): List<CollectorDto>? {
        val rows = dao.getAll()
        if (rows.isEmpty()) return null
        val oldest = rows.minOf { it.cachedAtMillis }
        if (now() - oldest > ttlMillis) return null
        return rows.map { it.toDto() }
    }

    override suspend fun readStale(): List<CollectorDto>? {
        val rows = dao.getAll()
        if (rows.isEmpty()) return null
        return rows.map { it.toDto() }
    }

    override suspend fun write(collectors: List<CollectorDto>) {
        if (collectors.isEmpty()) return
        val timestamp = now()
        val entities = collectors.mapNotNull { it.toEntity(timestamp) }
        if (entities.isEmpty()) return
        dao.replaceAll(entities)
    }

    companion object {
        const val DEFAULT_TTL_MILLIS: Long = 5 * 60 * 1000L
    }
}
