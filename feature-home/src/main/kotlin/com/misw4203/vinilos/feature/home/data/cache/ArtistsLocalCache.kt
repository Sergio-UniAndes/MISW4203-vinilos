package com.misw4203.vinilos.feature.home.data.cache

import com.misw4203.vinilos.feature.home.data.remote.dto.MusicianDto

interface ArtistsLocalCache {
    suspend fun read(): List<MusicianDto>?
    suspend fun write(artists: List<MusicianDto>)
}

class NoopArtistsLocalCache : ArtistsLocalCache {
    override suspend fun read(): List<MusicianDto>? = null
    override suspend fun write(artists: List<MusicianDto>) = Unit
}
