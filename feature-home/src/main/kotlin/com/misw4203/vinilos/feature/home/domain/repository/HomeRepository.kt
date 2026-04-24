package com.misw4203.vinilos.feature.home.domain.repository

import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun observeItems(): Flow<List<HomeItem>>
    fun observeItem(id: String): Flow<HomeItem?>
}

