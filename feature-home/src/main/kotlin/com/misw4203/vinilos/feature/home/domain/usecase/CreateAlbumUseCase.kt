package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository

class CreateAlbumUseCase(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(album: AlbumDto): Boolean = repository.createAlbum(album)
}

