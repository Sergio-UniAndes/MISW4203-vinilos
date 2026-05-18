package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository

class AddTrackUseCase(private val repository: HomeRepository) {
    suspend operator fun invoke(albumId: String, name: String, duration: String): Boolean =
        repository.addTrack(albumId, name, duration)
}
