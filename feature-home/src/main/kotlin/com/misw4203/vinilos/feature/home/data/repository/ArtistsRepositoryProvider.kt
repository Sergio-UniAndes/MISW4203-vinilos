package com.misw4203.vinilos.feature.home.data.repository

import android.content.Context
import com.misw4203.vinilos.core.utils.config.BackendConfig
import com.misw4203.vinilos.feature.home.data.cache.ArtistsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.NoopArtistsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.RoomArtistsLocalCache
import com.misw4203.vinilos.feature.home.data.cache.VinilosDatabase
import com.misw4203.vinilos.feature.home.data.remote.HttpArtistsService
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository

fun provideArtistsRepository(
    context: Context? = null,
    baseUrl: String = BackendConfig.BASE_URL,
): ArtistsRepository {
    return RemoteArtistsRepository(
        service = HttpArtistsService(baseUrl),
        localCache = provideArtistsLocalCache(context),
    )
}

private fun provideArtistsLocalCache(context: Context?): ArtistsLocalCache {
    if (context == null) return NoopArtistsLocalCache()
    return RoomArtistsLocalCache(VinilosDatabase.get(context).artistDao())
}
