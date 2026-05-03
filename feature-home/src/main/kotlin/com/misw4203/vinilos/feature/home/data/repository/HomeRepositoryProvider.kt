package com.misw4203.vinilos.feature.home.data.repository

import android.content.Context
import com.misw4203.vinilos.core.utils.config.BackendConfig
import com.misw4203.vinilos.feature.home.data.cache.AlbumsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.NoopAlbumsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.RoomAlbumsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.VinilosDatabase
import com.misw4203.vinilos.feature.home.data.remote.HomeService
import com.misw4203.vinilos.feature.home.data.remote.HttpHomeService
import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository

fun provideHomeRepository(
    context: Context? = null,
    baseUrl: String = BackendConfig.BASE_URL,
): HomeRepository {
    return RemoteHomeRepository(
        service = provideHomeService(baseUrl),
        localCache = provideAlbumsLocalCache(context),
    )
}

fun provideHomeService(baseUrl: String = BackendConfig.BASE_URL): HomeService {
    return HttpHomeService(baseUrl)
}

private fun provideAlbumsLocalCache(context: Context?): AlbumsLocalCache {
    if (context == null) return NoopAlbumsLocalCache()
    return RoomAlbumsLocalCache(VinilosDatabase.get(context).albumDao())
}
