package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class ObserveHomeItemsUseCase(
    private val repository: HomeRepository,
) {
    operator fun invoke(): Flow<List<HomeItem>> = repository.observeItems()
}

