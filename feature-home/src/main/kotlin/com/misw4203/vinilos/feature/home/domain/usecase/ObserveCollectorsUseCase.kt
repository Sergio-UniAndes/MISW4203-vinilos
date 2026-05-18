package com.misw4203.vinilos.feature.home.domain.usecase

import com.misw4203.vinilos.feature.home.domain.model.Collector
import com.misw4203.vinilos.feature.home.domain.repository.CollectorsRepository
import kotlinx.coroutines.flow.Flow

class ObserveCollectorsUseCase(
    private val repository: CollectorsRepository,
) {
    operator fun invoke(): Flow<List<Collector>> = repository.observeCollectors()
}
