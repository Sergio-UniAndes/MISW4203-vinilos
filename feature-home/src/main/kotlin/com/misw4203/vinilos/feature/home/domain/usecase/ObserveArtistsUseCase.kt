package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.Artist
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository
import kotlinx.coroutines.flow.Flow

class ObserveArtistsUseCase(
    private val repository: ArtistsRepository,
) {
    operator fun invoke(): Flow<List<Artist>> = repository.observeArtists()
}
