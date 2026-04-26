package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.mapper.toHomeItem
import com.misw4203.vinilos.feature.home.data.remote.HomeService
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteHomeRepository(
    private val service: HomeService,
) : HomeRepository {

    override fun observeItems(): Flow<List<HomeItem>> = flow {
        val albums = service.getAlbums()
        emit(albums.mapIndexed { index, dto -> dto.toHomeItem(index) })
    }.flowOn(Dispatchers.IO)

    override fun observeItem(id: String): Flow<HomeItem?> = flow {
        emit(service.getAlbum(id)?.toHomeItem(0))
    }.flowOn(Dispatchers.IO)
}
