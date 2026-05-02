package com.misw4203.vinilos.feature.home.domain.repository

import com.misw4203.vinilos.feature.home.domain.model.Artist
import kotlinx.coroutines.flow.Flow

interface ArtistsRepository {
    fun observeArtists(): Flow<List<Artist>>
    fun observeArtist(id: Long): Flow<Artist?>
}
