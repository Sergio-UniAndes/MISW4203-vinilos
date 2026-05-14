package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.cache.CollectorsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.NoopCollectorsLocalCache
import com.misw4203.vinilos.feature.home.data.mapper.toCollector
import com.misw4203.vinilos.feature.home.data.remote.CollectorsService
import com.misw4203.vinilos.feature.home.data.remote.dto.CollectorDto
import com.misw4203.vinilos.feature.home.domain.model.Collector
import com.misw4203.vinilos.feature.home.domain.repository.CollectorsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class RemoteCollectorsRepository(
    private val service: CollectorsService,
    private val localCache: CollectorsLocalCache = NoopCollectorsLocalCache(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : CollectorsRepository {

    private val collectorsCache = MutableStateFlow<List<Collector>?>(null)

    override fun observeCollectors(): Flow<List<Collector>> = flow {
        var hasEmitted = false

        collectorsCache.value?.let {
            emit(it)
            hasEmitted = true
        }

        if (collectorsCache.value == null) {
            localCache.read()?.let { dtos ->
                val collectors = dtos.toCollectors()
                collectorsCache.value = collectors
                emit(collectors)
                hasEmitted = true
            }
        }

        val fresh = try {
            service.getCollectors()
        } catch (_: Exception) {
            if (!hasEmitted) {
                val stale = localCache.readStale()?.toCollectors().orEmpty()
                collectorsCache.value = stale
                emit(stale)
            }
            return@flow
        }

        val freshCollectors = fresh.toCollectors()
        if (fresh.isNotEmpty()) {
            localCache.write(fresh)
        }
        // HttpCollectorsService swallows transport failures and returns []; keep cache instead of blanking.
        if (freshCollectors.isEmpty() && collectorsCache.value?.isNotEmpty() == true) {
            return@flow
        }
        if (freshCollectors != collectorsCache.value) {
            collectorsCache.value = freshCollectors
            emit(freshCollectors)
        }
    }.flowOn(ioDispatcher)

    private fun List<CollectorDto>.toCollectors(): List<Collector> = map { it.toCollector() }
}
