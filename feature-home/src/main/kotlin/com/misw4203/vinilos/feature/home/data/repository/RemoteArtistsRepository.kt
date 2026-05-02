package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.mapper.toArtist
import com.misw4203.vinilos.feature.home.data.remote.ArtistsService
import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteArtistsRepository(
    private val service: ArtistsService,
) : ArtistsRepository {

    override fun observeArtists(): Flow<List<Artist>> = flow {
        val musicians = service.getMusicians()
        emit(musicians.map { it.toArtist() })
    }.flowOn(Dispatchers.IO)

    override fun observeArtist(id: Long): Flow<Artist?> = flow {
        val musicians = service.getMusicians()
        val artist = musicians.find { it.id == id }?.toArtist()
        emit(artist)
    }.flowOn(Dispatchers.IO)
}

