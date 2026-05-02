package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.data.remote.ArtistsService
import com.misw4203.vinilos.feature.home.data.remote.HttpArtistsService
import com.misw4203.vinilos.feature.home.domain.repository.ArtistsRepository

private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"

fun provideArtistsRepository(baseUrl: String = DEFAULT_BASE_URL): ArtistsRepository {
    return RemoteArtistsRepository(provideArtistsService(baseUrl))
}

fun provideArtistsService(baseUrl: String = DEFAULT_BASE_URL): ArtistsService {
    return HttpArtistsService(baseUrl)
}

