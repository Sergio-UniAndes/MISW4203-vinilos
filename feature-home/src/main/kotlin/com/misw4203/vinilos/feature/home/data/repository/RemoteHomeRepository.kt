package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.cache.AlbumsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.NoopAlbumsLocalCache
import com.misw4203.vinilos.feature.home.data.mapper.toHomeItem
import com.misw4203.vinilos.feature.home.data.remote.HomeService
import com.misw4203.vinilos.feature.home.data.remote.dto.AlbumDto
import com.misw4203.vinilos.feature.home.domain.model.HomeItem
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class RemoteHomeRepository(
    private val service: HomeService,
    private val localCache: AlbumsLocalCache = NoopAlbumsLocalCache(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : HomeRepository {

    private val itemsCache = MutableStateFlow<List<HomeItem>?>(null)

    override fun observeItems(): Flow<List<HomeItem>> = flow {
        var hasEmitted = false
        itemsCache.value?.let {
            emit(it)
            hasEmitted = true
        }

        if (itemsCache.value == null) {
            localCache.read()?.let { dtos ->
                val items = dtos.toHomeItems()
                itemsCache.value = items
                emit(items)
                hasEmitted = true
            }
        }

        val fresh = try {
            service.getAlbums()
        } catch (_: Exception) {
            if (!hasEmitted) {
                val stale = localCache.readStale()?.toHomeItems().orEmpty()
                itemsCache.value = stale
                emit(stale)
            }
            return@flow
        }
        val freshItems = fresh.toHomeItems()
        if (fresh.isNotEmpty()) {
            localCache.write(fresh)
        }
        // HttpHomeService swallows transport failures and returns []; keep cache instead of blanking.
        if (freshItems.isEmpty() && itemsCache.value?.isNotEmpty() == true) {
            return@flow
        }
        if (freshItems != itemsCache.value) {
            itemsCache.value = freshItems
            emit(freshItems)
        }
    }.flowOn(ioDispatcher)

    override fun observeItem(id: String): Flow<HomeItem?> = flow {
        emit(service.getAlbum(id)?.toHomeItem(0))
    }.flowOn(ioDispatcher)

    override suspend fun createAlbum(album: AlbumDto): Boolean {
        return try {
            withContext(ioDispatcher) {
                service.createAlbum(album)
            }
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun uploadCover(contentResolver: android.content.ContentResolver, uriString: String): String? {
        return try {
            withContext(ioDispatcher) {
                service.uploadCover(contentResolver, uriString)
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun addTrack(albumId: String, name: String, duration: String): Boolean {
        return try {
            withContext(ioDispatcher) {
                service.addTrack(albumId, name, duration)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun List<AlbumDto>.toHomeItems(): List<HomeItem> =
        mapIndexed { index, dto -> dto.toHomeItem(index) }
}
