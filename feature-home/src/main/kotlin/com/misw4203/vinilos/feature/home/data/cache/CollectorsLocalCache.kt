package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto

interface CollectorsLocalCache {
    suspend fun read(): List<CollectorDto>?
    suspend fun readStale(): List<CollectorDto>? = read()
    suspend fun write(collectors: List<CollectorDto>)
}

class NoopCollectorsLocalCache : CollectorsLocalCache {
    override suspend fun read(): List<CollectorDto>? = null
    override suspend fun readStale(): List<CollectorDto>? = null
    override suspend fun write(collectors: List<CollectorDto>) = Unit
}
