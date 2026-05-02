package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import kotlinx.coroutines.flow.Flow

class ObserveArtistDetailUseCase(
    private val repository: ArtistsRepository,
) {
    operator fun invoke(id: Long): Flow<Artist?> = repository.observeArtist(id)
}
