package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.core.utils.config.BackendConfig
import com.misw4203.vinilos.feature.home.data.remote.HttpArtistsService
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository

fun provideArtistsRepository(baseUrl: String = BackendConfig.BASE_URL): ArtistsRepository {
    return RemoteArtistsRepository(HttpArtistsService(baseUrl))
}
