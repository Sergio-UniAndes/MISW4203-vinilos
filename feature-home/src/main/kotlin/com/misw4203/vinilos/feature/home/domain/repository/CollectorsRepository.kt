package com.misw4203.vinilos.feature.home.domain.repository

import com.misw4203.vinilos.feature.home.domain.model.Collector
import kotlinx.coroutines.flow.Flow

interface CollectorsRepository {
    fun observeCollectors(): Flow<List<Collector>>
}
