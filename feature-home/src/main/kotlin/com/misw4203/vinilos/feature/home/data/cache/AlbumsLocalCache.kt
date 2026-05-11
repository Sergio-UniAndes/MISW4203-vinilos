package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto

interface AlbumsLocalCache {
    suspend fun read(): List<AlbumDto>?
    suspend fun readStale(): List<AlbumDto>? = read()
    suspend fun write(albums: List<AlbumDto>)
}

class NoopAlbumsLocalCache : AlbumsLocalCache {
    override suspend fun read(): List<AlbumDto>? = null
    override suspend fun readStale(): List<AlbumDto>? = null
    override suspend fun write(albums: List<AlbumDto>) = Unit
}
