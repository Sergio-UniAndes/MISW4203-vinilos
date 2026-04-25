package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class ObserveAlbumDetailUseCase(
    private val repository: HomeRepository,
) {
    operator fun invoke(id: String): Flow<HomeItem?> = repository.observeItem(id)
}

