package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MockHomeRepository : HomeRepository {

    private val items = MutableStateFlow(
        listOf(
            HomeItem("1", "Dark Side of the Moon", "Pink Floyd", 1973),
            HomeItem("2", "Abbey Road", "The Beatles", 1969),
            HomeItem("3", "Back in Black", "AC/DC", 1980),
            HomeItem("4", "Random Access Memories", "Daft Punk", 2013),
        ),
    )

    override fun observeItems(): Flow<List<HomeItem>> = items.asStateFlow()
}

