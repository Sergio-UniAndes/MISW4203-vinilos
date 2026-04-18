package com.misw4203.vinilos.feature.home.data.repository

import com.misw4203.vinilos.feature.home.domain.repository.HomeRepository

private const val DEFAULT_BASE_URL = "http://10.0.2.2:3000/"

fun provideHomeRepository(baseUrl: String = DEFAULT_BASE_URL): HomeRepository {
    return RemoteHomeRepository(baseUrl)
}
