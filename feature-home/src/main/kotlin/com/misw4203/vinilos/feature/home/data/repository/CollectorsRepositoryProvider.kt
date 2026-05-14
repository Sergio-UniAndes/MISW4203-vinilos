package com.misw4203.vinilos.feature.home.data.repository

import android.content.Context
import com.misw4203.vinilos.core.utils.config.BackendConfig
import com.misw4203.vinilos.feature.home.data.cache.CollectorsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.NoopCollectorsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.RoomCollectorsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.VinilosDatabase
import com.misw4203.vinilos.feature.home.data.remote.HttpCollectorsService
import com.misw4203.vinilos.feature.home.domain.repository.CollectorsRepository

fun provideCollectorsRepository(
    context: Context? = null,
    baseUrl: String = BackendConfig.BASE_URL,
): CollectorsRepository {
    return RemoteCollectorsRepository(
        service = HttpCollectorsService(baseUrl),
        localCache = provideCollectorsLocalCache(context),
    )
}

private fun provideCollectorsLocalCache(context: Context?): CollectorsLocalCache {
    if (context == null) return NoopCollectorsLocalCache()
    return RoomCollectorsLocalCache(VinilosDatabase.get(context).collectorDao())
}
